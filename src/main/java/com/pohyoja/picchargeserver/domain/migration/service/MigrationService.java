package com.pohyoja.picchargeserver.domain.migration.service;

import com.pohyoja.picchargeserver.common.exception.CustomException;
import com.pohyoja.picchargeserver.domain.family.entity.Family;
import com.pohyoja.picchargeserver.domain.family.exception.FamilyCustomErrorCode;
import com.pohyoja.picchargeserver.domain.family.repository.FamilyRepository;
import com.pohyoja.picchargeserver.domain.member.entity.Member;
import com.pohyoja.picchargeserver.domain.member.exception.MemberCustomErrorCode;
import com.pohyoja.picchargeserver.domain.member.repository.MemberRepository;
import com.pohyoja.picchargeserver.domain.photo.dto.PhotoDTO;
import com.pohyoja.picchargeserver.domain.photo.dto.ReactionDTO;
import com.pohyoja.picchargeserver.domain.photo.dto.request.PhotoAddRequest;
import com.pohyoja.picchargeserver.domain.photo.entity.Photo;
import com.pohyoja.picchargeserver.domain.photo.entity.Reaction;
import com.pohyoja.picchargeserver.domain.photo.repository.PhotoRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MigrationService {

    private final FamilyRepository familyRepository;
    private final MemberRepository memberRepository;
    private final PhotoRepository photoRepository;

    /**
     * 사진 추가
     */
    @Transactional
    public PhotoDTO migratePhoto(Long familyId, String currentUserId, PhotoAddRequest request,
                                 LocalDateTime uploadDate, int fire, int love, int like, int star) {
        Member member = findMemberById(currentUserId);
        Family family = findFamilyById(familyId);
        validateFamilyMember(family, member);

        Photo photo = new Photo(request.id(), request.url(), new Reaction());
        Reaction reaction = photo.getReaction();
        reaction.incrementFire(fire);
        reaction.incrementLove(love);
        reaction.incrementLike(like);
        reaction.incrementStar(star);

        photo.setUploadMember(member);
        family.addPhoto(photo);

        Photo savedPhoto = photoRepository.save(photo);
        familyRepository.save(family);

        photoRepository.updatePhotoCreatedAndUpdatedAt(savedPhoto.getId(), uploadDate, uploadDate);
        photo.setCustomDatesForMigration(uploadDate);

        return new PhotoDTO(
                request.id(),
                member.getName(),
                photo.getCreatedAt(),
                request.url(),
                ReactionDTO.empty(),
                family.getMembers().stream().map(Member::getName).toList(),
                currentUserId,
                familyId
        );
    }

    /**
     * 가족 ID로 가족 조회
     */
    private Family findFamilyById(Long familyId) {
        return familyRepository.findById(familyId)
                .orElseThrow(() -> new CustomException(FamilyCustomErrorCode.FAMILY_NOT_FOUND));
    }

    /**
     * 멤버 ID로 멤버 조회
     */
    private Member findMemberById(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberCustomErrorCode.MEMBER_ID_NOT_FOUND));
    }

    /**
     * 가족 구성원 여부 확인
     */
    private void validateFamilyMember(Family family, Member member) {
        if (member.getFamily() != family) {
            throw new CustomException(FamilyCustomErrorCode.NOT_FAMILY_MEMBER);
        }
    }
}

package com.pohyoja.picchargeserver.domain.migration.service;

import com.pohyoja.picchargeserver.common.exception.CustomException;
import com.pohyoja.picchargeserver.domain.family.dto.response.FamilyIdResponse;
import com.pohyoja.picchargeserver.domain.family.dto.response.FamilyResponse;
import com.pohyoja.picchargeserver.domain.family.dto.response.InviteCodeResponse;
import com.pohyoja.picchargeserver.domain.family.entity.Family;
import com.pohyoja.picchargeserver.domain.family.exception.FamilyCustomErrorCode;
import com.pohyoja.picchargeserver.domain.family.repository.FamilyRepository;
import com.pohyoja.picchargeserver.domain.family.service.FamilyService;
import com.pohyoja.picchargeserver.domain.member.dto.MemberDTO;
import com.pohyoja.picchargeserver.domain.member.entity.Member;
import com.pohyoja.picchargeserver.domain.member.exception.MemberCustomErrorCode;
import com.pohyoja.picchargeserver.domain.member.repository.MemberRepository;
import com.pohyoja.picchargeserver.domain.member.service.MemberService;
import com.pohyoja.picchargeserver.domain.migration.dto.request.PhotoMigrateRequest;
import com.pohyoja.picchargeserver.domain.migration.dto.response.FamilyCreateResponse;
import com.pohyoja.picchargeserver.domain.photo.dto.PhotoDTO;
import com.pohyoja.picchargeserver.domain.photo.dto.request.PhotoAddRequest;
import com.pohyoja.picchargeserver.domain.photo.entity.Photo;
import com.pohyoja.picchargeserver.domain.photo.entity.Reaction;
import com.pohyoja.picchargeserver.domain.photo.exception.PhotoCustomErrorCode;
import com.pohyoja.picchargeserver.domain.photo.repository.PhotoRepository;
import com.pohyoja.picchargeserver.domain.photo.service.PhotoService;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class MigrationService {

    private final FamilyRepository familyRepository;
    private final MemberRepository memberRepository;
    private final PhotoRepository photoRepository;

    private final FamilyService familyService;
    private final MemberService memberService;
    private final PhotoService photoService;

    @Value("${app.cdn-prefix}")
    private String cdnPrefix;

    /**
     * 유저 생성
     */
    public MemberDTO createUser(String name, String memberId, String email) {
        return memberService.saveMember(name, memberId, email);
    }

    /**
     * 현재 가족 조회
     */
    public FamilyIdResponse getCurrentFamily(String memberId) {
        return familyService.getCurrentFamily(memberId);
    }

    /**
     * 중복 사진 조회
     */
    public boolean existsSamePhoto(Long familyId, String uploadMemberId, LocalDateTime targetTime) {
        return photoRepository.existsSamePhoto(familyId, uploadMemberId, targetTime) == 1;
    }

    /**
     * 사진 추가
     */
    public PhotoDTO migratePhoto(PhotoMigrateRequest photoMigrateRequest, PhotoAddRequest photoAddRequest) {
        validateUrl(photoAddRequest.url());

        Member member = findMemberById(photoMigrateRequest.memberId());
        Family family = findFamilyById(photoMigrateRequest.familyId());
        validateFamilyMember(family, member);

        Photo photo = new Photo(photoAddRequest.id(), photoAddRequest.url(), new Reaction());
        Reaction reaction = photo.getReaction();
        reaction.incrementFire(photoMigrateRequest.fire());
        reaction.incrementLove(photoMigrateRequest.love());
        reaction.incrementLike(photoMigrateRequest.like());
        reaction.incrementStar(photoMigrateRequest.star());

        photo.setUploadMember(member);
        family.addPhoto(photo);

        Photo savedPhoto = photoRepository.save(photo);
        familyRepository.save(family);

        photoRepository.updatePhotoCreatedAndUpdatedAt(
                savedPhoto.getId(),
                photoMigrateRequest.uploadDate(),
                photoMigrateRequest.uploadDate());
        return PhotoDTO.of(savedPhoto);
    }

    /**
     * 가족 생성
     */
    public FamilyCreateResponse createFamily(String memberId) {
        FamilyResponse familyResponse = familyService.createFamily(memberId);
        InviteCodeResponse inviteCodeResponse = familyService.createInviteCode(familyResponse.id(), memberId);
        return new FamilyCreateResponse(familyResponse.id(), inviteCodeResponse.code());
    }

    /**
     * 가족 참여
     */
    public FamilyResponse joinFamily(String code, String memberId) {
        return familyService.joinFamilyByInviteCode(code, memberId);
    }

    private void validateUrl(String photoUrl) {
        if (photoUrl == null || !photoUrl.startsWith(cdnPrefix)) {
            throw new CustomException(PhotoCustomErrorCode.INVALID_PHOTO_URL);
        }

        String lower = photoUrl.toLowerCase();
        if (!lower.matches(".*\\.(jpg|jpeg|png|webp|gif)$")) {
            throw new CustomException(PhotoCustomErrorCode.INVALID_PHOTO_URL);
        }
    }

    /**
     * 사진 삭제
     */
    public void deletePhoto(String memberId, UUID photoId) {
        Member member = findMemberById(memberId);
        photoService.deletePhoto(member.getFamily().getId(), photoId, memberId);
    }

    /**
     * 유저 삭제
     */
    public void deleteUser(String memberId) {
        memberService.deleteMember(memberId);
    }

    private Family findFamilyById(Long familyId) {
        return familyRepository.findById(familyId)
                .orElseThrow(() -> new CustomException(FamilyCustomErrorCode.FAMILY_NOT_FOUND));
    }

    private Member findMemberById(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberCustomErrorCode.MEMBER_ID_NOT_FOUND));
    }

    private void validateFamilyMember(Family family, Member member) {
        if (member.getFamily() != family) {
            throw new CustomException(FamilyCustomErrorCode.NOT_FAMILY_MEMBER);
        }
    }
}

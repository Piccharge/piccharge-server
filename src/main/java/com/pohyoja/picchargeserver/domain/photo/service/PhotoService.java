package com.pohyoja.picchargeserver.domain.photo.service;

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
import com.pohyoja.picchargeserver.domain.photo.dto.request.ReactionAddRequest;
import com.pohyoja.picchargeserver.domain.photo.dto.response.PhotoUrlResponse;
import com.pohyoja.picchargeserver.domain.photo.dto.response.PhotosResponse;
import com.pohyoja.picchargeserver.domain.photo.entity.Photo;
import com.pohyoja.picchargeserver.domain.photo.entity.Reaction;
import com.pohyoja.picchargeserver.domain.photo.exception.PhotoCustomErrorCode;
import com.pohyoja.picchargeserver.domain.photo.repository.PhotoRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PhotoService {

    private final FamilyRepository familyRepository;
    private final MemberRepository memberRepository;
    private final PhotoRepository photoRepository;

    /**
     * 가족의 최신 사진 조회
     */
    public PhotoDTO fetchLatestPhoto(Long familyId, String currentUserId) {
        Member member = findMemberById(currentUserId);
        Family family = findFamilyById(familyId);
        validateFamilyMember(family, member);

        Photo latestPhoto = photoRepository.findTopByFamilyOrderByCreatedAtDesc(family)
                .orElseThrow(() -> new CustomException(PhotoCustomErrorCode.PHOTO_NOT_FOUND));

        return PhotoDTO.of(latestPhoto);
    }

    /**
     * 가족의 사진 목록 조회 (페이지네이션)
     */
    public PhotosResponse fetchPhotos(Long familyId, String currentUserId, int page, int size) {
        Member member = findMemberById(currentUserId);
        Family family = findFamilyById(familyId);
        validateFamilyMember(family, member);
        validatePageSize(page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Photo> photos = photoRepository.findByFamilyId(familyId, pageable);
        boolean hasNext = photos.hasNext();
        List<PhotoUrlResponse> photoUrls = new ArrayList<>();
        for (Photo photo : photos) {
            photoUrls.add(PhotoUrlResponse.of(photo));
        }
        return new PhotosResponse(photoUrls, hasNext);
    }

    private void validatePageSize(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new CustomException(PhotoCustomErrorCode.INVALID_PAGE_SIZE);
        }
    }

    /**
     * 사진 상세 조회
     */
    public PhotoDTO getPhoto(Long familyId, UUID photoId, String currentUserId) {
        Member member = findMemberById(currentUserId);
        Family family = findFamilyById(familyId);
        validateFamilyMember(family, member);
        Photo photo = findPhotoById(photoId);
        return PhotoDTO.of(photo);
    }

    /**
     * 사진 추가
     */
    @Transactional
    public PhotoDTO addPhoto(Long familyId, String currentUserId, PhotoAddRequest request) {
        Member member = findMemberById(currentUserId);
        Family family = findFamilyById(familyId);
        validateFamilyMember(family, member);

        Photo photo = new Photo(request.id(), request.url(), new Reaction());
        photo.setUploadMember(member);
        family.addPhoto(photo);

        photoRepository.save(photo);
        familyRepository.save(family);

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
     * 사진 삭제
     */
    @Transactional
    public void deletePhoto(Long familyId, UUID photoId, String currentUserId) {
        Member member = findMemberById(currentUserId);
        Family family = findFamilyById(familyId);
        validateFamilyMember(family, member);
        Photo photo = findPhotoById(photoId);
        validateOwnership(photo, member);
        photo.clearAssociations();
        photoRepository.delete(photo);
    }

    private Photo findPhotoById(UUID photoId) {
        return photoRepository.findById(photoId)
                .orElseThrow(() -> new CustomException(PhotoCustomErrorCode.PHOTO_NOT_FOUND));
    }

    /**
     * 사진에 반응 추가
     */
    @Transactional
    public ReactionDTO addReaction(Long familyId, UUID photoId, String currentUserId, ReactionAddRequest request) {
        Member member = findMemberById(currentUserId);
        Family family = findFamilyById(familyId);
        validateFamilyMember(family, member);
        Reaction reaction = findPhotoById(photoId).getReaction();
        Reaction reactionCache = family.getTotalReaction();
        if (request.count() <= 0) {
            throw new CustomException(PhotoCustomErrorCode.INVALID_REACTION_COUNT);
        }
        switch (request.type().trim().toUpperCase()) {
            case "LOVE" -> {
                reaction.incrementLove(request.count());
                reactionCache.incrementLove(request.count());
            }
            case "FIRE" -> {
                reaction.incrementFire(request.count());
                reactionCache.incrementFire(request.count());
            }
            case "STAR" -> {
                reaction.incrementStar(request.count());
                reactionCache.incrementStar(request.count());
            }
            case "LIKE" -> {
                reaction.incrementLike(request.count());
                reactionCache.incrementLike(request.count());
            }
            default -> throw new CustomException(PhotoCustomErrorCode.INVALID_REACTION_TYPE);
        }
        return ReactionDTO.of(reaction);
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

    /**
     * 본인이 올린 사진인지 확인
     */
    private void validateOwnership(Photo photo, Member member) {
        if (!photo.getUploadMember().equals(member)) {
            throw new CustomException(PhotoCustomErrorCode.NOT_PHOTO_OWNER);
        }
    }
}

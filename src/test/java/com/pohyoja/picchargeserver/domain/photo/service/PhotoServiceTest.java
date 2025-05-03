package com.pohyoja.picchargeserver.domain.photo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pohyoja.picchargeserver.common.exception.CustomException;
import com.pohyoja.picchargeserver.domain.family.entity.Family;
import com.pohyoja.picchargeserver.domain.family.exception.FamilyCustomErrorCode;
import com.pohyoja.picchargeserver.domain.family.repository.FamilyRepository;
import com.pohyoja.picchargeserver.domain.member.entity.Member;
import com.pohyoja.picchargeserver.domain.member.entity.Role;
import com.pohyoja.picchargeserver.domain.member.repository.MemberRepository;
import com.pohyoja.picchargeserver.domain.photo.dto.PhotoDTO;
import com.pohyoja.picchargeserver.domain.photo.dto.ReactionDTO;
import com.pohyoja.picchargeserver.domain.photo.dto.request.PhotoAddRequest;
import com.pohyoja.picchargeserver.domain.photo.dto.request.ReactionAddRequest;
import com.pohyoja.picchargeserver.domain.photo.dto.response.PhotosResponse;
import com.pohyoja.picchargeserver.domain.photo.entity.Photo;
import com.pohyoja.picchargeserver.domain.photo.entity.Reaction;
import com.pohyoja.picchargeserver.domain.photo.exception.PhotoCustomErrorCode;
import com.pohyoja.picchargeserver.domain.photo.repository.PhotoRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(PhotoService.class)
@DisplayName("PhotoService 테스트")
class PhotoServiceTest {

    @Autowired
    private PhotoService photoService;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private FamilyRepository familyRepository;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setup() {
        photoRepository.deleteAll();
        memberRepository.deleteAll();
        familyRepository.deleteAll();
    }

    // 테스트용 고정 데이터
    private static final String MEMBER_ID = "test-member-id";
    private static final String MEMBER_NAME = "테스트멤버";
    private static final String MEMBER_EMAIL = "test@example.com";
    private static final Role MEMBER_ROLE = Role.PARENT;
    private static final String PHOTO_URL = "https://example.com/photo.jpg";

    // 테스트 데이터 생성 헬퍼 메소드
    private Member createTestMember() {
        return Member.builder()
                .uid(MEMBER_ID)
                .name(MEMBER_NAME)
                .email(MEMBER_EMAIL)
                .role(MEMBER_ROLE)
                .build();
    }

    private Member createTestMember(String uid, String name, String email, Role role) {
        return Member.builder()
                .uid(uid)
                .name(name)
                .email(email)
                .role(role)
                .build();
    }

    private Member saveMember(Member member) {
        return memberRepository.save(member);
    }

    private Member saveTestMember() {
        return saveMember(createTestMember());
    }

    private Family createAndSaveFamily() {
        Family family = new Family();
        return familyRepository.save(family);
    }

    private Family createAndSaveFamilyWithMember(Member member) {
        Family family = new Family();
        family.addMember(member);
        return familyRepository.save(family);
    }

    private Photo createPhoto(UUID id, String url, Family family, Member member) {
        Photo photo = new Photo(id, url, new Reaction());
        photo.setFamily(family);
        photo.setUploadMember(member);
        return photo;
    }

    private Photo savePhoto(Photo photo) {
        return photoRepository.save(photo);
    }

    private Photo createAndSavePhoto(Family family, Member member) {
        UUID photoId = UUID.randomUUID();
        Photo photo = createPhoto(photoId, PHOTO_URL, family, member);
        photo.setFamily(family);
        photo.setUploadMember(member);
        family.addPhoto(photo);
        photoRepository.save(photo);
        familyRepository.save(family);
        return photo;
    }

    private Photo createAndSavePhoto(UUID id, String url, Family family, Member member) {
        Photo photo = createPhoto(id, url, family, member);
        photo.setFamily(family);
        photo.setUploadMember(member);
        family.addPhoto(photo);
        photoRepository.save(photo);
        familyRepository.save(family);
        return photo;
    }

    @Nested
    @DisplayName("fetchLatestPhoto 테스트")
    class FetchLatestPhotoTests {

        @Test
        @DisplayName("최신 사진을 정상적으로 조회한다")
        void fetchLatestPhoto_Success() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamilyWithMember(member);
            Photo photo = createAndSavePhoto(family, member);

            // When
            PhotoDTO result = photoService.fetchLatestPhoto(family.getId(), member.getUid());

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(photo.getId());
            assertThat(result.urlString()).isEqualTo(photo.getUrl());
            assertThat(result.uploadBy()).isEqualTo(member.getName());
            assertThat(result.uploadUserId()).isEqualTo(member.getUid());
            assertThat(result.familyId()).isEqualTo(family.getId());
        }

        @Test
        @DisplayName("사진이 두장 있을 경우 날짜가 가장 최신인 사진이 불러와진다")
        void fetchLatestPhoto_ReturnsNewestPhoto() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamilyWithMember(member);

            // 첫 번째 사진 (이전 날짜)
            Photo olderPhoto = createAndSavePhoto(UUID.randomUUID(), "https://example.com/older.jpg", family, member);

            // 두 번째 사진 (최신 날짜) - 1초 후에 생성
            try {
                Thread.sleep(1000); // 1초 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Photo newerPhoto = createAndSavePhoto(UUID.randomUUID(), "https://example.com/newer.jpg", family, member);

            // When
            PhotoDTO result = photoService.fetchLatestPhoto(family.getId(), member.getUid());

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(newerPhoto.getId());
            assertThat(result.urlString()).isEqualTo(newerPhoto.getUrl());
        }

        @Test
        @DisplayName("사진이 없을 경우 예외가 발생한다")
        void fetchLatestPhoto_ThrowsExceptionWhenNoPhotos() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamilyWithMember(member);

            // When & Then
            assertThatThrownBy(() -> photoService.fetchLatestPhoto(family.getId(), member.getUid()))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(PhotoCustomErrorCode.PHOTO_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("가족 구성원이 아닌 경우 예외가 발생한다")
        void fetchLatestPhoto_ThrowsExceptionWhenNotFamilyMember() {
            // Given
            Member member = saveTestMember();
            Member otherMember = saveMember(createTestMember("other-id", "다른멤버", "other@example.com", Role.PARENT));
            Family family = createAndSaveFamilyWithMember(member);
            createAndSavePhoto(family, member);

            // When & Then
            assertThatThrownBy(() -> photoService.fetchLatestPhoto(family.getId(), otherMember.getUid()))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(FamilyCustomErrorCode.NOT_FAMILY_MEMBER.getCode()));
        }
    }

    @Nested
    @DisplayName("fetchPhotos 테스트")
    class FetchPhotosTests {

        @Test
        @DisplayName("사진 목록을 정상적으로 조회한다")
        void fetchPhotos_Success() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamilyWithMember(member);
            Photo photo1 = createAndSavePhoto(UUID.randomUUID(), "https://example.com/photo1.jpg", family, member);
            Photo photo2 = createAndSavePhoto(UUID.randomUUID(), "https://example.com/photo2.jpg", family, member);

            // When
            PhotosResponse result = photoService.fetchPhotos(family.getId(), member.getUid(), 0, 10);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.photoUrls()).hasSize(2);
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        @DisplayName("사진이 최신 순으로 정렬되어 있다")
        void fetchPhotos_SortedByCreatedAtDesc() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamilyWithMember(member);

            // 첫 번째 사진
            Photo olderPhoto = createAndSavePhoto(UUID.randomUUID(), "https://example.com/older.jpg", family, member);

            // 두 번째 사진 - 1초 후에 생성
            try {
                Thread.sleep(1000); // 1초 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Photo newerPhoto = createAndSavePhoto(UUID.randomUUID(), "https://example.com/newer.jpg", family, member);

            // When
            PhotosResponse result = photoService.fetchPhotos(family.getId(), member.getUid(), 0, 10);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.photoUrls()).hasSize(2);
            // 최신 사진이 먼저 나와야 함
            assertThat(result.photoUrls().get(0).urlString()).isEqualTo(newerPhoto.getUrl());
            assertThat(result.photoUrls().get(1).urlString()).isEqualTo(olderPhoto.getUrl());
        }

        @Test
        @DisplayName("페이징이 정상적으로 작동한다")
        void fetchPhotos_PaginationWorks() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamilyWithMember(member);

            // 5개의 사진 생성
            for (int i = 0; i < 5; i++) {
                createAndSavePhoto(UUID.randomUUID(), "https://example.com/photo" + i + ".jpg", family, member);
                try {
                    Thread.sleep(100); // 시간 차이를 두기 위해 대기
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // When - 첫 페이지 (2개)
            PhotosResponse firstPage = photoService.fetchPhotos(family.getId(), member.getUid(), 0, 2);

            // When - 두 번째 페이지 (2개)
            PhotosResponse secondPage = photoService.fetchPhotos(family.getId(), member.getUid(), 1, 2);

            // When - 세 번째 페이지 (1개)
            PhotosResponse thirdPage = photoService.fetchPhotos(family.getId(), member.getUid(), 2, 2);

            // Then
            assertThat(firstPage.photoUrls()).hasSize(2);
            assertThat(secondPage.photoUrls()).hasSize(2);
            assertThat(thirdPage.photoUrls()).hasSize(1);

            // 모든 사진이 다른지 확인
            assertThat(firstPage.photoUrls().get(0).urlString()).isNotEqualTo(
                    secondPage.photoUrls().get(0).urlString());
            assertThat(firstPage.photoUrls().get(1).urlString()).isNotEqualTo(
                    secondPage.photoUrls().get(1).urlString());
            assertThat(secondPage.photoUrls().get(0).urlString()).isNotEqualTo(
                    thirdPage.photoUrls().get(0).urlString());
        }

        @Test
        @DisplayName("hasNext가 정상적으로 작동한다")
        void fetchPhotos_HasNextWorks() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamilyWithMember(member);

            // 5개의 사진 생성
            for (int i = 0; i < 5; i++) {
                createAndSavePhoto(UUID.randomUUID(), "https://example.com/photo" + i + ".jpg", family, member);
            }

            // When - 첫 페이지 (2개)
            PhotosResponse firstPage = photoService.fetchPhotos(family.getId(), member.getUid(), 0, 2);

            // When - 두 번째 페이지 (2개)
            PhotosResponse secondPage = photoService.fetchPhotos(family.getId(), member.getUid(), 1, 2);

            // When - 세 번째 페이지 (1개)
            PhotosResponse thirdPage = photoService.fetchPhotos(family.getId(), member.getUid(), 2, 2);

            // When - 네 번째 페이지 (0개)
            PhotosResponse fourthPage = photoService.fetchPhotos(family.getId(), member.getUid(), 3, 2);

            // Then
            assertThat(firstPage.hasNext()).isTrue();
            assertThat(secondPage.hasNext()).isTrue();
            assertThat(thirdPage.hasNext()).isFalse();
            assertThat(fourthPage.hasNext()).isFalse();
        }

        @Test
        @DisplayName("사진이 없을 경우 빈 목록이 반환된다")
        void fetchPhotos_ReturnsEmptyListWhenNoPhotos() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamilyWithMember(member);

            // When
            PhotosResponse result = photoService.fetchPhotos(family.getId(), member.getUid(), 0, 10);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.photoUrls()).isEmpty();
            assertThat(result.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("getPhoto 테스트")
    class GetPhotoTests {

        @Test
        @DisplayName("사진을 정상적으로 조회한다")
        void getPhoto_Success() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamilyWithMember(member);
            Photo photo = createAndSavePhoto(family, member);

            // When
            PhotoDTO result = photoService.getPhoto(family.getId(), photo.getId(), member.getUid());

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(photo.getId());
            assertThat(result.urlString()).isEqualTo(photo.getUrl());
            assertThat(result.uploadBy()).isEqualTo(member.getName());
            assertThat(result.uploadUserId()).isEqualTo(member.getUid());
            assertThat(result.familyId()).isEqualTo(family.getId());
        }

        @Test
        @DisplayName("존재하지 않는 사진 ID로 조회 시 예외가 발생한다")
        void getPhoto_ThrowsExceptionWhenPhotoNotFound() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamilyWithMember(member);
            UUID nonExistentPhotoId = UUID.randomUUID();

            // When & Then
            assertThatThrownBy(() -> photoService.getPhoto(family.getId(), nonExistentPhotoId, member.getUid()))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(PhotoCustomErrorCode.PHOTO_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("가족 구성원이 아닌 경우 예외가 발생한다")
        void getPhoto_ThrowsExceptionWhenNotFamilyMember() {
            // Given
            Member member = saveTestMember();
            Member otherMember = saveMember(createTestMember("other-id", "다른멤버", "other@example.com", Role.PARENT));
            Family family = createAndSaveFamilyWithMember(member);
            Photo photo = createAndSavePhoto(family, member);

            // When & Then
            assertThatThrownBy(() -> photoService.getPhoto(family.getId(), photo.getId(), otherMember.getUid()))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(FamilyCustomErrorCode.NOT_FAMILY_MEMBER.getCode()));
        }
    }

    @Nested
    @DisplayName("addPhoto 테스트")
    class AddPhotoTests {

        @Test
        @DisplayName("사진을 정상적으로 추가한다")
        void addPhoto_Success() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamilyWithMember(member);
            UUID photoId = UUID.randomUUID();
            PhotoAddRequest request = new PhotoAddRequest(photoId, PHOTO_URL);

            // When
            PhotoDTO result = photoService.addPhoto(family.getId(), member.getUid(), request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(photoId);
            assertThat(result.urlString()).isEqualTo(PHOTO_URL);
            assertThat(result.uploadBy()).isEqualTo(member.getName());
            assertThat(result.uploadUserId()).isEqualTo(member.getUid());
            assertThat(result.familyId()).isEqualTo(family.getId());

            // 실제로 저장되었는지 확인
            assertThat(photoRepository.findById(photoId)).isPresent();
        }

        @Test
        @DisplayName("가족 구성원이 아닌 경우 예외가 발생한다")
        void addPhoto_ThrowsExceptionWhenNotFamilyMember() {
            // Given
            Member member = saveTestMember();
            Member otherMember = saveMember(createTestMember("other-id", "다른멤버", "other@example.com", Role.PARENT));
            Family family = createAndSaveFamilyWithMember(member);
            UUID photoId = UUID.randomUUID();
            PhotoAddRequest request = new PhotoAddRequest(photoId, PHOTO_URL);

            // When & Then
            assertThatThrownBy(() -> photoService.addPhoto(family.getId(), otherMember.getUid(), request))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(FamilyCustomErrorCode.NOT_FAMILY_MEMBER.getCode()));
        }
    }

    @Nested
    @DisplayName("deletePhoto 테스트")
    class DeletePhotoTests {

        @Test
        @DisplayName("사진을 정상적으로 삭제한다")
        void deletePhoto_Success() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamilyWithMember(member);
            Photo photo = createAndSavePhoto(family, member);
            UUID photoId = photo.getId();

            // When
            photoService.deletePhoto(family.getId(), photoId, member.getUid());

            // Then
            assertThat(photoRepository.findById(photoId)).isEmpty();
        }

        @Test
        @DisplayName("가족 구성원이지만 올린 사람이 아닐 경우 예외가 발생한다")
        void deletePhoto_ThrowsExceptionWhenNotPhotoOwner() {
            // Given
            Member member = saveTestMember();
            Member familyMember = saveMember(
                    createTestMember("family-member", "가족멤버", "family@example.com", Role.CHILD));
            Family family = createAndSaveFamilyWithMember(member);
            family.addMember(familyMember);
            familyRepository.save(family);

            Photo photo = createAndSavePhoto(family, member);
            UUID photoId = photo.getId();

            // When & Then
            assertThatThrownBy(() -> photoService.deletePhoto(family.getId(), photoId, familyMember.getUid()))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(PhotoCustomErrorCode.NOT_PHOTO_OWNER.getCode()));
        }

        @Test
        @DisplayName("가족 구성원이 아닌 경우 예외가 발생한다")
        void deletePhoto_ThrowsExceptionWhenNotFamilyMember() {
            // Given
            Member member = saveTestMember();
            Member otherMember = saveMember(createTestMember("other-id", "다른멤버", "other@example.com", Role.PARENT));
            Family family = createAndSaveFamilyWithMember(member);
            Photo photo = createAndSavePhoto(family, member);
            UUID photoId = photo.getId();

            // When & Then
            assertThatThrownBy(() -> photoService.deletePhoto(family.getId(), photoId, otherMember.getUid()))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(FamilyCustomErrorCode.NOT_FAMILY_MEMBER.getCode()));
        }

        @Test
        @DisplayName("존재하지 않는 사진 ID로 삭제 시 예외가 발생한다")
        void deletePhoto_ThrowsExceptionWhenPhotoNotFound() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamilyWithMember(member);
            UUID nonExistentPhotoId = UUID.randomUUID();

            // When & Then
            assertThatThrownBy(() -> photoService.deletePhoto(family.getId(), nonExistentPhotoId, member.getUid()))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(PhotoCustomErrorCode.PHOTO_NOT_FOUND.getCode()));
        }
    }

    @Nested
    @DisplayName("addReaction 테스트")
    class AddReactionTests {

        @Test
        @DisplayName("반응을 정상적으로 추가한다")
        void addReaction_Success() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamilyWithMember(member);
            Photo photo = createAndSavePhoto(family, member);
            ReactionAddRequest request = new ReactionAddRequest("LOVE", 1);

            // When
            ReactionDTO result = photoService.addReaction(family.getId(), photo.getId(), member.getUid(), request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.love()).isEqualTo(1);
            assertThat(result.fire()).isEqualTo(0);
            assertThat(result.star()).isEqualTo(0);
            assertThat(result.like()).isEqualTo(0);

            // 실제로 저장되었는지 확인
            Photo updatedPhoto = photoRepository.findById(photo.getId()).orElseThrow();
            assertThat(updatedPhoto.getReaction().getLoveCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("여러 종류의 반응을 추가할 수 있다")
        void addReaction_MultipleTypes() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamilyWithMember(member);
            Photo photo = createAndSavePhoto(family, member);

            // When
            photoService.addReaction(family.getId(), photo.getId(), member.getUid(), new ReactionAddRequest("LOVE", 2));
            photoService.addReaction(family.getId(), photo.getId(), member.getUid(), new ReactionAddRequest("FIRE", 1));
            ReactionDTO result = photoService.addReaction(family.getId(), photo.getId(), member.getUid(),
                    new ReactionAddRequest("STAR", 3));

            // Then
            assertThat(result).isNotNull();
            assertThat(result.love()).isEqualTo(2);
            assertThat(result.fire()).isEqualTo(1);
            assertThat(result.star()).isEqualTo(3);
            assertThat(result.like()).isEqualTo(0);

            // 실제로 저장되었는지 확인
            Photo updatedPhoto = photoRepository.findById(photo.getId()).orElseThrow();
            assertThat(updatedPhoto.getReaction().getLoveCount()).isEqualTo(2);
            assertThat(updatedPhoto.getReaction().getFireCount()).isEqualTo(1);
            assertThat(updatedPhoto.getReaction().getStarCount()).isEqualTo(3);
            assertThat(updatedPhoto.getReaction().getLikeCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("가족 구성원이 아닌 경우 예외가 발생한다")
        void addReaction_ThrowsExceptionWhenNotFamilyMember() {
            // Given
            Member member = saveTestMember();
            Member otherMember = saveMember(createTestMember("other-id", "다른멤버", "other@example.com", Role.PARENT));
            Family family = createAndSaveFamilyWithMember(member);
            Photo photo = createAndSavePhoto(family, member);
            ReactionAddRequest request = new ReactionAddRequest("LOVE", 1);

            // When & Then
            assertThatThrownBy(
                    () -> photoService.addReaction(family.getId(), photo.getId(), otherMember.getUid(), request))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(FamilyCustomErrorCode.NOT_FAMILY_MEMBER.getCode()));
        }

        @Test
        @DisplayName("유효하지 않은 반응 타입인 경우 예외가 발생한다")
        void addReaction_ThrowsExceptionWhenInvalidReactionType() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamilyWithMember(member);
            Photo photo = createAndSavePhoto(family, member);
            ReactionAddRequest request = new ReactionAddRequest("INVALID_TYPE", 1);

            // When & Then
            assertThatThrownBy(() -> photoService.addReaction(family.getId(), photo.getId(), member.getUid(), request))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(PhotoCustomErrorCode.INVALID_REACTION_TYPE.getCode()));
        }
    }
}

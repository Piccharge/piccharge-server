package com.pohyoja.picchargeserver.domain.family.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pohyoja.picchargeserver.common.exception.CustomException;
import com.pohyoja.picchargeserver.domain.family.dto.response.FamilyResponse;
import com.pohyoja.picchargeserver.domain.family.dto.response.FamilyUserNamesResponse;
import com.pohyoja.picchargeserver.domain.family.dto.response.InviteCodeResponse;
import com.pohyoja.picchargeserver.domain.family.entity.Family;
import com.pohyoja.picchargeserver.domain.family.entity.InviteCode;
import com.pohyoja.picchargeserver.domain.family.exception.FamilyCustomErrorCode;
import com.pohyoja.picchargeserver.domain.family.repository.FamilyRepository;
import com.pohyoja.picchargeserver.domain.family.repository.InviteCodeRepository;
import com.pohyoja.picchargeserver.domain.member.entity.Member;
import com.pohyoja.picchargeserver.domain.member.entity.Role;
import com.pohyoja.picchargeserver.domain.member.exception.MemberCustomErrorCode;
import com.pohyoja.picchargeserver.domain.member.repository.MemberRepository;
import com.pohyoja.picchargeserver.domain.photo.dto.ReactionDTO;
import com.pohyoja.picchargeserver.domain.photo.repository.PhotoRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(FamilyService.class)
class FamilyServiceTest {

    @Autowired
    private FamilyService familyService;

    @Autowired
    private FamilyRepository familyRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private InviteCodeRepository inviteCodeRepository;

    @BeforeEach
    void setup() {
        memberRepository.deleteAll();
        familyRepository.deleteAll();
        inviteCodeRepository.deleteAll();
        photoRepository.deleteAll();
    }

    // 테스트용 고정 데이터
    private static final String MEMBER_ID = "test-member-id";
    private static final String MEMBER_NAME = "테스트멤버";
    private static final String MEMBER_EMAIL = "test@example.com";
    private static final Role MEMBER_ROLE = Role.PARENT;

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

    private InviteCode createAndSaveInviteCode(Family family) {
        InviteCode inviteCode = new InviteCode(family);
        return inviteCodeRepository.save(inviteCode);
    }

    // Helper method to verify reaction counts are all zero
    private void verifyAllReactionCountsAreZero(ReactionDTO reactionDTO) {
        assertThat(reactionDTO).isNotNull();
        assertThat(reactionDTO.love()).isZero();
        assertThat(reactionDTO.fire()).isZero();
        assertThat(reactionDTO.star()).isZero();
        assertThat(reactionDTO.like()).isZero();
    }

    @Nested
    @DisplayName("가족 정보 조회 기능")
    class GetFamilyTests {

        @Test
        @DisplayName("가족 정보를 정상적으로 조회한다")
        void getFamily_Success() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamilyWithMember(member);

            // When
            FamilyResponse response = familyService.getFamily(family.getId(), member.getUid());

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(family.getId());
            assertThat(response.latestUploadTime()).isNotNull();

            // photoRepository.countByFamilyId()를 모킹하지 않은 경우, 0이 나온다는 가정 하에 검증
            assertThat(response.totalPhotoCount()).isZero();

            // Reaction 관련 값이 기본 0으로 잘 매핑되는지 확인
            verifyAllReactionCountsAreZero(response.reactionsCount());
        }

        @Test
        @DisplayName("존재하지 않는 가족 ID로 조회 시 예외가 발생한다")
        void getFamily_FamilyNotFound() {
            // Given
            Member member = saveTestMember();
            Long nonExistentFamilyId = 999L;

            // When & Then
            assertThatThrownBy(() -> familyService.getFamily(nonExistentFamilyId, member.getUid()))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(FamilyCustomErrorCode.FAMILY_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("가족 구성원이 아닌 경우 예외가 발생한다")
        void getFamily_NotFamilyMember() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamily(); // 멤버가 속하지 않은 가족

            // When & Then
            assertThatThrownBy(() -> familyService.getFamily(family.getId(), member.getUid()))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(FamilyCustomErrorCode.NOT_FAMILY_MEMBER.getCode()));
        }

        @Test
        @DisplayName("존재하지 않는 멤버 ID로 조회 시 예외가 발생한다")
        void getFamily_MemberNotFound() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamilyWithMember(member);
            String nonExistentMemberId = "non-existent-member-id";

            // When & Then
            assertThatThrownBy(() -> familyService.getFamily(family.getId(), nonExistentMemberId))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(MemberCustomErrorCode.MEMBER_ID_NOT_FOUND.getCode()));
        }
    }

    @Nested
    @DisplayName("가족 구성원 이름 조회 기능")
    class GetFamilyUserNamesTests {

        @Test
        @DisplayName("가족 구성원 이름을 정상적으로 조회한다")
        void getFamilyUserNames_Success() {
            // Given
            Member member1 = saveTestMember();
            Member member2 = saveMember(createTestMember("member2", "멤버2", "member2@example.com", Role.CHILD));

            Family family = createAndSaveFamilyWithMember(member1);
            family.addMember(member2);
            familyRepository.save(family);

            // When
            FamilyUserNamesResponse response = familyService.getFamilyUserNames(family.getId(), member1.getUid());

            // Then
            assertThat(response).isNotNull();
            assertThat(response.names()).hasSize(2);
            assertThat(response.names()).contains(member1.getName(), member2.getName());
        }

        @Test
        @DisplayName("존재하지 않는 가족 ID로 조회 시 예외가 발생한다")
        void getFamilyUserNames_FamilyNotFound() {
            // Given
            Member member = saveTestMember();
            Long nonExistentFamilyId = 999L;

            // When & Then
            assertThatThrownBy(() -> familyService.getFamilyUserNames(nonExistentFamilyId, member.getUid()))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(FamilyCustomErrorCode.FAMILY_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("존재하지 않는 멤버 ID로 조회 시 예외가 발생한다")
        void getFamilyUserNames_MemberNotFound() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamilyWithMember(member);
            String nonExistentMemberId = "non-existent-member-id";

            // When & Then
            assertThatThrownBy(() -> familyService.getFamilyUserNames(family.getId(), nonExistentMemberId))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(MemberCustomErrorCode.MEMBER_ID_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("가족은 있지만 멤버 ID가 잘못된 경우 예외가 발생한다")
        void getFamilyUserNames_FamilyExistsButMemberNotFound() {
            // Given
            Family family = createAndSaveFamily(); // 멤버가 없는 가족
            String nonExistentMemberId = "non-existent-member-id";

            // When & Then
            assertThatThrownBy(() -> familyService.getFamilyUserNames(family.getId(), nonExistentMemberId))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(MemberCustomErrorCode.MEMBER_ID_NOT_FOUND.getCode()));
        }
    }

    @Nested
    @DisplayName("가족 생성 기능")
    class CreateFamilyTests {

        @Test
        @DisplayName("가족을 정상적으로 생성한다")
        void createFamily_Success() {
            // Given
            Member member = saveTestMember();
            LocalDateTime beforeCreation = LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusSeconds(1);

            // When
            FamilyResponse response = familyService.createFamily(member.getUid());

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isNotNull();

            // 가족에 멤버가 추가되었는지 확인
            Member updatedMember = memberRepository.findById(member.getUid()).orElseThrow();
            assertThat(updatedMember.getFamily()).isNotNull();
            assertThat(updatedMember.getFamily().getId()).isEqualTo(response.id());

            // 생성 직후 lastPhotoAt이 now()로 세팅되므로, response.latestUploadTime()이 now()와 크게 벗어나지 않는지 검증
            assertThat(response.latestUploadTime()).isAfter(beforeCreation);
            assertThat(response.latestUploadTime()).isBefore(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusSeconds(1));

            // 가족 막 생성한 직후 사진 개수가 0인지 확인
            assertThat(response.totalPhotoCount()).isZero();

            // Reaction 관련 값이 기본 0으로 잘 매핑되는지 확인
            verifyAllReactionCountsAreZero(response.reactionsCount());
        }

        @Test
        @DisplayName("이미 가족에 속한 멤버가 가족 생성 시 예외가 발생한다")
        void createFamily_AlreadyFamilyMember() {
            // Given
            Member member = saveTestMember();

            // When & Then
            assertThatThrownBy(() -> familyService.createFamily(member.getUid()))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(FamilyCustomErrorCode.ALREADY_FAMILY_MEMBER.getCode()));
        }
    }

    @Nested
    @DisplayName("가족 나가기 기능")
    class LeaveFamilyTests {

        @Test
        @DisplayName("가족을 정상적으로 나간다")
        void leaveFamily_Success() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamilyWithMember(member);

            // When
            familyService.leaveFamily(family.getId(), member.getUid());

            // Then
            Member updatedMember = memberRepository.findById(member.getUid()).orElseThrow();
            assertThat(updatedMember.getFamily()).isNull();
        }

        @Test
        @DisplayName("여러 명이 남은 가족에서 한 명이 탈퇴해도 가족은 유지된다")
        void leaveFamily_WithMultipleMembers_FamilyRemains() {
            // Given
            Member member1 = saveTestMember();
            Member member2 = saveMember(createTestMember("member2", "멤버2", "member2@example.com", Role.CHILD));

            Family family = createAndSaveFamilyWithMember(member1);
            family.addMember(member2);
            familyRepository.save(family);
            Long familyId = family.getId();

            // When
            familyService.leaveFamily(familyId, member1.getUid());

            // Then
            // 탈퇴한 멤버는 가족이 없어야 함
            Member updatedMember1 = memberRepository.findById(member1.getUid()).orElseThrow();
            assertThat(updatedMember1.getFamily()).isNull();

            // 가족은 여전히 존재해야 함
            assertThat(familyRepository.findById(familyId)).isPresent();

            // 다른 멤버는 여전히 가족에 속해 있어야 함
            Family updatedFamily = familyRepository.findById(familyId).orElseThrow();
            assertThat(updatedFamily.getMembers()).hasSize(1);
            assertThat(updatedFamily.getMembers().stream().map(Member::getUid).toList()).contains(member2.getUid());
        }

        @Test
        @DisplayName("마지막 구성원이 가족을 나가면 가족이 삭제된다")
        void leaveFamily_LastMember_DeletesFamily() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamilyWithMember(member);
            Long familyId = family.getId();

            // When
            familyService.leaveFamily(familyId, member.getUid());

            // Then
            assertThat(familyRepository.findById(familyId)).isEmpty();
        }

        @Test
        @DisplayName("가족 구성원이 아닌 경우 예외가 발생한다")
        void leaveFamily_NotFamilyMember() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamily(); // 멤버가 속하지 않은 가족

            // When & Then
            assertThatThrownBy(() -> familyService.leaveFamily(family.getId(), member.getUid()))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(FamilyCustomErrorCode.NOT_FAMILY_MEMBER.getCode()));
        }

        @Test
        @DisplayName("존재하지 않는 멤버 ID로 호출 시 예외가 발생한다")
        void leaveFamily_MemberNotFound() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamilyWithMember(member);
            String nonExistentMemberId = "non-existent-member-id";

            // When & Then
            assertThatThrownBy(() -> familyService.leaveFamily(family.getId(), nonExistentMemberId))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(MemberCustomErrorCode.MEMBER_ID_NOT_FOUND.getCode()));
        }
    }

    @Nested
    @DisplayName("초대 코드 생성 기능")
    class CreateInviteCodeTests {

        @Test
        @DisplayName("초대 코드를 정상적으로 생성한다")
        void createInviteCode_Success() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamilyWithMember(member);
            LocalDateTime beforeCreation = LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusSeconds(1);

            // When
            InviteCodeResponse response = familyService.createInviteCode(family.getId(), member.getUid());

            // Then
            assertThat(response).isNotNull();
            assertThat(response.code()).isNotEmpty();

            // expiresAt 경계값 검사 - 현재 시점과 비교해 expiresAt이 무조건 미래인지
            assertThat(response.expiresAt()).isAfter(beforeCreation);
            assertThat(response.expiresAt()).isAfter(LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusSeconds(1));

            // 실제로 저장되었는지 확인
            Optional<InviteCode> savedCode = inviteCodeRepository.findByCode(response.code());
            assertThat(savedCode).isPresent();
            assertThat(savedCode.get().getFamily().getId()).isEqualTo(family.getId());
        }

        @Test
        @DisplayName("초대 코드 생성 시 inviteCodeRepository.save()가 호출된다")
        void createInviteCode_VerifySaveIsCalled() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamilyWithMember(member);

            // Mock repository to verify save is called
            InviteCodeRepository mockRepository = org.mockito.Mockito.mock(InviteCodeRepository.class);
            org.mockito.Mockito.when(mockRepository.save(org.mockito.ArgumentMatchers.any(InviteCode.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0)); // Return the argument

            FamilyService serviceMock = new FamilyService(
                    familyRepository,
                    memberRepository,
                    photoRepository,
                    mockRepository
            );

            // When
            serviceMock.createInviteCode(family.getId(), member.getUid());

            // Then
            org.mockito.Mockito.verify(mockRepository, org.mockito.Mockito.times(1))
                    .save(org.mockito.ArgumentMatchers.any(InviteCode.class));
        }

        @Test
        @DisplayName("이미 동일 코드가 DB에 있을 때 5번 재시도 후 CustomException이 발생한다")
        void createInviteCode_DuplicateCode() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamilyWithMember(member);

            // Mock the inviteCodeRepository to throw DataIntegrityViolationException
            InviteCodeRepository mockRepository = org.mockito.Mockito.mock(InviteCodeRepository.class);
            org.mockito.Mockito.when(mockRepository.save(org.mockito.ArgumentMatchers.any(InviteCode.class)))
                    .thenThrow(new org.springframework.dao.DataIntegrityViolationException("Duplicate code"));

            // Create service with mock repository
            FamilyService serviceMock = new FamilyService(
                    familyRepository,
                    memberRepository,
                    photoRepository,
                    mockRepository
            );

            // When & Then
            assertThatThrownBy(() -> serviceMock.createInviteCode(family.getId(), member.getUid()))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(FamilyCustomErrorCode.INVITE_CODE_GENERATION_FAILED.getCode()));
        }

        @Test
        @DisplayName("가족 구성원이 아닌 경우 예외가 발생한다")
        void createInviteCode_NotFamilyMember() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamily(); // 멤버가 속하지 않은 가족

            // When & Then
            assertThatThrownBy(() -> familyService.createInviteCode(family.getId(), member.getUid()))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(FamilyCustomErrorCode.NOT_FAMILY_MEMBER.getCode()));
        }
    }

    @Nested
    @DisplayName("초대 코드로 가족 구성원 이름 조회 기능")
    class GetFamilyUserNamesByInviteCodeTests {

        @Test
        @DisplayName("초대 코드로 가족 구성원 이름을 정상적으로 조회한다")
        void getFamilyUserNamesByInviteCode_Success() {
            // Given
            Member member1 = saveTestMember();
            Member member2 = saveMember(createTestMember("member2", "멤버2", "member2@example.com", Role.CHILD));

            Family family = createAndSaveFamilyWithMember(member1);
            family.addMember(member2);
            familyRepository.save(family);

            InviteCode inviteCode = createAndSaveInviteCode(family);

            // When
            FamilyUserNamesResponse response = familyService.getFamilyUserNamesByInviteCode(inviteCode.getCode());

            // Then
            assertThat(response).isNotNull();
            assertThat(response.names()).hasSize(2);
            assertThat(response.names()).contains(member1.getName(), member2.getName());
        }

        @Test
        @DisplayName("존재하지 않는 초대 코드로 조회 시 예외가 발생한다")
        void getFamilyUserNamesByInviteCode_InviteCodeNotFound() {
            // Given
            String nonExistentCode = "NONEXI";

            // When & Then
            assertThatThrownBy(() -> familyService.getFamilyUserNamesByInviteCode(nonExistentCode))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(FamilyCustomErrorCode.INVITE_CODE_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("만료되지 않은 초대 코드는 정상적으로 처리된다")
        void getFamilyUserNamesByInviteCode_NotExpired() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamilyWithMember(member);
            InviteCode inviteCode = createAndSaveInviteCode(family);

            // 만료 시간이 미래인지 확인
            assertThat(inviteCode.getExpiresAt()).isAfter(LocalDateTime.now(ZoneId.of("Asia/Seoul")));

            // When
            FamilyUserNamesResponse response = familyService.getFamilyUserNamesByInviteCode(inviteCode.getCode());

            // Then
            assertThat(response).isNotNull();
            assertThat(response.names()).contains(member.getName());
        }

        @Test
        @DisplayName("만료된 초대 코드로 조회 시 예외가 발생한다")
        void getFamilyUserNamesByInviteCode_Expired() {
            // Given
            Member member = saveTestMember();
            Family family = createAndSaveFamilyWithMember(member);

            // 만료된 초대 코드 생성 (리플렉션 사용)
            InviteCode inviteCode = new InviteCode(family);

            // 리플렉션으로 만료 시간을 과거로 설정
            try {
                java.lang.reflect.Field expiresAtField = InviteCode.class.getDeclaredField("expiresAt");
                expiresAtField.setAccessible(true);
                expiresAtField.set(inviteCode, LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusDays(1));
            } catch (Exception e) {
                throw new RuntimeException("Failed to set expired time", e);
            }

            inviteCodeRepository.save(inviteCode);

            // 만료 시간이 과거인지 확인
            assertThat(inviteCode.getExpiresAt()).isBefore(LocalDateTime.now(ZoneId.of("Asia/Seoul")));

            // When & Then
            assertThatThrownBy(() -> familyService.getFamilyUserNamesByInviteCode(inviteCode.getCode()))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(FamilyCustomErrorCode.INVITE_CODE_EXPIRED.getCode()));
        }
    }

    @Nested
    @DisplayName("초대 코드로 가족 참가 기능")
    class JoinFamilyByInviteCodeTests {

        @Test
        @DisplayName("초대 코드로 가족에 정상적으로 참가한다")
        void joinFamilyByInviteCode_Success() {
            // Given
            Member existingMember = saveMember(
                    createTestMember("existing", "기존멤버", "existing@example.com", Role.PARENT));
            Family family = createAndSaveFamilyWithMember(existingMember);
            InviteCode inviteCode = createAndSaveInviteCode(family);

            Member newMember = saveMember(createTestMember("new", "새멤버", "new@example.com", Role.CHILD));

            // When
            FamilyResponse response = familyService.joinFamilyByInviteCode(inviteCode.getCode(), newMember.getUid());

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(family.getId());

            // 멤버가 가족에 추가되었는지 확인
            Member updatedMember = memberRepository.findById(newMember.getUid()).orElseThrow();
            assertThat(updatedMember.getFamily()).isNotNull();
            assertThat(updatedMember.getFamily().getId()).isEqualTo(family.getId());

            // 가족에 멤버가 추가되었는지 확인
            Family updatedFamily = familyRepository.findById(family.getId()).orElseThrow();
            List<String> memberIds = updatedFamily.getMembers().stream().map(Member::getUid).toList();
            assertThat(memberIds).contains(existingMember.getUid(), newMember.getUid());

            // photoRepository.countByFamilyId()가 호출되는지 확인 (모킹하지 않았으므로 0 반환 예상)
            assertThat(response.totalPhotoCount()).isZero();

            // Reaction 관련 값이 기본 0으로 잘 매핑되는지 확인
            verifyAllReactionCountsAreZero(response.reactionsCount());
        }

        @Test
        @DisplayName("동일한 멤버가 같은 가족에 2회 연속 가입 시 예외가 발생한다")
        void joinFamilyByInviteCode_AlreadyJoined() {
            // Given
            Member existingMember = saveMember(
                    createTestMember("existing", "기존멤버", "existing@example.com", Role.PARENT));
            Family family = createAndSaveFamilyWithMember(existingMember);
            InviteCode inviteCode = createAndSaveInviteCode(family);

            Member newMember = saveMember(createTestMember("new", "새멤버", "new@example.com", Role.CHILD));

            // 첫 번째 가입 (정상)
            FamilyResponse response = familyService.joinFamilyByInviteCode(inviteCode.getCode(), newMember.getUid());
            assertThat(response).isNotNull();

            // 두 번째 가입 시도 (예외 발생 예상)
            assertThatThrownBy(() -> familyService.joinFamilyByInviteCode(inviteCode.getCode(), newMember.getUid()))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(FamilyCustomErrorCode.ALREADY_FAMILY_MEMBER.getCode()));
        }

        @Test
        @DisplayName("이미 가족에 속한 멤버가 다른 가족에 참가 시 예외가 발생한다")
        void joinFamilyByInviteCode_AlreadyFamilyMember() {
            // Given
            Member member = saveTestMember();

            Member otherMember = saveMember(createTestMember("other", "다른멤버", "other@example.com", Role.PARENT));
            Family family2 = createAndSaveFamilyWithMember(otherMember);
            InviteCode inviteCode = createAndSaveInviteCode(family2);

            // When & Then
            assertThatThrownBy(() -> familyService.joinFamilyByInviteCode(inviteCode.getCode(), member.getUid()))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(FamilyCustomErrorCode.ALREADY_FAMILY_MEMBER.getCode()));
        }

        @Test
        @DisplayName("존재하지 않는 초대 코드로 참가 시 예외가 발생한다")
        void joinFamilyByInviteCode_InviteCodeNotFound() {
            // Given
            Member member = saveTestMember();
            String nonExistentCode = "NONEXI";

            // When & Then
            assertThatThrownBy(() -> familyService.joinFamilyByInviteCode(nonExistentCode, member.getUid()))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(FamilyCustomErrorCode.INVITE_CODE_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("존재하지 않는 멤버 ID로 참가 시 예외가 발생한다")
        void joinFamilyByInviteCode_MemberNotFound() {
            // Given
            Member existingMember = saveMember(
                    createTestMember("existing", "기존멤버", "existing@example.com", Role.PARENT));
            Family family = createAndSaveFamilyWithMember(existingMember);
            InviteCode inviteCode = createAndSaveInviteCode(family);
            String nonExistentMemberId = "non-existent-member-id";

            // When & Then
            assertThatThrownBy(() -> familyService.joinFamilyByInviteCode(inviteCode.getCode(), nonExistentMemberId))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(MemberCustomErrorCode.MEMBER_ID_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("초대 코드 사용 후 삭제 시 두 번째 조회에서 예외가 발생한다")
        void joinFamilyByInviteCode_CodeUsedAndDeleted() {
            // Given
            Member existingMember = saveMember(
                    createTestMember("existing", "기존멤버", "existing@example.com", Role.PARENT));
            Family family = createAndSaveFamilyWithMember(existingMember);
            InviteCode inviteCode = createAndSaveInviteCode(family);
            String inviteCodeValue = inviteCode.getCode();

            Member newMember = saveMember(createTestMember("new", "새멤버", "new@example.com", Role.CHILD));

            // First join is successful
            familyService.joinFamilyByInviteCode(inviteCodeValue, newMember.getUid());

            // Manually delete the invite code to simulate code deletion after use
            inviteCodeRepository.delete(inviteCode);

            // Create another member to try joining with the same code
            Member anotherMember = saveMember(createTestMember("another", "또다른멤버", "another@example.com", Role.CHILD));

            // When & Then - Second attempt should fail
            assertThatThrownBy(() -> familyService.joinFamilyByInviteCode(inviteCodeValue, anotherMember.getUid()))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(FamilyCustomErrorCode.INVITE_CODE_NOT_FOUND.getCode()));
        }
    }
}

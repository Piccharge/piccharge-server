package com.pohyoja.picchargeserver.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pohyoja.picchargeserver.common.exception.CustomException;
import com.pohyoja.picchargeserver.domain.member.dto.MemberDTO;
import com.pohyoja.picchargeserver.domain.member.entity.Member;
import com.pohyoja.picchargeserver.domain.member.entity.Role;
import com.pohyoja.picchargeserver.domain.member.exception.MemberCustomErrorCode;
import com.pohyoja.picchargeserver.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class MemberServiceTest {

    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setup() {
        memberService = new MemberService(memberRepository);
    }

    // 테스트용 고정 데이터
    private static final String TEST_UID = "test-uid-123";
    private static final String TEST_NAME = "테스트이름";
    private static final String TEST_EMAIL = "test@example.com";
    private static final Role TEST_ROLE = Role.PARENT;

    // 테스트 데이터 생성 헬퍼 메소드
    private Member createTestMember() {
        return Member.builder()
                .uid(TEST_UID)
                .name(TEST_NAME)
                .email(TEST_EMAIL)
                .role(TEST_ROLE)
                .build();
    }

    private Member saveTestMember() {
        return memberRepository.save(createTestMember());
    }

    @BeforeEach
    void cleanup() {
        memberRepository.deleteAll();
    }

    @Nested
    @DisplayName("회원 조회 기능")
    class FindMemberTests {

        @Test
        @DisplayName("이메일로 회원 조회 시 정상적으로 반환한다")
        void findMemberByEmail_Success() {
            // Given
            saveTestMember();

            // When
            MemberDTO result = memberService.findMember(TEST_EMAIL, null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(TEST_UID);
            assertThat(result.email()).isEqualTo(TEST_EMAIL);
            assertThat(result.name()).isEqualTo(TEST_NAME);
        }

        @Test
        @DisplayName("이름으로 회원 조회 시 정상적으로 반환한다")
        void findMemberByName_Success() {
            // Given
            saveTestMember();

            // When
            MemberDTO result = memberService.findMember(null, TEST_NAME);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(TEST_UID);
            assertThat(result.name()).isEqualTo(TEST_NAME);
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 조회 시 예외가 발생한다")
        void findMemberByEmail_NotFound() {
            // Given
            String nonExistentEmail = "notfound@example.com";

            // When & Then
            assertThatThrownBy(() -> memberService.findMember(nonExistentEmail, null))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        @DisplayName("존재하지 않는 이름으로 조회 시 예외가 발생한다")
        void findMemberByName_NotFound() {
            // Given
            String nonExistentName = "존재하지않는이름";

            // When & Then
            assertThatThrownBy(() -> memberService.findMember(null, nonExistentName))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        @DisplayName("이메일과 이름 모두 null인 경우 예외가 발생한다")
        void findMember_BothNull() {
            // When & Then
            assertThatThrownBy(() -> memberService.findMember(null, null))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        @DisplayName("ID로 회원 조회 시 정상적으로 반환한다")
        void findMemberById_Success() {
            // Given
            saveTestMember();

            // When
            MemberDTO result = memberService.findMemberById(TEST_UID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(TEST_UID);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 예외가 발생한다")
        void findMemberById_NotFound() {
            // Given
            String nonExistentId = "non-existent-id";

            // When & Then
            assertThatThrownBy(() -> memberService.findMemberById(nonExistentId))
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    @DisplayName("회원 저장 기능")
    class SaveMemberTests {

        @Test
        @DisplayName("새로운 회원 정보 저장 시 정상적으로 저장된다")
        void saveMember_Success() {
            // Given
            String newUid = "new-uid-456";
            String newName = "새멤버";
            String newEmail = "new@example.com";

            // When
            MemberDTO savedDTO = memberService.saveMember(newName, newUid, newEmail);

            // Then
            assertThat(savedDTO).isNotNull();
            assertThat(savedDTO.id()).isEqualTo(newUid);
            assertThat(savedDTO.name()).isEqualTo(newName);
            assertThat(savedDTO.email()).isEqualTo(newEmail);

            // DB에서 조회하여 확인
            Member savedMember = memberRepository.findById(newUid).orElse(null);
            assertThat(savedMember).isNotNull();
            assertThat(savedMember.getUid()).isEqualTo(newUid);
            assertThat(savedMember.getName()).isEqualTo(newName);
        }

        @Test
        @DisplayName("이미 존재하는 ID로 저장 시 예외가 발생한다")
        void saveMember_DuplicateID() {
            // Given
            saveTestMember();

            String duplicateName = "다른이름";
            String duplicateEmail = "different@example.com";

            // When & Then
            assertThatThrownBy(() -> memberService.saveMember(duplicateName, TEST_UID, duplicateEmail))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        @DisplayName("이미 존재하는 이름으로 저장 시 예외가 발생한다")
        void saveMember_DuplicateName() {
            // Given
            saveTestMember(); // 기존 회원 저장 (TEST_NAME 사용)

            String newUid = "new-uid-789";
            String duplicateName = TEST_NAME; // 이미 존재하는 이름
            String newEmail = "another@example.com";

            // When & Then
            assertThatThrownBy(() -> memberService.saveMember(duplicateName, newUid, newEmail))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        @DisplayName("이름이 2자 미만인 경우 예외가 발생한다")
        void saveMember_NameTooShort() {
            // Given
            String newUid = "new-uid-short";
            String shortName = "김"; // 1자 (한글 기준)
            String newEmail = "short@example.com";

            // When & Then
            assertThatThrownBy(() -> memberService.saveMember(shortName, newUid, newEmail))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(MemberCustomErrorCode.INVALID_NAME_LENGTH.getCode()));
        }

        @Test
        @DisplayName("이름이 12자를 초과하는 경우 예외가 발생한다")
        void saveMember_NameTooLong() {
            // Given
            String newUid = "new-uid-long";
            String longName = "가나다라마바사아자차카타파"; // 13자 (한글 기준)
            String newEmail = "long@example.com";

            // When & Then
            assertThatThrownBy(() -> memberService.saveMember(longName, newUid, newEmail))
                    .isInstanceOf(CustomException.class)
                    .matches(e -> ((CustomException) e).getErrorCode().getCode()
                            .equals(MemberCustomErrorCode.INVALID_NAME_LENGTH.getCode()));
        }

        @Test
        @DisplayName("이름이 2자에서 12자 사이인 경우 정상적으로 저장된다")
        void saveMember_ValidNameLength() {
            // Given
            String newUid = "new-uid-valid";
            String validName = "김철수"; // 3자 (한글 기준)
            String newEmail = "valid@example.com";

            // When
            MemberDTO savedDTO = memberService.saveMember(validName, newUid, newEmail);

            // Then
            assertThat(savedDTO).isNotNull();
            assertThat(savedDTO.name()).isEqualTo(validName);

            // DB에서 조회하여 확인
            Member savedMember = memberRepository.findById(newUid).orElse(null);
            assertThat(savedMember).isNotNull();
            assertThat(savedMember.getName()).isEqualTo(validName);
        }
    }

    @Nested
    @DisplayName("회원 삭제 기능")
    class DeleteMemberTests {

        @Test
        @DisplayName("존재하는 회원 ID로 삭제 시 정상적으로 삭제된다")
        void deleteMember_Success() {
            // Given
            saveTestMember();

            // 삭제 전 존재 확인
            assertThat(memberRepository.existsById(TEST_UID)).isTrue();

            // When
            memberService.deleteMember(TEST_UID);

            // Then
            assertThat(memberRepository.existsById(TEST_UID)).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 ID로 삭제 시 예외가 발생한다")
        void deleteMember_NotFound() {
            // Given
            String nonExistentId = "non-existent-id";

            // When & Then
            assertThatThrownBy(() -> memberService.deleteMember(nonExistentId))
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    @DisplayName("DTO 변환 테스트")
    class ConvertToDTOTests {

        @Test
        @DisplayName("Family가 없는 회원의 DTO 변환 시 관련 필드가 비어있다")
        void convertToDTO_WithoutFamily() {
            // Given
            saveTestMember();

            // When
            MemberDTO dto = memberService.findMemberById(TEST_UID);

            // Then
            assertThat(dto).isNotNull();
            assertThat(dto.connectedTo()).isNotNull().isEmpty();
            assertThat(dto.familyId()).isNull();
        }

        @Test
        @DisplayName("특수값(null, 빈값 등)이 있는 회원도 정상적으로 DTO로 변환된다")
        void convertToDTO_WithSpecialValues() {
            // Given
            Member specialMember = Member.builder()
                    .uid("special-uid")
                    .name("")  // 빈 문자열
                    .email(null)  // null 이메일
                    .role(null)   // null 역할
                    .build();
            memberRepository.save(specialMember);

            // When
            MemberDTO dto = memberService.findMemberById("special-uid");

            // Then
            assertThat(dto).isNotNull();
            assertThat(dto.id()).isEqualTo("special-uid");
            assertThat(dto.name()).isEmpty();
            assertThat(dto.email()).isNull();
            assertThat(dto.role()).isNull();
        }
    }
}

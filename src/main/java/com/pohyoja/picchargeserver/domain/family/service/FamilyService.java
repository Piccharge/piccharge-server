package com.pohyoja.picchargeserver.domain.family.service;

import com.pohyoja.picchargeserver.common.exception.CustomException;
import com.pohyoja.picchargeserver.domain.family.dto.response.FamilyResponse;
import com.pohyoja.picchargeserver.domain.family.dto.response.FamilyUserNamesResponse;
import com.pohyoja.picchargeserver.domain.family.dto.response.InviteCodeResponse;
import com.pohyoja.picchargeserver.domain.family.dto.response.LatestUploadTimeResponse;
import com.pohyoja.picchargeserver.domain.family.entity.Family;
import com.pohyoja.picchargeserver.domain.family.entity.InviteCode;
import com.pohyoja.picchargeserver.domain.family.exception.FamilyCustomErrorCode;
import com.pohyoja.picchargeserver.domain.family.repository.FamilyRepository;
import com.pohyoja.picchargeserver.domain.family.repository.InviteCodeRepository;
import com.pohyoja.picchargeserver.domain.member.entity.Member;
import com.pohyoja.picchargeserver.domain.member.exception.MemberCustomErrorCode;
import com.pohyoja.picchargeserver.domain.member.repository.MemberRepository;
import com.pohyoja.picchargeserver.domain.photo.dto.ReactionDTO;
import com.pohyoja.picchargeserver.domain.photo.repository.PhotoRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final MemberRepository memberRepository;
    private final PhotoRepository photoRepository;
    private final InviteCodeRepository inviteCodeRepository;

    /**
     * 가족 정보 조회
     */
    public FamilyResponse getFamily(Long familyId, String currentUserId) {
        Member member = findMemberById(currentUserId);
        Family family = findFamilyById(familyId);
        validateFamilyMember(family, member);

        return convertToFamilyResponse(family);
    }

    /**
     * 가족 구성원들 이름 가져오기
     */
    public FamilyUserNamesResponse getFamilyUserNames(Long familyId, String currentUserId) {
        Member member = findMemberById(currentUserId);
        Family family = findFamilyById(familyId);
        validateFamilyMember(family, member);

        List<String> memberNames = family.getMembers().stream()
                .map(Member::getName)
                .toList();

        return new FamilyUserNamesResponse(memberNames);
    }

    /**
     * 가족 생성
     */
    @Transactional
    public FamilyResponse createFamily(String currentUserId) {
        Member member = findMemberById(currentUserId);

        if (member.getFamily() != null) {
            throw new CustomException(FamilyCustomErrorCode.ALREADY_FAMILY_MEMBER);
        }

        Family family = new Family();
        family.addMember(member);

        Family savedFamily = familyRepository.save(family);
        log.info("Family created: {}", savedFamily.getId());

        return convertToFamilyResponse(savedFamily);
    }

    /**
     * 가족 나가기 (본인)
     */
    @Transactional
    public void leaveFamily(Long familyId, String currentUserId) {
        Member member = findMemberById(currentUserId);
        Family family = findFamilyById(familyId);
        validateFamilyMember(family, member);

        // 연관관계 제거
        member.setFamily(null);
        family.getMembers().remove(member);

        if (family.getMembers().isEmpty()) {
            deleteFamily(family, member);
        }
        log.info("Member {} left family {}", currentUserId, familyId);
    }

    /**
     * 가족 삭제
     */
    private void deleteFamily(Family family, Member member) {
        familyRepository.delete(family);
        log.info("Family deleted: {}", family.getId());
    }

    /**
     * 가족의 최근 사진 업로드 시간 조회
     */
    public LatestUploadTimeResponse getLatestPhotoUploadTime(Long familyId, String currentUserId) {
        Member member = findMemberById(currentUserId);
        Family family = findFamilyById(familyId);
        validateFamilyMember(family, member);

        LocalDateTime latestTime = family.getLastPhotoAt();
        return new LatestUploadTimeResponse(latestTime);
    }

    /**
     * 초대 코드 생성
     */
    @Transactional
    public InviteCodeResponse createInviteCode(Long familyId, String currentUserId) {
        Member member = findMemberById(currentUserId);
        Family family = findFamilyById(familyId);
        validateFamilyMember(family, member);

        if (family.getMembers().size() >= 6) {
            throw new CustomException(FamilyCustomErrorCode.FAMILY_ALREADY_FULL);
        }

        InviteCode inviteCode;
        // 최대 5회까지 재시도 (너무 오래 걸리지 않도록 제한)
        for (int attempt = 1; ; attempt++) {
            inviteCode = new InviteCode(family);
            try {
                inviteCodeRepository.save(inviteCode);
                // 저장 성공하면 루프 탈출
                break;
            } catch (DataIntegrityViolationException ex) {
                // 유니크 제약 위반이라면 코드가 중복된 것.
                if (attempt >= 5) {
                    throw new CustomException(FamilyCustomErrorCode.INVITE_CODE_GENERATION_FAILED);
                }
                // 로그 남기고 재시도
                log.warn("Invite code collision on attempt {}: {}. Retrying...", attempt, inviteCode.getCode());
            }
        }

        log.info("Invite code created for family {}: {}", familyId, inviteCode.getCode());

        return new InviteCodeResponse(
                inviteCode.getCode(),
                inviteCode.getExpiresAt()
        );
    }

    /**
     * 초대 코드로 가족 방 구성원들 이름 조회
     */
    public FamilyUserNamesResponse getFamilyUserNamesByInviteCode(String code) {
        InviteCode inviteCode = findInviteCodeByCode(code);

        validateInviteCodeExpired(inviteCode);

        List<String> memberNames = inviteCode.getFamily().getMembers().stream()
                .map(Member::getName)
                .toList();
        return new FamilyUserNamesResponse(memberNames);
    }

    /**
     * 초대 코드로 가족 참가
     */
    @Transactional
    public FamilyResponse joinFamilyByInviteCode(String code, String currentUserId) {
        Member member = findMemberById(currentUserId);

        if (member.getFamily() != null) {
            throw new CustomException(FamilyCustomErrorCode.ALREADY_FAMILY_MEMBER);
        }

        InviteCode inviteCode = findInviteCodeByCode(code);
        validateInviteCodeExpired(inviteCode);

        Family family = inviteCode.getFamily();
        if (family.getMembers().size() >= 6) {
            throw new CustomException(FamilyCustomErrorCode.FAMILY_ALREADY_FULL);
        }
        family.addMember(member);

        log.info("Member {} joined family {} with invite code", currentUserId, family.getId());

        return convertToFamilyResponse(family);
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
     * 초대 코드로 조회
     */
    private InviteCode findInviteCodeByCode(String code) {
        return inviteCodeRepository.findByCode(code)
                .orElseThrow(() -> new CustomException(FamilyCustomErrorCode.INVITE_CODE_NOT_FOUND));
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
     * Family 엔티티를 FamilyResponse DTO로 변환
     */
    private FamilyResponse convertToFamilyResponse(Family family) {
        return new FamilyResponse(
                family.getId(),
                family.getLastPhotoAt(),
                photoRepository.countByFamilyId(family.getId()),
                ReactionDTO.of(family.getReaction())
        );
    }

    /**
     * 초대 코드 만료 검증
     */
    private static void validateInviteCodeExpired(InviteCode inviteCode) {
        if (inviteCode.getExpiresAt().isBefore(LocalDateTime.now(ZoneId.of("Asia/Seoul")))) {
            throw new CustomException(FamilyCustomErrorCode.INVITE_CODE_EXPIRED);
        }
    }
}
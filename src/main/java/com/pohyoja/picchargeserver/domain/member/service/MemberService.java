package com.pohyoja.picchargeserver.domain.member.service;

import com.pohyoja.picchargeserver.common.exception.CustomException;
import com.pohyoja.picchargeserver.domain.member.dto.MemberDTO;
import com.pohyoja.picchargeserver.domain.member.entity.Member;
import com.pohyoja.picchargeserver.domain.member.entity.Role;
import com.pohyoja.picchargeserver.domain.member.exception.MemberCustomErrorCode;
import com.pohyoja.picchargeserver.domain.member.repository.MemberRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberDTO findMember(String email, String name) {
        if (email != null) {
            return findMemberByEmail(email);
        } else if (name != null) {
            return findMemberByName(name);
        }
        throw new CustomException(MemberCustomErrorCode.MISSING_PARAMETER);
    }

    /**
     * 이메일로 유저를 조회합니다.
     */
    private MemberDTO findMemberByEmail(String email) {
        log.info("Fetching user by email: {}", email);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(MemberCustomErrorCode.MEMBER_EMAIL_NOT_FOUND));

        return convertToDTO(member);
    }

    /**
     * 이름으로 유저를 조회합니다.
     */
    private MemberDTO findMemberByName(String name) {
        log.info("Fetching user by name: {}", name);
        Member member = memberRepository.findByName(name)
                .orElseThrow(() -> new CustomException(MemberCustomErrorCode.MEMBER_NAME_NOT_FOUND));

        return convertToDTO(member);
    }

    /**
     * ID로 유저를 조회합니다.
     */
    public MemberDTO findMemberById(String uid) {
        Member member = memberRepository.findById(uid)
                .orElseThrow(() -> new CustomException(MemberCustomErrorCode.MEMBER_ID_NOT_FOUND));

        return convertToDTO(member);
    }

    /**
     * 유저 정보를 저장합니다.
     */
    @Transactional
    public MemberDTO saveMember(String name, String uid, String email) {
        if (memberRepository.existsById(uid)) {
            throw new CustomException(MemberCustomErrorCode.MEMBER_ALREADY_EXISTS);
        }

        // 이름 길이 체크 (2자에서 12자 이내, 한글 기준)
        if (name != null && !name.isEmpty() && (name.length() < 2 || name.length() > 12)) {
            throw new CustomException(MemberCustomErrorCode.INVALID_NAME_LENGTH);
        }

        // 이름 중복 체크 (이름이 null이 아닌 경우에만)
        if (name != null && !name.isEmpty() && memberRepository.findByName(name).isPresent()) {
            throw new CustomException(MemberCustomErrorCode.MEMBER_ALREADY_EXISTS);
        }

        Member member = Member.builder()
                .uid(uid)
                .name(name)
                .email(email)
                .role(Role.CHILD)
                .build();

        Member savedMember = memberRepository.save(member);
        log.info("Member saved: {}", savedMember.getUid());
        return convertToDTO(savedMember);
    }

    /**
     * 유저 정보를 삭제합니다.
     */
    @Transactional
    public void deleteMember(String uid) {
        if (!memberRepository.existsById(uid)) {
            throw new CustomException(MemberCustomErrorCode.MEMBER_ID_NOT_FOUND);
        }
        memberRepository.deleteById(uid);
        log.info("Member deleted: {}", uid);
    }

    /**
     * Member 엔티티를 DTO로 변환합니다.
     */
    private MemberDTO convertToDTO(Member member) {
        List<String> connectedTo = new ArrayList<>();
        if (member.getFamily() != null && member.getFamily().getMembers() != null) {
            member.getFamily().getMembers().stream()
                    .filter(m -> !m.getUid().equals(member.getUid()))
                    .forEach(m -> connectedTo.add(m.getUid()));
        }

        Long familyId = null;
        if (member.getFamily() != null) {
            familyId = member.getFamily().getId();
        }

        return new MemberDTO(
                member.getUid(),
                member.getName(),
                member.getRole(),
                member.getEmail(),
                connectedTo,
                member.getUploadCycle(),
                familyId
        );
    }
}

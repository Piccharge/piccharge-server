package com.pohyoja.picchargeserver.domain.member.controller;

import com.pohyoja.picchargeserver.common.BaseResponse;
import com.pohyoja.picchargeserver.common.security.JwtUserDetails;
import com.pohyoja.picchargeserver.domain.member.dto.MemberDTO;
import com.pohyoja.picchargeserver.domain.member.dto.request.MemberAddRequest;
import com.pohyoja.picchargeserver.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/users")
    @Operation(summary = "이메일 또는 이름으로 회원 조회",
            description = "이메일이나 이름으로 회원을 조회합니다. 둘 중 하나는 반드시 입력해야 합니다.")
    public BaseResponse<MemberDTO> fetchUser(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name) {

        return BaseResponse.onSuccess(memberService.findMember(email, name));
    }

    /**
     * 원격 스토리지에 유저 정보를 저장합니다.
     */
    @PostMapping("/users")
    public BaseResponse<MemberDTO> addUser(
            @RequestBody MemberAddRequest memberAddRequest,
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        log.info("Adding user. Request from uid: {}", userDetails.uid());
        return BaseResponse.onSuccess(memberService.saveMember(
                memberAddRequest.name(),
                userDetails.uid(),
                userDetails.email()));
    }

    /**
     * 원격 스토리지에서 유저 정보를 삭제합니다.
     */
    @DeleteMapping("/users")
    public BaseResponse<Void> deleteUser(@AuthenticationPrincipal JwtUserDetails userDetails) {
        log.info("Deleting user with uid: {}", userDetails.uid());
        memberService.deleteMember(userDetails.uid());
        return BaseResponse.onSuccess(null);
    }

    /**
     * 사용자 본인의 정보를 가져옵니다.
     */
    @GetMapping("/me")
    public BaseResponse<MemberDTO> fetchCurrentUser(@AuthenticationPrincipal JwtUserDetails userDetails) {
        log.info("Fetching current user information for uid: {}", userDetails.uid());
        return BaseResponse.onSuccess(memberService.findMemberById(userDetails.uid()));
    }
}
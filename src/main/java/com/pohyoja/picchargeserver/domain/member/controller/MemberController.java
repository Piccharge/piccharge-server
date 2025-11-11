package com.pohyoja.picchargeserver.domain.member.controller;

import com.pohyoja.picchargeserver.common.BaseResponse;
import com.pohyoja.picchargeserver.common.security.JwtUserDetails;
import com.pohyoja.picchargeserver.domain.member.dto.MemberDTO;
import com.pohyoja.picchargeserver.domain.member.dto.request.MemberAddRequest;
import com.pohyoja.picchargeserver.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "01. 유저 API", description = "회원 정보를 관리하는 API")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/users")
    @Operation(
            summary = "이메일 또는 이름으로 회원 조회",
            description = "이메일이나 이름으로 회원을 조회합니다. 둘 중 하나는 반드시 입력해야 합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (필수 매개변수 누락)", content = @Content)
    })
    public BaseResponse<MemberDTO> fetchUser(
            @Parameter(description = "조회할 회원의 이메일", example = "example@email.com")
            @RequestParam(required = false) String email,

            @Parameter(description = "조회할 회원의 이름", example = "홍길동")
            @RequestParam(required = false) String name) {

        return BaseResponse.onSuccess(memberService.findMember(email, name));
    }

    @PostMapping("/users")
    @Operation(
            summary = "회원 정보 저장",
            description = "원격 스토리지에 유저 정보를 저장합니다. 토큰에서 추출한 인증 정보를 바탕으로 저장합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "저장 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (이름 길이 제한, 중복 회원 등)", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    public BaseResponse<MemberDTO> addUser(
            @Parameter(description = "저장할 회원 정보", required = true)
            @RequestBody MemberAddRequest memberAddRequest,

            @Parameter(description = "현재 인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        log.info("Adding user. Request from uid: {}", userDetails.uid());
        return BaseResponse.onSuccess(memberService.saveMember(
                memberAddRequest.name(),
                userDetails.uid(),
                userDetails.email()));
    }

    @DeleteMapping("/users")
    @Operation(
            summary = "회원 정보 삭제",
            description = "원격 스토리지에서 현재 인증된 사용자의 정보를 삭제합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    public BaseResponse<Void> deleteUser(
            @Parameter(description = "현재 인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        log.info("Deleting user with uid: {}", userDetails.uid());
        memberService.deleteMember(userDetails.uid());
        return BaseResponse.onSuccess(null);
    }

    @GetMapping("/me")
    @Operation(
            summary = "현재 사용자 정보 조회",
            description = "현재 인증된 사용자의 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    public BaseResponse<MemberDTO> fetchCurrentUser(
            @Parameter(description = "현재 인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        log.info("Fetching current user information for uid: {}", userDetails.uid());
        return BaseResponse.onSuccess(memberService.findMemberById(userDetails.uid()));
    }
}
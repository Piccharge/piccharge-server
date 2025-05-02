package com.pohyoja.picchargeserver.domain.family.controller;

import com.pohyoja.picchargeserver.common.BaseResponse;
import com.pohyoja.picchargeserver.common.security.JwtUserDetails;
import com.pohyoja.picchargeserver.domain.family.dto.response.FamilyResponse;
import com.pohyoja.picchargeserver.domain.family.dto.response.FamilyUserNamesResponse;
import com.pohyoja.picchargeserver.domain.family.dto.response.InviteCodeResponse;
import com.pohyoja.picchargeserver.domain.family.dto.response.LatestUploadTimeResponse;
import com.pohyoja.picchargeserver.domain.family.service.FamilyService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "02. 가족 API", description = "가족 관련 API")
public class FamilyController {

    private final FamilyService familyService;

    @GetMapping("/families/{familyId}")
    @Operation(
            summary = "가족 정보 조회",
            description = "가족 ID로 가족 정보를 조회합니다. 가족 구성원만 접근 가능합니다. 누적 업로드 수, 누적 반응 수를 포함합니다 (홈 화면에서 호출)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "가족을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "403", description = "가족 구성원이 아님", content = @Content)
    })
    public BaseResponse<FamilyResponse> getFamily(
            @Parameter(description = "조회할 가족 ID", required = true, example = "1")
            @PathVariable Long familyId,

            @Parameter(description = "현재 인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        log.info("Getting family info for family ID: {}", familyId);
        return BaseResponse.onSuccess(familyService.getFamily(familyId, userDetails.uid()));
    }

    @GetMapping("/families/{familyId}/members")
    @Operation(
            summary = "가족 구성원들 이름 조회",
            description = "가족 ID로 가족 구성원들의 이름을 조회합니다. 가족 구성원만 접근 가능합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "가족을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "403", description = "가족 구성원이 아님", content = @Content)
    })
    public BaseResponse<FamilyUserNamesResponse> getFamilyUserNames(
            @Parameter(description = "조회할 가족 ID", required = true, example = "1")
            @PathVariable Long familyId,

            @Parameter(description = "현재 인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        log.info("Getting family members for family ID: {}", familyId);
        return BaseResponse.onSuccess(familyService.getFamilyUserNames(familyId, userDetails.uid()));
    }

    @PostMapping("/families")
    @Operation(
            summary = "가족 생성",
            description = "새로운 가족 방을 생성합니다. 이미 가족에 참여중이면 불가능합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "이미 가족 구성원임", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    public BaseResponse<FamilyResponse> createFamily(
            @Parameter(description = "현재 인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        log.info("Creating new family by user: {}", userDetails.uid());
        return BaseResponse.onSuccess(familyService.createFamily(userDetails.uid()));
    }

    @DeleteMapping("/families/{familyId}")
    @Operation(
            summary = "가족 나가기 (본인)",
            description = "가족 방에서 나갑니다. 본인만 가능합니다. 구성원이 1명인 경우 가족이 삭제됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "나가기 성공"),
            @ApiResponse(responseCode = "404", description = "가족을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "403", description = "가족 구성원이 아님", content = @Content)
    })
    public BaseResponse<Void> leaveFamily(
            @Parameter(description = "나갈 가족 ID", required = true, example = "1")
            @PathVariable Long familyId,

            @Parameter(description = "현재 인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        log.info("User {} leaving family {}", userDetails.uid(), familyId);
        familyService.leaveFamily(familyId, userDetails.uid());
        return BaseResponse.onSuccess(null);
    }

    @GetMapping("/families/{familyId}/photos/latest-upload-time")
    @Operation(
            summary = "가족에 가장 최근에 업로드된 사진의 업로드 시간 가져오기",
            description = "가족 방에 가장 최근에 업로드된 사진의 시간을 조회합니다. 가족 구성원만 접근 가능합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "가족을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "403", description = "가족 구성원이 아님", content = @Content)
    })
    public BaseResponse<LatestUploadTimeResponse> getLatestPhotoUploadTime(
            @Parameter(description = "조회할 가족 ID", required = true, example = "1")
            @PathVariable Long familyId,

            @Parameter(description = "현재 인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        log.info("Getting latest photo upload time for family ID: {}", familyId);
        return BaseResponse.onSuccess(familyService.getLatestPhotoUploadTime(familyId, userDetails.uid()));
    }

    @PostMapping("/families/{familyId}/invite-codes")
    @Operation(
            summary = "초대 코드 생성",
            description = "가족 방 초대 코드를 생성합니다. 가족 구성원만 생성 가능하며, 가족 구성원이 6명 이상이면 생성할 수 없습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "생성 성공"),
            @ApiResponse(responseCode = "404", description = "가족을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "403", description = "가족 구성원이 아님", content = @Content),
            @ApiResponse(responseCode = "400", description = "가족 구성원이 이미 6명으로 가득 참", content = @Content),
            @ApiResponse(responseCode = "500", description = "초대 코드 생성 실패", content = @Content)
    })
    public BaseResponse<InviteCodeResponse> createInviteCode(
            @Parameter(description = "초대 코드를 생성할 가족 ID", required = true, example = "1")
            @PathVariable Long familyId,

            @Parameter(description = "현재 인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        log.info("Creating invite code for family ID: {} by user {}", familyId, userDetails.uid());
        return BaseResponse.onSuccess(familyService.createInviteCode(familyId, userDetails.uid()));
    }

    @GetMapping("/invite-codes/{code}/family")
    @Operation(
            summary = "초대 코드로 가족 방 구성원들 이름 조회",
            description = "초대 코드를 통해 방 입장 전, 가족 방의 구성원을 확인하기 위해 이름들을 조회합니다. 코드 만료 시 예외를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "초대 코드를 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "400", description = "만료된 초대 코드", content = @Content)
    })
    public BaseResponse<FamilyUserNamesResponse> getFamilyUserNamesByInviteCode(
            @Parameter(description = "조회할 초대 코드", required = true, example = "ABCDEF")
            @PathVariable String code) {

        log.info("Getting family info by invite code: {}", code);
        return BaseResponse.onSuccess(familyService.getFamilyUserNamesByInviteCode(code));
    }

    @PostMapping("/invite-codes/{code}/join")
    @Operation(
            summary = "초대 코드로 가족 방 참가하기",
            description = "초대 코드를 통해 가족 방에 참가합니다. 이미 다른 가족에 속해 있거나, 코드가 만료되었거나, 가족 인원이 가득 찬 경우 참가할 수 없습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "참가 성공"),
            @ApiResponse(responseCode = "404", description = "초대 코드를 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "400", description = "이미 가족 구성원이거나, 만료된 초대 코드이거나, 가족 인원이 가득 참", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    public BaseResponse<FamilyResponse> joinFamilyByInviteCode(
            @Parameter(description = "참가할 초대 코드", required = true, example = "ABCDEF")
            @PathVariable String code,

            @Parameter(description = "현재 인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        log.info("User {} joining family with invite code: {}", userDetails.uid(), code);
        return BaseResponse.onSuccess(familyService.joinFamilyByInviteCode(code, userDetails.uid()));
    }
}
package com.pohyoja.picchargeserver.domain.family.controller;

import com.pohyoja.picchargeserver.common.BaseResponse;
import com.pohyoja.picchargeserver.common.security.JwtUserDetails;
import com.pohyoja.picchargeserver.domain.family.dto.response.FamilyResponse;
import com.pohyoja.picchargeserver.domain.family.dto.response.FamilyUserNamesResponse;
import com.pohyoja.picchargeserver.domain.family.dto.response.InviteCodeResponse;
import com.pohyoja.picchargeserver.domain.family.dto.response.LatestUploadTimeResponse;
import com.pohyoja.picchargeserver.domain.family.service.FamilyService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Family", description = "가족 API")
public class FamilyController {

    private final FamilyService familyService;

    @GetMapping("/families/{familyId}")
    @Operation(summary = "가족 정보 조회",
            description = "가족 ID로 가족 정보를 조회합니다. 가족 구성원만 가능합니다. 누적 업로드 수, 누적 반응 수 포함 (홈 화면에서 호출)")
    public BaseResponse<FamilyResponse> getFamily(
            @PathVariable Long familyId,
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        log.info("Getting family info for family ID: {}", familyId);
        return BaseResponse.onSuccess(familyService.getFamily(familyId, userDetails.uid()));
    }

    @GetMapping("/families/{familyId}/members")
    @Operation(summary = "가족 구성원들 이름 조회",
            description = "가족 ID로 가족 구성원들의 이름들을 조회합니다. 가족 구성원만 가능합니다.")
    public BaseResponse<FamilyUserNamesResponse> getFamilyUserNames(
            @PathVariable Long familyId,
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        log.info("Getting family members for family ID: {}", familyId);
        return BaseResponse.onSuccess(familyService.getFamilyUserNames(familyId, userDetails.uid()));
    }

    @PostMapping("/families")
    @Operation(summary = "가족 생성",
            description = "새로운 가족 방을 생성합니다. 이미 가족에 참여중이면 불가능합니다.")
    public BaseResponse<FamilyResponse> createFamily(
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        log.info("Creating new family by user: {}", userDetails.uid());
        return BaseResponse.onSuccess(familyService.createFamily(userDetails.uid()));
    }

    @DeleteMapping("/families/{familyId}")
    @Operation(summary = "가족 나가기 (본인)",
            description = "가족 방에서 나갑니다. 본인만 가능합니다. 구성원이 1명인 경우 가족이 삭제됩니다.")
    public BaseResponse<Void> leaveFamily(
            @PathVariable Long familyId,
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        log.info("User {} leaving family {}", userDetails.uid(), familyId);
        familyService.leaveFamily(familyId, userDetails.uid());
        return BaseResponse.onSuccess(null);
    }

    @GetMapping("/families/{familyId}/photos/latest-upload-time")
    @Operation(summary = "가족에 가장 최근에 업로드된 사진의 업로드 시간 가져오기",
            description = "가족 방에 가장 최근에 업로드된 사진의 시간을 조회합니다.")
    public BaseResponse<LatestUploadTimeResponse> getLatestPhotoUploadTime(
            @PathVariable Long familyId,
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        log.info("Getting latest photo upload time for family ID: {}", familyId);
        return BaseResponse.onSuccess(familyService.getLatestPhotoUploadTime(familyId, userDetails.uid()));
    }

    @PostMapping("/families/{familyId}/invite-codes")
    @Operation(summary = "초대 코드 생성",
            description = "가족 방 초대 코드를 생성합니다.")
    public BaseResponse<InviteCodeResponse> createInviteCode(
            @PathVariable Long familyId,
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        log.info("Creating invite code for family ID: {} by user {}", familyId, userDetails.uid());
        return BaseResponse.onSuccess(familyService.createInviteCode(familyId, userDetails.uid()));
    }

    @GetMapping("/invite-codes/{code}/family")
    @Operation(summary = "초대 코드로 가족 방 구성원들 이름 조회",
            description = "초대 코드를 통해 방 입장 전, 가족 방의 구성원을 확인하기 위해 이름들을 조회합니다. 코드 만료 시 예외를 반환합니다.")
    public BaseResponse<FamilyUserNamesResponse> getFamilyUserNamesByInviteCode(
            @PathVariable String code) {

        log.info("Getting family info by invite code: {}", code);
        return BaseResponse.onSuccess(familyService.getFamilyUserNamesByInviteCode(code));
    }

    @PostMapping("/invite-codes/{code}/join")
    @Operation(summary = "초대 코드로 가족 방 참가하기",
            description = "초대 코드를 통해 가족 방에 참가합니다. 코드 만료 시 예외를 반환합니다")
    public BaseResponse<FamilyResponse> joinFamilyByInviteCode(
            @PathVariable String code,
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        log.info("User {} joining family with invite code: {}", userDetails.uid(), code);
        return BaseResponse.onSuccess(familyService.joinFamilyByInviteCode(code, userDetails.uid()));
    }
}
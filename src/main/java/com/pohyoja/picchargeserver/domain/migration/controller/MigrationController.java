package com.pohyoja.picchargeserver.domain.migration.controller;

import com.pohyoja.picchargeserver.common.BaseResponse;
import com.pohyoja.picchargeserver.domain.family.dto.response.FamilyIdResponse;
import com.pohyoja.picchargeserver.domain.family.dto.response.FamilyResponse;
import com.pohyoja.picchargeserver.domain.family.service.FamilyService;
import com.pohyoja.picchargeserver.domain.member.dto.MemberDTO;
import com.pohyoja.picchargeserver.domain.migration.dto.request.JoinFamilyRequest;
import com.pohyoja.picchargeserver.domain.migration.dto.request.MemberCreateRequest;
import com.pohyoja.picchargeserver.domain.migration.dto.request.MemberIdRequest;
import com.pohyoja.picchargeserver.domain.migration.dto.request.PhotoMigrateRequest;
import com.pohyoja.picchargeserver.domain.migration.dto.response.FamilyCreateResponse;
import com.pohyoja.picchargeserver.domain.migration.service.MigrationService;
import com.pohyoja.picchargeserver.domain.photo.dto.PhotoDTO;
import com.pohyoja.picchargeserver.domain.photo.dto.request.PhotoAddRequest;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class MigrationController {

    private final FamilyService familyService;
    private final MigrationService migrationService;

    // 1. 유저 생성
    @PutMapping("/migrations/create-user")
    public BaseResponse<MemberDTO> createUser(
            @RequestBody MemberCreateRequest memberCreateRequest
    ) {
        return BaseResponse.onSuccess(migrationService.createUser(
                memberCreateRequest.name(),
                memberCreateRequest.memberId(),
                memberCreateRequest.email()
        ));
    }

    // 2. 유저가 참여하고 있는 가족이 있는지 확인
    @GetMapping("/migrations/current-family/{memberId}")
    public BaseResponse<FamilyIdResponse> getCurrentFamily(
            @PathVariable String memberId
    ) {
        return BaseResponse.onSuccess(migrationService.getCurrentFamily(memberId));
    }


    // 3. 가족 및 초대 코드 생성
    @PostMapping("/migrations/create-family")
    public BaseResponse<FamilyCreateResponse> createFamily(
            @RequestBody MemberIdRequest memberIdRequest
    ) {
        return BaseResponse.onSuccess(migrationService.createFamily(memberIdRequest.memberId()));
    }

    // 4. 가족 참여
    @PutMapping("/migrations/join-family")
    public BaseResponse<FamilyResponse> joinFamily(
            @RequestBody JoinFamilyRequest joinFamilyRequest
    ) {
        return BaseResponse.onSuccess(migrationService.joinFamily(
                joinFamilyRequest.code(),
                joinFamilyRequest.memberId()
        ));
    }

    // 5. 중복 사진 조회
    @GetMapping("/migrations/duplicate-photo/{familyId}/{uploadMemberId}/{targetTime}")
    public BaseResponse<Boolean> existsSamePhoto(
            @PathVariable Long familyId,
            @PathVariable String uploadMemberId,
            @PathVariable LocalDateTime targetTime
    ) {
        return BaseResponse.onSuccess(migrationService.existsSamePhoto(
                familyId,
                uploadMemberId,
                targetTime
        ));
    }

    // 6. 사진 업로드
    @PutMapping("/migrations/migrate-photo")
    public BaseResponse<PhotoDTO> migratePhoto(
            @RequestBody PhotoMigrateRequest photoMigrateRequest
    ) {
        return BaseResponse.onSuccess(migrationService.migratePhoto(
                photoMigrateRequest,
                new PhotoAddRequest(
                        photoMigrateRequest.id(),
                        photoMigrateRequest.url()
                )));
    }

    // 7. 사진 삭제
    @DeleteMapping("/migrations/delete-photo/{memberId}/{photoId}")
    public BaseResponse<Void> deletePhoto(
            @PathVariable String memberId,
            @PathVariable UUID photoId
    ) {
        migrationService.deletePhoto(memberId, photoId);
        return BaseResponse.onSuccess(null);
    }

    // 8. 유저 삭제
    @DeleteMapping("/migrations/delete-user/{memberId}")
    public BaseResponse<Void> deleteUser(
            @PathVariable String memberId
    ) {
        migrationService.deleteUser(memberId);
        return BaseResponse.onSuccess(null);
    }
}

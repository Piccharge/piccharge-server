package com.pohyoja.picchargeserver.domain.photo.controller;

import com.pohyoja.picchargeserver.common.BaseResponse;
import com.pohyoja.picchargeserver.common.security.JwtUserDetails;
import com.pohyoja.picchargeserver.domain.photo.dto.PhotoDTO;
import com.pohyoja.picchargeserver.domain.photo.dto.ReactionDTO;
import com.pohyoja.picchargeserver.domain.photo.dto.request.PhotoAddRequest;
import com.pohyoja.picchargeserver.domain.photo.dto.request.ReactionAddRequest;
import com.pohyoja.picchargeserver.domain.photo.dto.response.PhotosResponse;
import com.pohyoja.picchargeserver.domain.photo.service.PhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "03. 사진 API", description = "사진 관련 API")
public class PhotoController {

    private final PhotoService photoService;

    @GetMapping("/families/{familyId}/photos/latest")
    @Operation(
            summary = "가족의 최신 사진 조회",
            description = "유저가 소속된 가족 방의 id를 통해 가장 최근에 업로드된 사진 데이터를 가져옵니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "가족을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "403", description = "가족 구성원이 아님", content = @Content)
    })
    public BaseResponse<PhotoDTO> fetchLatestPhoto(
            @Parameter(description = "조회할 가족 ID", required = true, example = "1")
            @PathVariable Long familyId,

            @Parameter(description = "현재 인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        log.info("Fetching latest photo for family ID: {}", familyId);
        return BaseResponse.onSuccess(photoService.fetchLatestPhoto(familyId, userDetails.uid()));
    }

    @GetMapping("/families/{familyId}/photos")
    @Operation(
            summary = "가족의 사진 url 목록 조회",
            description = "유저가 소속된 가족 방의 id를 통해 사진의 url 데이터를 가져옵니다. (페이지네이션 지원)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "가족을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "403", description = "가족 구성원이 아님", content = @Content)
    })
    public BaseResponse<PhotosResponse> fetchPhotos(
            @Parameter(description = "조회할 가족 ID", required = true, example = "1")
            @PathVariable Long familyId,

            @Parameter(description = "현재 인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal JwtUserDetails userDetails,

            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "19") int size) {

        log.info("Fetching photos for family ID: {}", familyId);
        return BaseResponse.onSuccess(photoService.fetchPhotos(familyId, userDetails.uid(), page, size));
    }

    @GetMapping("/families/{familyId}/photos/{photoId}")
    @Operation(
            summary = "사진 상세 조회",
            description = "사진 id로 사진 하나의 데이터를 가져옵니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "가족 또는 사진을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "403", description = "가족 구성원이 아님", content = @Content)
    })
    public BaseResponse<PhotoDTO> getPhoto(
            @Parameter(description = "조회할 가족 ID", required = true, example = "1")
            @PathVariable Long familyId,

            @Parameter(description = "조회할 사진 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID photoId,

            @Parameter(description = "현재 인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        log.info("Fetching photo ID: {} from family ID: {}", photoId, familyId);
        return BaseResponse.onSuccess(photoService.getPhoto(familyId, photoId, userDetails.uid()));
    }

    @PostMapping("/families/{familyId}/photos")
    @Operation(
            summary = "사진 추가",
            description = "유저의 가족 방에 사진 정보를 저장합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "저장 성공"),
            @ApiResponse(responseCode = "404", description = "가족을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "403", description = "가족 구성원이 아님", content = @Content),
            @ApiResponse(responseCode = "400", description = "요청 데이터 오류", content = @Content)
    })
    public BaseResponse<PhotoDTO> addPhoto(
            @Parameter(description = "사진을 추가할 가족 ID", required = true, example = "1")
            @PathVariable Long familyId,

            @Parameter(description = "현재 인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal JwtUserDetails userDetails,

            @Parameter(description = "사진 추가 요청 정보", required = true)
            @RequestBody PhotoAddRequest request) {

        log.info("Adding photo to family ID: {} by user: {}", familyId, userDetails.uid());
        return BaseResponse.onSuccess(photoService.addPhoto(familyId, userDetails.uid(), request));
    }

    @DeleteMapping("/families/{familyId}/photos/{photoId}")
    @Operation(
            summary = "사진 삭제",
            description = "원격 스토리지에서 일치하는 ID의 사진을 삭제합니다. url에 저장된 원격 사진도 삭제됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "가족 또는 사진을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "403", description = "가족 구성원이 아니거나 사진 작성자가 아님", content = @Content)
    })
    public BaseResponse<Void> deletePhoto(
            @Parameter(description = "삭제할 가족 ID", required = true, example = "1")
            @PathVariable Long familyId,

            @Parameter(description = "삭제할 사진 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID photoId,

            @Parameter(description = "현재 인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        log.info("Deleting photo ID: {} from family ID: {}", photoId, familyId);
        photoService.deletePhoto(familyId, photoId, userDetails.uid());
        return BaseResponse.onSuccess(null);
    }

    @PostMapping("/families/{familyId}/photos/{photoId}/reactions")
    @Operation(
            summary = "사진에 반응 추가",
            description = "사진에 리액션(감정표현)을 추가합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추가 성공"),
            @ApiResponse(responseCode = "404", description = "가족 또는 사진을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "403", description = "가족 구성원이 아님", content = @Content),
            @ApiResponse(responseCode = "400", description = "요청 데이터 오류", content = @Content)
    })
    public BaseResponse<ReactionDTO> addReaction(
            @Parameter(description = "가족 ID", required = true, example = "1")
            @PathVariable Long familyId,

            @Parameter(description = "사진 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID photoId,

            @Parameter(description = "현재 인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal JwtUserDetails userDetails,

            @Parameter(description = "반응 추가 요청 정보", required = true)
            @RequestBody ReactionAddRequest request) {

        log.info("Adding reaction to photo ID: {} in family ID: {}", photoId, familyId);
        return BaseResponse.onSuccess(photoService.addReaction(familyId, photoId, userDetails.uid(), request));
    }
}
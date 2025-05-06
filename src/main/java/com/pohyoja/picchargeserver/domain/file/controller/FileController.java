package com.pohyoja.picchargeserver.domain.file.controller;

import com.pohyoja.picchargeserver.common.BaseResponse;
import com.pohyoja.picchargeserver.domain.file.dto.response.UrlResponse;
import com.pohyoja.picchargeserver.domain.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "04. File API", description = "S3로 파일를 업로드 하기 위해 사용되는 API 입니다.")
public class FileController {
    private final FileService fileService;

    @Operation(summary = "미리 서명된 URL 받기", description = "S3 버켓에 파일을 업로드하기 위한 미리 서명된 URL을 받습니다. 사진 데이터를 binary로 담아서, 반환된 url 값에 PUT 요청을 보내면 업로드 됩니다.")
    @GetMapping("/files/presigned-url/{extension}")
    public BaseResponse<UrlResponse> getPresignedUrl(
            @Parameter(description = "파일 확장자", example = "webp")
            @PathVariable(name = "extension") String extension,
            @Parameter(description = "원하는 파일 이름 (UUID 형식, 필수 아님)", example = "d421cc8d-d15a-4161-8eaa-86df5d5d699e")
            @RequestParam(name = "fileName", required = false) String fileName) {
        return BaseResponse.onSuccess(fileService.getPresignedPutUrl(extension, fileName));
    }
}

package com.pohyoja.picchargeserver.domain.photo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record PhotoAddRequest(
        @Schema(description = "사진 ID", example = "d421cc8d-d15a-4161-8eaa-86df5d5d699e")
        UUID id,
        @Schema(description = "사진 URL", example = "https://test.cloudfront.net/prod/d421cc8d-d15a-4161-8eaa-86df5d5d699e.webp")
        String url
) {}
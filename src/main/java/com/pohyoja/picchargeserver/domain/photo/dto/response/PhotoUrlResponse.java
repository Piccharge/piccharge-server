package com.pohyoja.picchargeserver.domain.photo.dto.response;

import com.pohyoja.picchargeserver.domain.photo.entity.Photo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

public record PhotoUrlResponse(
        @Schema(description = "사진 ID", example = "d421cc8d-d15a-4161-8eaa-86df5d5d699e")
        UUID id,
        @Schema(description = "사진 URL", example = "https://d36vcr5w3hfui8.cloudfront.net/prod/d421cc8d-d15a-4161-8eaa-86df5d5d699e.webp")
        String urlString,
        LocalDateTime uploadDate
) {
    public static PhotoUrlResponse of(Photo photo) {
        return new PhotoUrlResponse(photo.getId(), photo.getUrl(), photo.getCreatedAt());
    }
}

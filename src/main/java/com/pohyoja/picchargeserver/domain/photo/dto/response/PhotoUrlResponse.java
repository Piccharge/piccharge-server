package com.pohyoja.picchargeserver.domain.photo.dto.response;

import com.pohyoja.picchargeserver.domain.photo.entity.Photo;
import java.time.LocalDateTime;
import java.util.UUID;

public record PhotoUrlResponse(
        UUID id,
        String urlString,
        LocalDateTime uploadDate
) {
    public static PhotoUrlResponse of(Photo photo) {
        return new PhotoUrlResponse(photo.getId(), photo.getUrl(), photo.getCreatedAt());
    }
}

package com.pohyoja.picchargeserver.domain.photo.dto;

import com.pohyoja.picchargeserver.domain.family.entity.Family;
import com.pohyoja.picchargeserver.domain.member.entity.Member;
import com.pohyoja.picchargeserver.domain.photo.entity.Photo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PhotoDTO(
        @Schema(description = "사진 ID", example = "d421cc8d-d15a-4161-8eaa-86df5d5d699e")
        UUID id,
        String uploadBy,
        LocalDateTime uploadDate,
        @Schema(description = "사진 URL", example = "https://test.cloudfront.net/prod/d421cc8d-d15a-4161-8eaa-86df5d5d699e.webp")
        String urlString,
        ReactionDTO reactions,
        List<String> sharedWith,
        String uploadUserId,
        Long familyId
) {
    public static PhotoDTO of(Photo photo) {
        Member uploadMember = photo.getUploadMember();
        Family family = photo.getFamily();
        return new PhotoDTO(
                photo.getId(),
                uploadMember.getName(),
                photo.getCreatedAt(),
                photo.getUrl(),
                ReactionDTO.of(photo.getReaction()),
                family.getMembers().stream().map(Member::getName).toList(),
                photo.getUploadMember().getUid(),
                family.getId()
        );
    }
}

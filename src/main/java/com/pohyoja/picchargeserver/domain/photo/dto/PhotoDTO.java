package com.pohyoja.picchargeserver.domain.photo.dto;

import com.pohyoja.picchargeserver.domain.family.entity.Family;
import com.pohyoja.picchargeserver.domain.member.entity.Member;
import com.pohyoja.picchargeserver.domain.photo.entity.Photo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PhotoDTO(
        UUID id,
        String uploadBy,
        LocalDateTime uploadDate,
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

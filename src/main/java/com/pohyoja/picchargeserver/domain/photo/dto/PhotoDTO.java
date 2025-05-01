package com.pohyoja.picchargeserver.domain.photo.dto;

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
        String uploadUser,
        Long familyId
) {}

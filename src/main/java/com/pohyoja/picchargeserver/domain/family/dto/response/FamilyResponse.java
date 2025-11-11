package com.pohyoja.picchargeserver.domain.family.dto.response;

import com.pohyoja.picchargeserver.domain.photo.dto.ReactionDTO;
import java.time.LocalDateTime;

public record FamilyResponse(
        Long id,
        LocalDateTime latestUploadTime,
        int totalPhotoCount,
        ReactionDTO reactionsCount
) {}

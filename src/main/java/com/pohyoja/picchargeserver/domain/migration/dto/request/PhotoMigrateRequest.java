package com.pohyoja.picchargeserver.domain.migration.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

public record PhotoMigrateRequest(
        Long familyId,
        String memberId,
        UUID id,
        String url,
        LocalDateTime uploadDate,
        int fire,
        int love,
        int like,
        int star
) {
}

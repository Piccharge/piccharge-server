package com.pohyoja.picchargeserver.domain.family.dto.response;

import java.time.LocalDateTime;

public record LatestUploadTimeResponse(
        LocalDateTime latestUploadTime
) {}


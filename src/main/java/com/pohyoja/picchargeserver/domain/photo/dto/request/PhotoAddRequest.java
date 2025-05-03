package com.pohyoja.picchargeserver.domain.photo.dto.request;

import java.util.UUID;

public record PhotoAddRequest(
        UUID id,
        String url
) {}
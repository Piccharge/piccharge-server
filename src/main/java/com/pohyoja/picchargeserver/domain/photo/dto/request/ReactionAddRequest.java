package com.pohyoja.picchargeserver.domain.photo.dto.request;

public record ReactionAddRequest(
        String type,
        int count
) {}
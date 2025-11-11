package com.pohyoja.picchargeserver.domain.migration.dto.request;

public record MemberCreateRequest(
        String name,
        String memberId,
        String email
) {
}

package com.pohyoja.picchargeserver.domain.migration.dto.request;

public record JoinFamilyRequest(
        String memberId,
        Long familyId,
        String code
) {
}

package com.pohyoja.picchargeserver.domain.member.dto;

import com.pohyoja.picchargeserver.domain.member.entity.Role;
import java.util.List;

public record MemberDTO(
        String id,
        String name,
        Role role,
        String email,
        List<String> connectedTo,
        int uploadCycle,
        Long familyId
) {}

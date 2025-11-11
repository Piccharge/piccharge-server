package com.pohyoja.picchargeserver.domain.family.repository;

import com.pohyoja.picchargeserver.domain.family.entity.InviteCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InviteCodeRepository extends JpaRepository<InviteCode, Long> {
    Optional<InviteCode> findByCode(String code);
}
package com.pohyoja.picchargeserver.domain.family.repository;

import com.pohyoja.picchargeserver.domain.family.entity.Family;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyRepository extends JpaRepository<Family, Long> {
}

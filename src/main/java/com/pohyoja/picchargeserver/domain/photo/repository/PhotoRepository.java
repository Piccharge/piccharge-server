package com.pohyoja.picchargeserver.domain.photo.repository;

import com.pohyoja.picchargeserver.domain.family.entity.Family;
import com.pohyoja.picchargeserver.domain.photo.entity.Photo;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoRepository extends JpaRepository<Photo, UUID> {
    int countByFamilyId(Long familyId);

    Optional<Photo> findTopByFamilyOrderByCreatedAtDesc(Family family);

    Page<Photo> findByFamilyId(Long familyId, Pageable pageable);
}

package com.pohyoja.picchargeserver.domain.photo.repository;

import com.pohyoja.picchargeserver.domain.family.entity.Family;
import com.pohyoja.picchargeserver.domain.photo.entity.Photo;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PhotoRepository extends JpaRepository<Photo, UUID> {
    int countByFamilyId(Long familyId);

    Optional<Photo> findTopByFamilyOrderByCreatedAtDesc(Family family);

    Page<Photo> findByFamilyId(Long familyId, Pageable pageable);

    @Modifying
    @Query(value = "UPDATE photo SET created_at = :createdAt, updated_at = :updatedAt WHERE photo_id = :photoId", nativeQuery = true)
    void updatePhotoCreatedAndUpdatedAt(@Param("photoId") UUID photoId,
                                        @Param("createdAt") LocalDateTime createdAt,
                                        @Param("updatedAt") LocalDateTime updatedAt);
}

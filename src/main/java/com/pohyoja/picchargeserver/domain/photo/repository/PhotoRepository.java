package com.pohyoja.picchargeserver.domain.photo.repository;

import com.pohyoja.picchargeserver.domain.photo.entity.Photo;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoRepository extends JpaRepository<Photo, UUID> {
}

package com.pohyoja.picchargeserver.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.OffsetDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    private static final String TIMESTAMP_0 = "TIMESTAMP(0)";

    @CreatedDate
    @Column(nullable = false, updatable = false, columnDefinition = TIMESTAMP_0)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false, columnDefinition = TIMESTAMP_0)
    private OffsetDateTime updatedAt;
}
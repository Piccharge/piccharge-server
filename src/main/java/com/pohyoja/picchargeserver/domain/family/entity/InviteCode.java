package com.pohyoja.picchargeserver.domain.family.entity;

import static com.pohyoja.picchargeserver.domain.family.util.InviteCodeGenerator.generateRandomCode;
import static jakarta.persistence.FetchType.LAZY;

import com.pohyoja.picchargeserver.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "invite_code")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InviteCode extends BaseEntity {
    @Id @GeneratedValue
    @Column(name = "invite_code_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "family_id")
    private Family family;

    private final LocalDateTime expiresAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusHours(1);

    public InviteCode(Family family) {
        this.family = family;
        this.code = generateRandomCode();
    }
}

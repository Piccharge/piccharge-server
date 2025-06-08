package com.pohyoja.picchargeserver.domain.member.entity;

import static jakarta.persistence.FetchType.LAZY;

import com.pohyoja.picchargeserver.common.BaseEntity;
import com.pohyoja.picchargeserver.domain.family.entity.Family;
import com.pohyoja.picchargeserver.domain.photo.entity.Photo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {
    @Id
    @Column(name = "member_id", length = 28)
    @Size(min = 28, max = 28)
    private String uid;

    @NotBlank
    @Column(unique = true, nullable = false) // 마이그레이션 이전 로직 유지
    private String name;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private int uploadCycle = 3;

    @NotBlank
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "family_id")
    private Family family;

    @OneToMany(mappedBy = "uploadMember")
    private List<Photo> photos = new ArrayList<>();

    @Builder
    public Member(String uid, String name, String email, Role role) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public void setFamily(Family family) {
        if (this.family != null && this.family != family) {
            this.family.getMembers().remove(this);
        }
        this.family = family;
        if (family != null && !family.getMembers().contains(this)) {
            family.getMembers().add(this);
        }
    }
}

package com.pohyoja.picchargeserver.domain.member.entity;

import static jakarta.persistence.FetchType.LAZY;

import com.pohyoja.picchargeserver.common.BaseEntity;
import com.pohyoja.picchargeserver.domain.family.entity.Family;
import com.pohyoja.picchargeserver.domain.photo.entity.Photo;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
    @Id @Column(name = "member_id")
    private String uid;

    private String name;

    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "family_id")
    private Family family;

    @OneToMany(mappedBy = "uploadMember", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Photo> photos = new ArrayList<>();

    @Builder
    public Member(String uid, String name, String email, Role role, Family family) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.role = role;
        this.family = family;
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

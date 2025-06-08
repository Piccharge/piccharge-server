package com.pohyoja.picchargeserver.domain.photo.entity;

import static jakarta.persistence.FetchType.LAZY;

import com.pohyoja.picchargeserver.common.BaseEntity;
import com.pohyoja.picchargeserver.domain.family.entity.Family;
import com.pohyoja.picchargeserver.domain.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "photo", indexes = {
        @Index(name = "idx_photo_family_created_at", columnList = "family_id, created_at DESC")
})
public class Photo extends BaseEntity {
    @Id @Column(name = "photo_id")
    private UUID id;

    @Column(nullable = false)
    private String url;

    @Embedded
    private Reaction reaction = new Reaction();

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "family_id")
    private Family family;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "upload_member_id")
    private Member uploadMember;

    public Photo(UUID id, String url, Reaction reaction) {
        this.id = id;
        this.url = url;
        this.reaction = reaction;
    }

    // 연관관계 편의 메서드
    public void setFamily(Family family) {
        if (this.family != null && this.family != family) {
            this.family.getPhotos().remove(this);
        }

        this.family = family;

        if (family != null && !family.getPhotos().contains(this)) {
            family.getPhotos().add(this);
        }
    }

    public void setUploadMember(Member uploadMember) {
        if (this.uploadMember != null && this.uploadMember != uploadMember) {
            this.uploadMember.getPhotos().remove(this);
        }

        this.uploadMember = uploadMember;

        if (uploadMember != null && !uploadMember.getPhotos().contains(this)) {
            uploadMember.getPhotos().add(this);
        }
    }

    public void clearAssociations() {
        setFamily(null);
        setUploadMember(null);
    }
}

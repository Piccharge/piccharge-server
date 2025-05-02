package com.pohyoja.picchargeserver.domain.family.entity;

import com.pohyoja.picchargeserver.common.BaseEntity;
import com.pohyoja.picchargeserver.domain.member.entity.Member;
import com.pohyoja.picchargeserver.domain.photo.entity.Photo;
import com.pohyoja.picchargeserver.domain.photo.entity.Reaction;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "family")
@Getter
@NoArgsConstructor
public class Family extends BaseEntity {
    @Id @GeneratedValue
    @Column(name = "family_id")
    private Long id;

    @Column(nullable = false)
    private LocalDateTime lastPhotoAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

    @Embedded
    private Reaction reaction = new Reaction();

    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL)
    private List<Member> members = new ArrayList<>();

    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Photo> photos = new ArrayList<>();

    // 연관관계 메소드 호출
    public void addMember(Member member) {
        member.setFamily(this);
    }

    public void addPhoto(Photo photo) {
        photo.setFamily(this);

        this.lastPhotoAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }
}

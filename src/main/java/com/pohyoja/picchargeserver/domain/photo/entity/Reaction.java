package com.pohyoja.picchargeserver.domain.photo.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reaction {
    private int loveCount = 0;
    private int fireCount = 0;
    private int starCount = 0;
    private int likeCount = 0;

    public void incrementLove(int count) {
        this.loveCount += count;
    }

    public void incrementFire(int count) {
        this.fireCount += count;
    }

    public void incrementStar(int count) {
        this.starCount += count;
    }

    public void incrementLike(int count) {
        this.likeCount += count;
    }
}

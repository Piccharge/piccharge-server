package com.pohyoja.picchargeserver.domain.photo.dto;

import com.pohyoja.picchargeserver.domain.photo.entity.Reaction;

public record ReactionDTO(
        int love,
        int fire,
        int star,
        int like
) {
    public static ReactionDTO of(Reaction reaction) {
        if (reaction == null) {
            return new ReactionDTO(0, 0, 0, 0);
        }

        return new ReactionDTO(
                reaction.getLoveCount(),
                reaction.getFireCount(),
                reaction.getStarCount(),
                reaction.getLikeCount()
        );
    }
}
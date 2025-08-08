package com.boardly.features.card.domain.valueobject;

import java.util.Objects;

import com.boardly.features.user.domain.model.UserId;

public class CardMember {

    private final UserId userId;

    public CardMember(UserId userId) {
        this.userId = userId;
    }

    public UserId getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        CardMember cardMember = (CardMember) obj;
        return Objects.equals(userId, cardMember.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return String.format("CardMember{userId=%s}", userId);
    }
}

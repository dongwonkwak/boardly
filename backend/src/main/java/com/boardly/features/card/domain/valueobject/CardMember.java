package com.boardly.features.card.domain.valueobject;

import java.time.Instant;
import java.util.Objects;

import com.boardly.features.user.domain.model.UserId;

public class CardMember {

    private final UserId userId;
    private final Instant assignedAt;

    public CardMember(UserId userId) {
        this.userId = userId;
        this.assignedAt = Instant.now();
    }

    public CardMember(UserId userId, Instant assignedAt) {
        this.userId = userId;
        this.assignedAt = assignedAt;
    }

    public UserId getUserId() {
        return userId;
    }

    public Instant getAssignedAt() {
        return assignedAt;
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
        return String.format("CardMember{userId=%s, assignedAt=%s}", userId, assignedAt);
    }
}

package com.boardly.domain.card;

import com.boardly.domain.user.UserId;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 카드 담당자 도메인 엔티티
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardAssignee {

    private CardAssigneeId id;
    private CardId cardId;
    private UserId userId;
    private Instant assignedAt;

    @Builder
    public CardAssignee(CardAssigneeId id, CardId cardId, UserId userId, Instant assignedAt) {
        this.id = id;
        this.cardId = cardId;
        this.userId = userId;
        this.assignedAt = assignedAt;
    }

    /**
     * 카드 담당자 할당 팩토리 메서드
     */
    public static CardAssignee assign(CardId cardId, UserId userId) {
        return CardAssignee.builder()
                .id(CardAssigneeId.generate())
                .cardId(cardId)
                .userId(userId)
                .assignedAt(Instant.now())
                .build();
    }
}

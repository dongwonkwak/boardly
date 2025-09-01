package com.boardly.domain.card;

import com.boardly.domain.user.UserId;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 카드 담당자 도메인 엔티티
 * 합성키 (cardId + userId)를 사용하여 식별
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = { "cardId", "userId" })
public class CardAssignee {

    private CardId cardId;
    private UserId userId;
    private Instant assignedAt;

    @Builder
    public CardAssignee(CardId cardId, UserId userId, Instant assignedAt) {
        this.cardId = cardId;
        this.userId = userId;
        this.assignedAt = assignedAt;
    }

    /**
     * 카드 담당자 할당 팩토리 메서드
     */
    public static CardAssignee assign(CardId cardId, UserId userId) {
        return CardAssignee.builder()
                .cardId(cardId)
                .userId(userId)
                .assignedAt(Instant.now())
                .build();
    }
}

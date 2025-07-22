package com.boardly.features.card.presentation.response;

import com.boardly.features.card.domain.model.Card;
import java.time.Instant;

/**
 * 카드 응답 DTO
 */
public record CardResponse(
        String cardId,
        String title,
        String description,
        int position,
        String listId,
        Instant createdAt,
        Instant updatedAt) {

    /**
     * Card 도메인 객체로부터 Response 생성
     */
    public static CardResponse from(Card card) {
        return new CardResponse(
                card.getCardId().getId(),
                card.getTitle(),
                card.getDescription(),
                card.getPosition(),
                card.getListId().getId(),
                card.getCreatedAt(),
                card.getUpdatedAt());
    }
}
package com.boardly.features.card.presentation.response;

import java.time.Instant;

import com.boardly.features.card.domain.model.Card;

/**
 * 카드 응답 DTO
 */
public record CardResponse(
        String cardId,
        String title,
        String description,
        int position,
        String listId,
        String priority,
        String createdBy,
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
                card.getPriority() != null ? card.getPriority().getValue() : null,
                card.getCreatedBy().getId(),
                card.getCreatedAt(),
                card.getUpdatedAt());
    }
}
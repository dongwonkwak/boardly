package com.boardly.features.card.presentation.request;

/**
 * 카드 이동 요청 DTO
 */
public record MoveCardRequest(
        String targetListId,
        Integer newPosition) {
}
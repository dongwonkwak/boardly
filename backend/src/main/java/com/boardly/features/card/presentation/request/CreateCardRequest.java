package com.boardly.features.card.presentation.request;

/**
 * 카드 생성 요청 DTO
 */
public record CreateCardRequest(
        String title,
        String description,
        String listId) {
}
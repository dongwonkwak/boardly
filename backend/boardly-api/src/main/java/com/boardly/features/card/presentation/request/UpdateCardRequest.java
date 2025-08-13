package com.boardly.features.card.presentation.request;

/**
 * 카드 수정 요청 DTO
 */
public record UpdateCardRequest(
        String title,
        String description) {
}
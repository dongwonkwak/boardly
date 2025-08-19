package com.boardly.api.adapters.in.rest.card.request;

/**
 * 카드 수정 요청 DTO
 */
public record UpdateCardRequest(
        String title,
        String description) {
}
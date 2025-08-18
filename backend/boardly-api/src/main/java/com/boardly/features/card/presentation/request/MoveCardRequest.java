package com.boardly.api.adapters.in.rest.card.request;

/**
 * 카드 이동 요청 DTO
 */
public record MoveCardRequest(
        String targetListId,
        Integer newPosition) {
}
package com.boardly.api.adapters.in.rest.card.request;

/**
 * 카드 생성 요청 DTO
 */
public record CreateCardRequest(
        String title,
        String description,
        String listId,
        String workspaceId) {
}
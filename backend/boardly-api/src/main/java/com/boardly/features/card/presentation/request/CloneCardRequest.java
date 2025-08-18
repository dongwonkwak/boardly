package com.boardly.api.adapters.in.rest.card.request;

/**
 * 카드 복제 요청 DTO
 */
public record CloneCardRequest(
        String newTitle,
        String targetListId) {
}
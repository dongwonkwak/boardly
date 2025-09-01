package com.boardly.features.card.presentation.request;

/**
 * 카드 복제 요청 DTO
 */
public record CloneCardRequest(
        String newTitle,
        String targetListId) {
}
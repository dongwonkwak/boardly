package com.boardly.features.card.presentation.request;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 카드 시작일 업데이트 요청 DTO
 * 
 * @param startDate 시작일 (null인 경우 시작일 제거)
 * 
 * @since 1.0.0
 */
@Schema(description = "카드 시작일 업데이트 요청")
public record UpdateCardStartDateRequest(
        @Schema(description = "시작일", example = "2024-01-01T00:00:00Z") @JsonFormat(shape = JsonFormat.Shape.STRING) Instant startDate) {
}
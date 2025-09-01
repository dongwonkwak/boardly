package com.boardly.features.card.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 카드 완료 상태 업데이트 요청 DTO
 * 
 * @param isCompleted 완료 상태
 * 
 * @since 1.0.0
 */
@Schema(description = "카드 완료 상태 업데이트 요청")
public record UpdateCardCompletedRequest(
        @Schema(description = "완료 상태", example = "false") @NotNull(message = "완료 상태는 필수입니다") Boolean isCompleted) {
}
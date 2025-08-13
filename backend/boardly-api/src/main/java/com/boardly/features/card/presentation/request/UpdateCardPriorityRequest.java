package com.boardly.features.card.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 카드 우선순위 업데이트 요청 DTO
 * 
 * @param priority 우선순위 (low, medium, high, urgent)
 * 
 * @since 1.0.0
 */
@Schema(description = "카드 우선순위 업데이트 요청")
public record UpdateCardPriorityRequest(
        @Schema(description = "우선순위", example = "medium", allowableValues = {
                "low", "medium", "high",
                "urgent" }) @NotBlank(message = "우선순위는 필수입니다") @Pattern(regexp = "^(low|medium|high|urgent)$", message = "유효하지 않은 우선순위입니다") String priority){
}
package com.boardly.features.card.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record RemoveCardLabelRequest(
        @NotBlank(message = "라벨 ID는 필수입니다") String labelId) {
}
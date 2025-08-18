package com.boardly.api.adapters.in.rest.card.request;

import jakarta.validation.constraints.NotBlank;

public record AddCardLabelRequest(
        @NotBlank(message = "라벨 ID는 필수입니다") String labelId) {
}
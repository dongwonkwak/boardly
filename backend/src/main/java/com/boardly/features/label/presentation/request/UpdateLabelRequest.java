package com.boardly.features.label.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "라벨 수정 요청")
public record UpdateLabelRequest(
        @Schema(description = "라벨 이름", example = "긴급") @NotBlank(message = "라벨 이름은 필수입니다") @Size(min = 1, max = 50, message = "라벨 이름은 1자 이상 50자 이하여야 합니다") String name,

        @Schema(description = "라벨 색상 (HEX 코드)", example = "#FF0000") @NotBlank(message = "라벨 색상은 필수입니다") @Size(min = 4, max = 7, message = "라벨 색상은 4자 이상 7자 이하여야 합니다") String color) {
}
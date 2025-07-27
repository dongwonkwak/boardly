package com.boardly.features.label.presentation.response;

import java.time.Instant;

import com.boardly.features.label.domain.model.Label;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "라벨 응답")
public record LabelResponse(
        @Schema(description = "라벨 ID", example = "label-123") String labelId,

        @Schema(description = "보드 ID", example = "board-123") String boardId,

        @Schema(description = "라벨 이름", example = "긴급") String name,

        @Schema(description = "라벨 색상", example = "#FF0000") String color,

        @Schema(description = "생성 시간") Instant createdAt,

        @Schema(description = "수정 시간") Instant updatedAt) {

    public static LabelResponse from(Label label) {
        return new LabelResponse(
                label.getLabelId().getId(),
                label.getBoardId().getId(),
                label.getName(),
                label.getColor(),
                label.getCreatedAt(),
                label.getUpdatedAt());
    }
}
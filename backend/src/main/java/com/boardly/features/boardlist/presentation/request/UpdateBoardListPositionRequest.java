package com.boardly.features.boardlist.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 보드 리스트 위치 변경 요청
 * 
 * @param position 새로운 위치 (0부터 시작)
 * 
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
@Schema(description = "보드 리스트 위치 변경 요청")
public class UpdateBoardListPositionRequest {

    @Min(value = 0, message = "위치는 0 이상이어야 합니다")
    @Schema(description = "새로운 위치 (0부터 시작)", example = "2", required = true)
    private Integer position;

    public UpdateBoardListPositionRequest(Integer position) {
        this.position = position;
    }
} 
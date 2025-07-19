package com.boardly.features.boardlist.presentation.request;

import com.boardly.features.boardlist.domain.model.ListColor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 보드 리스트 생성 요청
 * 
 * @param title 리스트 제목
 * @param description 리스트 설명
 * @param color 리스트 색상 (선택사항)
 * 
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
@Schema(description = "보드 리스트 생성 요청")
public class CreateBoardListRequest {

    @NotBlank(message = "리스트 제목은 필수입니다")
    @Size(min = 1, max = 100, message = "리스트 제목은 1자 이상 100자 이하여야 합니다")
    @Schema(description = "리스트 제목", example = "할 일", required = true)
    private String title;

    @Size(max = 500, message = "리스트 설명은 500자 이하여야 합니다")
    @Schema(description = "리스트 설명", example = "해야 할 일들을 관리하는 리스트입니다")
    private String description;

    @Schema(description = "리스트 색상", example = "#0079BF")
    private String color;

    public CreateBoardListRequest(String title, String description, String color) {
        this.title = title;
        this.description = description;
        this.color = color;
    }

    public ListColor getListColor() {
        return color != null ? ListColor.of(color) : ListColor.of("#0079BF");
    }
} 
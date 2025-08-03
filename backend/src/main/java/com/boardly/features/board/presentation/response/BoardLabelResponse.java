package com.boardly.features.board.presentation.response;

import com.boardly.features.label.domain.model.Label;

/**
 * 보드 라벨 응답 DTO
 * 
 * @param id          라벨 ID
 * @param name        라벨 이름
 * @param color       라벨 색상
 * @param description 라벨 설명
 * 
 * @since 1.0.0
 */
public record BoardLabelResponse(
        String id,
        String name,
        String color,
        String description) {

    /**
     * Label 도메인 모델을 BoardLabelResponse로 변환합니다.
     * 
     * @param label 변환할 Label 도메인 모델
     * @return BoardLabelResponse 객체
     */
    public static BoardLabelResponse from(Label label) {
        return new BoardLabelResponse(
                label.getLabelId().getId(),
                label.getName(),
                label.getColor(),
                "" // Label 도메인에 description 필드가 없으므로 빈 문자열 사용
        );
    }
}
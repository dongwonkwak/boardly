package com.boardly.features.board.presentation.response;

/**
 * 카드 라벨 응답 DTO
 * 
 * @param id    라벨 ID
 * @param name  라벨 이름
 * @param color 라벨 색상
 * 
 * @since 1.0.0
 */
public record CardLabelResponse(
        String id,
        String name,
        String color) {

    /**
     * 라벨 정보로 CardLabelResponse를 생성합니다.
     * 
     * @param id    라벨 ID
     * @param name  라벨 이름
     * @param color 라벨 색상
     * @return CardLabelResponse 객체
     */
    public static CardLabelResponse of(String id, String name, String color) {
        return new CardLabelResponse(id, name, color);
    }
}
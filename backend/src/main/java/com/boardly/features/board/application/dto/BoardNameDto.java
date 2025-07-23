package com.boardly.features.board.application.dto;

/**
 * 보드 이름 정보 DTO
 */
public record BoardNameDto(String title) {

    /**
     * 기본 보드 이름 DTO 생성
     */
    public static BoardNameDto defaultBoard() {
        return new BoardNameDto("알 수 없는 보드");
    }
}
package com.boardly.features.board.presentation.request;

/**
 * 보드 생성 요청 DTO
 */
public record CreateBoardRequest(
        String title,
        String description
) {} 
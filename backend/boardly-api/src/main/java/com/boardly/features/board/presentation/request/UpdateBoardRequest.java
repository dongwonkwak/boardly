package com.boardly.features.board.presentation.request;

/**
 * 보드 업데이트 요청 DTO
 */
public record UpdateBoardRequest(
        String title,
        String description
) {} 
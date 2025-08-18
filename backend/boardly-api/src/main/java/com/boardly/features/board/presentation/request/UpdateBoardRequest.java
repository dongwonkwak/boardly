package com.boardly.api.adapters.in.rest.board.request;

/**
 * 보드 업데이트 요청 DTO
 */
public record UpdateBoardRequest(
        String title,
        String description
) {} 
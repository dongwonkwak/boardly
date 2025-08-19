package com.boardly.api.adapters.in.rest.board.request;

/**
 * 보드 생성 요청 DTO
 */
public record CreateBoardRequest(
        String title,
        String description
) {} 
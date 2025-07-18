package com.boardly.features.board.presentation.response;

import com.boardly.features.board.domain.model.Board;
import java.time.Instant;

/**
 * 보드 응답 DTO
 */
public record BoardResponse(
        String boardId,
        String title,
        String description,
        boolean isArchived,
        String ownerId,
        boolean isStarred,
        Instant createdAt,
        Instant updatedAt
) {

    /**
     * Board 도메인 객체로부터 Response 생성
     */
    public static BoardResponse from(Board board) {
        return new BoardResponse(
                board.getBoardId().getId(),
                board.getTitle(),
                board.getDescription(),
                board.isArchived(),
                board.getOwnerId().getId(),
                board.isStarred(),
                board.getCreatedAt(),
                board.getUpdatedAt()
        );
    }
} 
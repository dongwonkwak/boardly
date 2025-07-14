package com.boardly.features.board.presentation.response;

import com.boardly.features.board.domain.model.Board;
import java.time.LocalDateTime;

/**
 * 보드 응답 DTO
 */
public record BoardResponse(
        String boardId,
        String title,
        String description,
        boolean isArchived,
        String ownerId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BoardResponse from(Board board) {
        return new BoardResponse(
                board.getBoardId().getId(),
                board.getTitle(),
                board.getDescription(),
                board.isArchived(),
                board.getOwnerId().getId(),
                board.getCreatedAt(),
                board.getUpdatedAt()
        );
    }
} 
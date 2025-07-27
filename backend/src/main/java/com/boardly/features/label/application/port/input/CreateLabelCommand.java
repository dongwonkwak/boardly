package com.boardly.features.label.application.port.input;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.user.domain.model.UserId;

public record CreateLabelCommand(
        BoardId boardId,
        UserId requesterId,
        String name,
        String color) {

    public static CreateLabelCommand of(BoardId boardId, UserId requesterId, String name, String color) {
        return new CreateLabelCommand(
                boardId,
                requesterId,
                name != null ? name.trim() : "",
                color != null ? color.trim() : "");
    }
}

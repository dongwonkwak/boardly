package com.boardly.features.label.application.port.input;

import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.UserId;

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

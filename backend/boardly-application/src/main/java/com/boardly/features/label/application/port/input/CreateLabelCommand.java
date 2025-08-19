package com.boardly.features.label.application.port.input;

import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.common.value.WorkspaceId;

public record CreateLabelCommand(
        BoardId boardId,
        WorkspaceId workspaceId,
        UserId requesterId,
        String name,
        String color) {

    public static CreateLabelCommand of(BoardId boardId, WorkspaceId workspaceId, UserId requesterId, String name, String color) {
        return new CreateLabelCommand(
                boardId,
                workspaceId,
                requesterId,
                name != null ? name.trim() : "",
                color != null ? color.trim() : "");
    }
}

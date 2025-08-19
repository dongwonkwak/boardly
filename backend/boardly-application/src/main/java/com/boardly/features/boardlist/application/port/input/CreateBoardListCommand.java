package com.boardly.features.boardlist.application.command;

import static org.apache.commons.lang3.StringUtils.trim;

import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.ListColor;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.common.value.WorkspaceId;

public record CreateBoardListCommand(
    String title,
    String description,
    ListColor color,
    BoardId boardId,
    WorkspaceId workspaceId,
    UserId userId) {
  public static CreateBoardListCommand of(
      String title,
      String description,
      ListColor color,
      BoardId boardId,
      WorkspaceId workspaceId,
      UserId userId) {
    return new CreateBoardListCommand(
        trim(title),
        description != null ? description.trim() : null,
        color,
        boardId,
        workspaceId,
        userId);
  }
}

package com.boardly.features.board.application.command;

import static org.apache.commons.lang3.StringUtils.trim;

import com.boardly.shared.common.value.UserId;
import com.boardly.shared.common.value.WorkspaceId;

public record CreateBoardCommand(
    String title,
    String description,
    WorkspaceId workspaceId,
    UserId ownerId) {
  // canonical constructor만 허용되므로, 입력값을 정제하는 정적 팩토리 메서드를 사용합니다.
  public static CreateBoardCommand of(String title, String description, WorkspaceId workspaceId, UserId ownerId) {
    return new CreateBoardCommand(
        trim(title),
        description != null ? description.trim() : null,
        workspaceId,
        ownerId);
  }
}

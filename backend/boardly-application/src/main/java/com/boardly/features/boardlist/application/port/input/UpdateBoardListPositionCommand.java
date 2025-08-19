package com.boardly.features.boardlist.application.command;

import com.boardly.shared.common.value.ListId;
import com.boardly.shared.common.value.UserId;

/**
 * 보드 리스트 위치 변경 커맨드
 */
public record UpdateBoardListPositionCommand(
  ListId listId,
  UserId userId,
  int newPosition
) {
  public boolean isValidPosition() { return newPosition >= 0; }
}

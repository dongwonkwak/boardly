package com.boardly.features.boardlist.application.command;

import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.ListColor;
import com.boardly.shared.common.value.UserId;

import static org.apache.commons.lang3.StringUtils.trim;

/**
 * 보드 리스트 생성 커맨드
 */
public record CreateBoardListCommand(
  BoardId boardId,
  UserId userId,
  String title,
  String description,
  ListColor color
) {
  public CreateBoardListCommand(BoardId boardId, UserId userId, String title, String description) {
    this(boardId, userId, trim(title), description, ListColor.defaultColor());
  }
  public CreateBoardListCommand(BoardId boardId, UserId userId, String title) {
    this(boardId, userId, trim(title), null, ListColor.defaultColor());
  }
}

package com.boardly.features.boardlist.application.command;

import com.boardly.shared.common.value.ListColor;
import com.boardly.shared.common.value.ListId;
import com.boardly.shared.common.value.UserId;

import static org.apache.commons.lang3.StringUtils.trim;

/**
 * 보드 리스트 수정 커맨드
 */
public record UpdateBoardListCommand(
  ListId listId,
  UserId userId,
  String title,
  String description,
  ListColor color
) {
  public UpdateBoardListCommand(ListId listId, UserId userId, String title, String description) {
    this(listId, userId, trim(title), description, null);
  }
}

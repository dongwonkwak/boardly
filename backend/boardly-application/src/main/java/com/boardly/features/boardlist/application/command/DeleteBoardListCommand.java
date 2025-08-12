package com.boardly.features.boardlist.application.command;

import com.boardly.shared.common.value.ListId;
import com.boardly.shared.common.value.UserId;

/**
 * 보드 리스트 삭제 커맨드
 */
public record DeleteBoardListCommand(
  ListId listId,
  UserId userId
) {}

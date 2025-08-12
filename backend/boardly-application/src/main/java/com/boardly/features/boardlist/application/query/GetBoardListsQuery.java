package com.boardly.features.boardlist.application.query;

import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.UserId;

/**
 * 단일 보드 리스트 조회 커맨드
 */
public record GetBoardListsQuery(
  BoardId boardId,
  UserId userId
) {}

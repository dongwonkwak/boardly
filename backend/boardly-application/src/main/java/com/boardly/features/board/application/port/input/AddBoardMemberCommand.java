package com.boardly.features.board.application.command;

import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.BoardRole;
import com.boardly.shared.common.value.UserId;

/**
 * 보드 멤버 추가 명령
 */
public record AddBoardMemberCommand(
    BoardId boardId,
    UserId userId,
    BoardRole role,
    UserId requestedBy
) {}

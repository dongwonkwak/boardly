package com.boardly.features.board.application.port.input;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.model.BoardRole;
import com.boardly.features.user.domain.model.UserId;

/**
 * 보드 멤버 추가 명령
 */
public record AddBoardMemberCommand(
        BoardId boardId,
        UserId userId,
        BoardRole role,
        UserId requestedBy) {
}
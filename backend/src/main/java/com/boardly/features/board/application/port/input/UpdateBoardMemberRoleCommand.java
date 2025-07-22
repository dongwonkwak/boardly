package com.boardly.features.board.application.port.input;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.model.BoardRole;
import com.boardly.features.user.domain.model.UserId;

/**
 * 보드 멤버 역할 수정 명령
 */
public record UpdateBoardMemberRoleCommand(
        BoardId boardId,
        UserId targetUserId,
        BoardRole newRole,
        UserId requestedBy) {
}
package com.boardly.features.board.application.command;

import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.BoardRole;
import com.boardly.shared.common.value.UserId;

/**
 * 보드 멤버 역할 수정 명령
 */
public record UpdateBoardMemberRoleCommand(
        BoardId boardId,
        UserId targetUserId,
        BoardRole newRole,
        UserId requestedBy) {
}
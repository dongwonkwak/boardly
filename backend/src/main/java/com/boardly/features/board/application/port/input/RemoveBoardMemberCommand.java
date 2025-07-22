package com.boardly.features.board.application.port.input;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.user.domain.model.UserId;

/**
 * 보드 멤버 삭제 명령
 */
public record RemoveBoardMemberCommand(
        BoardId boardId,
        UserId targetUserId,
        UserId requestedBy) {
}
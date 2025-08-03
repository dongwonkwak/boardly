package com.boardly.features.board.application.port.input;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.user.domain.model.UserId;

/**
 * 보드 상세 조회 커맨드
 * 
 * <p>
 * 보드 상세 정보 조회에 필요한 정보를 담는 불변 객체입니다.
 * 
 * @param boardId 조회할 보드의 ID
 * @param userId  조회하는 사용자의 ID
 * 
 * @since 1.0.0
 */
public record GetBoardDetailCommand(
        BoardId boardId,
        UserId userId) {
}
package com.boardly.features.board.application.query;

import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.UserId;

/**
 * 보드 상세 조회 쿼리
 * 
 * <p>
 * 보드 상세 정보 조회에 필요한 정보를 담는 불변 객체입니다.
 * 
 * @param boardId 조회할 보드의 ID
 * @param userId  조회하는 사용자의 ID
 * 
 * @since 1.0.0
 */
public record GetBoardDetailQuery(
        BoardId boardId,
        UserId userId) {
}
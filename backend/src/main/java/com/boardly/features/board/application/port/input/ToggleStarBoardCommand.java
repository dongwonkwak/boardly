package com.boardly.features.board.application.port.input;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.user.domain.model.UserId;

/**
 * 보드 즐겨찾기 상태 토글 커맨드
 * 
 * <p>보드의 즐겨찾기 상태 토글을 위한 데이터를 담는 불변 객체입니다.
 * 
 * @param boardId 즐겨찾기 상태를 토글할 보드의 ID (필수)
 * @param requestedBy 변경을 요청하는 사용자 ID (권한 확인용, 필수)
 * 
 * @since 1.0.0
 */
public record ToggleStarBoardCommand(
    BoardId boardId,
    UserId requestedBy
) {
    /**
     * 보드 즐겨찾기 상태 토글 커맨드를 생성합니다.
     * 
     * @param boardId 즐겨찾기 상태를 토글할 보드의 ID (필수)
     * @param requestedBy 변경을 요청하는 사용자 ID (필수)
     * @return ToggleStarBoardCommand 객체
     */
    public static ToggleStarBoardCommand of(BoardId boardId, UserId requestedBy) {
        return new ToggleStarBoardCommand(boardId, requestedBy);
    }
} 
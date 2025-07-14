package com.boardly.features.board.application.port.input;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.user.domain.model.UserId;

/**
 * 보드 아카이브 상태 변경 커맨드
 * 
 * <p>보드의 아카이브 상태 변경을 위한 데이터를 담는 불변 객체입니다.
 * 
 * @param boardId 아카이브 상태를 변경할 보드의 ID (필수)
 * @param requestedBy 변경을 요청하는 사용자 ID (권한 확인용, 필수)
 * 
 * @since 1.0.0
 */
public record ArchiveBoardCommand(
    BoardId boardId,
    UserId requestedBy
) {
    /**
     * 보드 아카이브 상태 변경 커맨드를 생성합니다.
     * 
     * @param boardId 아카이브 상태를 변경할 보드의 ID (필수)
     * @param requestedBy 변경을 요청하는 사용자 ID (필수)
     * @return ArchiveBoardCommand 객체
     */
    public static ArchiveBoardCommand of(BoardId boardId, UserId requestedBy) {
        return new ArchiveBoardCommand(boardId, requestedBy);
    }
} 
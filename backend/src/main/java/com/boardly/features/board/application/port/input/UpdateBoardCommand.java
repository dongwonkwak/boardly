package com.boardly.features.board.application.port.input;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.user.domain.model.UserId;
import static org.apache.commons.lang3.StringUtils.trim;

/**
 * 보드 업데이트 커맨드
 * 
 * <p>보드의 제목과 설명 수정을 위한 데이터를 담는 불변 객체입니다.
 * 
 * @param boardId 업데이트할 보드의 ID (필수)
 * @param title 변경할 보드 제목 (선택사항, null이면 변경하지 않음)
 * @param description 변경할 보드 설명 (선택사항, null이면 변경하지 않음)
 * @param requestedBy 수정을 요청하는 사용자 ID (권한 확인용, 필수)
 * 
 * @since 1.0.0
 */
public record UpdateBoardCommand(
    BoardId boardId,
    String title,
    String description,
    UserId requestedBy
) {
    /**
     * 보드 업데이트 커맨드를 생성합니다.
     * 
     * <p>입력값을 정제하여 안전한 커맨드 객체를 생성합니다.
     * 
     * @param boardId 업데이트할 보드의 ID (필수)
     * @param title 변경할 보드 제목 (선택사항, trim 처리됨)
     * @param description 변경할 보드 설명 (선택사항, trim 처리됨)
     * @param requestedBy 수정을 요청하는 사용자 ID (필수)
     * @return 정제된 UpdateBoardCommand 객체
     */
    public static UpdateBoardCommand of(BoardId boardId, String title, String description, UserId requestedBy) {
        return new UpdateBoardCommand(
            boardId,
            trim(title),
            trim(description),
            requestedBy
        );
    }
} 
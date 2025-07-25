package com.boardly.features.boardlist.application.port.input;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.user.domain.model.UserId;


/**
 * 단일 보드 리스트 조회 커맨드
 * 
 * <p>특정 리스트의 상세 정보를 조회하는 데 필요한 정보를 담는 불변 객체입니다.
 * 
 * @param boardId 조회할 보드의 ID
 * @param userId 조회를 요청하는 사용자의 ID
 * 
 * @since 1.0.0
 */
public record GetBoardListsCommand(
  BoardId boardId,
  UserId userId
) {

}

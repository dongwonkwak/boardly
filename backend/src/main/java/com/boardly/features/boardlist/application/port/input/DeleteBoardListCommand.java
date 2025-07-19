package com.boardly.features.boardlist.application.port.input;

import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.user.domain.model.UserId;


/**
 * 보드 리스트 삭제 커맨드
 * 
 * <p>보드 리스트 삭제에 필요한 정보를 담는 불변 객체입니다.
 * 
 * @param listId 삭제할 리스트의 ID
 * @param userId 삭제를 요청하는 사용자의 ID
 * 
 * @since 1.0.0
 */
public record DeleteBoardListCommand(
  ListId listId,
  UserId userId
) {

}

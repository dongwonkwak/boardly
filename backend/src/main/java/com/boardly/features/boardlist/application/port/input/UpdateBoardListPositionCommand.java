package com.boardly.features.boardlist.application.port.input;

import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.user.domain.model.UserId;

/**
 * 보드 리스트 위치 변경 커맨드
 * 
 * <p>드래그 앤 드롭을 통한 리스트 순서 변경에 필요한 정보를 담는 불변 객체입니다.
 * 
 * @param listId 위치를 변경할 리스트의 ID
 * @param userId 변경을 요청하는 사용자의 ID
 * @param newPosition 새로운 위치 (0부터 시작)
 * 
 * @since 1.0.0
 */
public record UpdateBoardListPositionCommand(
  ListId listId,
  UserId userId,
  int newPosition
) {
  /**
   * 새로운 위치가 유효한지 검증합니다.
   * 
   * @return 위치가 0 이상이면 true
   */
  public boolean isValidPosition() {
    return newPosition >= 0;
  }
}

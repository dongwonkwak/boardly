package com.boardly.features.boardlist.application.port.input;

import com.boardly.features.boardlist.domain.model.ListColor;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.user.domain.model.UserId;

import static org.apache.commons.lang3.StringUtils.trim;

/**
 * 보드 리스트 수정 커맨드
 * 
 * <p>기존 보드 리스트의 정보를 수정하는 데 필요한 정보를 담는 불변 객체입니다.
 * 
 * @param listId 수정할 리스트의 ID
 * @param userId 수정을 요청하는 사용자의 ID
 * @param title 새로운 리스트 제목 (1-100자)
 * @param description 새로운 리스트 설명 (선택사항, 최대 500자)
 * @param color 새로운 리스트 색상 (선택사항)
 * 
 * @since 1.0.0
 */
public record UpdateBoardListCommand(
  ListId listId,
  UserId userId,
  String title,
  String description,
  ListColor color
) {
  /**
   *  색상 변경 없이 제목과 설명만 수정하는 생성자
   */
  public UpdateBoardListCommand(ListId listId, UserId userId, String title, String description) {
    this(listId, userId, trim(title), description, null);
  }
}

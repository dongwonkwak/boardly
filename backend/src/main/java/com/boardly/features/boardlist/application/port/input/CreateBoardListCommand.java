package com.boardly.features.boardlist.application.port.input;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.boardlist.domain.model.ListColor;
import com.boardly.features.user.domain.model.UserId;

import static org.apache.commons.lang3.StringUtils.trim;


/**
 * 보드 리스트 생성 커맨드
 * 
 * <p>새로운 보드 리스트 생성에 필요한 정보를 담는 불변 객체입니다.
 * 
 * @param boardId 리스트가 속할 보드의 ID
 * @param userId 리스트를 생성하는 사용자의 ID
 * @param title 리스트 제목 (1-100자)
 * @param description 리스트 설명 (선택사항, 최대 500자)
 * @param color 리스트 색상 (선택사항, 기본값: BLUE)
 * 
 * @since 1.0.0
 */
public record CreateBoardListCommand(
  BoardId boardId,
  UserId userId,
  String title,
  String description,
  ListColor color
) {
  /**
     * 색상이 지정되지 않은 경우 기본 색상을 사용하는 생성자
     */
    public CreateBoardListCommand(BoardId boardId, UserId userId, String title, String description) {
      this(boardId, userId, trim(title), description, ListColor.defaultColor());
  }
  
  /**
   * 설명과 색상이 지정되지 않은 경우 기본값을 사용하는 생성자
   */
  public CreateBoardListCommand(BoardId boardId, UserId userId, String title) {
      this(boardId, userId, trim(title), null, ListColor.defaultColor());
  }
} 
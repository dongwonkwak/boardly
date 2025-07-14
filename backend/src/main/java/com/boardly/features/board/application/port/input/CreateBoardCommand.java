package com.boardly.features.board.application.port.input;

import com.boardly.features.user.domain.model.UserId;

import static org.apache.commons.lang3.StringUtils.trim;

public record CreateBoardCommand(
  String title,
  String description,
  UserId ownerId
) {
  // canonical constructor만 허용되므로, 입력값을 정제하는 정적 팩토리 메서드를 사용합니다.
  public static CreateBoardCommand of(String title, String description, UserId ownerId) {
    return new CreateBoardCommand(
      trim(title),
      description != null ? description.trim() : null,
      ownerId
    );
  }
}

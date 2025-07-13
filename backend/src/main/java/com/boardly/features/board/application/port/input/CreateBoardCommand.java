package com.boardly.features.board.application.port.input;

import com.boardly.features.user.domain.model.UserId;

public record CreateBoardCommand(
  String title,
  String description,
  UserId ownerId
) {
}

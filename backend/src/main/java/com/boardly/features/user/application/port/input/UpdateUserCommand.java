package com.boardly.features.user.application.port.input;

import com.boardly.features.user.domain.model.UserId;

public record UpdateUserCommand(
  UserId userId,
  String firstName,
  String lastName
) {
}

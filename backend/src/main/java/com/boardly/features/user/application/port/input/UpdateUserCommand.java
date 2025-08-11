package com.boardly.features.user.application.port.input;

import com.boardly.shared.common.value.UserId;

public record UpdateUserCommand(
  UserId userId,
  String firstName,
  String lastName
) {
}

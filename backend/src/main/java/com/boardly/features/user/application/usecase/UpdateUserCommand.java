package com.boardly.features.user.application.usecase;

import com.boardly.features.user.domain.model.UserId;

public record UpdateUserCommand(
  UserId userId,
  String firstName,
  String lastName
) {
}

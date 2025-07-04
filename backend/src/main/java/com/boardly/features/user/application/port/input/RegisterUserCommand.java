package com.boardly.features.user.application.port.input;

public record RegisterUserCommand(
  String email,
  String password,
  String firstName,
  String lastName
) {
}

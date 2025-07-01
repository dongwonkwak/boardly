package com.boardly.features.user.application.usecase;

public record RegisterUserCommand(
  String email,
  String password,
  String firstName,
  String lastName
) {
}

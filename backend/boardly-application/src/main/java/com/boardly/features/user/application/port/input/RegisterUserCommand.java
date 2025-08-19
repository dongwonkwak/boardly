package com.boardly.features.user.application.command;

public record RegisterUserCommand(
  String email,
  String password,
  String firstName,
  String lastName
) {}

package com.boardly.features.user.application.usecase;


public interface RegisterUserUseCase {

  void register(RegisterUserCommand command);

  record RegisterUserCommand(
    String email,
    String password,
    String firstName,
    String lastName
  ) {}
}

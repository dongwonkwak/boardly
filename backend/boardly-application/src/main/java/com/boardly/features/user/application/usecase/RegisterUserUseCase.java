package com.boardly.features.user.application.usecase;

import com.boardly.features.user.application.command.RegisterUserCommand;
import com.boardly.features.user.domain.User;
import com.boardly.shared.common.error.Failure;
import io.vavr.control.Either;

public interface RegisterUserUseCase {
  Either<Failure, User> register(RegisterUserCommand command);
}

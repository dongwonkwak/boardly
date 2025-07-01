package com.boardly.features.user.application.usecase;


import com.boardly.features.user.domain.model.User;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;

public interface RegisterUserUseCase {

  Either<Failure, User> register(RegisterUserCommand command);

}

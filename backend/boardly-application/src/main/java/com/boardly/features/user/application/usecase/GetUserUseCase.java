package com.boardly.features.user.application.usecase;

import com.boardly.features.user.domain.User;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.common.value.UserId;
import io.vavr.control.Either;

public interface GetUserUseCase {
  Either<Failure, User> get(UserId userId);
}

package com.boardly.features.user.application.usecase;

import com.boardly.features.user.application.port.input.UpdateUserCommand;
import com.boardly.features.user.domain.model.User;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;

public interface UpdateUserUseCase {
    
    Either<Failure, User> update(UpdateUserCommand command);
} 
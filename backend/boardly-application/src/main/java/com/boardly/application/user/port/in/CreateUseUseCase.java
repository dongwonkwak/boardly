package com.boardly.application.user.port.in;

import com.boardly.domain.common.Failure;
import com.boardly.domain.user.User;
import io.vavr.control.Either;

/**
 * 회원가입 UseCase
 */
public interface CreateUseUseCase {

    /**
     * 회원가입 실행
     */
    Either<Failure, User> execute(CreateUserCommand command);
}

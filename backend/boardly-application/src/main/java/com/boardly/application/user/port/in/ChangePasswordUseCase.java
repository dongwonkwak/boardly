package com.boardly.application.user.port.in;

import com.boardly.domain.common.Failure;
import com.boardly.domain.user.User;
import io.vavr.control.Either;

/**
 * 비밀번호 변경 UseCase
 */
public interface ChangePasswordUseCase {

    /**
     * 비밀번호 변경 실행
     */
    Either<Failure, User> execute(ChangePasswordCommand command);
}

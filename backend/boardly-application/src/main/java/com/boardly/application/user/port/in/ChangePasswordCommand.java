package com.boardly.application.user.port.in;

import com.boardly.application.port.in.traits.Authenticatable;
import com.boardly.application.port.in.traits.Identifiable;
import com.boardly.domain.user.Password;
import com.boardly.domain.user.UserId;

import lombok.Builder;

/**
 * 비밀번호 변경 명령
 * <p>
 * 기존 사용자의 비밀번호를 새로운 비밀번호로 변경합니다.
 * 사용자 ID와 새로운 비밀번호를 포함합니다.
 * </p>
 */
@Builder
public record ChangePasswordCommand(
        UserId userId,
        Password password) implements UserCommand, Identifiable<UserId>, Authenticatable {

    @Override
    public UserId id() {
        return userId;
    }
}

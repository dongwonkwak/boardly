package com.boardly.application.user.port.in;

import com.boardly.application.port.in.traits.Authenticatable;
import com.boardly.application.port.in.traits.HasDisplayName;
import com.boardly.application.port.in.traits.HasEmail;
import com.boardly.domain.user.Email;
import com.boardly.domain.user.Password;

import lombok.Builder;

/**
 * 사용자 생성 명령
 * <p>
 * 회원가입 시 필요한 모든 정보를 포함합니다.
 * 이메일, 비밀번호, 표시 이름을 통해 새로운 사용자를 생성합니다.
 * </p>
 */
@Builder
public record CreateUserCommand(
        Email email,
        Password password,
        String displayName) implements UserCommand, HasEmail, Authenticatable, HasDisplayName {
}
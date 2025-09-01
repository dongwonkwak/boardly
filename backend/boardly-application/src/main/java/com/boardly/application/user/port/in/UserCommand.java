package com.boardly.application.user.port.in;

import com.boardly.application.port.in.Command;

/**
 * 사용자 관련 명령들의 기본 인터페이스
 * <p>
 * 모든 사용자 관련 Command는 이 인터페이스를 구현해야 합니다.
 * Sealed interface를 통해 컴파일 타임에 모든 구현체를 알 수 있습니다.
 * </p>
 */
public sealed interface UserCommand extends Command
        permits CreateUserCommand, ChangePasswordCommand {
}

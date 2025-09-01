package com.boardly.application.port.in.traits;

import com.boardly.domain.user.Password;

/**
 * 인증 정보(비밀번호)를 가지는 명령을 나타내는 인터페이스
 */
public interface Authenticatable {
    Password password();
}

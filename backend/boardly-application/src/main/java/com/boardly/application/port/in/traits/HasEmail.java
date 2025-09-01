package com.boardly.application.port.in.traits;

import com.boardly.domain.user.Email;

/**
 * 이메일을 가지는 명령을 나타내는 인터페이스
 */
public interface HasEmail {
    Email email();
}

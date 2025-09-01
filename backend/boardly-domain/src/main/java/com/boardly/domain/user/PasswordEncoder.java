package com.boardly.domain.user;

/**
 * 비밀번호 암호화 인터페이스
 */
public interface PasswordEncoder {

    /**
     * 비밀번호 암호화
     */
    String encode(String rawPassword);

    /**
     * 비밀번호 검증
     */
    boolean matches(String rawPassword, String encodedPassword);
}

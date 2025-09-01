package com.boardly.domain.user;

/**
 * 이메일 도메인 값 객체
 * <p>
 * 검증은 Application Layer의 Validator에서 수행되며,
 * 도메인 계층에서는 이미 검증된 값만 받는다고 가정합니다.
 */
public record Email(String value) {

    public Email {
        // 최소한의 null 체크만 수행 (도메인 불변성 보장)
        if (value == null) {
            throw new IllegalArgumentException("Email value cannot be null");
        }
        // 정규화: 소문자 변환 및 공백 제거
        value = value.trim().toLowerCase();
    }

    /**
     * 팩토리 메서드 - 이미 검증된 값으로 생성
     */
    public static Email of(String validatedValue) {
        return new Email(validatedValue);
    }

    /**
     * 이메일 문자열 반환
     */
    @Override
    public String toString() {
        return value;
    }
}
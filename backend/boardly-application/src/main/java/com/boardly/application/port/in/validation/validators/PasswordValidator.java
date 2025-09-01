package com.boardly.application.port.in.validation.validators;

import java.util.regex.Pattern;

import com.boardly.application.port.in.traits.Authenticatable;
import com.boardly.application.port.in.validation.CommonValidator;
import com.boardly.application.port.in.validation.Validator;
import com.boardly.domain.user.Password;

/**
 * 비밀번호 검증기
 * <p>
 * notBlank -> minLength -> maxLength -> pattern 순서로 체이닝하여 검증합니다.
 * 체이닝 중 하나라도 실패하면 즉시 반환합니다.
 * </p>
 */
public final class PasswordValidator {

    // 최소 8자, 대문자, 소문자, 숫자, 특수문자 모두 포함
    // 허용 특수문자: ! @ # $ % ^ & * ( ) _ + - = { } [ ] | : ; " ' < > , . ? /
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|:;\"'<>,.?/])[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{}|:;\"'<>,.?/]+$");

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;

    private PasswordValidator() {
        // 유틸리티 클래스
    }

    /**
     * Authenticatable trait를 구현한 객체의 비밀번호를 검증합니다.
     * 
     * @param <T> Authenticatable을 구현한 타입
     * @return 검증기
     */
    public static <T extends Authenticatable> Validator<T> validate() {
        return target -> {
            Password password = target.password();
            String passwordValue = password.getHash(); // 실제로는 원시 비밀번호가 필요하지만 현재는 hash 값 사용

            return CommonValidator.notBlank("password")
                    .then(CommonValidator.minLength("password", MIN_LENGTH))
                    .then(CommonValidator.maxLength("password", MAX_LENGTH))
                    .then(CommonValidator.pattern("password", PASSWORD_PATTERN))
                    .validate(passwordValue)
                    .map(result -> target); // 원본 객체 반환
        };
    }

    /**
     * 비밀번호 문자열을 직접 검증합니다.
     * 
     * @return 검증기
     */
    public static Validator<String> validatePasswordString() {
        return CommonValidator.notBlank("password")
                .then(CommonValidator.minLength("password", MIN_LENGTH))
                .then(CommonValidator.maxLength("password", MAX_LENGTH))
                .then(CommonValidator.pattern("password", PASSWORD_PATTERN));
    }

    /**
     * 비밀번호 강도만 검증합니다 (길이와 복잡성).
     * 
     * @return 검증기
     */
    public static Validator<String> validatePasswordStrength() {
        return CommonValidator.minLength("password", MIN_LENGTH)
                .then(CommonValidator.maxLength("password", MAX_LENGTH))
                .then(CommonValidator.pattern("password", PASSWORD_PATTERN));
    }
}

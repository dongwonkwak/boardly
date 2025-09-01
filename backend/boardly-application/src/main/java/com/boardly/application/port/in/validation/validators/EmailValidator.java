package com.boardly.application.port.in.validation.validators;

import java.util.regex.Pattern;

import com.boardly.application.port.in.traits.HasEmail;
import com.boardly.application.port.in.validation.CommonValidator;
import com.boardly.application.port.in.validation.Validator;
import com.boardly.domain.user.Email;

/**
 * 이메일 검증기
 * <p>
 * notBlank -> minLength -> maxLength -> pattern 순서로 체이닝하여 검증합니다.
 * 체이닝 중 하나라도 실패하면 즉시 반환합니다.
 * </p>
 */
public final class EmailValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^(?!.*\\.\\.)[A-Za-z0-9+_.-]+@(?!.*\\.\\.)[A-Za-z0-9-]+\\.[A-Za-z0-9-.]+$");

    private static final int MIN_LENGTH = 5; // a@b.c
    private static final int MAX_LENGTH = 254; // RFC 5321 표준

    private EmailValidator() {
        // 유틸리티 클래스
    }

    /**
     * HasEmail trait를 구현한 객체의 이메일을 검증합니다.
     * 
     * @param <T> HasEmail을 구현한 타입
     * @return 검증기
     */
    public static <T extends HasEmail> Validator<T> validate() {
        return target -> {
            Email email = target.email();
            String emailValue = email.value();

            return CommonValidator.notBlank("email")
                    .then(CommonValidator.minLength("email", MIN_LENGTH))
                    .then(CommonValidator.maxLength("email", MAX_LENGTH))
                    .then(CommonValidator.pattern("email", EMAIL_PATTERN))
                    .validate(emailValue)
                    .map(result -> target); // 원본 객체 반환
        };
    }

    /**
     * 이메일 문자열을 직접 검증합니다.
     * 
     * @return 검증기
     */
    public static Validator<String> validateEmailString() {
        return CommonValidator.notBlank("email")
                .then(CommonValidator.minLength("email", MIN_LENGTH))
                .then(CommonValidator.maxLength("email", MAX_LENGTH))
                .then(CommonValidator.pattern("email", EMAIL_PATTERN));
    }
}

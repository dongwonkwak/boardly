package com.boardly.application.port.in.validation.validators;

import java.util.regex.Pattern;

import com.boardly.application.port.in.traits.HasDisplayName;
import com.boardly.application.port.in.validation.CommonValidator;
import com.boardly.application.port.in.validation.Validator;

/**
 * 표시 이름 검증기
 * <p>
 * notBlank -> minLength -> maxLength -> pattern 순서로 체이닝하여 검증합니다.
 * 체이닝 중 하나라도 실패하면 즉시 반환합니다.
 * </p>
 */
public final class DisplayNameValidator {

    // 한글, 영문, 숫자, 공백만 허용 (특수문자 제외)
    private static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile(
            "^[가-힣A-Za-z0-9\\s]+$");

    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 50;

    private DisplayNameValidator() {
        // 유틸리티 클래스
    }

    /**
     * HasDisplayName trait를 구현한 객체의 표시 이름을 검증합니다.
     * 
     * @param <T> HasDisplayName을 구현한 타입
     * @return 검증기
     */
    public static <T extends HasDisplayName> Validator<T> validate() {
        return target -> {
            String displayName = target.displayName();

            return CommonValidator.notBlank("displayName")
                    .then(CommonValidator.minLength("displayName", MIN_LENGTH))
                    .then(CommonValidator.maxLength("displayName", MAX_LENGTH))
                    .then(CommonValidator.pattern("displayName", DISPLAY_NAME_PATTERN))
                    .validate(displayName)
                    .map(result -> target); // 원본 객체 반환
        };
    }

    /**
     * 표시 이름 문자열을 직접 검증합니다.
     * 
     * @return 검증기
     */
    public static Validator<String> validateDisplayNameString() {
        return CommonValidator.notBlank("displayName")
                .then(CommonValidator.minLength("displayName", MIN_LENGTH))
                .then(CommonValidator.maxLength("displayName", MAX_LENGTH))
                .then(CommonValidator.pattern("displayName", DISPLAY_NAME_PATTERN));
    }

    /**
     * 표시 이름 길이만 검증합니다 (패턴 검증 제외).
     * 
     * @return 검증기
     */
    public static Validator<String> validateDisplayNameLength() {
        return CommonValidator.notBlank("displayName")
                .then(CommonValidator.minLength("displayName", MIN_LENGTH))
                .then(CommonValidator.maxLength("displayName", MAX_LENGTH));
    }
}

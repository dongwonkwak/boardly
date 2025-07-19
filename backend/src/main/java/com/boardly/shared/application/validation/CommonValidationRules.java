package com.boardly.shared.application.validation;

import java.util.function.Function;
import java.util.regex.Pattern;

import com.boardly.shared.application.validation.Validator;

/**
 * 공통 검증 규칙들을 제공하는 유틸리티 클래스
 * 모든 도메인에서 공통으로 사용되는 검증 로직을 중앙화
 */
public final class CommonValidationRules {
    
    // === 공통 패턴 정의 ===
    public static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    public static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$"
    );
    
    public static final Pattern NAME_PATTERN = Pattern.compile(
        "^[a-zA-Z가-힣]+$"
    );
    
    public static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    
    // === 공통 상수 정의 ===
    public static final int EMAIL_MAX_LENGTH = 100;
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 20;
    public static final int NAME_MAX_LENGTH = 50;
    public static final int TITLE_MAX_LENGTH = 100;
    public static final int DESCRIPTION_MAX_LENGTH = 500;
    
    private CommonValidationRules() {
        // 유틸리티 클래스이므로 인스턴스화 방지
    }
    
    // === ID 검증 ===
    
    /**
     * ID 필드가 null이 아닌지 검증
     */
    public static <T, ID> Validator<T> idRequired(
            Function<T, ID> idExtractor,
            String fieldName,
            ValidationMessageResolver messageResolver) {
        return Validator.fieldWithMessage(
            idExtractor,
            id -> id != null,
            fieldName,
            "validation.common.id.required",
            messageResolver
        );
    }
    
    // === 문자열 길이 검증 ===
    
    /**
     * 문자열 최소 길이 검증
     */
    public static <T> Validator<T> minLength(
            Function<T, String> stringExtractor,
            String fieldName,
            int minLength,
            ValidationMessageResolver messageResolver) {
        return Validator.fieldWithMessage(
            stringExtractor,
            str -> str == null || str.length() >= minLength,
            fieldName,
            "validation.common.min.length",
            messageResolver,
            minLength
        );
    }
    
    /**
     * 문자열 최대 길이 검증
     */
    public static <T> Validator<T> maxLength(
            Function<T, String> stringExtractor,
            String fieldName,
            int maxLength,
            ValidationMessageResolver messageResolver) {
        return Validator.fieldWithMessage(
            stringExtractor,
            str -> str == null || str.length() <= maxLength,
            fieldName,
            "validation.common.max.length",
            messageResolver,
            maxLength
        );
    }
    

    
    /**
     * 문자열 길이 범위 검증
     */
    public static <T> Validator<T> lengthRange(
            Function<T, String> stringExtractor,
            String fieldName,
            int minLength,
            int maxLength,
            ValidationMessageResolver messageResolver) {
        return Validator.fieldWithMessage(
            stringExtractor,
            str -> str == null || (str.length() >= minLength && str.length() <= maxLength),
            fieldName,
            "validation.common.length.range",
            messageResolver,
            minLength, maxLength
        );
    }
    
    // === 필수 입력 검증 ===
    
    /**
     * 문자열 필수 입력 검증 (null, empty, blank 체크)
     */
    public static <T> Validator<T> required(
            Function<T, String> stringExtractor,
            String fieldName,
            ValidationMessageResolver messageResolver) {
        return Validator.fieldWithMessage(
            stringExtractor,
            str -> str != null && !str.trim().isEmpty(),
            fieldName,
            "validation.common.required",
            messageResolver
        );
    }
    

    
    /**
     * 객체 필수 입력 검증 (null 체크)
     */
    public static <T, U> Validator<T> objectRequired(
            Function<T, U> objectExtractor,
            String fieldName,
            ValidationMessageResolver messageResolver) {
        return Validator.fieldWithMessage(
            objectExtractor,
            obj -> obj != null,
            fieldName,
            "validation.common.required",
            messageResolver
        );
    }
    
    // === 패턴 검증 ===
    
    /**
     * 이메일 형식 검증
     */
    public static <T> Validator<T> email(
            Function<T, String> emailExtractor,
            String fieldName,
            ValidationMessageResolver messageResolver) {
        return Validator.fieldWithMessage(
            emailExtractor,
            email -> email == null || EMAIL_PATTERN.matcher(email).matches(),
            fieldName,
            "validation.common.email",
            messageResolver
        );
    }
    

    
    /**
     * 비밀번호 형식 검증
     */
    public static <T> Validator<T> password(
            Function<T, String> passwordExtractor,
            String fieldName,
            ValidationMessageResolver messageResolver) {
        return Validator.fieldWithMessage(
            passwordExtractor,
            password -> password == null || PASSWORD_PATTERN.matcher(password).matches(),
            fieldName,
            "validation.common.password.pattern",
            messageResolver
        );
    }
    
    /**
     * 이름 형식 검증 (한글/영문만)
     */
    public static <T> Validator<T> name(
            Function<T, String> nameExtractor,
            String fieldName,
            ValidationMessageResolver messageResolver) {
        return Validator.fieldWithMessage(
            nameExtractor,
            name -> name == null || NAME_PATTERN.matcher(name).matches(),
            fieldName,
            "validation.common.name.pattern",
            messageResolver
        );
    }
    
    /**
     * HTML 태그 포함 여부 검증
     */
    public static <T> Validator<T> noHtmlTags(
            Function<T, String> textExtractor,
            String fieldName,
            ValidationMessageResolver messageResolver) {
        return Validator.fieldWithMessage(
            textExtractor,
            text -> text == null || !HTML_TAG_PATTERN.matcher(text).find(),
            fieldName,
            "validation.common.html.not.allowed",
            messageResolver
        );
    }
    
    /**
     * 커스텀 패턴 검증
     */
    public static <T> Validator<T> pattern(
            Function<T, String> stringExtractor,
            String fieldName,
            Pattern pattern,
            String messageCode,
            ValidationMessageResolver messageResolver,
            Object... messageArgs) {
        return Validator.fieldWithMessage(
            stringExtractor,
            str -> str == null || pattern.matcher(str).matches(),
            fieldName,
            messageCode,
            messageResolver,
            messageArgs
        );
    }
    
    // === 복합 검증 ===
    
    /**
     * 이메일 완전 검증 (필수 + 형식 + 길이)
     */
    public static <T> Validator<T> emailComplete(
            Function<T, String> emailExtractor,
            String fieldName,
            ValidationMessageResolver messageResolver) {
        return Validator.chain(
            required(emailExtractor, fieldName, messageResolver),
            email(emailExtractor, fieldName, messageResolver),
            maxLength(emailExtractor, fieldName, EMAIL_MAX_LENGTH, messageResolver)
        );
    }
    

    
    /**
     * 비밀번호 완전 검증 (필수 + 길이 + 형식)
     */
    public static <T> Validator<T> passwordComplete(
            Function<T, String> passwordExtractor,
            String fieldName,
            ValidationMessageResolver messageResolver) {
        return Validator.chain(
            required(passwordExtractor, fieldName, messageResolver),
            minLength(passwordExtractor, fieldName, PASSWORD_MIN_LENGTH, messageResolver),
            maxLength(passwordExtractor, fieldName, PASSWORD_MAX_LENGTH, messageResolver),
            password(passwordExtractor, fieldName, messageResolver)
        );
    }
    
    /**
     * 이름 완전 검증 (필수 + 길이 + 형식)
     */
    public static <T> Validator<T> nameComplete(
            Function<T, String> nameExtractor,
            String fieldName,
            ValidationMessageResolver messageResolver) {
        return Validator.chain(
            required(nameExtractor, fieldName, messageResolver),
            maxLength(nameExtractor, fieldName, NAME_MAX_LENGTH, messageResolver),
            name(nameExtractor, fieldName, messageResolver)
        );
    }
    
    /**
     * 제목 완전 검증 (필수 + 길이 + HTML 태그 금지)
     */
    public static <T> Validator<T> titleComplete(
            Function<T, String> titleExtractor,
            String fieldName,
            ValidationMessageResolver messageResolver) {
        return Validator.chain(
            required(titleExtractor, fieldName, messageResolver),
            maxLength(titleExtractor, fieldName, TITLE_MAX_LENGTH, messageResolver),
            noHtmlTags(titleExtractor, fieldName, messageResolver)
        );
    }
    
    /**
     * 설명 완전 검증 (길이 + HTML 태그 금지)
     */
    public static <T> Validator<T> descriptionComplete(
            Function<T, String> descriptionExtractor,
            String fieldName,
            ValidationMessageResolver messageResolver) {
        return Validator.chain(
            maxLength(descriptionExtractor, fieldName, DESCRIPTION_MAX_LENGTH, messageResolver),
            noHtmlTags(descriptionExtractor, fieldName, messageResolver)
        );
    }
} 
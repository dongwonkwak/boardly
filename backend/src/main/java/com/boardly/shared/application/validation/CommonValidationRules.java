package com.boardly.shared.application.validation;

import java.util.function.Function;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 공통 검증 규칙들을 제공하는 클래스
 * 필드 중심의 재사용 가능한 검증 로직들을 포함합니다.
 */
@Component
@RequiredArgsConstructor
public class CommonValidationRules {

    private final ValidationMessageResolver messageResolver;

    // 공통 패턴들
    public static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    public static final Pattern PASSWORD_PATTERN = Pattern
            .compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$");
    public static final Pattern NAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z\\s]+$");
    public static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");

    // 공통 상수들
    public static final int EMAIL_MAX_LENGTH = 100;
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 20;
    public static final int NAME_MAX_LENGTH = 50;
    public static final int TITLE_MAX_LENGTH = 50;
    public static final int DESCRIPTION_MAX_LENGTH = 500;

    // 기본 검증 메서드들
    public static <T> Validator<T> required(Function<T, String> fieldExtractor, String fieldName,
            ValidationMessageResolver messageResolver) {
        return Validator.fieldWithMessage(
                fieldExtractor,
                value -> value != null && !value.trim().isEmpty(),
                fieldName,
                "validation." + fieldName + ".required",
                messageResolver);
    }

    public static <T> Validator<T> maxLength(Function<T, String> fieldExtractor, String fieldName, int maxLength,
            ValidationMessageResolver messageResolver) {
        return Validator.fieldWithMessage(
                fieldExtractor,
                value -> value == null || value.length() <= maxLength,
                fieldName,
                "validation." + fieldName + ".max.length",
                messageResolver,
                maxLength);
    }

    public static <T> Validator<T> minLength(Function<T, String> fieldExtractor, String fieldName, int minLength,
            ValidationMessageResolver messageResolver) {
        return Validator.fieldWithMessage(
                fieldExtractor,
                value -> value == null || value.length() >= minLength,
                fieldName,
                "validation." + fieldName + ".min.length",
                messageResolver,
                minLength);
    }

    public static <T> Validator<T> pattern(Function<T, String> fieldExtractor, String fieldName, Pattern pattern,
            ValidationMessageResolver messageResolver) {
        return Validator.fieldWithMessage(
                fieldExtractor,
                value -> value == null || pattern.matcher(value).matches(),
                fieldName,
                "validation." + fieldName + ".pattern",
                messageResolver);
    }

    public static <T> Validator<T> noHtmlTags(Function<T, String> fieldExtractor, String fieldName,
            ValidationMessageResolver messageResolver) {
        return Validator.fieldWithMessage(
                fieldExtractor,
                value -> value == null || !HTML_TAG_PATTERN.matcher(value).find(),
                fieldName,
                "validation." + fieldName + ".invalid",
                messageResolver);
    }

    public static <T> Validator<T> idRequired(Function<T, Object> fieldExtractor, String fieldName,
            ValidationMessageResolver messageResolver) {
        return Validator.fieldWithMessage(
                fieldExtractor,
                value -> value != null,
                fieldName,
                "validation." + fieldName + ".required",
                messageResolver);
    }

    // 필드별 완성된 검증 메서드들
    public <T> Validator<T> emailComplete(Function<T, String> emailExtractor) {
        return Validator.chain(
                required(emailExtractor, "email", messageResolver),
                pattern(emailExtractor, "email", EMAIL_PATTERN, messageResolver),
                maxLength(emailExtractor, "email", EMAIL_MAX_LENGTH, messageResolver));
    }

    public <T> Validator<T> passwordComplete(Function<T, String> passwordExtractor) {
        return Validator.chain(
                required(passwordExtractor, "password", messageResolver),
                minLength(passwordExtractor, "password", PASSWORD_MIN_LENGTH, messageResolver),
                maxLength(passwordExtractor, "password", PASSWORD_MAX_LENGTH, messageResolver),
                pattern(passwordExtractor, "password", PASSWORD_PATTERN, messageResolver));
    }

    public <T> Validator<T> firstNameComplete(Function<T, String> firstNameExtractor) {
        return Validator.chain(
                required(firstNameExtractor, "firstName", messageResolver),
                maxLength(firstNameExtractor, "firstName", NAME_MAX_LENGTH, messageResolver),
                pattern(firstNameExtractor, "firstName", NAME_PATTERN, messageResolver));
    }

    public <T> Validator<T> lastNameComplete(Function<T, String> lastNameExtractor) {
        return Validator.chain(
                required(lastNameExtractor, "lastName", messageResolver),
                maxLength(lastNameExtractor, "lastName", NAME_MAX_LENGTH, messageResolver),
                pattern(lastNameExtractor, "lastName", NAME_PATTERN, messageResolver));
    }

    public <T> Validator<T> titleComplete(Function<T, String> titleExtractor) {
        return Validator.chain(
                required(titleExtractor, "title", messageResolver),
                minLength(titleExtractor, "title", 1, messageResolver),
                maxLength(titleExtractor, "title", TITLE_MAX_LENGTH, messageResolver),
                noHtmlTags(titleExtractor, "title", messageResolver));
    }

    public <T> Validator<T> titleOptional(Function<T, String> titleExtractor) {
        return Validator.chain(
                minLength(titleExtractor, "title", 1, messageResolver),
                maxLength(titleExtractor, "title", TITLE_MAX_LENGTH, messageResolver),
                noHtmlTags(titleExtractor, "title", messageResolver));
    }

    public <T> Validator<T> descriptionComplete(Function<T, String> descriptionExtractor) {
        return Validator.chain(
                maxLength(descriptionExtractor, "description", DESCRIPTION_MAX_LENGTH, messageResolver),
                noHtmlTags(descriptionExtractor, "description", messageResolver));
    }

    public <T> Validator<T> userIdRequired(Function<T, Object> userIdExtractor) {
        return idRequired(userIdExtractor, "userId", messageResolver);
    }

    public <T> Validator<T> boardIdRequired(Function<T, Object> boardIdExtractor) {
        return idRequired(boardIdExtractor, "boardId", messageResolver);
    }

    public <T> Validator<T> listIdRequired(Function<T, Object> listIdExtractor) {
        return idRequired(listIdExtractor, "listId", messageResolver);
    }

    public <T> Validator<T> positionRequired(Function<T, Object> positionExtractor) {
        return Validator.fieldWithMessage(
                positionExtractor,
                value -> value != null,
                "position",
                "validation.position.required",
                messageResolver);
    }

    public <T> Validator<T> colorRequired(Function<T, String> colorExtractor) {
        return Validator.fieldWithMessage(
                colorExtractor,
                value -> value != null && !value.trim().isEmpty(),
                "color",
                "validation.color.required",
                messageResolver);
    }

    public <T> Validator<T> listColorRequired(Function<T, Object> colorExtractor) {
        return Validator.chain(
                Validator.fieldWithMessage(
                        colorExtractor,
                        value -> value != null,
                        "color",
                        "validation.color.required",
                        messageResolver),
                Validator.fieldWithMessage(
                        colorExtractor,
                        value -> {
                            if (value == null)
                                return true; // null은 이미 위에서 검증됨
                            if (value instanceof com.boardly.features.boardlist.domain.model.ListColor listColor) {
                                return com.boardly.features.boardlist.domain.model.ListColor
                                        .isValidColor(listColor.color());
                            }
                            return false;
                        },
                        "color",
                        "validation.color.invalid",
                        messageResolver));
    }

    /**
     * 카드 제목 완전 검증 (필수, 1-200자, HTML 태그 금지)
     */
    public <T> Validator<T> cardTitleComplete(Function<T, String> titleExtractor) {
        return Validator.chain(
                required(titleExtractor, "title", messageResolver),
                minLength(titleExtractor, "title", 1, messageResolver),
                maxLength(titleExtractor, "title", 200, messageResolver),
                noHtmlTags(titleExtractor, "title", messageResolver));
    }

    /**
     * 카드 설명 완전 검증 (선택사항, 2000자까지, HTML 태그 금지)
     */
    public <T> Validator<T> cardDescriptionComplete(Function<T, String> descriptionExtractor) {
        return Validator.chain(
                maxLength(descriptionExtractor, "description", 2000, messageResolver),
                noHtmlTags(descriptionExtractor, "description", messageResolver));
    }
}
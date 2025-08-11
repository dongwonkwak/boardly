package com.boardly.shared.validation;

import java.util.Objects;
import java.util.function.Function;

public final class Validators {
    private Validators() {}

    public static Validator<String> notBlank(String fieldName, String message) {
        return Validator.field(Function.identity(), s -> s != null && !s.trim().isEmpty(), fieldName, message);
    }

    public static Validator<String> maxLength(String fieldName, int max, String message) {
        return Validator.field(Function.identity(), s -> s != null && s.length() <= max, fieldName, message);
    }

    public static Validator<String> minLength(String fieldName, int min, String message) {
        return Validator.field(Function.identity(), s -> s != null && s.length() >= min, fieldName, message);
    }

    public static <T> Validator<T> notNull(String fieldName, String message) {
        return Validator.field(Function.identity(), Objects::nonNull, fieldName, message);
    }

    public static Validator<String> email(String fieldName, String message) {
        // 간단한 이메일 패턴 (필요 시 강화)
        return Validator.field(Function.identity(), s -> s != null && s.matches("^[^@\n]+@[^@\n]+\\.[^@\n]+$"), fieldName, message);
    }
}

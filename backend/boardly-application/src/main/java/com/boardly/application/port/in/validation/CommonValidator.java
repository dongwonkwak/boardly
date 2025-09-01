package com.boardly.application.port.in.validation;

import java.util.function.Function;

public final class CommonValidator {

    private CommonValidator() {
    }

    public static Validator<String> notBlank(String fieldName) {
        return Validator.field(
                Function.identity(), s -> s != null && !s.trim().isEmpty(),
                fieldName, "common.validation.field.required",
                new Object[] { "field." + fieldName });
    }

    public static Validator<String> minLength(String fieldName, int minLength) {
        return Validator.field(
                Function.identity(), s -> s != null && s.length() >= minLength,
                fieldName, "common.validation.field.minLength",
                new Object[] { "field." + fieldName, minLength });
    }

    public static Validator<String> maxLength(String fieldName, int maxLength) {
        return Validator.field(
                Function.identity(), s -> s != null && s.length() <= maxLength,
                fieldName, "common.validation.field.maxLength",
                new Object[] { "field." + fieldName, maxLength });
    }

    public static Validator<String> pattern(String fieldName, java.util.regex.Pattern pattern) {
        return Validator.field(
                Function.identity(), s -> s != null && pattern.matcher(s).matches(),
                fieldName, "common.validation.field.pattern",
                new Object[] { "field." + fieldName });
    }
}

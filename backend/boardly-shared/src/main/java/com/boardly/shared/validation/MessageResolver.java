package com.boardly.shared.validation;

import java.util.Locale;

public interface MessageResolver {

    String getMessage(String code, Object... args);

    String getMessage(String code, Locale locale, Object... args);

    String getMessageWithDefault(String code, String defaultMessage, Object... args);

    String getValidationMessage(String field, String rule, Object... args);

    String getCommonValidationMessage(String rule, Object... args);

}

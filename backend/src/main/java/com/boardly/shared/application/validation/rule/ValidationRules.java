package com.boardly.shared.application.validation.rule;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Validation;

@Component
public final class ValidationRules {
  private final ValidationMessageResolver messageResolver;

  public ValidationRules(ValidationMessageResolver messageResolver) {
    this.messageResolver = messageResolver;
  }

  public <T> FieldValidationRule<T> required() {
    return (value, field) -> {
      if (value == null || (value instanceof String && StringUtils.isBlank((String) value))) {
        return Validation.invalid(
          Failure.FieldViolation.builder()
            .field(field)
            .message(messageResolver.getFieldMessage("validation.required", field))
            .build()
        );
      }
      return Validation.valid(value);
    };
  }

  public FieldValidationRule<String> minLength(int minLength) {
    return (value, field) -> {
      if (value != null && value.length() < minLength) {
        return Validation.invalid(
          Failure.FieldViolation.builder()
            .field(field)
            .message(messageResolver.getFieldMessage("validation.minLength", field, minLength))
            .rejectedValue(value)
            .build()
        );
      }
      return Validation.valid(value);
    };
  }

  public FieldValidationRule<String> maxLength(int maxLength) {
    return (value, field) -> {
      if (value != null && value.length() > maxLength) {
        return Validation.invalid(
          Failure.FieldViolation.builder()
            .field(field)
            .message(messageResolver.getFieldMessage("validation.maxLength", field, maxLength))
            .rejectedValue(value)
            .build()
        );
      }
      return Validation.valid(value);
    };
  }

  public FieldValidationRule<String> lengthBetween(int minLength, int maxLength) {
    return (value, field) -> {
      if (value != null && (value.length() < minLength || value.length() > maxLength)) {
        return Validation.invalid(
          Failure.FieldViolation.builder()
            .field(field)
            .message(messageResolver.getFieldMessage("validation.lengthBetween", field, minLength, maxLength))
            .rejectedValue(value)
            .build()
        );
      }
      return Validation.valid(value);
    };
  }

  public FieldValidationRule<String> matchesPattern(String pattern, String messageKey) {
    return (value, field) -> {
      if (value != null && !value.matches(pattern)) {
        return Validation.invalid(
          Failure.FieldViolation.builder()
            .field(field)
            .message(messageResolver.getFieldMessage(messageKey, field))
            .rejectedValue(value)
            .build()
        );
      }
      return Validation.valid(value);
    };
  }

  public <T extends Number> FieldValidationRule<T> min(T minValue) {
    return (value, field) -> {
      if (value != null && value.doubleValue() < minValue.doubleValue()) {
        return Validation.invalid(
          Failure.FieldViolation.builder()
            .field(field)
            .message(messageResolver.getFieldMessage("validation.number.min", field, minValue))
            .rejectedValue(value)
            .build()
        );
      }
      return Validation.valid(value);
    };
  }

  public <T extends Number> FieldValidationRule<T> max(T maxValue) {
    return (value, field) -> {
      if (value != null && value.doubleValue() > maxValue.doubleValue()) {
        return Validation.invalid(
          Failure.FieldViolation.builder()
            .field(field)
            .message(messageResolver.getFieldMessage("validation.number.max", field, maxValue))
            .rejectedValue(value)
            .build()
        );
      }
      return Validation.valid(value);
    };
  }
}

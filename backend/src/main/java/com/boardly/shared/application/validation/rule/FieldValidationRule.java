package com.boardly.shared.application.validation.rule;

import com.boardly.shared.domain.common.Failure.FieldViolation;

import io.vavr.control.Validation;

/**
 * 필드 검증 규칙을 정의하는 함수형 인터페이스입니다.
 * 
 * @param <T> 검증할 값의 타입
 * @param field 검증 중인 필드의 이름
 */
@FunctionalInterface
public interface FieldValidationRule<T> {
  Validation<FieldViolation, T> validate(T value, String field);
}

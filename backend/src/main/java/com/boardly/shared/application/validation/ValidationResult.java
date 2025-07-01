package com.boardly.shared.application.validation;


import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

import com.boardly.shared.domain.common.Failure.FieldViolation;
import com.boardly.shared.domain.common.Failure;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValidationResult<T> {
  private final Validation<Seq<FieldViolation>, T> validation;

  /**
   * 검증 성공 결과를 생성합니다.
   * @param <T> 검증 결과 타입
   * @param value 성공한 검증 결과 값
   * @return 성공한 결과
   */
  public static <T> ValidationResult<T> valid(T value) {
    return new ValidationResult<>(Validation.valid(value));
  }

  /**
   * 검증 실패 결과를 생성합니다. (단일)
   * @param <T> 검증 결과 타입
   * @param violation 검증 실패 결과
   * @return 검증 실패 결과
   */
  public static <T> ValidationResult<T> invalid(FieldViolation violation) {
    return new ValidationResult<>(Validation.invalid(io.vavr.collection.List.of(violation)));
  }

  /**
   * 검증 실패 결과를 생성합니다. (다중)
   * @param <T> 검증 결과 타입
   * @param violations 검증 실패 결과
   * @return 검증 실패 결과
   */
  public static <T> ValidationResult<T> invalid(Seq<FieldViolation> violations) {
    return new ValidationResult<>(Validation.invalid(violations));
  }

  /**
   * 검증 실패 결과를 생성합니다. (단일)
   * @param <T> 검증 결과 타입
   * @param field 검증 실패 필드
   * @param message 검증 실패 메시지
   * @param rejectedValue 거절된 값
   * @return 검증 실패 결과
   */
  public static <T> ValidationResult<T> invalid(String field, String message, Object rejectedValue) {
    var violation = FieldViolation.builder()
      .field(field)
      .message(message)
      .rejectedValue(rejectedValue)
      .build();
    return invalid(violation);
  }

  public static <T> ValidationResult<T> of(Validation<Seq<FieldViolation>, T> validation) {
    return new ValidationResult<>(validation);
  }

  public boolean isValid() {
    return validation.isValid();
  }

  public boolean isInvalid() {
    return validation.isInvalid();
  }

  /**
   * 검증 성공 결과를 반환합니다.
   * @return 검증 성공 결과
   * @throws IllegalStateException 검증 실패 시 예외 발생
   */
  public T get() {
    if (validation.isInvalid()) {
      throw new IllegalStateException("ValidationResult is invalid");
    }
    return validation.get();
  }

  /**
   * 검증 실패 결과를 반환합니다.
   * @return 검증 실패 결과
   */
  public Seq<FieldViolation> getErrors() {
    return validation.getError();
  }

  /**
   * 검증 실패 결과를 컬렉션으로 반환합니다.
   * @return 검증 실패 결과
   */
  public Collection<FieldViolation> getErrorsAsCollection() {
    return getErrors().asJava();
  }

  /**
   * 검증 결과를 변환합니다.
   * @param <U> 변환된 결과 타입
   * @param mapper 변환 함수
   * @return 변환된 결과
   */
  public <U> ValidationResult<U> map(Function<T, U> mapper) {
    return new ValidationResult<>(validation.map(mapper));
  }

  /**
   * 검증 결과를 다른 ValidationResult로 변환합니다.
   * @param <U> 변환된 결과 타입
   * @param <R> 변환된 결과 타입
   * @param mapper 변환 함수
   * @return 변환된 결과
   */
  public <U, R> ValidationResult<R> flatMap(Function<T, ValidationResult<R>> mapper) {
    if (validation.isInvalid()) {
      return new ValidationResult<>(Validation.invalid(validation.getError()));
    }

    return mapper.apply(validation.get());
  }

  /**
   * 두 검증 결과를 결합합니다.
   * @param <U> 결합할 결과 타입
   * @param <R> 결합된 결과 타입
   * @param other 결합할 결과
   * @param combiner 결합 함수
   * @return 결합된 결과
   */
  public <U, R> ValidationResult<R> combine(
    ValidationResult<U> other,
    Function<T, Function<U, R>> combiner) {
      return ValidationResult.of(
        validation.combine(other.validation)
          .ap((t, u) -> combiner.apply(t).apply(u))
          .mapError(errors -> errors.flatMap(e -> e))
      );
  }

  /**
   * 검증 실패 결과를 Failure로 변환합니다.
   * @param defaultMessage 기본 메시지
   * @return 실패 결과
   */
  public Failure toFailure(String defaultMessage) {
    if (validation.isValid()) {
      throw new IllegalStateException("ValidationResult is valid");
    }

    return Failure.ofValidation(defaultMessage, getErrorsAsCollection());
  }

  /**
   * 검증 성공시 결과를 소비합니다.
   * @param consumer 소비 함수
   * @return 검증 결과
   */
  public ValidationResult<T> peek(Consumer<T> consumer) {
    validation.peek(consumer);
    return this;
  }

  /**
   * 검증 실패시 결과를 소비합니다.
   * @param consumer 소비 함수
   * @return 검증 결과
   */
  public ValidationResult<T> peekError(Consumer<Seq<FieldViolation>> consumer) {
    if (validation.isInvalid()) {
      consumer.accept(validation.getError());
    }
    return this;
  }

  /**
   * 검증 결과에 따라 다른 값 반환
   * @param <U> 분기 결과 타입
   * @param onInvalid 검증 실패 시 함수
   * @param onValid 검증 성공 시 함수
   * @return 분기 결과
   */
  public <U> U fold(Function<Seq<FieldViolation>, U> onInvalid, Function<T, U> onValid) {
    return validation.fold(onInvalid, onValid);
  }

  @Override
  public String toString() {
    return validation.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    ValidationResult<?> other = (ValidationResult<?>) obj;
    return validation.equals(other.validation);
  }

  @Override
  public int hashCode() {
    return validation.hashCode();
  }
}

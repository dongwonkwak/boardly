package com.boardly.shared.application.validation;

import java.util.function.Function;
import java.util.function.Predicate;

import com.boardly.shared.domain.common.Failure.FieldViolation;

import io.vavr.control.Validation;
import io.vavr.collection.Seq;

@FunctionalInterface
public interface Validator<T> {

  /**
   * 검증을 수행하고 ValidationResult를 반환합니다.
   * @param target 검증 대상
   * @return 검증 결과
   */
  ValidationResult<T> validate(T target);


  /**
   * 두 검증기를 연결하여 하나의 검증기로 만듭니다.
   * 모든 검증을 수행. 실패한 검증 결과를 모두 수집하여 하나의 검증 결과로 반환합니다.
   * @param other 연결할 검증기
   * @return 연결된 검증기
   */
  default Validator<T> and(Validator<T> other) {
    return target -> {
      var thisResult = this.validate(target);
      var otherResult = other.validate(target);

      // 둘 다 성공하면 성공
      if (thisResult.isValid() && otherResult.isValid()) {
        return ValidationResult.valid(target);
      }

      // 둘 중 하나라도 실패하면 실패. 위반 사항을 모두 수집
      Seq<FieldViolation> violations = io.vavr.collection.List.empty();
      if (thisResult.isInvalid()) {
        violations = violations.appendAll(thisResult.getErrors());
      }
      if (otherResult.isInvalid()) {
        violations = violations.appendAll(otherResult.getErrors());
      }
      return ValidationResult.invalid(violations);
    };
  }

  /**
   * 두 Validator를 순차적으로 실행하여 첫 번째 실패에서 중단
   * 첫 번째 검증이 실패하면 두 번째 검증은 실행하지 않음
   * 하나의 에러만 리턴하고 싶을 때 사용
   */
  default Validator<T> then(Validator<T> other) {
    return target -> {
      ValidationResult<T> thisResult = this.validate(target);
      if (thisResult.isInvalid()) {
        return thisResult; // 첫 번째 검증 실패 시 즉시 반환
      }
      return other.validate(target); // 첫 번째 성공 시 두 번째 검증 실행
    };
  }

  /**
   * 조건이 참일 때만 검증을 수행합니다.
   * @param condition 조건
   * @return 검증 결과
   */
  default Validator<T> when(Predicate<T> condition) {
    return target -> {
      if (condition.test(target)) {
        return this.validate(target);
      }
      return ValidationResult.valid(target);
    };
  }

  /**
   * 검증 대상을 변환하여 검증을 수행합니다.
   * @param <U> 변환된 검증 대상 타입
   * @param mapper 변환 함수
   * @return 변환된 검증 결과
   */
  default <U> Validator<U> contramap(Function<U, T> mapper) {
    return target -> this.validate(mapper.apply(target))
            .map(result -> target); // 원본 타입으로 다시 변환
  }

  // 항상 성공
  static <T> Validator<T> valid() {
    return ValidationResult::valid;
  }

  // 항상 실패
  static <T> Validator<T> invalid(String field, String message) {
    return target -> ValidationResult.invalid(field, message, target);
  }

  /**
   * 조건이 참일 때만 검증을 수행합니다.
   * @param predicate 조건
   * @param field 필드명
   * @param message 메시지
   * @return 검증 결과
   */
  static <T> Validator<T> of(Predicate<T> predicate, String field, String message) {
    return target -> {
      if (predicate.test(target)) {
        return ValidationResult.valid(target);
      }

      return ValidationResult.invalid(field, message, target);
    };
  }


  /**
   * 특정 필드를 검증하는 검증기를 생성합니다.
   * @param <T> 검증 대상 타입
   * @param <F> 필드 타입
   * @param fieldExtractor 필드 추출 함수
   * @param fieldValidator 필드 검증 함수
   * @param fieldName 필드명
   * @param errorMessage 에러 메시지
   * @return 검증 결과
   */
  static <T, F> Validator<T> field(
          Function<T, F> fieldExtractor,
          Predicate<F> fieldValidator,
          String fieldName,
          String errorMessage) {
    return target -> {
      F fieldValue = fieldExtractor.apply(target);
      if (fieldValidator.test(fieldValue)) {
        return ValidationResult.valid(target);
      }

      return ValidationResult.invalid(fieldName, errorMessage, fieldValue);
    };
  }

  /**
   * 국제화 메시지를 사용하는 필드별 검증 Validator 생성
   *
   * @param fieldExtractor 필드 추출 함수
   * @param fieldValidator 필드 검증자
   * @param fieldName 필드명
   * @param messageCode 메시지 코드
   * @param messageResolver 메시지 리졸버
   * @param messageArgs 메시지 파라미터
   */
  static <T, F> Validator<T> fieldWithMessage(Function<T, F> fieldExtractor,
                                              Predicate<F> fieldValidator,
                                              String fieldName,
                                              String messageCode,
                                              ValidationMessageResolver messageResolver,
                                              Object... messageArgs) {
    return target -> {
      F fieldValue = fieldExtractor.apply(target);
      if (fieldValidator.test(fieldValue)) {
        return ValidationResult.valid(target);
      }
      String errorMessage = messageResolver.getMessage(messageCode, messageArgs);
      return ValidationResult.invalid(fieldName, errorMessage, fieldValue);
    };
  }

  /**
   * 여러 Validator를 결합하여 모든 검증을 수행
   */
  @SafeVarargs
  static <T> Validator<T> combine(Validator<T>... validators) {
    return target -> {
      Seq<FieldViolation> allViolations = io.vavr.collection.List.empty();
      boolean hasErrors = false;

      for (Validator<T> validator : validators) {
        ValidationResult<T> result = validator.validate(target);
        if (result.isInvalid()) {
          hasErrors = true;
          allViolations = allViolations.appendAll(result.getErrors());
        }
      }

      if (hasErrors) {
        return ValidationResult.invalid(allViolations);
      }
      return ValidationResult.valid(target);
    };
  }

  /**
   * 여러 Validator를 순차적으로 실행하여 첫 번째 실패에서 중단
   * 하나의 에러만 리턴하고 싶을 때 사용
   */
  @SafeVarargs
  static <T> Validator<T> chain(Validator<T>... validators) {
    return target -> {
      for (Validator<T> validator : validators) {
        ValidationResult<T> result = validator.validate(target);
        if (result.isInvalid()) {
          return result; // 첫 번째 실패에서 즉시 반환
        }
      }
      return ValidationResult.valid(target);
    };
  }

  /**
   * Vavr Validation을 직접 사용하는 Validator 생성
   */
  static <T> Validator<T> fromValidation(Function<T, Validation<Seq<FieldViolation>, T>> validationFunc) {
    return target -> ValidationResult.of(validationFunc.apply(target));
  }
}

package com.boardly.shared.application.validation.rule;

import java.util.ArrayList;
import java.util.List;
import com.boardly.shared.domain.common.Failure.FieldViolation;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;

public class CompositeFieldValidator<T> {
  private final List<FieldValidationRule<T>> rules;

  private CompositeFieldValidator(List<FieldValidationRule<T>> rules) {
    this.rules = rules;
  }

  /**
   * 검증 규칙을 초기화하여 새로운 CompositeFieldValidator를 생성합니다.
   * 
   * @param rules 검증 규칙 목록
   * @return CompositeFieldValidator<T> - 생성된 검증 규칙 컴포지트
   */
  @SafeVarargs
  public static <T> CompositeFieldValidator<T> of(FieldValidationRule<T>... rules) {
    return new CompositeFieldValidator<>(List.of(rules));
  }
 
  public CompositeFieldValidator<T> and(FieldValidationRule<T> rule) {
    List<FieldValidationRule<T>> newRules = new ArrayList<>(rules);
    newRules.add(rule);
    return new CompositeFieldValidator<>(newRules);
  }

  /**
   * 등록된 모든 검증 규칙을 순차적으로 실행하여 값을 검증합니다.
   * 
   * @param value 검증할 값
   * @param fieldName 검증 중인 필드의 이름
   * @return Validation<Seq<FieldViolation>, T> - 검증 성공 시 원본 값을 포함하는 Success,
   *         검증 실패 시 모든 FieldViolation을 포함하는 Failure
   */
  public Validation<Seq<FieldViolation>, T> validate(T value, String fieldName) {
    return Validation.<FieldViolation, FieldValidationRule<T>, T>traverse(
            rules,
            rule -> rule.validate(value, fieldName).mapError(io.vavr.collection.List::of))
        .map(ignored -> value);
  }
}

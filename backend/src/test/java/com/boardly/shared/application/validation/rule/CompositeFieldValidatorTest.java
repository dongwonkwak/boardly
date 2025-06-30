package com.boardly.shared.application.validation.rule;

import com.boardly.shared.domain.common.Failure.FieldViolation;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CompositeFieldValidator 단위 테스트")
class CompositeFieldValidatorTest {

    private static final String FIELD_NAME = "testField";
    private static final String TEST_VALUE = "testValue";
    
    private FieldValidationRule<String> successRule1;
    private FieldValidationRule<String> successRule2;
    private FieldValidationRule<String> failureRule1;
    private FieldValidationRule<String> failureRule2;

    private FieldViolation violation1;
    private FieldViolation violation2;

    @BeforeEach
    void setUp() {
        violation1 = FieldViolation.builder().field(FIELD_NAME).message("Error 1").build();
        violation2 = FieldViolation.builder().field(FIELD_NAME).message("Error 2").build();

        successRule1 = (value, field) -> Validation.valid(value);
        successRule2 = (value, field) -> Validation.valid(value);
        
        failureRule1 = (value, field) -> Validation.invalid(violation1);
        failureRule2 = (value, field) -> Validation.invalid(violation2);
    }

    @Test
    @DisplayName("모든 규칙이 성공하면 유효한 결과를 반환한다")
    void shouldReturnValidWhenAllRulesSucceed() {
        CompositeFieldValidator<String> validator = CompositeFieldValidator.of(successRule1, successRule2);
        
        Validation<Seq<FieldViolation>, String> result = validator.validate(TEST_VALUE, FIELD_NAME);

        assertThat(result.isValid()).isTrue();
        assertThat(result.get()).isEqualTo(TEST_VALUE);
    }

    @Test
    @DisplayName("하나의 규칙이 실패하면 해당 오류를 포함한 잘못된 결과를 반환한다")
    void shouldReturnInvalidWithOneErrorWhenOneRuleFails() {
        CompositeFieldValidator<String> validator = CompositeFieldValidator.of(successRule1, failureRule1);

        Validation<Seq<FieldViolation>, String> result = validator.validate(TEST_VALUE, FIELD_NAME);

        assertThat(result.isInvalid()).isTrue();
        Seq<FieldViolation> errors = result.getError();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).isEqualTo(violation1);
    }

    @Test
    @DisplayName("여러 규칙이 실패하면 모든 오류를 포함한 잘못된 결과를 반환한다")
    void shouldReturnInvalidWithAllErrorsWhenMultipleRulesFail() {
        CompositeFieldValidator<String> validator = CompositeFieldValidator.of(successRule1, failureRule1, successRule2, failureRule2);

        Validation<Seq<FieldViolation>, String> result = validator.validate(TEST_VALUE, FIELD_NAME);

        assertThat(result.isInvalid()).isTrue();
        Seq<FieldViolation> errors = result.getError();
        assertThat(errors).hasSize(2);
        assertThat(errors).containsExactlyInAnyOrder(violation1, violation2);
    }

    @Test
    @DisplayName("and() 메소드로 규칙을 추가할 수 있다")
    void shouldAddRuleWithAndMethod() {
        CompositeFieldValidator<String> validator = CompositeFieldValidator.of(failureRule1);
        validator = validator.and(failureRule2);

        Validation<Seq<FieldViolation>, String> result = validator.validate(TEST_VALUE, FIELD_NAME);
        
        assertThat(result.isInvalid()).isTrue();
        Seq<FieldViolation> errors = result.getError();
        assertThat(errors).hasSize(2);
        assertThat(errors).containsExactlyInAnyOrder(violation1, violation2);
    }
    
    @Test
    @DisplayName("규칙이 없으면 항상 유효한 결과를 반환한다")
    void shouldReturnValidWhenNoRules() {
        CompositeFieldValidator<String> validator = CompositeFieldValidator.of();

        Validation<Seq<FieldViolation>, String> result = validator.validate(TEST_VALUE, FIELD_NAME);

        assertThat(result.isValid()).isTrue();
        assertThat(result.get()).isEqualTo(TEST_VALUE);
    }
} 
package com.boardly.shared.application.validation.rule;

import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure.FieldViolation;
import io.vavr.control.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidationRules 단위 테스트")
class ValidationRulesTest {

    @Mock
    private ValidationMessageResolver messageResolver;

    private ValidationRules validationRules;

    private static final String FIELD_NAME = "testField";
    private static final String ERROR_MESSAGE = "error message";

    @BeforeEach
    void setUp() {
        validationRules = new ValidationRules(messageResolver);
        // 모든 메시지 요청에 대해 기본 오류 메시지를 반환하도록 설정
        lenient().when(messageResolver.getFieldMessage(anyString(), anyString(), any(Object[].class)))
            .thenReturn(ERROR_MESSAGE);
    }

    @Nested
    @DisplayName("required 규칙")
    class RequiredRule {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("null, 비어있거나 공백인 문자열에 대해 실패한다")
        void shouldFailForInvalidStrings(String invalidValue) {
            Validation<FieldViolation, String> result = validationRules.<String>required().validate(invalidValue, FIELD_NAME);
            assertInvalid(result);
        }

        @Test
        @DisplayName("유효한 값에 대해 성공한다")
        void shouldSucceedForValidValue() {
            Validation<FieldViolation, String> result = validationRules.<String>required().validate("valid", FIELD_NAME);
            assertValid(result);
        }
    }

    @Nested
    @DisplayName("minLength 규칙")
    class MinLengthRule {
        private final int minLength = 5;

        @Test
        @DisplayName("null 값은 통과시킨다 (required와 조합)")
        void shouldPassForNull() {
            Validation<FieldViolation, String> result = validationRules.minLength(minLength).validate(null, FIELD_NAME);
            assertValid(result);
        }

        @Test
        @DisplayName("길이가 짧으면 실패한다")
        void shouldFailForShorterString() {
            Validation<FieldViolation, String> result = validationRules.minLength(minLength).validate("1234", FIELD_NAME);
            assertInvalid(result);
        }

        @ParameterizedTest
        @ValueSource(strings = {"12345", "123456"})
        @DisplayName("길이가 같거나 길면 성공한다")
        void shouldSucceedForEqualOrLongerString(String validString) {
            Validation<FieldViolation, String> result = validationRules.minLength(minLength).validate(validString, FIELD_NAME);
            assertValid(result);
        }
    }

    @Nested
    @DisplayName("maxLength 규칙")
    class MaxLengthRule {
        private final int maxLength = 10;

        @Test
        @DisplayName("null 값은 통과시킨다")
        void shouldPassForNull() {
            Validation<FieldViolation, String> result = validationRules.maxLength(maxLength).validate(null, FIELD_NAME);
            assertValid(result);
        }

        @Test
        @DisplayName("길이가 길면 실패한다")
        void shouldFailForLongerString() {
            Validation<FieldViolation, String> result = validationRules.maxLength(maxLength).validate("12345678901", FIELD_NAME);
            assertInvalid(result);
        }

        @ParameterizedTest
        @ValueSource(strings = {"1234567890", "12345"})
        @DisplayName("길이가 같거나 짧으면 성공한다")
        void shouldSucceedForEqualOrShorterString(String validString) {
            Validation<FieldViolation, String> result = validationRules.maxLength(maxLength).validate(validString, FIELD_NAME);
            assertValid(result);
        }
    }

    @Nested
    @DisplayName("lengthBetween 규칙")
    class LengthBetweenRule {
        private final int minLength = 5;
        private final int maxLength = 10;
        
        @Test
        @DisplayName("null 값은 통과시킨다")
        void shouldPassForNull() {
            Validation<FieldViolation, String> result = validationRules.lengthBetween(minLength, maxLength).validate(null, FIELD_NAME);
            assertValid(result);
        }

        @Test
        @DisplayName("길이가 짧으면 실패한다")
        void shouldFailForShorterString() {
            Validation<FieldViolation, String> result = validationRules.lengthBetween(minLength, maxLength).validate("1234", FIELD_NAME);
            assertInvalid(result);
        }

        @Test
        @DisplayName("길이가 길면 실패한다")
        void shouldFailForLongerString() {
            Validation<FieldViolation, String> result = validationRules.lengthBetween(minLength, maxLength).validate("12345678901", FIELD_NAME);
            assertInvalid(result);
        }

        @Test
        @DisplayName("길이가 범위 내에 있으면 성공한다")
        void shouldSucceedForStringWithinRange() {
            Validation<FieldViolation, String> result = validationRules.lengthBetween(minLength, maxLength).validate("1234567", FIELD_NAME);
            assertValid(result);
        }
    }

    @Nested
    @DisplayName("matchesPattern 규칙")
    class MatchesPatternRule {
        private final String pattern = "^[a-zA-Z]+$";
        private final String messageKey = "validation.pattern.mismatch";

        @Test
        @DisplayName("null 값은 통과시킨다")
        void shouldPassForNull() {
            Validation<FieldViolation, String> result = validationRules.matchesPattern(pattern, messageKey).validate(null, FIELD_NAME);
            assertValid(result);
        }
        
        @Test
        @DisplayName("패턴과 일치하지 않으면 실패한다")
        void shouldFailForMismatch() {
            Validation<FieldViolation, String> result = validationRules.matchesPattern(pattern, messageKey).validate("123", FIELD_NAME);
            assertInvalid(result);
        }

        @Test
        @DisplayName("패턴과 일치하면 성공한다")
        void shouldSucceedForMatch() {
            Validation<FieldViolation, String> result = validationRules.matchesPattern(pattern, messageKey).validate("abc", FIELD_NAME);
            assertValid(result);
        }
    }
    
    @Nested
    @DisplayName("min 규칙")
    class MinRule {
        private final int minValue = 10;

        @Test
        @DisplayName("null 값은 통과시킨다")
        void shouldPassForNull() {
            Validation<FieldViolation, Integer> result = validationRules.min(minValue).validate(null, FIELD_NAME);
            assertValid(result);
        }

        @Test
        @DisplayName("값이 작으면 실패한다")
        void shouldFailForSmallerValue() {
            Validation<FieldViolation, Integer> result = validationRules.min(minValue).validate(5, FIELD_NAME);
            assertInvalid(result);
        }

        @ParameterizedTest
        @ValueSource(ints = {10, 15})
        @DisplayName("값이 같거나 크면 성공한다")
        void shouldSucceedForEqualOrLargerValue(int validValue) {
            Validation<FieldViolation, Integer> result = validationRules.min(minValue).validate(validValue, FIELD_NAME);
            assertValid(result);
        }
    }

    @Nested
    @DisplayName("max 규칙")
    class MaxRule {
        private final int maxValue = 20;

        @Test
        @DisplayName("null 값은 통과시킨다")
        void shouldPassForNull() {
            Validation<FieldViolation, Integer> result = validationRules.max(maxValue).validate(null, FIELD_NAME);
            assertValid(result);
        }

        @Test
        @DisplayName("값이 크면 실패한다")
        void shouldFailForLargerValue() {
            Validation<FieldViolation, Integer> result = validationRules.max(maxValue).validate(25, FIELD_NAME);
            assertInvalid(result);
        }

        @ParameterizedTest
        @ValueSource(ints = {20, 15})
        @DisplayName("값이 같거나 작으면 성공한다")
        void shouldSucceedForEqualOrSmallerValue(int validValue) {
            Validation<FieldViolation, Integer> result = validationRules.max(maxValue).validate(validValue, FIELD_NAME);
            assertValid(result);
        }
    }


    private void assertValid(Validation<?, ?> result) {
        assertThat(result.isValid()).isTrue();
    }

    private void assertInvalid(Validation<?, ?> result) {
        assertThat(result.isInvalid()).isTrue();
        FieldViolation violation = (FieldViolation) result.getError();
        assertThat(violation.field()).isEqualTo(FIELD_NAME);
        assertThat(violation.message()).isEqualTo(ERROR_MESSAGE);
    }
} 
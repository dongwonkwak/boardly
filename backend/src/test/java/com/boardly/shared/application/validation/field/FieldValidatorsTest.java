package com.boardly.shared.application.validation.field;

import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.rule.CompositeFieldValidator;
import com.boardly.shared.application.validation.rule.ValidationRules;
import com.boardly.shared.domain.common.Failure.FieldViolation;
import io.vavr.collection.Seq;
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
import org.springframework.context.MessageSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("FieldValidators 통합 테스트")
class FieldValidatorsTest {

    @Mock
    private MessageSource messageSource;

    private FieldValidators fieldValidators;

    private static final String FIELD_NAME = "testField";

    @BeforeEach
    void setUp() {
        ValidationMessageResolver messageResolver = new ValidationMessageResolver(messageSource);
        ValidationRules validationRules = new ValidationRules(messageResolver);
        fieldValidators = new FieldValidators(validationRules);

        // 메시지 소스가 항상 고정된 메시지를 반환하도록 설정
        lenient().when(messageSource.getMessage(anyString(), any(), any()))
            .thenReturn("error message");
    }

    @Nested
    @DisplayName("email 검증")
    class EmailValidation {
        private CompositeFieldValidator<String> emailValidator;

        @BeforeEach
        void setup() {
            emailValidator = fieldValidators.email();
        }

        @ParameterizedTest
        @ValueSource(strings = {"test@example.com", "test.name@example.co.uk"})
        @DisplayName("유효한 이메일 형식에 성공한다")
        void shouldSucceedForValidEmails(String validEmail) {
            Validation<Seq<FieldViolation>, String> result = emailValidator.validate(validEmail, FIELD_NAME);
            assertThat(result.isValid()).isTrue();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "test", "test@", "@example.com", "test@.com"})
        @DisplayName("유효하지 않은 이메일 형식에 실패한다")
        void shouldFailForInvalidEmails(String invalidEmail) {
            Validation<Seq<FieldViolation>, String> result = emailValidator.validate(invalidEmail, FIELD_NAME);
            assertThat(result.isInvalid()).isTrue();
        }

        @Test
        @DisplayName("길이가 100자를 초과하면 실패한다")
        void shouldFailForLongEmail() {
            String longEmail = "a".repeat(92) + "@test.com"; // 101자
            Validation<Seq<FieldViolation>, String> result = emailValidator.validate(longEmail, FIELD_NAME);
            assertThat(result.isInvalid()).isTrue();
        }
    }

    @Nested
    @DisplayName("password 검증")
    class PasswordValidation {
        private CompositeFieldValidator<String> passwordValidator;

        @BeforeEach
        void setup() {
            passwordValidator = fieldValidators.password();
        }

        @Test
        @DisplayName("유효한 비밀번호에 성공한다")
        void shouldSucceedForValidPassword() {
            Validation<Seq<FieldViolation>, String> result = passwordValidator.validate("ValidPass1!", FIELD_NAME);
            assertThat(result.isValid()).isTrue();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("null 또는 빈 비밀번호에 실패한다")
        void shouldFailForNullOrEmpty(String password) {
            Validation<Seq<FieldViolation>, String> result = passwordValidator.validate(password, FIELD_NAME);
            assertThat(result.isInvalid()).isTrue();
        }
        
        @Test
        @DisplayName("길이가 8자 미만이면 실패한다")
        void shouldFailForShortPassword() {
            Validation<Seq<FieldViolation>, String> result = passwordValidator.validate("Vp1!", FIELD_NAME);
            assertThat(result.isInvalid()).isTrue();
        }

        @Test
        @DisplayName("길이가 20자를 초과하면 실패한다")
        void shouldFailForLongPassword() {
            Validation<Seq<FieldViolation>, String> result = passwordValidator.validate("VeryLongValidPassword1!", FIELD_NAME);
            assertThat(result.isInvalid()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"onlyletters", "onlynumbers123", "onlysymbols!@#", "NoSymbol123", "noletter1!", "NONUMBER$!"})
        @DisplayName("패턴 요구사항을 만족하지 못하면 실패한다")
        void shouldFailForPatternMismatch(String invalidPassword) {
            Validation<Seq<FieldViolation>, String> result = passwordValidator.validate(invalidPassword, FIELD_NAME);
            assertThat(result.isInvalid()).isTrue();
        }
    }

    @Nested
    @DisplayName("position 검증")
    class PositionValidation {
        private CompositeFieldValidator<Integer> positionValidator;
        
        @BeforeEach
        void setup() {
            positionValidator = fieldValidators.position();
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 100})
        @DisplayName("0 이상의 정수에 대해 성공한다")
        void shouldSucceedForValidPosition(int position) {
            Validation<Seq<FieldViolation>, Integer> result = positionValidator.validate(position, FIELD_NAME);
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("null 값에 대해 실패한다")
        void shouldFailForNull() {
            Validation<Seq<FieldViolation>, Integer> result = positionValidator.validate(null, FIELD_NAME);
            assertThat(result.isInvalid()).isTrue();
        }
        
        @Test
        @DisplayName("음수 값에 대해 실패한다")
        void shouldFailForNegativePosition() {
            Validation<Seq<FieldViolation>, Integer> result = positionValidator.validate(-1, FIELD_NAME);
            assertThat(result.isInvalid()).isTrue();
        }
    }
    
    @Nested
    @DisplayName("color 검증")
    class ColorValidation {
        private CompositeFieldValidator<String> colorValidator;
        
        @BeforeEach
        void setup() {
            colorValidator = fieldValidators.color();
        }

        @ParameterizedTest
        @ValueSource(strings = {"#ff0000", "#FFF", "#123456"})
        @DisplayName("유효한 HEX 색상 코드에 성공한다")
        void shouldSucceedForValidColor(String color) {
            Validation<Seq<FieldViolation>, String> result = colorValidator.validate(color, FIELD_NAME);
            assertThat(result.isValid()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"#ff00", "FFF", "123456", "#GGGGGG"})
        @DisplayName("유효하지 않은 HEX 색상 코드에 실패한다")
        void shouldFailForInvalidColor(String color) {
            Validation<Seq<FieldViolation>, String> result = colorValidator.validate(color, FIELD_NAME);
            assertThat(result.isInvalid()).isTrue();
        }
    }
} 
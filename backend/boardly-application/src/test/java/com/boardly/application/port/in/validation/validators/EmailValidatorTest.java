package com.boardly.application.port.in.validation.validators;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.boardly.application.port.in.traits.HasEmail;
import com.boardly.application.port.in.validation.ValidationResult;
import com.boardly.domain.user.Email;

import static org.assertj.core.api.Assertions.*;

/**
 * EmailValidator 테스트
 * <p>
 * validate() 메서드와 validateEmailString() 메서드 테스트를 포함합니다.
 * 각 메서드별로 중첩 클래스로 분리하여 테스트 코드를 구성합니다.
 * </p>
 */
@DisplayName("EmailValidator 테스트")
class EmailValidatorTest {

    /**
     * HasEmail trait를 구현한 테스트용 객체
     */
    private record TestCommand(Email email) implements HasEmail {
    }

    @Nested
    @DisplayName("validate() 메서드 테스트")
    class ValidateMethodTest {

        @Test
        @DisplayName("유효한 이메일 - 성공")
        void givenValidEmail_whenValidate_thenReturnsValidResult() {
            // given
            var email = Email.of("test@example.com");
            var command = new TestCommand(email);

            // when
            ValidationResult<TestCommand> result = EmailValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("null 이메일 - 실패")
        void givenNullEmail_whenValidate_thenReturnsInvalidResult() {
            // given
            var command = new TestCommand(null);

            // when & then
            assertThatThrownBy(() -> EmailValidator.<TestCommand>validate().validate(command))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("빈 문자열 이메일 - 실패")
        void givenEmptyEmail_whenValidate_thenReturnsInvalidResult() {
            // given
            var email = Email.of("");
            var command = new TestCommand(email);

            // when
            ValidationResult<TestCommand> result = EmailValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("email");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
        }

        @Test
        @DisplayName("공백만 있는 이메일 - 실패")
        void givenBlankEmail_whenValidate_thenReturnsInvalidResult() {
            // given
            var email = Email.of("   ");
            var command = new TestCommand(email);

            // when
            ValidationResult<TestCommand> result = EmailValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("email");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
        }

        @Test
        @DisplayName("너무 짧은 이메일 - 실패")
        void givenTooShortEmail_whenValidate_thenReturnsInvalidResult() {
            // given
            var email = Email.of("a@b");
            var command = new TestCommand(email);

            // when
            ValidationResult<TestCommand> result = EmailValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("email");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.minLength");
        }

        @Test
        @DisplayName("너무 긴 이메일 - 실패")
        void givenTooLongEmail_whenValidate_thenReturnsInvalidResult() {
            // given
            String longLocalPart = "a".repeat(250);
            var email = Email.of(longLocalPart + "@example.com");
            var command = new TestCommand(email);

            // when
            ValidationResult<TestCommand> result = EmailValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("email");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.maxLength");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "invalid-email",
                "test@",
                "@example.com",
                "test.example.com",
                "test@example"
        })
        @DisplayName("잘못된 이메일 형식 - 실패")
        void givenInvalidEmailFormat_whenValidate_thenReturnsInvalidResult(String invalidEmail) {
            // given
            var email = Email.of(invalidEmail);
            var command = new TestCommand(email);

            // when
            ValidationResult<TestCommand> result = EmailValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("email");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.pattern");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "test@example.com",
                "user.name@example.co.kr",
                "test+tag@example.com",
                "user_name@example-domain.com",
                "123@example.com",
                "a@b.co"
        })
        @DisplayName("유효한 이메일 형식들 - 성공")
        void givenValidEmailFormats_whenValidate_thenReturnsValidResult(String validEmail) {
            // given
            var email = Email.of(validEmail);
            var command = new TestCommand(email);

            // when
            ValidationResult<TestCommand> result = EmailValidator.<TestCommand>validate().validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }
    }

    @Nested
    @DisplayName("validateEmailString() 메서드 테스트")
    class ValidateEmailStringMethodTest {

        @Test
        @DisplayName("유효한 이메일 문자열 - 성공")
        void givenValidEmailString_whenValidateEmailString_thenReturnsValidResult() {
            // given
            String emailString = "test@example.com";

            // when
            ValidationResult<String> result = EmailValidator.validateEmailString().validate(emailString);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(emailString);
        }

        @Test
        @DisplayName("null 이메일 문자열 - 실패")
        void givenNullEmailString_whenValidateEmailString_thenReturnsInvalidResult() {
            // given
            String emailString = null;

            // when
            ValidationResult<String> result = EmailValidator.validateEmailString().validate(emailString);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("email");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
        }

        @Test
        @DisplayName("빈 이메일 문자열 - 실패")
        void givenEmptyEmailString_whenValidateEmailString_thenReturnsInvalidResult() {
            // given
            String emailString = "";

            // when
            ValidationResult<String> result = EmailValidator.validateEmailString().validate(emailString);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("email");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
        }

        @Test
        @DisplayName("공백만 있는 이메일 문자열 - 실패")
        void givenBlankEmailString_whenValidateEmailString_thenReturnsInvalidResult() {
            // given
            String emailString = "   ";

            // when
            ValidationResult<String> result = EmailValidator.validateEmailString().validate(emailString);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("email");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
        }

        @Test
        @DisplayName("너무 짧은 이메일 문자열 - 실패")
        void givenTooShortEmailString_whenValidateEmailString_thenReturnsInvalidResult() {
            // given
            String emailString = "a@b";

            // when
            ValidationResult<String> result = EmailValidator.validateEmailString().validate(emailString);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("email");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.minLength");
        }

        @Test
        @DisplayName("너무 긴 이메일 문자열 - 실패")
        void givenTooLongEmailString_whenValidateEmailString_thenReturnsInvalidResult() {
            // given
            String longLocalPart = "a".repeat(250);
            String emailString = longLocalPart + "@example.com";

            // when
            ValidationResult<String> result = EmailValidator.validateEmailString().validate(emailString);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("email");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.maxLength");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "invalid-email",
                "test@",
                "@example.com",
                "test.example.com",
                "test@example",
                "test..test@example.com",
                "test@example..com"
        })
        @DisplayName("잘못된 이메일 형식 문자열 - 실패")
        void givenInvalidEmailFormatString_whenValidateEmailString_thenReturnsInvalidResult(String invalidEmail) {
            // when
            ValidationResult<String> result = EmailValidator.validateEmailString().validate(invalidEmail);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("email");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.pattern");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "test@example.com",
                "user.name@example.co.kr",
                "test+tag@example.com",
                "user_name@example-domain.com",
                "123@example.com",
                "a@b.co"
        })
        @DisplayName("유효한 이메일 형식 문자열들 - 성공")
        void givenValidEmailFormatStrings_whenValidateEmailString_thenReturnsValidResult(String validEmail) {
            // when
            ValidationResult<String> result = EmailValidator.validateEmailString().validate(validEmail);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(validEmail);
        }

        @Test
        @DisplayName("경계값 테스트 - 최소 길이 이메일")
        void givenMinLengthEmail_whenValidateEmailString_thenReturnsValidResult() {
            // given - 5자리 최소 길이 이메일
            String emailString = "a@b.c";

            // when
            ValidationResult<String> result = EmailValidator.validateEmailString().validate(emailString);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(emailString);
        }

        @Test
        @DisplayName("경계값 테스트 - 최대 길이 이메일")
        void givenMaxLengthEmail_whenValidateEmailString_thenReturnsValidResult() {
            // given - 254자리 최대 길이 이메일
            String localPart = "a".repeat(245); // 245 + "@b.co" = 254
            String emailString = localPart + "@b.co";

            // when
            ValidationResult<String> result = EmailValidator.validateEmailString().validate(emailString);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(emailString);
        }
    }
}
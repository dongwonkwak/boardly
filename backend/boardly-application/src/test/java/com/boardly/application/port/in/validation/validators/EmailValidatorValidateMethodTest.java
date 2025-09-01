package com.boardly.application.port.in.validation.validators;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.boardly.application.port.in.traits.HasEmail;
import com.boardly.application.port.in.validation.ValidationResult;
import com.boardly.domain.user.Email;

import static org.assertj.core.api.Assertions.*;

/**
 * EmailValidator.validate() 메서드 전용 테스트
 * <p>
 * HasEmail trait를 구현한 객체를 검증하는 validate() 메서드의 모든 테스트 케이스를 포함합니다.
 * </p>
 */
@DisplayName("EmailValidator.validate() 메서드 테스트")
class EmailValidatorValidateMethodTest {

    /**
     * HasEmail trait를 구현한 테스트용 객체
     */
    private record TestCommand(Email email) implements HasEmail {
    }

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
        assertThat(result.get().email()).isEqualTo(email);
    }

    @Test
    @DisplayName("null 이메일 - NullPointerException 발생")
    void givenNullEmail_whenValidate_thenThrowsNullPointerException() {
        // given
        var command = new TestCommand(null);

        // when & then
        assertThatThrownBy(() -> EmailValidator.<TestCommand>validate().validate(command))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("빈 문자열 이메일 - notBlank 검증 실패")
    void givenEmptyEmail_whenValidate_thenReturnsNotBlankValidationError() {
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
        assertThat(result.getErrors().get(0).getRejectedValue()).isEqualTo("");
    }

    @Test
    @DisplayName("공백만 있는 이메일 - notBlank 검증 실패")
    void givenBlankEmail_whenValidate_thenReturnsNotBlankValidationError() {
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
        assertThat(result.getErrors().get(0).getRejectedValue()).isEqualTo("");
    }

    @Test
    @DisplayName("너무 짧은 이메일 - minLength 검증 실패")
    void givenTooShortEmail_whenValidate_thenReturnsMinLengthValidationError() {
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
        assertThat(result.getErrors().get(0).getRejectedValue()).isEqualTo("a@b");
        assertThat(result.getErrors().get(0).getMessageArgs()).containsExactly("field.email", 5);
    }

    @Test
    @DisplayName("정확히 최소 길이인 이메일 - 성공")
    void givenExactMinLengthEmail_whenValidate_thenReturnsValidResult() {
        // given - 5자리 정확히 최소 길이
        var email = Email.of("a@b.c");
        var command = new TestCommand(email);

        // when
        ValidationResult<TestCommand> result = EmailValidator.<TestCommand>validate().validate(command);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.get()).isEqualTo(command);
    }

    @Test
    @DisplayName("너무 긴 이메일 - maxLength 검증 실패")
    void givenTooLongEmail_whenValidate_thenReturnsMaxLengthValidationError() {
        // given - 255자 이메일 (최대 254자 초과)
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
        assertThat(result.getErrors().get(0).getMessageArgs()).containsExactly("field.email", 254);
    }

    @Test
    @DisplayName("정확히 최대 길이인 이메일 - 성공")
    void givenExactMaxLengthEmail_whenValidate_thenReturnsValidResult() {
        // given - 254자 정확히 최대 길이
        String localPart = "a".repeat(245); // 245 + "@b.co" = 254
        var email = Email.of(localPart + "@b.co");
        var command = new TestCommand(email);

        // when
        ValidationResult<TestCommand> result = EmailValidator.<TestCommand>validate().validate(command);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.get()).isEqualTo(command);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid-email", // @ 없음
            "test@", // 도메인 없음
            "@example.com", // 로컬 부분 없음
            "test.example.com", // @ 없음
            "test@example", // TLD 없음

    })
    @DisplayName("잘못된 이메일 형식 - pattern 검증 실패")
    void givenInvalidEmailFormat_whenValidate_thenReturnsPatternValidationError(String invalidEmail) {
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
        assertThat(result.getErrors().get(0).getRejectedValue()).isEqualTo(invalidEmail);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "test@example.com",
            "user.name@example.co.kr",
            "test+tag@example.com",
            "user_name@example-domain.com",
            "123@example.com",
            "test123@example123.com",
            "a@b.co",
            "very.long.email.address@very-long-domain-name.example.com"
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
        assertThat(result.get().email().value()).isEqualTo(validEmail);
    }

    @Test
    @DisplayName("이메일 대소문자 정규화 테스트")
    void givenMixedCaseEmail_whenValidate_thenReturnsValidResultWithLowercase() {
        // given
        var email = Email.of("Test.User@EXAMPLE.COM");
        var command = new TestCommand(email);

        // when
        ValidationResult<TestCommand> result = EmailValidator.<TestCommand>validate().validate(command);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.get().email().value()).isEqualTo("test.user@example.com");
    }

    @Test
    @DisplayName("이메일 공백 제거 테스트")
    void givenEmailWithSpaces_whenValidate_thenReturnsValidResultWithTrimmedEmail() {
        // given
        var email = Email.of("  test@example.com  ");
        var command = new TestCommand(email);

        // when
        ValidationResult<TestCommand> result = EmailValidator.<TestCommand>validate().validate(command);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.get().email().value()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("체이닝 검증 순서 테스트 - 첫 번째 실패에서 중단")
    void givenEmailFailingMultipleValidations_whenValidate_thenReturnsFirstFailure() {
        // given - 빈 문자열 (notBlank, minLength, pattern 모두 실패하지만 첫 번째만 반환)
        var email = Email.of("");
        var command = new TestCommand(email);

        // when
        ValidationResult<TestCommand> result = EmailValidator.<TestCommand>validate().validate(command);

        // then
        assertThat(result.isInvalid()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
    }
}
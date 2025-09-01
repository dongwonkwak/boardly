package com.boardly.application.port.in.validation.validators;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.boardly.application.port.in.validation.ValidationResult;

import static org.assertj.core.api.Assertions.*;

/**
 * EmailValidator.validateEmailString() 메서드 전용 테스트
 * <p>
 * 이메일 문자열을 직접 검증하는 validateEmailString() 메서드의 모든 테스트 케이스를 포함합니다.
 * </p>
 */
@DisplayName("EmailValidator.validateEmailString() 메서드 테스트")
class EmailValidatorValidateEmailStringMethodTest {

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
    @DisplayName("null 이메일 문자열 - notBlank 검증 실패")
    void givenNullEmailString_whenValidateEmailString_thenReturnsNotBlankValidationError() {
        // given
        String emailString = null;

        // when
        ValidationResult<String> result = EmailValidator.validateEmailString().validate(emailString);

        // then
        assertThat(result.isInvalid()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getField()).isEqualTo("email");
        assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
        assertThat(result.getErrors().get(0).getRejectedValue()).isNull();
    }

    @Test
    @DisplayName("빈 이메일 문자열 - notBlank 검증 실패")
    void givenEmptyEmailString_whenValidateEmailString_thenReturnsNotBlankValidationError() {
        // given
        String emailString = "";

        // when
        ValidationResult<String> result = EmailValidator.validateEmailString().validate(emailString);

        // then
        assertThat(result.isInvalid()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getField()).isEqualTo("email");
        assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
        assertThat(result.getErrors().get(0).getRejectedValue()).isEqualTo("");
    }

    @Test
    @DisplayName("공백만 있는 이메일 문자열 - notBlank 검증 실패")
    void givenBlankEmailString_whenValidateEmailString_thenReturnsNotBlankValidationError() {
        // given
        String emailString = "   ";

        // when
        ValidationResult<String> result = EmailValidator.validateEmailString().validate(emailString);

        // then
        assertThat(result.isInvalid()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getField()).isEqualTo("email");
        assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
        assertThat(result.getErrors().get(0).getRejectedValue()).isEqualTo("   ");
    }

    @Test
    @DisplayName("너무 짧은 이메일 문자열 - minLength 검증 실패")
    void givenTooShortEmailString_whenValidateEmailString_thenReturnsMinLengthValidationError() {
        // given
        String emailString = "a@b";

        // when
        ValidationResult<String> result = EmailValidator.validateEmailString().validate(emailString);

        // then
        assertThat(result.isInvalid()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getField()).isEqualTo("email");
        assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.minLength");
        assertThat(result.getErrors().get(0).getRejectedValue()).isEqualTo("a@b");
        assertThat(result.getErrors().get(0).getMessageArgs()).containsExactly("field.email", 5);
    }

    @Test
    @DisplayName("정확히 최소 길이인 이메일 문자열 - 성공")
    void givenExactMinLengthEmailString_whenValidateEmailString_thenReturnsValidResult() {
        // given - 5자리 정확히 최소 길이
        String emailString = "a@b.c";

        // when
        ValidationResult<String> result = EmailValidator.validateEmailString().validate(emailString);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.get()).isEqualTo(emailString);
    }

    @Test
    @DisplayName("너무 긴 이메일 문자열 - maxLength 검증 실패")
    void givenTooLongEmailString_whenValidateEmailString_thenReturnsMaxLengthValidationError() {
        // given - 255자 이메일 (최대 254자 초과)
        String longLocalPart = "a".repeat(250);
        String emailString = longLocalPart + "@example.com";

        // when
        ValidationResult<String> result = EmailValidator.validateEmailString().validate(emailString);

        // then
        assertThat(result.isInvalid()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getField()).isEqualTo("email");
        assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.maxLength");
        assertThat(result.getErrors().get(0).getRejectedValue()).isEqualTo(emailString);
        assertThat(result.getErrors().get(0).getMessageArgs()).containsExactly("field.email", 254);
    }

    @Test
    @DisplayName("정확히 최대 길이인 이메일 문자열 - 성공")
    void givenExactMaxLengthEmailString_whenValidateEmailString_thenReturnsValidResult() {
        // given - 254자 정확히 최대 길이
        String localPart = "a".repeat(245); // 245 + "@b.co" = 254
        String emailString = localPart + "@b.co";

        // when
        ValidationResult<String> result = EmailValidator.validateEmailString().validate(emailString);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.get()).isEqualTo(emailString);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid-email", // @ 없음
            "test@", // 도메인 없음
            "@example.com", // 로컬 부분 없음
            "test.example.com", // @ 없음
            "test@example", // TLD 없음
            "test@", // 도메인 없음
            "test@@example.com", // 중복 @
            "test@exam ple.com" // 공백 포함
    })
    @DisplayName("잘못된 이메일 형식 문자열 - pattern 검증 실패")
    void givenInvalidEmailFormatString_whenValidateEmailString_thenReturnsPatternValidationError(String invalidEmail) {
        // when
        ValidationResult<String> result = EmailValidator.validateEmailString().validate(invalidEmail);

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
            "very.long.email.address@very-long-domain-name.example.com",
            "user+tag+more@example.com",
            "test.email.with+symbol@example.com",
            "numeric123+test@subdomain.example.co.kr"
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
    @DisplayName("경계값 테스트 - 4자리 이메일 (최소길이-1)")
    void givenBelowMinLengthEmail_whenValidateEmailString_thenReturnsMinLengthValidationError() {
        // given - 4자리 이메일 (최소 5자 미만)
        String emailString = "a@bc";

        // when
        ValidationResult<String> result = EmailValidator.validateEmailString().validate(emailString);

        // then
        assertThat(result.isInvalid()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.minLength");
    }

    @Test
    @DisplayName("경계값 테스트 - 255자리 이메일 (최대길이+1)")
    void givenAboveMaxLengthEmail_whenValidateEmailString_thenReturnsMaxLengthValidationError() {
        // given - 255자리 이메일 (최대 254자 초과)
        String localPart = "a".repeat(250); // 250 + "@b.co" = 255
        String emailString = localPart + "@b.co";

        // when
        ValidationResult<String> result = EmailValidator.validateEmailString().validate(emailString);

        // then
        assertThat(result.isInvalid()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.maxLength");
    }

    @Test
    @DisplayName("특수 문자 포함 유효한 이메일")
    void givenEmailWithSpecialCharacters_whenValidateEmailString_thenReturnsValidResult() {
        // given
        String emailString = "test+tag.name_123@sub-domain.example-site.co.kr";

        // when
        ValidationResult<String> result = EmailValidator.validateEmailString().validate(emailString);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.get()).isEqualTo(emailString);
    }

    @Test
    @DisplayName("숫자로만 구성된 유효한 이메일")
    void givenNumericEmail_whenValidateEmailString_thenReturnsValidResult() {
        // given
        String emailString = "123456@123456.co";

        // when
        ValidationResult<String> result = EmailValidator.validateEmailString().validate(emailString);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.get()).isEqualTo(emailString);
    }

    @Test
    @DisplayName("체이닝 검증 순서 테스트 - 첫 번째 실패에서 중단")
    void givenEmailStringFailingMultipleValidations_whenValidateEmailString_thenReturnsFirstFailure() {
        // given - 빈 문자열 (notBlank, minLength, pattern 모두 실패하지만 첫 번째만 반환)
        String emailString = "";

        // when
        ValidationResult<String> result = EmailValidator.validateEmailString().validate(emailString);

        // then
        assertThat(result.isInvalid()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
    }

    @Test
    @DisplayName("탭 문자를 포함한 공백 문자열 - notBlank 검증 실패")
    void givenTabAndSpaceString_whenValidateEmailString_thenReturnsNotBlankValidationError() {
        // given
        String emailString = "\t \n ";

        // when
        ValidationResult<String> result = EmailValidator.validateEmailString().validate(emailString);

        // then
        assertThat(result.isInvalid()).isTrue();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
    }
}
package com.boardly.application.port.in.validation.validators;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.boardly.application.port.in.validation.ValidationResult;

/**
 * PasswordValidator 테스트
 */
@DisplayName("PasswordValidator 테스트")
class PasswordValidatorTest {

    @Nested
    @DisplayName("validatePasswordString() 메서드 테스트")
    class ValidatePasswordStringMethodTest {

        @Test
        @DisplayName("유효한 비밀번호 문자열 - 성공")
        void givenValidPasswordString_whenValidatePasswordString_thenReturnsValidResult() {
            // given
            String passwordString = "Password123!";

            // when
            ValidationResult<String> result = PasswordValidator.validatePasswordString().validate(passwordString);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(passwordString);
        }

        @Test
        @DisplayName("null 비밀번호 문자열 - notBlank 검증 실패")
        void givenNullPasswordString_whenValidatePasswordString_thenReturnsNotBlankValidationError() {
            // when
            ValidationResult<String> result = PasswordValidator.validatePasswordString().validate(null);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("password");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
            assertThat(result.getErrors().get(0).getMessageArgs()).containsExactly("field.password");
        }

        @Test
        @DisplayName("빈 비밀번호 문자열 - notBlank 검증 실패")
        void givenEmptyPasswordString_whenValidatePasswordString_thenReturnsNotBlankValidationError() {
            // given
            String passwordString = "";

            // when
            ValidationResult<String> result = PasswordValidator.validatePasswordString().validate(passwordString);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("password");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
            assertThat(result.getErrors().get(0).getMessageArgs()).containsExactly("field.password");
        }

        @Test
        @DisplayName("공백만 있는 비밀번호 문자열 - notBlank 검증 실패")
        void givenWhitespaceOnlyPasswordString_whenValidatePasswordString_thenReturnsNotBlankValidationError() {
            // given
            String passwordString = "   ";

            // when
            ValidationResult<String> result = PasswordValidator.validatePasswordString().validate(passwordString);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("password");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
            assertThat(result.getErrors().get(0).getMessageArgs()).containsExactly("field.password");
        }

        @Test
        @DisplayName("너무 짧은 비밀번호 문자열 - minLength 검증 실패")
        void givenTooShortPasswordString_whenValidatePasswordString_thenReturnsMinLengthValidationError() {
            // given
            String passwordString = "Pass1!";

            // when
            ValidationResult<String> result = PasswordValidator.validatePasswordString().validate(passwordString);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("password");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.minLength");
            assertThat(result.getErrors().get(0).getRejectedValue()).isEqualTo(passwordString);
            assertThat(result.getErrors().get(0).getMessageArgs()).containsExactly("field.password", 8);
        }

        @Test
        @DisplayName("정확히 최소 길이인 비밀번호 문자열 - 성공")
        void givenExactMinLengthPasswordString_whenValidatePasswordString_thenReturnsValidResult() {
            // given - 8자리 정확히 최소 길이
            String passwordString = "Pass1!@#";

            // when
            ValidationResult<String> result = PasswordValidator.validatePasswordString().validate(passwordString);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(passwordString);
        }

        @Test
        @DisplayName("너무 긴 비밀번호 문자열 - maxLength 검증 실패")
        void givenTooLongPasswordString_whenValidatePasswordString_thenReturnsMaxLengthValidationError() {
            // given - 129자 비밀번호 (최대 128자 초과)
            String longPassword = "A".repeat(125) + "1!@#";

            // when
            ValidationResult<String> result = PasswordValidator.validatePasswordString().validate(longPassword);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("password");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.maxLength");
            assertThat(result.getErrors().get(0).getRejectedValue()).isEqualTo(longPassword);
            assertThat(result.getErrors().get(0).getMessageArgs()).containsExactly("field.password", 128);
        }

        @Test
        @DisplayName("정확히 최대 길이인 비밀번호 문자열 - 성공")
        void givenExactMaxLengthPasswordString_whenValidatePasswordString_thenReturnsValidResult() {
            // given - 128자 정확히 최대 길이
            String longPassword = "A".repeat(122) + "a1!@";

            // when
            ValidationResult<String> result = PasswordValidator.validatePasswordString().validate(longPassword);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(longPassword);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "password123", // 영문자와 숫자만 (특수문자 없음)
                "PASSWORD123!", // 대문자, 숫자, 특수문자 (소문자 없음)
                "password!", // 영문자와 특수문자 (숫자 없음)
                "12345678!", // 숫자와 특수문자 (영문자 없음)
                "Password", // 영문자만 (숫자, 특수문자 없음)
                "12345678", // 숫자만 (영문자, 특수문자 없음)
                "!@#$%^&*", // 특수문자만 (영문자, 숫자 없음)
                "Password123", // 영문자와 숫자 (특수문자 없음)
                "Password!", // 영문자와 특수문자 (숫자 없음)
                "12345678!", // 숫자와 특수문자 (영문자 없음)
                "Pass 123!", // 공백 포함
                "Pass\t123!", // 탭 문자 포함
                "Pass\n123!", // 개행 문자 포함
                "Pass123!한글", // 한글 포함
                "Pass123`", // 백틱 포함 (허용되지 않는 특수문자)
                "Pass123~", // 틸드 포함 (허용되지 않는 특수문자)
                "Pass123§", // 섹션 기호 포함 (허용되지 않는 특수문자)
                "Pass123€", // 유로 기호 포함 (허용되지 않는 특수문자)
                "Pass123£", // 파운드 기호 포함 (허용되지 않는 특수문자)
                "Pass123¥", // 엔 기호 포함 (허용되지 않는 특수문자)
                "Pass123©", // 저작권 기호 포함 (허용되지 않는 특수문자)
                "Pass123®", // 등록 기호 포함 (허용되지 않는 특수문자)
                "Pass123™", // 상표 기호 포함 (허용되지 않는 특수문자)
                "Pass123°", // 도 기호 포함 (허용되지 않는 특수문자)
                "Pass123±", // 플러스마이너스 기호 포함 (허용되지 않는 특수문자)
                "Pass123×", // 곱셈 기호 포함 (허용되지 않는 특수문자)
                "Pass123÷", // 나눗셈 기호 포함 (허용되지 않는 특수문자)
                "Pass123∞", // 무한대 기호 포함 (허용되지 않는 특수문자)
                "Pass123∑", // 합계 기호 포함 (허용되지 않는 특수문자)
                "Pass123∏", // 곱 기호 포함 (허용되지 않는 특수문자)
                "Pass123√", // 제곱근 기호 포함 (허용되지 않는 특수문자)
                "Pass123∫", // 적분 기호 포함 (허용되지 않는 특수문자)
                "Pass123∂", // 편미분 기호 포함 (허용되지 않는 특수문자)
                "Pass123∇", // 나블라 기호 포함 (허용되지 않는 특수문자)
                "Pass123∆", // 델타 기호 포함 (허용되지 않는 특수문자)
                "Pass123π", // 파이 기호 포함 (허용되지 않는 특수문자)
                "Pass123α", // 알파 기호 포함 (허용되지 않는 특수문자)
                "Pass123β", // 베타 기호 포함 (허용되지 않는 특수문자)
                "Pass123γ", // 감마 기호 포함 (허용되지 않는 특수문자)
                "Pass123δ", // 델타 기호 포함 (허용되지 않는 특수문자)
                "Pass123ε", // 엡실론 기호 포함 (허용되지 않는 특수문자)
                "Pass123ζ", // 제타 기호 포함 (허용되지 않는 특수문자)
                "Pass123η", // 에타 기호 포함 (허용되지 않는 특수문자)
                "Pass123θ", // 세타 기호 포함 (허용되지 않는 특수문자)
                "Pass123λ", // 람다 기호 포함 (허용되지 않는 특수문자)
                "Pass123μ", // 뮤 기호 포함 (허용되지 않는 특수문자)
                "Pass123ξ", // 크시 기호 포함 (허용되지 않는 특수문자)
                "Pass123ρ", // 로 기호 포함 (허용되지 않는 특수문자)
                "Pass123σ", // 시그마 기호 포함 (허용되지 않는 특수문자)
                "Pass123τ", // 타우 기호 포함 (허용되지 않는 특수문자)
                "Pass123φ", // 파이 기호 포함 (허용되지 않는 특수문자)
                "Pass123χ", // 카이 기호 포함 (허용되지 않는 특수문자)
                "Pass123ψ", // 프시 기호 포함 (허용되지 않는 특수문자)
                "Pass123ω" // 오메가 기호 포함 (허용되지 않는 특수문자)
        })
        @DisplayName("잘못된 비밀번호 형식 문자열 - pattern 검증 실패")
        void givenInvalidPasswordFormatString_whenValidatePasswordString_thenReturnsPatternValidationError(String invalidPassword) {
            // when
            ValidationResult<String> result = PasswordValidator.validatePasswordString().validate(invalidPassword);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("password");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.pattern");
            assertThat(result.getErrors().get(0).getRejectedValue()).isEqualTo(invalidPassword);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "Password123!",
                "MySecure1@",
                "Test123#",
                "Spring4?",
                "Strong7!",
                "Complex8#",
                "Secure9$",
                "NewPass1^",
                "TestPass2(",
                "MyPass3)",
                "UserPass4_",
                "LoginPass5+",
                "SecurePass6-",
                "ValidPass7=",
                "StrongPass8[",
                "ComplexPass9]",
                "SafePass0{",
                "GoodPass1}",
                "BestPass2|",
                "TopPass3:",
                "NicePass4;",
                "CoolPass5\"",
                "WarmPass6'",
                "HotPass7<",
                "ColdPass8>",
                "FastPass9,",
                "SlowPass0.",
                "QuickPass1/"
        })
        @DisplayName("유효한 비밀번호 형식 문자열들 - 성공")
        void givenValidPasswordFormatStrings_whenValidatePasswordString_thenReturnsValidResult(String validPassword) {
            // when
            ValidationResult<String> result = PasswordValidator.validatePasswordString().validate(validPassword);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(validPassword);
        }

        @Test
        @DisplayName("체이닝 검증 순서 테스트 - 첫 번째 실패에서 중단")
        void givenInvalidPassword_whenValidatePasswordString_thenStopsAtFirstFailure() {
            // given - 빈 문자열 (notBlank에서 실패)
            String invalidPassword = "";

            // when
            ValidationResult<String> result = PasswordValidator.validatePasswordString().validate(invalidPassword);

            // then - notBlank 검증에서 실패하고 minLength, maxLength, pattern 검증은 실행되지 않음
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.required");
        }
    }

    @Nested
    @DisplayName("validate() 메서드 테스트")
    class ValidateMethodTest {

        @Test
        @DisplayName("유효한 비밀번호 - 성공")
        void givenValidPassword_whenValidate_thenReturnsValidResult() {
            // given
            TestAuthenticatable authenticatable = new TestAuthenticatable("Password123!");

            // when
            ValidationResult<?> result = PasswordValidator.validate().validate(authenticatable);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(authenticatable);
        }

        @Test
        @DisplayName("빈 문자열 비밀번호 - Password 생성 시 예외 발생")
        void givenEmptyPassword_whenCreateTestAuthenticatable_thenThrowsException() {
            // given & when & then
            assertThat(org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
                new TestAuthenticatable("");
            })).hasMessage("Password hash cannot be null or empty");
        }

        @Test
        @DisplayName("너무 짧은 비밀번호 - minLength 검증 실패")
        void givenTooShortPassword_whenValidate_thenReturnsMinLengthValidationError() {
            // given
            TestAuthenticatable authenticatable = new TestAuthenticatable("Pass1!");

            // when
            ValidationResult<?> result = PasswordValidator.validate().validate(authenticatable);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("password");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.minLength");
            assertThat(result.getErrors().get(0).getMessageArgs()).containsExactly("field.password", 8);
        }

        @Test
        @DisplayName("너무 긴 비밀번호 - maxLength 검증 실패")
        void givenTooLongPassword_whenValidate_thenReturnsMaxLengthValidationError() {
            // given
            String longPassword = "A".repeat(125) + "1!@#";
            TestAuthenticatable authenticatable = new TestAuthenticatable(longPassword);

            // when
            ValidationResult<?> result = PasswordValidator.validate().validate(authenticatable);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("password");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.maxLength");
            assertThat(result.getErrors().get(0).getMessageArgs()).containsExactly("field.password", 128);
        }

        @Test
        @DisplayName("잘못된 비밀번호 형식 - pattern 검증 실패")
        void givenInvalidPasswordFormat_whenValidate_thenReturnsPatternValidationError() {
            // given
            TestAuthenticatable authenticatable = new TestAuthenticatable("password123");

            // when
            ValidationResult<?> result = PasswordValidator.validate().validate(authenticatable);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("password");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.pattern");
        }

        @Test
        @DisplayName("공백만 있는 비밀번호 - Password 생성 시 예외 발생")
        void givenWhitespaceOnlyPassword_whenCreateTestAuthenticatable_thenThrowsException() {
            // given & when & then
            assertThat(org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
                new TestAuthenticatable("   ");
            })).hasMessage("Password hash cannot be null or empty");
        }

        @Test
        @DisplayName("정확히 최소 길이인 비밀번호 - 성공")
        void givenExactMinLengthPassword_whenValidate_thenReturnsValidResult() {
            // given - 8자리 정확히 최소 길이
            TestAuthenticatable authenticatable = new TestAuthenticatable("Pass1!@#");

            // when
            ValidationResult<?> result = PasswordValidator.validate().validate(authenticatable);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(authenticatable);
        }

        @Test
        @DisplayName("정확히 최대 길이인 비밀번호 - 성공")
        void givenExactMaxLengthPassword_whenValidate_thenReturnsValidResult() {
            // given - 128자 정확히 최대 길이
            String longPassword = "A".repeat(122) + "a1!@";
            TestAuthenticatable authenticatable = new TestAuthenticatable(longPassword);

            // when
            ValidationResult<?> result = PasswordValidator.validate().validate(authenticatable);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(authenticatable);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "Password123!",
                "MySecure1@",
                "Test123#",
                "Spring4?",
                "NewPass1^",
                "TestPass2(",
                "MyPass3)",
                "UserPass4_",
                "LoginPass5+",
                "SecurePass6-",
                "ValidPass7=",
                "StrongPass8[",
                "ComplexPass9]",
                "SafePass0{",
                "GoodPass1}",
                "BestPass2|",
                "TopPass3:",
                "NicePass4;",
                "CoolPass5\"",
                "WarmPass6'",
                "HotPass7<",
                "ColdPass8>",
                "FastPass9,",
                "SlowPass0.",
                "QuickPass1/"
        })
        @DisplayName("유효한 비밀번호 형식들 - 성공")
        void givenValidPasswordFormats_whenValidate_thenReturnsValidResult(String validPassword) {
            // given
            TestAuthenticatable authenticatable = new TestAuthenticatable(validPassword);

            // when
            ValidationResult<?> result = PasswordValidator.validate().validate(authenticatable);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(authenticatable);
        }
    }

    @Nested
    @DisplayName("validatePasswordStrength() 메서드 테스트")
    class ValidatePasswordStrengthMethodTest {

        @Test
        @DisplayName("유효한 비밀번호 강도 - 성공")
        void givenValidPasswordStrength_whenValidatePasswordStrength_thenReturnsValidResult() {
            // given
            String passwordString = "Password123!";

            // when
            ValidationResult<String> result = PasswordValidator.validatePasswordStrength().validate(passwordString);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(passwordString);
        }

        @Test
        @DisplayName("null 비밀번호 - minLength 검증 실패")
        void givenNullPassword_whenValidatePasswordStrength_thenReturnsMinLengthValidationError() {
            // when
            ValidationResult<String> result = PasswordValidator.validatePasswordStrength().validate(null);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("password");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.minLength");
            assertThat(result.getErrors().get(0).getMessageArgs()).containsExactly("field.password", 8);
        }

        @Test
        @DisplayName("빈 비밀번호 - minLength 검증 실패")
        void givenEmptyPassword_whenValidatePasswordStrength_thenReturnsMinLengthValidationError() {
            // given
            String passwordString = "";

            // when
            ValidationResult<String> result = PasswordValidator.validatePasswordStrength().validate(passwordString);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("password");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.minLength");
            assertThat(result.getErrors().get(0).getMessageArgs()).containsExactly("field.password", 8);
        }

        @Test
        @DisplayName("너무 짧은 비밀번호 - minLength 검증 실패")
        void givenTooShortPassword_whenValidatePasswordStrength_thenReturnsMinLengthValidationError() {
            // given
            String passwordString = "Pass1!";

            // when
            ValidationResult<String> result = PasswordValidator.validatePasswordStrength().validate(passwordString);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("password");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.minLength");
            assertThat(result.getErrors().get(0).getMessageArgs()).containsExactly("field.password", 8);
        }

        @Test
        @DisplayName("너무 긴 비밀번호 - maxLength 검증 실패")
        void givenTooLongPassword_whenValidatePasswordStrength_thenReturnsMaxLengthValidationError() {
            // given - 129자 비밀번호 (최대 128자 초과)
            String longPassword = "A".repeat(125) + "1!@#";

            // when
            ValidationResult<String> result = PasswordValidator.validatePasswordStrength().validate(longPassword);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("password");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.maxLength");
            assertThat(result.getErrors().get(0).getMessageArgs()).containsExactly("field.password", 128);
        }

        @Test
        @DisplayName("잘못된 비밀번호 형식 - pattern 검증 실패")
        void givenInvalidPasswordFormat_whenValidatePasswordStrength_thenReturnsPatternValidationError() {
            // given
            String passwordString = "password123";

            // when
            ValidationResult<String> result = PasswordValidator.validatePasswordStrength().validate(passwordString);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("password");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.pattern");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "Password123!",
                "MySecure1@",
                "Test123#",
                "Spring4?",
                "NewPass1^",
                "TestPass2(",
                "MyPass3)",
                "UserPass4_",
                "LoginPass5+",
                "SecurePass6-",
                "ValidPass7=",
                "StrongPass8[",
                "ComplexPass9]",
                "SafePass0{",
                "GoodPass1}",
                "BestPass2|",
                "TopPass3:",
                "NicePass4;",
                "CoolPass5\"",
                "WarmPass6'",
                "HotPass7<",
                "ColdPass8>",
                "FastPass9,",
                "SlowPass0.",
                "QuickPass1/"
        })
        @DisplayName("유효한 비밀번호 강도들 - 성공")
        void givenValidPasswordStrengths_whenValidatePasswordStrength_thenReturnsValidResult(String validPassword) {
            // when
            ValidationResult<String> result = PasswordValidator.validatePasswordStrength().validate(validPassword);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(validPassword);
        }
    }

    // 테스트용 Authenticatable 구현체
    private static class TestAuthenticatable implements com.boardly.application.port.in.traits.Authenticatable {
        private final com.boardly.domain.user.Password password;

        public TestAuthenticatable(String passwordValue) {
            this.password = com.boardly.domain.user.Password.fromHash(passwordValue);
        }

        @Override
        public com.boardly.domain.user.Password password() {
            return password;
        }
    }
}
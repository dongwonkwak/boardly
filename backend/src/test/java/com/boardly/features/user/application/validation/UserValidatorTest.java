package com.boardly.features.user.application.validation;

import com.boardly.features.user.application.port.input.RegisterUserCommand;
import com.boardly.features.user.application.port.input.UpdateUserCommand;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

    private UserValidator userValidator;

    @Mock
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(Locale.KOREAN);
        lenient().when(messageSource.getMessage(anyString(), any(Object[].class), any(Locale.class)))
            .thenAnswer(invocation -> {
                String code = invocation.getArgument(0);
                Object[] args = invocation.getArgument(1);
                StringBuilder message = new StringBuilder(code);
                if (args != null) {
                    for (Object arg : args) {
                        message.append(" ").append(arg);
                    }
                }
                return message.toString();
        });

        ValidationMessageResolver messageResolver = new ValidationMessageResolver(messageSource);
        CommonValidationRules commonValidationRules = new CommonValidationRules(messageResolver);
        userValidator = new UserValidator(commonValidationRules);
    }

    private RegisterUserCommand createValidCommand() {
        return new RegisterUserCommand(
                "test@example.com",
                "Password123!",
                "Gildong",
                "Hong"
        );
    }

    @Test
    @DisplayName("유효한 사용자 등록 정보는 검증을 통과해야 한다")
    void validateUserRegistration_withValidData_shouldBeValid() {
        // given
        RegisterUserCommand command = createValidCommand();

        // when
        ValidationResult<RegisterUserCommand> result = userValidator.validateUserRegistration(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("이메일이 null이면 검증에 실패해야 한다")
    void validateUserRegistration_withNullEmail_shouldBeInvalid() {
        // given
        RegisterUserCommand command = new RegisterUserCommand(null, "Password123!", "Gildong", "Hong");

        // when
        ValidationResult<RegisterUserCommand> result = userValidator.validateUserRegistration(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("email");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.email.required");
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 검증에 실패해야 한다")
    void validateUserRegistration_withInvalidEmailFormat_shouldBeInvalid() {
        // given
        RegisterUserCommand command = new RegisterUserCommand("invalid-email", "Password123!", "Gildong", "Hong");
        
        // when
        ValidationResult<RegisterUserCommand> result = userValidator.validateUserRegistration(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("email");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.email.pattern");
    }

    @Test
    @DisplayName("이메일 길이가 100자를 초과하면 검증에 실패해야 한다")
    void validateUserRegistration_withTooLongEmail_shouldBeInvalid() {
        // given
        String longEmail = "a".repeat(91) + "@example.com"; // 101 chars
        RegisterUserCommand command = new RegisterUserCommand(longEmail, "Password123!", "Gildong", "Hong");
        
        // when
        ValidationResult<RegisterUserCommand> result = userValidator.validateUserRegistration(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("email");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.email.max.length 100");
    }

    @Test
    @DisplayName("비밀번호가 null이면 검증에 실패해야 한다")
    void validateUserRegistration_withNullPassword_shouldBeInvalid() {
        // given
        RegisterUserCommand command = new RegisterUserCommand("test@example.com", null, "Gildong", "Hong");

        // when
        ValidationResult<RegisterUserCommand> result = userValidator.validateUserRegistration(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("password");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.password.required");
    }

    @Test
    @DisplayName("비밀번호가 8자 미만이면 검증에 실패해야 한다")
    void validateUserRegistration_withTooShortPassword_shouldBeInvalid() {
        // given
        RegisterUserCommand command = new RegisterUserCommand("test@example.com", "Pass1!", "Gildong", "Hong");

        // when
        ValidationResult<RegisterUserCommand> result = userValidator.validateUserRegistration(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("password");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.password.min.length 8");
    }

    @Test
    @DisplayName("비밀번호가 20자를 초과하면 검증에 실패해야 한다")
    void validateUserRegistration_withTooLongPassword_shouldBeInvalid() {
        // given
        RegisterUserCommand command = new RegisterUserCommand("test@example.com", "VeryLongPassword123!@", "Gildong", "Hong");
        
        // when
        ValidationResult<RegisterUserCommand> result = userValidator.validateUserRegistration(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("password");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.password.max.length 20");
    }

    @Test
    @DisplayName("비밀번호 패턴이 맞지 않으면 검증에 실패해야 한다")
    void validateUserRegistration_withInvalidPasswordPattern_shouldBeInvalid() {
        // given
        RegisterUserCommand command = new RegisterUserCommand("test@example.com", "password", "Gildong", "Hong"); // No numbers or special chars
        
        // when
        ValidationResult<RegisterUserCommand> result = userValidator.validateUserRegistration(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("password");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.password.pattern");
    }
    
    @Test
    @DisplayName("이름(firstName)이 null이면 검증에 실패해야 한다")
    void validateUserRegistration_withNullFirstName_shouldBeInvalid() {
        // given
        RegisterUserCommand command = new RegisterUserCommand("test@example.com", "Password123!", null, "Hong");
        
        // when
        ValidationResult<RegisterUserCommand> result = userValidator.validateUserRegistration(command);
        
        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("firstName");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.firstName.required");
    }

    @Test
    @DisplayName("이름(firstName)에 허용되지 않은 문자가 포함되면 검증에 실패해야 한다")
    void validateUserRegistration_withInvalidFirstNamePattern_shouldBeInvalid() {
        // given
        RegisterUserCommand command = new RegisterUserCommand("test@example.com", "Password123!", "Gildong1", "Hong");

        // when
        ValidationResult<RegisterUserCommand> result = userValidator.validateUserRegistration(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("firstName");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.firstName.pattern");
    }

    @Test
    @DisplayName("이름(firstName)이 50자를 초과하면 검증에 실패해야 한다")
    void validateUserRegistration_withTooLongFirstName_shouldBeInvalid() {
        // given
        String longFirstName = "a".repeat(51);
        RegisterUserCommand command = new RegisterUserCommand("test@example.com", "Password123!", longFirstName, "Hong");

        // when
        ValidationResult<RegisterUserCommand> result = userValidator.validateUserRegistration(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("firstName");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.firstName.max.length 50");
    }
    
    @Test
    @DisplayName("성(lastName)이 null이면 검증에 실패해야 한다")
    void validateUserRegistration_withNullLastName_shouldBeInvalid() {
        // given
        RegisterUserCommand command = new RegisterUserCommand("test@example.com", "Password123!", "Gildong", null);
        
        // when
        ValidationResult<RegisterUserCommand> result = userValidator.validateUserRegistration(command);
        
        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("lastName");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.lastName.required");
    }

    @Test
    @DisplayName("성(lastName)에 허용되지 않은 문자가 포함되면 검증에 실패해야 한다")
    void validateUserRegistration_withInvalidLastNamePattern_shouldBeInvalid() {
        // given
        RegisterUserCommand command = new RegisterUserCommand("test@example.com", "Password123!", "Gildong", "Hong1");

        // when
        ValidationResult<RegisterUserCommand> result = userValidator.validateUserRegistration(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("lastName");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.lastName.pattern");
    }

    @Test
    @DisplayName("성(lastName)이 50자를 초과하면 검증에 실패해야 한다")
    void validateUserRegistration_withTooLongLastName_shouldBeInvalid() {
        // given
        String longLastName = "a".repeat(51);
        RegisterUserCommand command = new RegisterUserCommand("test@example.com", "Password123!", "Gildong", longLastName);

        // when
        ValidationResult<RegisterUserCommand> result = userValidator.validateUserRegistration(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("lastName");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.lastName.max.length 50");
    }

    @Test
    @DisplayName("여러 필드가 유효하지 않으면 모든 오류를 반환해야 한다")
    void validateUserRegistration_withMultipleInvalidFields_shouldReturnAllErrors() {
        // given
        RegisterUserCommand command = new RegisterUserCommand("invalid-email", "short", "Gildong1", "Hong1");

        // when
        ValidationResult<RegisterUserCommand> result = userValidator.validateUserRegistration(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(4);
        assertThat(result.getErrors()).extracting("field").containsExactlyInAnyOrder("email", "password", "firstName", "lastName");
    }

    // =================================================================
    //                    User Update Validation Tests
    // =================================================================

    private UpdateUserCommand createValidUpdateCommand() {
        return new UpdateUserCommand(null, "Gildong", "Hong");
    }

    @Test
    @DisplayName("유효한 사용자 수정 정보는 검증을 통과해야 한다")
    void validateUserUpdate_withValidData_shouldBeValid() {
        // given
        UpdateUserCommand command = createValidUpdateCommand();

        // when
        ValidationResult<UpdateUserCommand> result = userValidator.validateUserUpdate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("수정 시 이름(firstName)이 null이면 검증에 실패해야 한다")
    void validateUserUpdate_withNullFirstName_shouldBeInvalid() {
        // given
        UpdateUserCommand command = new UpdateUserCommand(null, null, "Hong");
        
        // when
        ValidationResult<UpdateUserCommand> result = userValidator.validateUserUpdate(command);
        
        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("firstName");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.firstName.required");
    }

    @Test
    @DisplayName("수정 시 이름(firstName)에 허용되지 않은 문자가 포함되면 검증에 실패해야 한다")
    void validateUserUpdate_withInvalidFirstNamePattern_shouldBeInvalid() {
        // given
        UpdateUserCommand command = new UpdateUserCommand(null, "Gildong1", "Hong");

        // when
        ValidationResult<UpdateUserCommand> result = userValidator.validateUserUpdate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("firstName");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.firstName.pattern");
    }

    @Test
    @DisplayName("수정 시 이름(firstName)이 50자를 초과하면 검증에 실패해야 한다")
    void validateUserUpdate_withTooLongFirstName_shouldBeInvalid() {
        // given
        String longFirstName = "a".repeat(51);
        UpdateUserCommand command = new UpdateUserCommand(null, longFirstName, "Hong");

        // when
        ValidationResult<UpdateUserCommand> result = userValidator.validateUserUpdate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("firstName");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.firstName.max.length 50");
    }

    @Test
    @DisplayName("수정 시 성(lastName)이 null이면 검증에 실패해야 한다")
    void validateUserUpdate_withNullLastName_shouldBeInvalid() {
        // given
        UpdateUserCommand command = new UpdateUserCommand(null, "Gildong", null);
        
        // when
        ValidationResult<UpdateUserCommand> result = userValidator.validateUserUpdate(command);
        
        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("lastName");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.lastName.required");
    }

    @Test
    @DisplayName("수정 시 성(lastName)에 허용되지 않은 문자가 포함되면 검증에 실패해야 한다")
    void validateUserUpdate_withInvalidLastNamePattern_shouldBeInvalid() {
        // given
        UpdateUserCommand command = new UpdateUserCommand(null, "Gildong", "Hong1");

        // when
        ValidationResult<UpdateUserCommand> result = userValidator.validateUserUpdate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("lastName");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.lastName.pattern");
    }

    @Test
    @DisplayName("수정 시 여러 필드가 유효하지 않으면 모든 오류를 반환해야 한다")
    void validateUserUpdate_withMultipleInvalidFields_shouldReturnAllErrors() {
        // given
        UpdateUserCommand command = new UpdateUserCommand(null, "Gildong1", "Hong1");

        // when
        ValidationResult<UpdateUserCommand> result = userValidator.validateUserUpdate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(2);
        assertThat(result.getErrors()).extracting("field").containsExactlyInAnyOrder("firstName", "lastName");
    }
}

package com.boardly.features.user.application.validation;

import com.boardly.features.user.application.command.RegisterUserCommand;
import com.boardly.features.user.application.command.UpdateUserCommand;
import com.boardly.application.validation.CommonValidationRules;
import com.boardly.shared.validation.MessageResolver;
import com.boardly.shared.validation.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    private MessageResolver messageResolver;

    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(Locale.KOREAN);
        lenient().when(messageResolver.getMessage(anyString(), any())).thenAnswer(invocation -> {
            String code = invocation.getArgument(0);
            Object[] args;
            // Mockito may pass varargs directly, not as array
            if (invocation.getArguments().length > 1) {
                Object second = invocation.getArgument(1);
                if (second != null && second.getClass().isArray()) {
                    args = (Object[]) second;
                } else {
                    args = new Object[] { second };
                }
            } else {
                args = new Object[]{};
            }

            StringBuilder message = new StringBuilder(code);
            for (Object arg : args) {
                message.append(" ").append(arg);
            }
            return message.toString();
        });

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
        // required + pattern + maxLength의 체인이므로 여러 에러가 나올 수 있음
        assertThat(result.getErrors()).isNotEmpty();
        assertThat(result.getErrors()).extracting("field").contains("email");
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

    // ================= User Update =================

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
}

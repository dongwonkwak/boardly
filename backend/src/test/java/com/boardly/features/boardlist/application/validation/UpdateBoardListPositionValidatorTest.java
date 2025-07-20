package com.boardly.features.boardlist.application.validation;

import com.boardly.features.boardlist.application.port.input.UpdateBoardListPositionCommand;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;
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
@DisplayName("UpdateBoardListPositionValidator 테스트")
class UpdateBoardListPositionValidatorTest {

    private UpdateBoardListPositionValidator validator;

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
        validator = new UpdateBoardListPositionValidator(messageResolver);
    }

    private UpdateBoardListPositionCommand createValidCommand() {
        return new UpdateBoardListPositionCommand(
                new ListId("list-123"),
                new UserId("user-456"),
                2);
    }

    @Test
    @DisplayName("유효한 커맨드는 검증을 통과해야 한다")
    void validate_ValidCommand_ShouldPass() {
        // given
        UpdateBoardListPositionCommand command = createValidCommand();

        // when
        ValidationResult<UpdateBoardListPositionCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.get()).isEqualTo(command);
    }

    @Test
    @DisplayName("listId가 null인 경우 검증이 실패해야 한다")
    void validate_NullListId_ShouldFail() {
        // given
        UpdateBoardListPositionCommand command = new UpdateBoardListPositionCommand(
                null,
                new UserId("user-456"),
                2);

        // when
        ValidationResult<UpdateBoardListPositionCommand> result = validator.validate(command);

        // then
        assertThat(result.isInvalid()).isTrue();
        assertThat(result.getErrors()).hasSize(1);

        Failure.FieldViolation violation = result.getErrors().get(0);
        assertThat(violation.field()).isEqualTo("listId");
        assertThat(violation.message()).isEqualTo("validation.boardlist.listId.required");
        assertThat(violation.rejectedValue()).isNull();
    }

    @Test
    @DisplayName("userId가 null인 경우 검증이 실패해야 한다")
    void validate_NullUserId_ShouldFail() {
        // given
        UpdateBoardListPositionCommand command = new UpdateBoardListPositionCommand(
                new ListId("list-123"),
                null,
                2);

        // when
        ValidationResult<UpdateBoardListPositionCommand> result = validator.validate(command);

        // then
        assertThat(result.isInvalid()).isTrue();
        assertThat(result.getErrors()).hasSize(1);

        Failure.FieldViolation violation = result.getErrors().get(0);
        assertThat(violation.field()).isEqualTo("userId");
        assertThat(violation.message()).isEqualTo("validation.boardlist.userId.required");
        assertThat(violation.rejectedValue()).isNull();
    }

    @Test
    @DisplayName("newPosition이 음수인 경우 검증이 실패해야 한다")
    void validate_NegativePosition_ShouldFail() {
        // given
        UpdateBoardListPositionCommand command = new UpdateBoardListPositionCommand(
                new ListId("list-123"),
                new UserId("user-456"),
                -1);

        // when
        ValidationResult<UpdateBoardListPositionCommand> result = validator.validate(command);

        // then
        assertThat(result.isInvalid()).isTrue();
        assertThat(result.getErrors()).hasSize(1);

        Failure.FieldViolation violation = result.getErrors().get(0);
        assertThat(violation.field()).isEqualTo("newPosition");
        assertThat(violation.message()).isEqualTo("validation.boardlist.position.invalid");
        assertThat(violation.rejectedValue()).isEqualTo(-1);
    }

    @Test
    @DisplayName("newPosition이 0인 경우 검증을 통과해야 한다")
    void validate_ZeroPosition_ShouldPass() {
        // given
        UpdateBoardListPositionCommand command = new UpdateBoardListPositionCommand(
                new ListId("list-123"),
                new UserId("user-456"),
                0);

        // when
        ValidationResult<UpdateBoardListPositionCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.get()).isEqualTo(command);
    }

    @Test
    @DisplayName("newPosition이 양수인 경우 검증을 통과해야 한다")
    void validate_PositivePosition_ShouldPass() {
        // given
        UpdateBoardListPositionCommand command = new UpdateBoardListPositionCommand(
                new ListId("list-123"),
                new UserId("user-456"),
                100);

        // when
        ValidationResult<UpdateBoardListPositionCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.get()).isEqualTo(command);
    }

    @Test
    @DisplayName("listId와 userId가 모두 null인 경우 두 개의 오류가 발생해야 한다")
    void validate_NullListIdAndUserId_ShouldFailWithTwoErrors() {
        // given
        UpdateBoardListPositionCommand command = new UpdateBoardListPositionCommand(
                null,
                null,
                2);

        // when
        ValidationResult<UpdateBoardListPositionCommand> result = validator.validate(command);

        // then
        assertThat(result.isInvalid()).isTrue();
        assertThat(result.getErrors()).hasSize(2);

        var violations = result.getErrors();
        assertThat(violations.map(Failure.FieldViolation::field)).contains("listId", "userId");
        assertThat(violations.map(Failure.FieldViolation::message))
                .contains("validation.boardlist.listId.required", "validation.boardlist.userId.required");
    }

    @Test
    @DisplayName("listId가 null이고 newPosition이 음수인 경우 두 개의 오류가 발생해야 한다")
    void validate_NullListIdAndNegativePosition_ShouldFailWithTwoErrors() {
        // given
        UpdateBoardListPositionCommand command = new UpdateBoardListPositionCommand(
                null,
                new UserId("user-456"),
                -5);

        // when
        ValidationResult<UpdateBoardListPositionCommand> result = validator.validate(command);

        // then
        assertThat(result.isInvalid()).isTrue();
        assertThat(result.getErrors()).hasSize(2);

        var violations = result.getErrors();
        assertThat(violations.map(Failure.FieldViolation::field)).contains("listId", "newPosition");
        assertThat(violations.map(Failure.FieldViolation::message))
                .contains("validation.boardlist.listId.required", "validation.boardlist.position.invalid");
    }

    @Test
    @DisplayName("모든 필드가 유효하지 않은 경우 세 개의 오류가 발생해야 한다")
    void validate_AllInvalidFields_ShouldFailWithThreeErrors() {
        // given
        UpdateBoardListPositionCommand command = new UpdateBoardListPositionCommand(
                null,
                null,
                -10);

        // when
        ValidationResult<UpdateBoardListPositionCommand> result = validator.validate(command);

        // then
        assertThat(result.isInvalid()).isTrue();
        assertThat(result.getErrors()).hasSize(3);

        var violations = result.getErrors();
        assertThat(violations.map(Failure.FieldViolation::field)).contains("listId", "userId", "newPosition");
        assertThat(violations.map(Failure.FieldViolation::message))
                .contains("validation.boardlist.listId.required", "validation.boardlist.userId.required",
                        "validation.boardlist.position.invalid");
    }

    @Test
    @DisplayName("빈 ListId와 UserId 객체는 유효해야 한다")
    void validate_EmptyListIdAndUserId_ShouldPass() {
        // given
        UpdateBoardListPositionCommand command = new UpdateBoardListPositionCommand(
                new ListId(),
                new UserId(),
                5);

        // when
        ValidationResult<UpdateBoardListPositionCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.get()).isEqualTo(command);
    }
}
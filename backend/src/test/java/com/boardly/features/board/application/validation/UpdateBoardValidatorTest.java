package com.boardly.features.board.application.validation;

import com.boardly.features.board.application.port.input.UpdateBoardCommand;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateBoardValidatorTest {

    @Mock
    private ValidationMessageResolver messageResolver;

    private UpdateBoardValidator validator;

    @BeforeEach
    void setUp() {
        CommonValidationRules commonValidationRules = new CommonValidationRules(messageResolver);
        validator = new UpdateBoardValidator(commonValidationRules);
    }

    @Test
    @DisplayName("유효한 UpdateBoardCommand는 검증을 통과해야 한다")
    void validate_ValidCommand_ShouldPass() {
        // given
        UpdateBoardCommand command = UpdateBoardCommand.of(
                new BoardId("board-123"),
                "업데이트된 보드 제목",
                "업데이트된 보드 설명입니다.",
                new UserId("user-456"));

        // when
        ValidationResult<UpdateBoardCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.get()).isEqualTo(command);
    }

    @Test
    @DisplayName("boardId가 null인 경우 검증에 실패해야 한다")
    void validate_NullBoardId_ShouldFail() {
        // given
        when(messageResolver.getMessage("validation.boardId.required"))
                .thenReturn("보드 ID는 필수입니다");

        UpdateBoardCommand command = UpdateBoardCommand.of(
                null,
                "업데이트된 보드 제목",
                "업데이트된 보드 설명입니다.",
                new UserId("user-456"));

        // when
        ValidationResult<UpdateBoardCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);

        var violation = result.getErrors().get(0);
        assertThat(violation.field()).isEqualTo("boardId");
        assertThat(violation.message()).isEqualTo("보드 ID는 필수입니다");
        assertThat(violation.rejectedValue()).isNull();
    }

    @Test
    @DisplayName("requestedBy가 null인 경우 검증에 실패해야 한다")
    void validate_NullRequestedBy_ShouldFail() {
        // given
        when(messageResolver.getMessage("validation.userId.required"))
                .thenReturn("요청자 ID는 필수입니다");

        UpdateBoardCommand command = UpdateBoardCommand.of(
                new BoardId("board-123"),
                "업데이트된 보드 제목",
                "업데이트된 보드 설명입니다.",
                null);

        // when
        ValidationResult<UpdateBoardCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);

        var violation = result.getErrors().get(0);
        assertThat(violation.field()).isEqualTo("userId");
        assertThat(violation.message()).isEqualTo("요청자 ID는 필수입니다");
        assertThat(violation.rejectedValue()).isNull();
    }

    @Test
    @DisplayName("title이 100자를 초과하는 경우 검증에 실패해야 한다")
    void validate_TitleTooLong_ShouldFail() {
        // given
        when(messageResolver.getMessage("validation.title.max.length", 100))
                .thenReturn("보드 제목은 100자 이하여야 합니다");

        String longTitle = "a".repeat(101);
        UpdateBoardCommand command = UpdateBoardCommand.of(
                new BoardId("board-123"),
                longTitle,
                "업데이트된 보드 설명입니다.",
                new UserId("user-456"));

        // when
        ValidationResult<UpdateBoardCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);

        var violation = result.getErrors().get(0);
        assertThat(violation.field()).isEqualTo("title");
        assertThat(violation.message()).isEqualTo("보드 제목은 100자 이하여야 합니다");
        assertThat(violation.rejectedValue()).isEqualTo(longTitle);
    }

    @Test
    @DisplayName("description이 500자를 초과하는 경우 검증에 실패해야 한다")
    void validate_DescriptionTooLong_ShouldFail() {
        // given
        when(messageResolver.getMessage("validation.description.max.length", 500))
                .thenReturn("보드 설명은 500자 이하여야 합니다");

        String longDescription = "a".repeat(501);
        UpdateBoardCommand command = UpdateBoardCommand.of(
                new BoardId("board-123"),
                "업데이트된 보드 제목",
                longDescription,
                new UserId("user-456"));

        // when
        ValidationResult<UpdateBoardCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);

        var violation = result.getErrors().get(0);
        assertThat(violation.field()).isEqualTo("description");
        assertThat(violation.message()).isEqualTo("보드 설명은 500자 이하여야 합니다");
        assertThat(violation.rejectedValue()).isEqualTo(longDescription);
    }

    @Test
    @DisplayName("여러 필드가 유효하지 않은 경우 모든 오류가 반환되어야 한다")
    void validate_MultipleInvalidFields_ShouldReturnAllErrors() {
        // given
        when(messageResolver.getMessage("validation.boardId.required"))
                .thenReturn("보드 ID는 필수입니다");
        when(messageResolver.getMessage("validation.userId.required"))
                .thenReturn("요청자 ID는 필수입니다");
        when(messageResolver.getMessage("validation.title.max.length", 100))
                .thenReturn("보드 제목은 100자 이하여야 합니다");
        when(messageResolver.getMessage("validation.description.max.length", 500))
                .thenReturn("보드 설명은 500자 이하여야 합니다");

        String longTitle = "a".repeat(101);
        String longDescription = "a".repeat(501);
        UpdateBoardCommand command = UpdateBoardCommand.of(
                null,
                longTitle,
                longDescription,
                null);

        // when
        ValidationResult<UpdateBoardCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(4);

        var violations = result.getErrorsAsCollection();
        assertThat(violations).extracting("field")
                .containsExactlyInAnyOrder("boardId", "title", "description", "userId");
        assertThat(violations).extracting("message")
                .containsExactlyInAnyOrder(
                        "보드 ID는 필수입니다",
                        "보드 제목은 100자 이하여야 합니다",
                        "보드 설명은 500자 이하여야 합니다",
                        "요청자 ID는 필수입니다");
    }

    @Test
    @DisplayName("title이 null인 경우 검증을 통과해야 한다")
    void validate_NullTitle_ShouldPass() {
        // given
        UpdateBoardCommand command = UpdateBoardCommand.of(
                new BoardId("board-123"),
                null,
                "업데이트된 보드 설명입니다.",
                new UserId("user-456"));

        // when
        ValidationResult<UpdateBoardCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.get()).isEqualTo(command);
    }

    @Test
    @DisplayName("title이 빈 문자열인 경우 검증에 실패해야 한다")
    void validate_EmptyTitle_ShouldFail() {
        // given
        when(messageResolver.getMessage("validation.title.min.length", 1))
                .thenReturn("보드 제목은 1자 이상이어야 합니다");

        UpdateBoardCommand command = UpdateBoardCommand.of(
                new BoardId("board-123"),
                "",
                "업데이트된 보드 설명입니다.",
                new UserId("user-456"));

        // when
        ValidationResult<UpdateBoardCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);

        var violation = result.getErrors().get(0);
        assertThat(violation.field()).isEqualTo("title");
        assertThat(violation.message()).isEqualTo("보드 제목은 1자 이상이어야 합니다");
        assertThat(violation.rejectedValue()).isEqualTo("");
    }

    @Test
    @DisplayName("description이 null인 경우 검증을 통과해야 한다")
    void validate_NullDescription_ShouldPass() {
        // given
        UpdateBoardCommand command = UpdateBoardCommand.of(
                new BoardId("board-123"),
                "업데이트된 보드 제목",
                null,
                new UserId("user-456"));

        // when
        ValidationResult<UpdateBoardCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.get()).isEqualTo(command);
    }

    @Test
    @DisplayName("description이 빈 문자열인 경우 검증을 통과해야 한다")
    void validate_EmptyDescription_ShouldPass() {
        // given
        UpdateBoardCommand command = UpdateBoardCommand.of(
                new BoardId("board-123"),
                "업데이트된 보드 제목",
                "",
                new UserId("user-456"));

        // when
        ValidationResult<UpdateBoardCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.get()).isEqualTo(command);
    }

    @Test
    @DisplayName("title과 description이 정확히 최대 길이인 경우 검증을 통과해야 한다")
    void validate_MaxLengthFields_ShouldPass() {
        // given
        String maxTitle = "a".repeat(100);
        String maxDescription = "a".repeat(500);
        UpdateBoardCommand command = UpdateBoardCommand.of(
                new BoardId("board-123"),
                maxTitle,
                maxDescription,
                new UserId("user-456"));

        // when
        ValidationResult<UpdateBoardCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.get()).isEqualTo(command);
    }

    @Test
    @DisplayName("모든 필드가 null이거나 빈 문자열인 경우 필수 필드만 검증에 실패해야 한다")
    void validate_AllOptionalFieldsNull_ShouldOnlyFailRequiredFields() {
        // given
        when(messageResolver.getMessage("validation.boardId.required"))
                .thenReturn("보드 ID는 필수입니다");
        when(messageResolver.getMessage("validation.userId.required"))
                .thenReturn("요청자 ID는 필수입니다");

        UpdateBoardCommand command = UpdateBoardCommand.of(
                null,
                null,
                null,
                null);

        // when
        ValidationResult<UpdateBoardCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(2);

        var violations = result.getErrorsAsCollection();
        assertThat(violations).extracting("field")
                .containsExactlyInAnyOrder("boardId", "userId");
        assertThat(violations).extracting("message")
                .containsExactlyInAnyOrder("보드 ID는 필수입니다", "요청자 ID는 필수입니다");
    }

    @Test
    @DisplayName("ValidationResult를 Failure로 변환할 수 있어야 한다")
    void toFailure_ShouldConvertValidationResultToFailure() {
        // given
        when(messageResolver.getMessage("validation.boardId.required"))
                .thenReturn("보드 ID는 필수입니다");
        when(messageResolver.getMessage("validation.userId.required"))
                .thenReturn("요청자 ID는 필수입니다");

        UpdateBoardCommand command = UpdateBoardCommand.of(
                null,
                null,
                null,
                null);
        ValidationResult<UpdateBoardCommand> result = validator.validate(command);

        // when
        Failure failure = result.toFailure("입력 데이터가 유효하지 않습니다");

        // then
        assertThat(failure).isInstanceOf(Failure.InputError.class);
        var inputError = (Failure.InputError) failure;
        assertThat(inputError.getMessage()).isEqualTo("입력 데이터가 유효하지 않습니다");
        assertThat(inputError.getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(inputError.getViolations()).hasSize(2);
    }
}
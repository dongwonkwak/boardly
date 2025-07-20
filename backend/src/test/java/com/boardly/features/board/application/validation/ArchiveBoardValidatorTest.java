package com.boardly.features.board.application.validation;

import com.boardly.features.board.application.port.input.ArchiveBoardCommand;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArchiveBoardValidatorTest {

  @Mock
  private ValidationMessageResolver messageResolver;

  private ArchiveBoardValidator validator;

  @BeforeEach
  void setUp() {
    CommonValidationRules commonValidationRules = new CommonValidationRules(messageResolver);
    validator = new ArchiveBoardValidator(commonValidationRules);
  }

  @Test
  @DisplayName("유효한 ArchiveBoardCommand는 검증을 통과해야 한다")
  void validate_ValidCommand_ShouldPass() {
    // given
    ArchiveBoardCommand command = ArchiveBoardCommand.of(
        new BoardId("board-123"),
        new UserId("user-456"));

    // when
    ValidationResult<ArchiveBoardCommand> result = validator.validate(command);

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

    ArchiveBoardCommand command = ArchiveBoardCommand.of(
        null,
        new UserId("user-456"));

    // when
    ValidationResult<ArchiveBoardCommand> result = validator.validate(command);

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

    ArchiveBoardCommand command = ArchiveBoardCommand.of(
        new BoardId("board-123"),
        null);

    // when
    ValidationResult<ArchiveBoardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);

    var violation = result.getErrors().get(0);
    assertThat(violation.field()).isEqualTo("userId");
    assertThat(violation.message()).isEqualTo("요청자 ID는 필수입니다");
    assertThat(violation.rejectedValue()).isNull();
  }

  @Test
  @DisplayName("boardId와 requestedBy가 모두 null인 경우 모든 오류가 반환되어야 한다")
  void validate_BothNullFields_ShouldReturnAllErrors() {
    // given
    when(messageResolver.getMessage("validation.boardId.required"))
        .thenReturn("보드 ID는 필수입니다");
    when(messageResolver.getMessage("validation.userId.required"))
        .thenReturn("요청자 ID는 필수입니다");

    ArchiveBoardCommand command = ArchiveBoardCommand.of(
        null,
        null);

    // when
    ValidationResult<ArchiveBoardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(2);

    var violations = result.getErrorsAsCollection();
    assertThat(violations).extracting("field")
        .containsExactlyInAnyOrder("boardId", "userId");
    assertThat(violations).extracting("message")
        .containsExactlyInAnyOrder("보드 ID는 필수입니다", "요청자 ID는 필수입니다");
    assertThat(violations).extracting("rejectedValue")
        .containsOnlyNulls();
  }

  @Test
  @DisplayName("유효한 BoardId와 UserId 객체로 검증이 성공해야 한다")
  void validate_ValidObjects_ShouldPass() {
    // given
    BoardId boardId = new BoardId("board-123");
    UserId userId = new UserId("user-456");
    ArchiveBoardCommand command = ArchiveBoardCommand.of(boardId, userId);

    // when
    ValidationResult<ArchiveBoardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
    assertThat(result.get().boardId()).isEqualTo(boardId);
    assertThat(result.get().requestedBy()).isEqualTo(userId);
  }

  @Test
  @DisplayName("ValidationResult를 Failure로 변환할 수 있어야 한다")
  void toFailure_ShouldConvertValidationResultToFailure() {
    // given
    when(messageResolver.getMessage("validation.boardId.required"))
        .thenReturn("보드 ID는 필수입니다");
    when(messageResolver.getMessage("validation.userId.required"))
        .thenReturn("요청자 ID는 필수입니다");

    ArchiveBoardCommand command = ArchiveBoardCommand.of(
        null,
        null);
    ValidationResult<ArchiveBoardCommand> result = validator.validate(command);

    // when
    Failure failure = result.toFailure("입력 데이터가 유효하지 않습니다");

    // then
    assertThat(failure).isInstanceOf(Failure.InputError.class);
    var inputError = (Failure.InputError) failure;
    assertThat(inputError.getMessage()).isEqualTo("입력 데이터가 유효하지 않습니다");
    assertThat(inputError.getErrorCode()).isEqualTo("VALIDATION_ERROR");
    assertThat(inputError.getViolations()).hasSize(2);
  }

  @Test
  @DisplayName("검증 실패 시 get() 메서드를 호출하면 예외가 발생해야 한다")
  void get_InvalidResult_ShouldThrowException() {
    // given
    when(messageResolver.getMessage("validation.boardId.required"))
        .thenReturn("보드 ID는 필수입니다");

    ArchiveBoardCommand command = ArchiveBoardCommand.of(
        null,
        new UserId("user-456"));
    ValidationResult<ArchiveBoardCommand> result = validator.validate(command);

    // when & then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);
    assertThatThrownBy(() -> result.get())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("ValidationResult is invalid");
  }

  @Test
  @DisplayName("검증 성공 시 getErrors() 메서드를 호출하면 예외가 발생해야 한다")
  void getErrors_ValidResult_ShouldThrowException() {
    // given
    ArchiveBoardCommand command = ArchiveBoardCommand.of(
        new BoardId("board-123"),
        new UserId("user-456"));
    ValidationResult<ArchiveBoardCommand> result = validator.validate(command);

    // when & then
    assertThat(result.isValid()).isTrue();
    assertThatThrownBy(() -> result.getErrors())
        .isInstanceOf(java.util.NoSuchElementException.class);
  }
}
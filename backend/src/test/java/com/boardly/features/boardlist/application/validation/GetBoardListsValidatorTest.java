package com.boardly.features.boardlist.application.validation;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.boardlist.application.port.input.GetBoardListsCommand;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class GetBoardListsValidatorTest {

  @Mock
  private ValidationMessageResolver messageResolver;

  private GetBoardListsValidator validator;

  @BeforeEach
  void setUp() {
    // ValidationMessageResolver Mock 설정
    lenient().when(messageResolver.getMessage("validation.boardlist.boardId.required"))
        .thenReturn("보드 ID는 필수입니다");
    lenient().when(messageResolver.getMessage("validation.boardlist.userId.required"))
        .thenReturn("사용자 ID는 필수입니다");

    validator = new GetBoardListsValidator(messageResolver);
  }

  @Test
  @DisplayName("유효한 GetBoardListsCommand는 검증을 통과해야 한다")
  void validate_ValidCommand_ShouldPass() {
    // given
    GetBoardListsCommand command = new GetBoardListsCommand(
        new BoardId("board-123"),
        new UserId("user-456"));

    // when
    ValidationResult<GetBoardListsCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("boardId가 null인 경우 검증에 실패해야 한다")
  void validate_NullBoardId_ShouldFail() {
    // given
    GetBoardListsCommand command = new GetBoardListsCommand(
        null,
        new UserId("user-456"));

    // when
    ValidationResult<GetBoardListsCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);

    var violation = result.getErrors().get(0);
    assertThat(violation.field()).isEqualTo("boardId");
    assertThat(violation.message()).isEqualTo("보드 ID는 필수입니다");
    assertThat(violation.rejectedValue()).isNull();
  }

  @Test
  @DisplayName("userId가 null인 경우 검증에 실패해야 한다")
  void validate_NullUserId_ShouldFail() {
    // given
    GetBoardListsCommand command = new GetBoardListsCommand(
        new BoardId("board-123"),
        null);

    // when
    ValidationResult<GetBoardListsCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);

    var violation = result.getErrors().get(0);
    assertThat(violation.field()).isEqualTo("userId");
    assertThat(violation.message()).isEqualTo("사용자 ID는 필수입니다");
    assertThat(violation.rejectedValue()).isNull();
  }

  @Test
  @DisplayName("boardId와 userId가 모두 null인 경우 모든 오류가 반환되어야 한다")
  void validate_BothNullIds_ShouldReturnAllErrors() {
    // given
    GetBoardListsCommand command = new GetBoardListsCommand(
        null,
        null);

    // when
    ValidationResult<GetBoardListsCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(2);

    var violations = result.getErrorsAsCollection();
    assertThat(violations).extracting("field")
        .containsExactlyInAnyOrder("boardId", "userId");
    assertThat(violations).extracting("message")
        .containsExactlyInAnyOrder("보드 ID는 필수입니다", "사용자 ID는 필수입니다");
    assertThat(violations).extracting("rejectedValue")
        .containsOnlyNulls();
  }

  @Test
  @DisplayName("ValidationResult를 Failure로 변환할 수 있어야 한다")
  void toFailure_ShouldConvertValidationResultToFailure() {
    // given
    GetBoardListsCommand command = new GetBoardListsCommand(
        null,
        null);
    ValidationResult<GetBoardListsCommand> result = validator.validate(command);

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
  @DisplayName("유효한 검증 결과를 Failure로 변환하려고 하면 예외가 발생해야 한다")
  void toFailure_ValidResult_ShouldThrowException() {
    // given
    GetBoardListsCommand command = new GetBoardListsCommand(
        new BoardId("board-123"),
        new UserId("user-456"));
    ValidationResult<GetBoardListsCommand> result = validator.validate(command);

    // when & then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("검증 실패 시 get() 메서드를 호출하면 예외가 발생해야 한다")
  void get_InvalidResult_ShouldThrowException() {
    // given
    GetBoardListsCommand command = new GetBoardListsCommand(
        null,
        new UserId("user-456"));
    ValidationResult<GetBoardListsCommand> result = validator.validate(command);

    // when & then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);
  }
}
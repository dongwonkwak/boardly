package com.boardly.features.card.application.validation;

import com.boardly.features.card.application.port.input.MoveCardCommand;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoveCardValidatorTest {

  @Mock
  private ValidationMessageResolver messageResolver;

  private MoveCardValidator validator;

  @BeforeEach
  void setUp() {
    CommonValidationRules commonValidationRules = new CommonValidationRules(messageResolver);
    validator = new MoveCardValidator(commonValidationRules, messageResolver);
  }

  @Test
  @DisplayName("유효한 MoveCardCommand는 검증을 통과해야 한다")
  void validate_ValidCommand_ShouldPass() {
    // given
    MoveCardCommand command = MoveCardCommand.of(
        new CardId("card-123"),
        null,
        5,
        new UserId("user-789"));

    // when
    ValidationResult<MoveCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("newPosition이 0인 경우 검증을 통과해야 한다")
  void validate_ZeroPosition_ShouldPass() {
    // given
    MoveCardCommand command = MoveCardCommand.of(
        new CardId("card-123"),
        null,
        0,
        new UserId("user-789"));

    // when
    ValidationResult<MoveCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("newPosition이 큰 양수인 경우 검증을 통과해야 한다")
  void validate_LargePositivePosition_ShouldPass() {
    // given
    MoveCardCommand command = MoveCardCommand.of(
        new CardId("card-123"),
        null,
        999999,
        new UserId("user-789"));

    // when
    ValidationResult<MoveCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("cardId가 null인 경우 검증에 실패해야 한다")
  void validate_NullCardId_ShouldFail() {
    // given
    when(messageResolver.getMessage("validation.cardId.required"))
        .thenReturn("카드 ID는 필수입니다");

    MoveCardCommand command = MoveCardCommand.of(
        null,
        null,
        5,
        new UserId("user-789"));

    // when
    ValidationResult<MoveCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);

    var violation = result.getErrors().get(0);
    assertThat(violation.field()).isEqualTo("cardId");
    assertThat(violation.message()).isEqualTo("카드 ID는 필수입니다");
    assertThat(violation.rejectedValue()).isNull();
  }

  @Test
  @DisplayName("newPosition이 null인 경우 검증에 실패해야 한다")
  void validate_NullNewPosition_ShouldFail() {
    // given
    when(messageResolver.getMessage("validation.position.required"))
        .thenReturn("새로운 위치는 필수입니다");

    MoveCardCommand command = MoveCardCommand.of(
        new CardId("card-123"),
        null,
        null,
        new UserId("user-789"));

    // when
    ValidationResult<MoveCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);

    var violation = result.getErrors().get(0);
    assertThat(violation.field()).isEqualTo("newPosition");
    assertThat(violation.message()).isEqualTo("새로운 위치는 필수입니다");
    assertThat(violation.rejectedValue()).isNull();
  }

  @Test
  @DisplayName("newPosition이 음수인 경우 검증에 실패해야 한다")
  void validate_NegativeNewPosition_ShouldFail() {
    // given
    when(messageResolver.getMessage("validation.position.required"))
        .thenReturn("새로운 위치는 필수입니다");

    MoveCardCommand command = MoveCardCommand.of(
        new CardId("card-123"),
        null,
        -1,
        new UserId("user-789"));

    // when
    ValidationResult<MoveCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);

    var violation = result.getErrors().get(0);
    assertThat(violation.field()).isEqualTo("newPosition");
    assertThat(violation.message()).isEqualTo("새로운 위치는 필수입니다");
    assertThat(violation.rejectedValue()).isEqualTo(-1);
  }

  @Test
  @DisplayName("userId가 null인 경우 검증에 실패해야 한다")
  void validate_NullUserId_ShouldFail() {
    // given
    when(messageResolver.getMessage("validation.userId.required"))
        .thenReturn("사용자 ID는 필수입니다");

    MoveCardCommand command = MoveCardCommand.of(
        new CardId("card-123"),
        null,
        5,
        null);

    // when
    ValidationResult<MoveCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);

    var violation = result.getErrors().get(0);
    assertThat(violation.field()).isEqualTo("userId");
    assertThat(violation.message()).isEqualTo("사용자 ID는 필수입니다");
    assertThat(violation.rejectedValue()).isNull();
  }

  @Test
  @DisplayName("여러 필드가 유효하지 않은 경우 모든 오류가 반환되어야 한다")
  void validate_MultipleInvalidFields_ShouldReturnAllErrors() {
    // given
    when(messageResolver.getMessage("validation.cardId.required"))
        .thenReturn("카드 ID는 필수입니다");
    when(messageResolver.getMessage("validation.position.required"))
        .thenReturn("새로운 위치는 필수입니다");
    when(messageResolver.getMessage("validation.userId.required"))
        .thenReturn("사용자 ID는 필수입니다");

    MoveCardCommand command = MoveCardCommand.of(
        null,
        null,
        -5,
        null);

    // when
    ValidationResult<MoveCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(3);

    var errors = result.getErrors();
    assertThat(errors).anyMatch(error -> error.field().equals("cardId") &&
        error.message().equals("카드 ID는 필수입니다"));
    assertThat(errors).anyMatch(error -> error.field().equals("newPosition") &&
        error.message().equals("새로운 위치는 필수입니다"));
    assertThat(errors).anyMatch(error -> error.field().equals("userId") &&
        error.message().equals("사용자 ID는 필수입니다"));
  }

  @Test
  @DisplayName("newPosition이 Integer.MAX_VALUE인 경우 검증을 통과해야 한다")
  void validate_MaxIntegerPosition_ShouldPass() {
    // given
    MoveCardCommand command = MoveCardCommand.of(
        new CardId("card-123"),
        null,
        Integer.MAX_VALUE,
        new UserId("user-789"));

    // when
    ValidationResult<MoveCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("유효한 CardId와 UserId로 검증을 통과해야 한다")
  void validate_ValidIds_ShouldPass() {
    // given
    MoveCardCommand command = MoveCardCommand.of(
        new CardId("valid-card-id"),
        null,
        10,
        new UserId("valid-user-id"));

    // when
    ValidationResult<MoveCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("빈 문자열 ID로도 검증을 통과해야 한다 (ID 객체 생성 시점에서 검증)")
  void validate_EmptyStringIds_ShouldPass() {
    // given
    MoveCardCommand command = MoveCardCommand.of(
        new CardId(""),
        null,
        15,
        new UserId(""));

    // when
    ValidationResult<MoveCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("특수문자가 포함된 ID로도 검증을 통과해야 한다")
  void validate_SpecialCharacterIds_ShouldPass() {
    // given
    MoveCardCommand command = MoveCardCommand.of(
        new CardId("card-123!@#$%"),
        null,
        20,
        new UserId("user-789!@#$%"));

    // when
    ValidationResult<MoveCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("한글이 포함된 ID로도 검증을 통과해야 한다")
  void validate_KoreanIds_ShouldPass() {
    // given
    MoveCardCommand command = MoveCardCommand.of(
        new CardId("카드-123"),
        null,
        25,
        new UserId("사용자-789"));

    // when
    ValidationResult<MoveCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("이모지가 포함된 ID로도 검증을 통과해야 한다")
  void validate_EmojiIds_ShouldPass() {
    // given
    MoveCardCommand command = MoveCardCommand.of(
        new CardId("card-123🎉"),
        null,
        30,
        new UserId("user-789🎉"));

    // when
    ValidationResult<MoveCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }
}
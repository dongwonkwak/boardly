package com.boardly.features.card.application.validation;

import com.boardly.features.boardlist.domain.model.ListId;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class MoveCardValidatorTest {

  private MoveCardValidator moveCardValidator;

  @Mock
  private MessageSource messageSource;

  @BeforeEach
  void setUp() {
    LocaleContextHolder.setLocale(Locale.KOREAN);

    // MessageSource Mock 설정
    lenient().when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
        .thenAnswer(invocation -> {
          String key = invocation.getArgument(0);
          return switch (key) {
            case "validation.cardId.required" -> "Card ID is required";
            case "validation.position.required" -> "Position is required";
            case "validation.userId.required" -> "User ID is required";
            default -> key;
          };
        });

    ValidationMessageResolver messageResolver = new ValidationMessageResolver(messageSource);
    CommonValidationRules commonValidationRules = new CommonValidationRules(messageResolver);
    moveCardValidator = new MoveCardValidator(commonValidationRules, messageResolver);
  }

  private MoveCardCommand createValidCommand() {
    return new MoveCardCommand(
        new CardId(),
        new ListId(),
        0,
        new UserId());
  }

  // ==================== 파라미터화 테스트 데이터 제공 메서드들 ====================

  private static Stream<Arguments> validPositionTestData() {
    return Stream.of(
        Arguments.of(0), // 최소값
        Arguments.of(1),
        Arguments.of(10),
        Arguments.of(100),
        Arguments.of(1000),
        Arguments.of(Integer.MAX_VALUE)); // 최대값
  }

  private static Stream<Arguments> invalidPositionTestData() {
    return Stream.of(
        Arguments.of(null, "Position is required"),
        Arguments.of(-1, "Position is required"),
        Arguments.of(-10, "Position is required"),
        Arguments.of(-100, "Position is required"),
        Arguments.of(Integer.MIN_VALUE, "Position is required"));
  }

  // ==================== 기본 테스트 ====================

  @Test
  @DisplayName("유효한 카드 이동 정보는 검증을 통과해야 한다")
  void validate_withValidData_shouldBeValid() {
    // given
    MoveCardCommand command = createValidCommand();

    // when
    ValidationResult<MoveCardCommand> result = moveCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }

  @Test
  @DisplayName("targetListId가 null이어도 검증을 통과해야 한다 (같은 리스트 내 이동)")
  void validate_withNullTargetListId_shouldBeValid() {
    // given
    MoveCardCommand command = new MoveCardCommand(
        new CardId(),
        null, // 같은 리스트 내 이동
        5,
        new UserId());

    // when
    ValidationResult<MoveCardCommand> result = moveCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }

  @Test
  @DisplayName("targetListId가 유효한 값이어도 검증을 통과해야 한다 (다른 리스트로 이동)")
  void validate_withValidTargetListId_shouldBeValid() {
    // given
    MoveCardCommand command = new MoveCardCommand(
        new CardId(),
        new ListId(), // 다른 리스트로 이동
        3,
        new UserId());

    // when
    ValidationResult<MoveCardCommand> result = moveCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }

  // ==================== 파라미터화 테스트 - 위치 검증 ====================

  @ParameterizedTest
  @DisplayName("유효한 위치로 카드 이동 시 검증을 통과해야 한다")
  @MethodSource("validPositionTestData")
  void validate_withValidPosition_shouldBeValid(Integer position) {
    // given
    MoveCardCommand command = new MoveCardCommand(
        new CardId(),
        new ListId(),
        position,
        new UserId());

    // when
    ValidationResult<MoveCardCommand> result = moveCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }

  @ParameterizedTest
  @DisplayName("유효하지 않은 위치로 카드 이동 시 검증에 실패해야 한다")
  @MethodSource("invalidPositionTestData")
  void validate_withInvalidPosition_shouldBeInvalid(Integer position, String expectedMessage) {
    // given
    MoveCardCommand command = new MoveCardCommand(
        new CardId(),
        new ListId(),
        position,
        new UserId());

    // when
    ValidationResult<MoveCardCommand> result = moveCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).field()).isEqualTo("newPosition");
    assertThat(result.getErrors().get(0).message()).isEqualTo(expectedMessage);
  }

  // ==================== 필수 필드 검증 ====================

  @Test
  @DisplayName("cardId가 null이면 검증에 실패해야 한다")
  void validate_withNullCardId_shouldBeInvalid() {
    // given
    MoveCardCommand command = new MoveCardCommand(
        null,
        new ListId(),
        0,
        new UserId());

    // when
    ValidationResult<MoveCardCommand> result = moveCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).field()).isEqualTo("cardId");
    assertThat(result.getErrors().get(0).message()).isEqualTo("Card ID is required");
  }

  @Test
  @DisplayName("userId가 null이면 검증에 실패해야 한다")
  void validate_withNullUserId_shouldBeInvalid() {
    // given
    MoveCardCommand command = new MoveCardCommand(
        new CardId(),
        new ListId(),
        0,
        null);

    // when
    ValidationResult<MoveCardCommand> result = moveCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).field()).isEqualTo("userId");
    assertThat(result.getErrors().get(0).message()).isEqualTo("User ID is required");
  }

  // ==================== 복합 검증 ====================

  @Test
  @DisplayName("여러 필드가 유효하지 않으면 모든 오류를 반환해야 한다")
  void validate_withMultipleInvalidFields_shouldReturnAllErrors() {
    // given
    MoveCardCommand command = new MoveCardCommand(
        null, // 카드 ID 없음
        new ListId(),
        -1, // 잘못된 위치
        null // 사용자 ID 없음
    );

    // when
    ValidationResult<MoveCardCommand> result = moveCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(3);
    assertThat(result.getErrors()).extracting("field")
        .containsExactlyInAnyOrder("cardId", "newPosition", "userId");
  }

  @Test
  @DisplayName("cardId와 position만 유효하지 않으면 해당 오류들을 반환해야 한다")
  void validate_withCardIdAndPositionInvalid_shouldReturnSpecificErrors() {
    // given
    MoveCardCommand command = new MoveCardCommand(
        null, // 카드 ID 없음
        new ListId(),
        null, // 위치 없음
        new UserId());

    // when
    ValidationResult<MoveCardCommand> result = moveCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(2);
    assertThat(result.getErrors()).extracting("field")
        .containsExactlyInAnyOrder("cardId", "newPosition");
  }

  // ==================== 경계값 테스트 ====================

  @Test
  @DisplayName("위치가 정확히 0이면 검증을 통과해야 한다")
  void validate_withZeroPosition_shouldBeValid() {
    // given
    MoveCardCommand command = new MoveCardCommand(
        new CardId(),
        new ListId(),
        0,
        new UserId());

    // when
    ValidationResult<MoveCardCommand> result = moveCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }

  @Test
  @DisplayName("위치가 Integer.MAX_VALUE이면 검증을 통과해야 한다")
  void validate_withMaxIntegerPosition_shouldBeValid() {
    // given
    MoveCardCommand command = new MoveCardCommand(
        new CardId(),
        new ListId(),
        Integer.MAX_VALUE,
        new UserId());

    // when
    ValidationResult<MoveCardCommand> result = moveCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }

  @Test
  @DisplayName("위치가 -1이면 검증에 실패해야 한다")
  void validate_withNegativeOnePosition_shouldBeInvalid() {
    // given
    MoveCardCommand command = new MoveCardCommand(
        new CardId(),
        new ListId(),
        -1,
        new UserId());

    // when
    ValidationResult<MoveCardCommand> result = moveCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).field()).isEqualTo("newPosition");
    assertThat(result.getErrors().get(0).message()).isEqualTo("Position is required");
  }

  // ==================== 특수 케이스 테스트 ====================

  @Test
  @DisplayName("같은 리스트 내 이동 시 targetListId가 null이어도 검증을 통과해야 한다")
  void validate_sameListMoveWithNullTargetListId_shouldBeValid() {
    // given
    MoveCardCommand command = new MoveCardCommand(
        new CardId(),
        null, // 같은 리스트 내 이동
        5,
        new UserId());

    // when
    ValidationResult<MoveCardCommand> result = moveCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }

  @Test
  @DisplayName("다른 리스트로 이동 시 targetListId가 유효한 값이어도 검증을 통과해야 한다")
  void validate_differentListMoveWithValidTargetListId_shouldBeValid() {
    // given
    MoveCardCommand command = new MoveCardCommand(
        new CardId(),
        new ListId(), // 다른 리스트로 이동
        0,
        new UserId());

    // when
    ValidationResult<MoveCardCommand> result = moveCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }

  @Test
  @DisplayName("큰 위치 값으로도 검증을 통과해야 한다")
  void validate_withLargePosition_shouldBeValid() {
    // given
    MoveCardCommand command = new MoveCardCommand(
        new CardId(),
        new ListId(),
        999999,
        new UserId());

    // when
    ValidationResult<MoveCardCommand> result = moveCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }

  @Test
  @DisplayName("모든 필드가 유효한 경우 검증을 통과해야 한다")
  void validate_withAllValidFields_shouldBeValid() {
    // given
    MoveCardCommand command = new MoveCardCommand(
        new CardId(),
        new ListId(),
        10,
        new UserId());

    // when
    ValidationResult<MoveCardCommand> result = moveCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }
}
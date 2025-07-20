package com.boardly.features.card.application.validation;

import com.boardly.features.card.application.port.input.DeleteCardCommand;
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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class DeleteCardValidatorTest {

  private DeleteCardValidator deleteCardValidator;

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
            case "validation.userId.required" -> "User ID is required";
            default -> key;
          };
        });

    ValidationMessageResolver messageResolver = new ValidationMessageResolver(messageSource);
    CommonValidationRules commonValidationRules = new CommonValidationRules(messageResolver);
    deleteCardValidator = new DeleteCardValidator(commonValidationRules);
  }

  private DeleteCardCommand createValidCommand() {
    return new DeleteCardCommand(
        new CardId(),
        new UserId());
  }

  // ==================== 기본 테스트 ====================

  @Test
  @DisplayName("유효한 카드 삭제 정보는 검증을 통과해야 한다")
  void validate_withValidData_shouldBeValid() {
    // given
    DeleteCardCommand command = createValidCommand();

    // when
    ValidationResult<DeleteCardCommand> result = deleteCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }

  // ==================== 필수 필드 검증 ====================

  @Test
  @DisplayName("cardId가 null이면 검증에 실패해야 한다")
  void validate_withNullCardId_shouldBeInvalid() {
    // given
    DeleteCardCommand command = new DeleteCardCommand(
        null,
        new UserId());

    // when
    ValidationResult<DeleteCardCommand> result = deleteCardValidator.validate(command);

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
    DeleteCardCommand command = new DeleteCardCommand(
        new CardId(),
        null);

    // when
    ValidationResult<DeleteCardCommand> result = deleteCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).field()).isEqualTo("userId");
    assertThat(result.getErrors().get(0).message()).isEqualTo("User ID is required");
  }

  // ==================== 복합 검증 ====================

  @Test
  @DisplayName("cardId와 userId가 모두 null이면 모든 오류를 반환해야 한다")
  void validate_withBothFieldsNull_shouldReturnAllErrors() {
    // given
    DeleteCardCommand command = new DeleteCardCommand(
        null, // 카드 ID 없음
        null // 사용자 ID 없음
    );

    // when
    ValidationResult<DeleteCardCommand> result = deleteCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(2);
    assertThat(result.getErrors()).extracting("field")
        .containsExactlyInAnyOrder("cardId", "userId");
  }

  // ==================== 특수 케이스 테스트 ====================

  @Test
  @DisplayName("유효한 CardId와 UserId로 검증을 통과해야 한다")
  void validate_withValidIds_shouldBeValid() {
    // given
    CardId cardId = new CardId();
    UserId userId = new UserId();
    DeleteCardCommand command = new DeleteCardCommand(cardId, userId);

    // when
    ValidationResult<DeleteCardCommand> result = deleteCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }

  @Test
  @DisplayName("다른 CardId와 UserId 조합으로도 검증을 통과해야 한다")
  void validate_withDifferentValidIds_shouldBeValid() {
    // given
    CardId cardId = new CardId();
    UserId userId = new UserId();
    DeleteCardCommand command = new DeleteCardCommand(cardId, userId);

    // when
    ValidationResult<DeleteCardCommand> result = deleteCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }

  @Test
  @DisplayName("정상적인 삭제 요청은 검증을 통과해야 한다")
  void validate_normalDeleteRequest_shouldBeValid() {
    // given
    DeleteCardCommand command = DeleteCardCommand.of(
        new CardId(),
        new UserId());

    // when
    ValidationResult<DeleteCardCommand> result = deleteCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }
}
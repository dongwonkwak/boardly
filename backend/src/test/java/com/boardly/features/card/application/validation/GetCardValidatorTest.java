package com.boardly.features.card.application.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import com.boardly.features.card.application.port.input.GetCardCommand;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCardValidator 테스트")
class GetCardValidatorTest {

  @Mock
  private MessageSource messageSource;

  private GetCardValidator validator;

  @BeforeEach
  void setUp() {
    LocaleContextHolder.setLocale(Locale.KOREAN);

    // MessageSource Mock 설정
    lenient().when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
        .thenAnswer(invocation -> {
          String key = invocation.getArgument(0);
          return switch (key) {
            case "validation.cardId.required" -> "카드 ID는 필수 항목입니다";
            case "validation.userId.required" -> "사용자 ID는 필수 항목입니다";
            default -> key;
          };
        });

    ValidationMessageResolver messageResolver = new ValidationMessageResolver(messageSource);
    CommonValidationRules commonValidationRules = new CommonValidationRules(messageResolver);
    validator = new GetCardValidator(commonValidationRules);
  }

  @Test
  @DisplayName("유효한 GetCardCommand는 검증을 통과해야 한다")
  void shouldPassValidationWithValidCommand() {
    // given
    CardId cardId = new CardId("card-123");
    UserId userId = new UserId("user-123");
    GetCardCommand command = new GetCardCommand(cardId, userId);

    // when
    ValidationResult<GetCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }

  @Test
  @DisplayName("null cardId가 포함된 GetCardCommand는 검증에 실패해야 한다")
  void shouldFailValidationWithNullCardId() {
    // given
    UserId userId = new UserId("user-123");
    GetCardCommand command = new GetCardCommand(null, userId);

    // when
    ValidationResult<GetCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).isNotEmpty();
  }

  @Test
  @DisplayName("null userId가 포함된 GetCardCommand는 검증에 실패해야 한다")
  void shouldFailValidationWithNullUserId() {
    // given
    CardId cardId = new CardId("card-123");
    GetCardCommand command = new GetCardCommand(cardId, null);

    // when
    ValidationResult<GetCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).isNotEmpty();
  }

  @Test
  @DisplayName("모든 필드가 null인 GetCardCommand는 검증에 실패해야 한다")
  void shouldFailValidationWithAllNullFields() {
    // given
    GetCardCommand command = new GetCardCommand(null, null);

    // when
    ValidationResult<GetCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).isNotEmpty();
  }
}
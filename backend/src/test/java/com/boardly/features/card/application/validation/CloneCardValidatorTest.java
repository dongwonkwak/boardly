package com.boardly.features.card.application.validation;

import com.boardly.features.card.application.port.input.CloneCardCommand;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.boardlist.domain.model.ListId;
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
class CloneCardValidatorTest {

  @Mock
  private ValidationMessageResolver messageResolver;

  private CloneCardValidator validator;

  @BeforeEach
  void setUp() {
    CommonValidationRules commonValidationRules = new CommonValidationRules(messageResolver);
    validator = new CloneCardValidator(commonValidationRules);
  }

  @Test
  @DisplayName("유효한 CloneCardCommand는 검증을 통과해야 한다")
  void validate_ValidCommand_ShouldPass() {
    // given
    CloneCardCommand command = CloneCardCommand.of(
        new CardId("card-123"),
        "새로운 카드 제목",
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CloneCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("targetListId가 null인 경우에도 검증을 통과해야 한다")
  void validate_NullTargetListId_ShouldPass() {
    // given
    CloneCardCommand command = CloneCardCommand.of(
        new CardId("card-123"),
        "새로운 카드 제목",
        null,
        new UserId("user-789"));

    // when
    ValidationResult<CloneCardCommand> result = validator.validate(command);

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

    CloneCardCommand command = CloneCardCommand.of(
        null,
        "새로운 카드 제목",
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CloneCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);

    var violation = result.getErrors().get(0);
    assertThat(violation.field()).isEqualTo("cardId");
    assertThat(violation.message()).isEqualTo("카드 ID는 필수입니다");
    assertThat(violation.rejectedValue()).isNull();
  }

  @Test
  @DisplayName("newTitle이 null인 경우 검증에 실패해야 한다")
  void validate_NullNewTitle_ShouldFail() {
    // given
    when(messageResolver.getMessage("validation.title.required"))
        .thenReturn("카드 제목은 필수입니다");

    CloneCardCommand command = CloneCardCommand.of(
        new CardId("card-123"),
        null,
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CloneCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);

    var violation = result.getErrors().get(0);
    assertThat(violation.field()).isEqualTo("title");
    assertThat(violation.message()).isEqualTo("카드 제목은 필수입니다");
    assertThat(violation.rejectedValue()).isNull();
  }

  @Test
  @DisplayName("newTitle이 빈 문자열인 경우 검증에 실패해야 한다")
  void validate_EmptyNewTitle_ShouldFail() {
    // given
    when(messageResolver.getMessage("validation.title.required"))
        .thenReturn("카드 제목은 필수입니다");

    CloneCardCommand command = CloneCardCommand.of(
        new CardId("card-123"),
        "",
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CloneCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);

    var violation = result.getErrors().get(0);
    assertThat(violation.field()).isEqualTo("title");
    assertThat(violation.message()).isEqualTo("카드 제목은 필수입니다");
    assertThat(violation.rejectedValue()).isEqualTo("");
  }

  @Test
  @DisplayName("newTitle이 공백만 있는 경우 검증에 실패해야 한다")
  void validate_BlankNewTitle_ShouldFail() {
    // given
    when(messageResolver.getMessage("validation.title.required"))
        .thenReturn("카드 제목은 필수입니다");

    CloneCardCommand command = CloneCardCommand.of(
        new CardId("card-123"),
        "   ",
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CloneCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);

    var violation = result.getErrors().get(0);
    assertThat(violation.field()).isEqualTo("title");
    assertThat(violation.message()).isEqualTo("카드 제목은 필수입니다");
    assertThat(violation.rejectedValue()).isEqualTo("   ");
  }

  @Test
  @DisplayName("newTitle이 200자를 초과하는 경우 검증에 실패해야 한다")
  void validate_NewTitleTooLong_ShouldFail() {
    // given
    when(messageResolver.getMessage("validation.title.max.length", 200))
        .thenReturn("카드 제목은 200자 이하여야 합니다");

    String longTitle = "a".repeat(201);
    CloneCardCommand command = CloneCardCommand.of(
        new CardId("card-123"),
        longTitle,
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CloneCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);

    var violation = result.getErrors().get(0);
    assertThat(violation.field()).isEqualTo("title");
    assertThat(violation.message()).isEqualTo("카드 제목은 200자 이하여야 합니다");
    assertThat(violation.rejectedValue()).isEqualTo(longTitle);
  }

  @Test
  @DisplayName("newTitle에 HTML 태그가 포함된 경우 검증에 실패해야 한다")
  void validate_NewTitleWithHtmlTags_ShouldFail() {
    // given
    when(messageResolver.getMessage("validation.title.invalid"))
        .thenReturn("카드 제목에 HTML 태그를 포함할 수 없습니다");

    CloneCardCommand command = CloneCardCommand.of(
        new CardId("card-123"),
        "제목 <script>alert('xss')</script>",
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CloneCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);

    var violation = result.getErrors().get(0);
    assertThat(violation.field()).isEqualTo("title");
    assertThat(violation.message()).isEqualTo("카드 제목에 HTML 태그를 포함할 수 없습니다");
    assertThat(violation.rejectedValue()).isEqualTo("제목 <script>alert('xss')</script>");
  }

  @Test
  @DisplayName("userId가 null인 경우 검증에 실패해야 한다")
  void validate_NullUserId_ShouldFail() {
    // given
    when(messageResolver.getMessage("validation.userId.required"))
        .thenReturn("사용자 ID는 필수입니다");

    CloneCardCommand command = CloneCardCommand.of(
        new CardId("card-123"),
        "새로운 카드 제목",
        new ListId("list-456"),
        null);

    // when
    ValidationResult<CloneCardCommand> result = validator.validate(command);

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
    when(messageResolver.getMessage("validation.title.required"))
        .thenReturn("카드 제목은 필수입니다");
    when(messageResolver.getMessage("validation.userId.required"))
        .thenReturn("사용자 ID는 필수입니다");

    CloneCardCommand command = CloneCardCommand.of(
        null,
        "",
        new ListId("list-456"),
        null);

    // when
    ValidationResult<CloneCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(3);

    var violations = result.getErrorsAsCollection();
    assertThat(violations).extracting("field")
        .containsExactlyInAnyOrder("cardId", "title", "userId");
    assertThat(violations).extracting("message")
        .containsExactlyInAnyOrder(
            "카드 ID는 필수입니다",
            "카드 제목은 필수입니다",
            "사용자 ID는 필수입니다");
  }

  @Test
  @DisplayName("newTitle이 정확히 200자인 경우 검증을 통과해야 한다")
  void validate_MaxLengthNewTitle_ShouldPass() {
    // given
    String maxTitle = "a".repeat(200);
    CloneCardCommand command = CloneCardCommand.of(
        new CardId("card-123"),
        maxTitle,
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CloneCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("newTitle이 1자인 경우 검증을 통과해야 한다")
  void validate_MinLengthNewTitle_ShouldPass() {
    // given
    CloneCardCommand command = CloneCardCommand.of(
        new CardId("card-123"),
        "a",
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CloneCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("newTitle에 특수문자가 포함된 경우 검증을 통과해야 한다")
  void validate_NewTitleWithSpecialCharacters_ShouldPass() {
    // given
    CloneCardCommand command = CloneCardCommand.of(
        new CardId("card-123"),
        "카드 제목!@#$%^&*()_+-=[]{}|;':\",./?",
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CloneCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("newTitle에 한글이 포함된 경우 검증을 통과해야 한다")
  void validate_NewTitleWithKorean_ShouldPass() {
    // given
    CloneCardCommand command = CloneCardCommand.of(
        new CardId("card-123"),
        "한글 카드 제목입니다",
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CloneCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("newTitle에 이모지가 포함된 경우 검증을 통과해야 한다")
  void validate_NewTitleWithEmoji_ShouldPass() {
    // given
    CloneCardCommand command = CloneCardCommand.of(
        new CardId("card-123"),
        "카드 제목 🎉✨",
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CloneCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }
}
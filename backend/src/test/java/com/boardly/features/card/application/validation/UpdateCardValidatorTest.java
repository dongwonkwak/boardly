package com.boardly.features.card.application.validation;

import com.boardly.features.card.application.port.input.UpdateCardCommand;
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
class UpdateCardValidatorTest {

  @Mock
  private ValidationMessageResolver messageResolver;

  private UpdateCardValidator validator;

  @BeforeEach
  void setUp() {
    CommonValidationRules commonValidationRules = new CommonValidationRules(messageResolver);
    validator = new UpdateCardValidator(commonValidationRules);
  }

  @Test
  @DisplayName("유효한 UpdateCardCommand는 검증을 통과해야 한다")
  void validate_ValidCommand_ShouldPass() {
    // given
    UpdateCardCommand command = UpdateCardCommand.of(
        new CardId("card-123"),
        "수정된 카드 제목",
        "수정된 카드 설명입니다",
        new UserId("user-789"));

    // when
    ValidationResult<UpdateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("description이 null인 경우에도 검증을 통과해야 한다")
  void validate_NullDescription_ShouldPass() {
    // given
    UpdateCardCommand command = UpdateCardCommand.of(
        new CardId("card-123"),
        "수정된 카드 제목",
        null,
        new UserId("user-789"));

    // when
    ValidationResult<UpdateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("description이 빈 문자열인 경우에도 검증을 통과해야 한다")
  void validate_EmptyDescription_ShouldPass() {
    // given
    UpdateCardCommand command = UpdateCardCommand.of(
        new CardId("card-123"),
        "수정된 카드 제목",
        "",
        new UserId("user-789"));

    // when
    ValidationResult<UpdateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("title이 null인 경우 검증에 실패해야 한다")
  void validate_NullTitle_ShouldFail() {
    // given
    when(messageResolver.getMessage("validation.title.required"))
        .thenReturn("카드 제목은 필수입니다");

    UpdateCardCommand command = UpdateCardCommand.of(
        new CardId("card-123"),
        null,
        "수정된 카드 설명입니다",
        new UserId("user-789"));

    // when
    ValidationResult<UpdateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);

    var violation = result.getErrors().get(0);
    assertThat(violation.field()).isEqualTo("title");
    assertThat(violation.message()).isEqualTo("카드 제목은 필수입니다");
    assertThat(violation.rejectedValue()).isNull();
  }

  @Test
  @DisplayName("title이 빈 문자열인 경우 검증에 실패해야 한다")
  void validate_EmptyTitle_ShouldFail() {
    // given
    when(messageResolver.getMessage("validation.title.required"))
        .thenReturn("카드 제목은 필수입니다");

    UpdateCardCommand command = UpdateCardCommand.of(
        new CardId("card-123"),
        "",
        "수정된 카드 설명입니다",
        new UserId("user-789"));

    // when
    ValidationResult<UpdateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);

    var violation = result.getErrors().get(0);
    assertThat(violation.field()).isEqualTo("title");
    assertThat(violation.message()).isEqualTo("카드 제목은 필수입니다");
    assertThat(violation.rejectedValue()).isEqualTo("");
  }

  @Test
  @DisplayName("title이 공백만 있는 경우 검증에 실패해야 한다")
  void validate_BlankTitle_ShouldFail() {
    // given
    when(messageResolver.getMessage("validation.title.required"))
        .thenReturn("카드 제목은 필수입니다");

    UpdateCardCommand command = UpdateCardCommand.of(
        new CardId("card-123"),
        "   ",
        "수정된 카드 설명입니다",
        new UserId("user-789"));

    // when
    ValidationResult<UpdateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);

    var violation = result.getErrors().get(0);
    assertThat(violation.field()).isEqualTo("title");
    assertThat(violation.message()).isEqualTo("카드 제목은 필수입니다");
    assertThat(violation.rejectedValue()).isEqualTo("");
  }

  @Test
  @DisplayName("title이 200자를 초과하는 경우 검증에 실패해야 한다")
  void validate_TitleTooLong_ShouldFail() {
    // given
    when(messageResolver.getMessage("validation.title.max.length", 200))
        .thenReturn("카드 제목은 200자 이하여야 합니다");

    String longTitle = "a".repeat(201);
    UpdateCardCommand command = UpdateCardCommand.of(
        new CardId("card-123"),
        longTitle,
        "수정된 카드 설명입니다",
        new UserId("user-789"));

    // when
    ValidationResult<UpdateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);

    var violation = result.getErrors().get(0);
    assertThat(violation.field()).isEqualTo("title");
    assertThat(violation.message()).isEqualTo("카드 제목은 200자 이하여야 합니다");
    assertThat(violation.rejectedValue()).isEqualTo(longTitle);
  }

  @Test
  @DisplayName("title에 HTML 태그가 포함된 경우 검증에 실패해야 한다")
  void validate_TitleWithHtmlTags_ShouldFail() {
    // given
    when(messageResolver.getMessage("validation.title.invalid"))
        .thenReturn("카드 제목에 HTML 태그는 허용되지 않습니다");

    UpdateCardCommand command = UpdateCardCommand.of(
        new CardId("card-123"),
        "<script>alert('xss')</script>",
        "수정된 카드 설명입니다",
        new UserId("user-789"));

    // when
    ValidationResult<UpdateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);

    var violation = result.getErrors().get(0);
    assertThat(violation.field()).isEqualTo("title");
    assertThat(violation.message()).isEqualTo("카드 제목에 HTML 태그는 허용되지 않습니다");
    assertThat(violation.rejectedValue()).isEqualTo("<script>alert('xss')</script>");
  }

  @Test
  @DisplayName("description이 2000자를 초과하는 경우 검증에 실패해야 한다")
  void validate_DescriptionTooLong_ShouldFail() {
    // given
    when(messageResolver.getMessage("validation.description.max.length", 2000))
        .thenReturn("카드 설명은 2000자 이하여야 합니다");

    String longDescription = "a".repeat(2001);
    UpdateCardCommand command = UpdateCardCommand.of(
        new CardId("card-123"),
        "수정된 카드 제목",
        longDescription,
        new UserId("user-789"));

    // when
    ValidationResult<UpdateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);

    var violation = result.getErrors().get(0);
    assertThat(violation.field()).isEqualTo("description");
    assertThat(violation.message()).isEqualTo("카드 설명은 2000자 이하여야 합니다");
    assertThat(violation.rejectedValue()).isEqualTo(longDescription);
  }

  @Test
  @DisplayName("description에 HTML 태그가 포함된 경우 검증에 실패해야 한다")
  void validate_DescriptionWithHtmlTags_ShouldFail() {
    // given
    when(messageResolver.getMessage("validation.description.invalid"))
        .thenReturn("카드 설명에 HTML 태그는 허용되지 않습니다");

    UpdateCardCommand command = UpdateCardCommand.of(
        new CardId("card-123"),
        "수정된 카드 제목",
        "<b>굵은 텍스트</b>",
        new UserId("user-789"));

    // when
    ValidationResult<UpdateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);

    var violation = result.getErrors().get(0);
    assertThat(violation.field()).isEqualTo("description");
    assertThat(violation.message()).isEqualTo("카드 설명에 HTML 태그는 허용되지 않습니다");
    assertThat(violation.rejectedValue()).isEqualTo("<b>굵은 텍스트</b>");
  }

  @Test
  @DisplayName("cardId가 null인 경우 검증에 실패해야 한다")
  void validate_NullCardId_ShouldFail() {
    // given
    when(messageResolver.getMessage("validation.cardId.required"))
        .thenReturn("카드 ID는 필수입니다");

    UpdateCardCommand command = UpdateCardCommand.of(
        null,
        "수정된 카드 제목",
        "수정된 카드 설명입니다",
        new UserId("user-789"));

    // when
    ValidationResult<UpdateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);

    var violation = result.getErrors().get(0);
    assertThat(violation.field()).isEqualTo("cardId");
    assertThat(violation.message()).isEqualTo("카드 ID는 필수입니다");
    assertThat(violation.rejectedValue()).isNull();
  }

  @Test
  @DisplayName("userId가 null인 경우 검증에 실패해야 한다")
  void validate_NullUserId_ShouldFail() {
    // given
    when(messageResolver.getMessage("validation.userId.required"))
        .thenReturn("사용자 ID는 필수입니다");

    UpdateCardCommand command = UpdateCardCommand.of(
        new CardId("card-123"),
        "수정된 카드 제목",
        "수정된 카드 설명입니다",
        null);

    // when
    ValidationResult<UpdateCardCommand> result = validator.validate(command);

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

    UpdateCardCommand command = UpdateCardCommand.of(
        null,
        null,
        "수정된 카드 설명입니다",
        null);

    // when
    ValidationResult<UpdateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(3);

    var errors = result.getErrors();
    assertThat(errors).anyMatch(error -> error.field().equals("cardId") &&
        error.message().equals("카드 ID는 필수입니다"));
    assertThat(errors).anyMatch(error -> error.field().equals("title") &&
        error.message().equals("카드 제목은 필수입니다"));
    assertThat(errors).anyMatch(error -> error.field().equals("userId") &&
        error.message().equals("사용자 ID는 필수입니다"));
  }

  @Test
  @DisplayName("title이 정확히 200자인 경우 검증을 통과해야 한다")
  void validate_MaxLengthTitle_ShouldPass() {
    // given
    String maxLengthTitle = "a".repeat(200);
    UpdateCardCommand command = UpdateCardCommand.of(
        new CardId("card-123"),
        maxLengthTitle,
        "수정된 카드 설명입니다",
        new UserId("user-789"));

    // when
    ValidationResult<UpdateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("title이 1자인 경우 검증을 통과해야 한다")
  void validate_MinLengthTitle_ShouldPass() {
    // given
    UpdateCardCommand command = UpdateCardCommand.of(
        new CardId("card-123"),
        "a",
        "수정된 카드 설명입니다",
        new UserId("user-789"));

    // when
    ValidationResult<UpdateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("description이 정확히 2000자인 경우 검증을 통과해야 한다")
  void validate_MaxLengthDescription_ShouldPass() {
    // given
    String maxLengthDescription = "a".repeat(2000);
    UpdateCardCommand command = UpdateCardCommand.of(
        new CardId("card-123"),
        "수정된 카드 제목",
        maxLengthDescription,
        new UserId("user-789"));

    // when
    ValidationResult<UpdateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("title에 특수문자가 포함된 경우 검증을 통과해야 한다")
  void validate_TitleWithSpecialCharacters_ShouldPass() {
    // given
    UpdateCardCommand command = UpdateCardCommand.of(
        new CardId("card-123"),
        "수정된 카드 제목!@#$%^&*()",
        "수정된 카드 설명입니다",
        new UserId("user-789"));

    // when
    ValidationResult<UpdateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("title에 한글이 포함된 경우 검증을 통과해야 한다")
  void validate_TitleWithKorean_ShouldPass() {
    // given
    UpdateCardCommand command = UpdateCardCommand.of(
        new CardId("card-123"),
        "수정된 카드 제목입니다",
        "수정된 카드 설명입니다",
        new UserId("user-789"));

    // when
    ValidationResult<UpdateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("title에 이모지가 포함된 경우 검증을 통과해야 한다")
  void validate_TitleWithEmoji_ShouldPass() {
    // given
    UpdateCardCommand command = UpdateCardCommand.of(
        new CardId("card-123"),
        "수정된 카드 제목 🎉",
        "수정된 카드 설명입니다",
        new UserId("user-789"));

    // when
    ValidationResult<UpdateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("description에 특수문자가 포함된 경우 검증을 통과해야 한다")
  void validate_DescriptionWithSpecialCharacters_ShouldPass() {
    // given
    UpdateCardCommand command = UpdateCardCommand.of(
        new CardId("card-123"),
        "수정된 카드 제목",
        "수정된 카드 설명!@#$%^&*()",
        new UserId("user-789"));

    // when
    ValidationResult<UpdateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("description에 한글이 포함된 경우 검증을 통과해야 한다")
  void validate_DescriptionWithKorean_ShouldPass() {
    // given
    UpdateCardCommand command = UpdateCardCommand.of(
        new CardId("card-123"),
        "수정된 카드 제목",
        "수정된 카드 설명입니다",
        new UserId("user-789"));

    // when
    ValidationResult<UpdateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("description에 이모지가 포함된 경우 검증을 통과해야 한다")
  void validate_DescriptionWithEmoji_ShouldPass() {
    // given
    UpdateCardCommand command = UpdateCardCommand.of(
        new CardId("card-123"),
        "수정된 카드 제목",
        "수정된 카드 설명 🎉",
        new UserId("user-789"));

    // when
    ValidationResult<UpdateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }
}
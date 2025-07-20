package com.boardly.features.card.application.validation;

import com.boardly.features.card.application.port.input.CreateCardCommand;
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
class CreateCardValidatorTest {

  @Mock
  private ValidationMessageResolver messageResolver;

  private CreateCardValidator validator;

  @BeforeEach
  void setUp() {
    CommonValidationRules commonValidationRules = new CommonValidationRules(messageResolver);
    validator = new CreateCardValidator(commonValidationRules);
  }

  @Test
  @DisplayName("유효한 CreateCardCommand는 검증을 통과해야 한다")
  void validate_ValidCommand_ShouldPass() {
    // given
    CreateCardCommand command = CreateCardCommand.of(
        "새로운 카드 제목",
        "카드 설명입니다",
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CreateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("description이 null인 경우에도 검증을 통과해야 한다")
  void validate_NullDescription_ShouldPass() {
    // given
    CreateCardCommand command = CreateCardCommand.of(
        "새로운 카드 제목",
        null,
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CreateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("description이 빈 문자열인 경우에도 검증을 통과해야 한다")
  void validate_EmptyDescription_ShouldPass() {
    // given
    CreateCardCommand command = CreateCardCommand.of(
        "새로운 카드 제목",
        "",
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CreateCardCommand> result = validator.validate(command);

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

    CreateCardCommand command = CreateCardCommand.of(
        null,
        "카드 설명입니다",
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CreateCardCommand> result = validator.validate(command);

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

    CreateCardCommand command = CreateCardCommand.of(
        "",
        "카드 설명입니다",
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CreateCardCommand> result = validator.validate(command);

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

    CreateCardCommand command = CreateCardCommand.of(
        "   ",
        "카드 설명입니다",
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CreateCardCommand> result = validator.validate(command);

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
    CreateCardCommand command = CreateCardCommand.of(
        longTitle,
        "카드 설명입니다",
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CreateCardCommand> result = validator.validate(command);

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

    CreateCardCommand command = CreateCardCommand.of(
        "<script>alert('xss')</script>",
        "카드 설명입니다",
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CreateCardCommand> result = validator.validate(command);

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
    CreateCardCommand command = CreateCardCommand.of(
        "카드 제목",
        longDescription,
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CreateCardCommand> result = validator.validate(command);

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

    CreateCardCommand command = CreateCardCommand.of(
        "카드 제목",
        "<b>굵은 텍스트</b>",
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CreateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);

    var violation = result.getErrors().get(0);
    assertThat(violation.field()).isEqualTo("description");
    assertThat(violation.message()).isEqualTo("카드 설명에 HTML 태그는 허용되지 않습니다");
    assertThat(violation.rejectedValue()).isEqualTo("<b>굵은 텍스트</b>");
  }

  @Test
  @DisplayName("listId가 null인 경우 검증에 실패해야 한다")
  void validate_NullListId_ShouldFail() {
    // given
    when(messageResolver.getMessage("validation.listId.required"))
        .thenReturn("리스트 ID는 필수입니다");

    CreateCardCommand command = CreateCardCommand.of(
        "카드 제목",
        "카드 설명입니다",
        null,
        new UserId("user-789"));

    // when
    ValidationResult<CreateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);

    var violation = result.getErrors().get(0);
    assertThat(violation.field()).isEqualTo("listId");
    assertThat(violation.message()).isEqualTo("리스트 ID는 필수입니다");
    assertThat(violation.rejectedValue()).isNull();
  }

  @Test
  @DisplayName("userId가 null인 경우 검증에 실패해야 한다")
  void validate_NullUserId_ShouldFail() {
    // given
    when(messageResolver.getMessage("validation.userId.required"))
        .thenReturn("사용자 ID는 필수입니다");

    CreateCardCommand command = CreateCardCommand.of(
        "카드 제목",
        "카드 설명입니다",
        new ListId("list-456"),
        null);

    // when
    ValidationResult<CreateCardCommand> result = validator.validate(command);

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
    when(messageResolver.getMessage("validation.title.required"))
        .thenReturn("카드 제목은 필수입니다");
    when(messageResolver.getMessage("validation.listId.required"))
        .thenReturn("리스트 ID는 필수입니다");
    when(messageResolver.getMessage("validation.userId.required"))
        .thenReturn("사용자 ID는 필수입니다");

    CreateCardCommand command = CreateCardCommand.of(
        null,
        "카드 설명입니다",
        null,
        null);

    // when
    ValidationResult<CreateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(3);

    var errors = result.getErrors();
    assertThat(errors).anyMatch(error -> error.field().equals("title") &&
        error.message().equals("카드 제목은 필수입니다"));
    assertThat(errors).anyMatch(error -> error.field().equals("listId") &&
        error.message().equals("리스트 ID는 필수입니다"));
    assertThat(errors).anyMatch(error -> error.field().equals("userId") &&
        error.message().equals("사용자 ID는 필수입니다"));
  }

  @Test
  @DisplayName("title이 정확히 200자인 경우 검증을 통과해야 한다")
  void validate_MaxLengthTitle_ShouldPass() {
    // given
    String maxLengthTitle = "a".repeat(200);
    CreateCardCommand command = CreateCardCommand.of(
        maxLengthTitle,
        "카드 설명입니다",
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CreateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("title이 1자인 경우 검증을 통과해야 한다")
  void validate_MinLengthTitle_ShouldPass() {
    // given
    CreateCardCommand command = CreateCardCommand.of(
        "a",
        "카드 설명입니다",
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CreateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("description이 정확히 2000자인 경우 검증을 통과해야 한다")
  void validate_MaxLengthDescription_ShouldPass() {
    // given
    String maxLengthDescription = "a".repeat(2000);
    CreateCardCommand command = CreateCardCommand.of(
        "카드 제목",
        maxLengthDescription,
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CreateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("title에 특수문자가 포함된 경우 검증을 통과해야 한다")
  void validate_TitleWithSpecialCharacters_ShouldPass() {
    // given
    CreateCardCommand command = CreateCardCommand.of(
        "카드 제목!@#$%^&*()",
        "카드 설명입니다",
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CreateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("title에 한글이 포함된 경우 검증을 통과해야 한다")
  void validate_TitleWithKorean_ShouldPass() {
    // given
    CreateCardCommand command = CreateCardCommand.of(
        "새로운 카드 제목입니다",
        "카드 설명입니다",
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CreateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }

  @Test
  @DisplayName("title에 이모지가 포함된 경우 검증을 통과해야 한다")
  void validate_TitleWithEmoji_ShouldPass() {
    // given
    CreateCardCommand command = CreateCardCommand.of(
        "카드 제목 🎉",
        "카드 설명입니다",
        new ListId("list-456"),
        new UserId("user-789"));

    // when
    ValidationResult<CreateCardCommand> result = validator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
    assertThat(result.get()).isEqualTo(command);
  }
}
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
class UpdateCardValidatorTest {

  private UpdateCardValidator updateCardValidator;

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
            case "validation.title.required" -> "Card title is required";
            case "validation.title.min.length" -> "Card title must be at least 1 character long";
            case "validation.title.max.length" -> "Card title must be no more than 200 characters long";
            case "validation.title.invalid" -> "Card title cannot contain HTML tags";
            case "validation.description.max.length" -> "Card description must be no more than 2000 characters long";
            case "validation.description.invalid" -> "Card description cannot contain HTML tags";
            case "validation.userId.required" -> "User ID is required";
            default -> key;
          };
        });

    ValidationMessageResolver messageResolver = new ValidationMessageResolver(messageSource);
    CommonValidationRules commonValidationRules = new CommonValidationRules(messageResolver);
    updateCardValidator = new UpdateCardValidator(commonValidationRules);
  }

  private UpdateCardCommand createValidCommand() {
    return new UpdateCardCommand(
        new CardId(),
        "유효한 카드 제목",
        "유효한 카드 설명",
        new UserId());
  }

  // ==================== 파라미터화 테스트 데이터 제공 메서드들 ====================

  private static Stream<Arguments> invalidTitleTestData() {
    return Stream.of(
        Arguments.of(null, "Card title is required"),
        Arguments.of("", "Card title is required"),
        Arguments.of("   ", "Card title is required"));
  }

  private static Stream<Arguments> tooLongTitleTestData() {
    return Stream.of(
        Arguments.of("a".repeat(201), "Card title must be no more than 200 characters long"));
  }

  private static Stream<Arguments> htmlTagTitleTestData() {
    return Stream.of(
        Arguments.of("<script>alert('test')</script>", "Card title cannot contain HTML tags"),
        Arguments.of("<div>내용</div>", "Card title cannot contain HTML tags"),
        Arguments.of("<p>단락</p>", "Card title cannot contain HTML tags"),
        Arguments.of("<b>굵게</b>", "Card title cannot contain HTML tags"),
        Arguments.of("<i>기울임</i>", "Card title cannot contain HTML tags"));
  }

  private static Stream<Arguments> validTitleTestData() {
    return Stream.of(
        Arguments.of("프로젝트-2024_v1.0 (최종)!"),
        Arguments.of("한글 제목 테스트"),
        Arguments.of("English Title Test"),
        Arguments.of("프로젝트 2024 1차"),
        Arguments.of("a".repeat(200)), // 정확히 200자
        Arguments.of("a"), // 1자
        Arguments.of("카드 제목"),
        Arguments.of("Task #123"),
        Arguments.of("버그 수정"),
        Arguments.of("새로운 기능 추가"));
  }

  private static Stream<Arguments> htmlTagDescriptionTestData() {
    return Stream.of(
        Arguments.of("<script>alert('test')</script>", "Card description cannot contain HTML tags"),
        Arguments.of("<div>내용</div>", "Card description cannot contain HTML tags"),
        Arguments.of("<p>단락</p>", "Card description cannot contain HTML tags"),
        Arguments.of("<b>굵게</b>", "Card description cannot contain HTML tags"),
        Arguments.of("<i>기울임</i>", "Card description cannot contain HTML tags"));
  }

  private static Stream<Arguments> validDescriptionTestData() {
    return Stream.of(
        Arguments.of("a".repeat(2000)), // 정확히 2000자
        Arguments.of("유효한 설명"),
        Arguments.of(""),
        Arguments.of((String) null),
        Arguments.of("마크다운 형식의 설명\n- 항목 1\n- 항목 2"),
        Arguments.of("긴 설명 " + "a".repeat(1990)));
  }

  private static Stream<Arguments> tooLongDescriptionTestData() {
    return Stream.of(
        Arguments.of("a".repeat(2001), "Card description must be no more than 2000 characters long"));
  }

  // ==================== 기본 테스트 ====================

  @Test
  @DisplayName("유효한 카드 수정 정보는 검증을 통과해야 한다")
  void validate_withValidData_shouldBeValid() {
    // given
    UpdateCardCommand command = createValidCommand();

    // when
    ValidationResult<UpdateCardCommand> result = updateCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }

  @Test
  @DisplayName("설명이 null이어도 검증을 통과해야 한다 (optional)")
  void validate_withNullDescription_shouldBeValid() {
    // given
    UpdateCardCommand command = new UpdateCardCommand(
        new CardId(),
        "유효한 카드 제목",
        null,
        new UserId());

    // when
    ValidationResult<UpdateCardCommand> result = updateCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }

  @Test
  @DisplayName("설명이 빈 문자열이어도 검증을 통과해야 한다")
  void validate_withEmptyDescription_shouldBeValid() {
    // given
    UpdateCardCommand command = new UpdateCardCommand(
        new CardId(),
        "유효한 카드 제목",
        "",
        new UserId());

    // when
    ValidationResult<UpdateCardCommand> result = updateCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }

  // ==================== 파라미터화 테스트 - 제목 검증 ====================

  @ParameterizedTest
  @DisplayName("유효한 제목으로 카드 수정 시 검증을 통과해야 한다")
  @MethodSource("validTitleTestData")
  void validate_withValidTitle_shouldBeValid(String title) {
    // given
    UpdateCardCommand command = new UpdateCardCommand(
        new CardId(),
        title,
        "설명",
        new UserId());

    // when
    ValidationResult<UpdateCardCommand> result = updateCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }

  @ParameterizedTest
  @DisplayName("유효하지 않은 제목으로 카드 수정 시 검증에 실패해야 한다")
  @MethodSource("invalidTitleTestData")
  void validate_withInvalidTitle_shouldBeInvalid(String title, String expectedMessage) {
    // given
    UpdateCardCommand command = new UpdateCardCommand(
        new CardId(),
        title,
        "설명",
        new UserId());

    // when
    ValidationResult<UpdateCardCommand> result = updateCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).field()).isEqualTo("title");
    assertThat(result.getErrors().get(0).message()).isEqualTo(expectedMessage);
  }

  @ParameterizedTest
  @DisplayName("너무 긴 제목으로 카드 수정 시 검증에 실패해야 한다")
  @MethodSource("tooLongTitleTestData")
  void validate_withTooLongTitle_shouldBeInvalid(String title, String expectedMessage) {
    // given
    UpdateCardCommand command = new UpdateCardCommand(
        new CardId(),
        title,
        "설명",
        new UserId());

    // when
    ValidationResult<UpdateCardCommand> result = updateCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).field()).isEqualTo("title");
    assertThat(result.getErrors().get(0).message()).isEqualTo(expectedMessage);
  }

  @ParameterizedTest
  @DisplayName("HTML 태그가 포함된 제목으로 카드 수정 시 검증에 실패해야 한다")
  @MethodSource("htmlTagTitleTestData")
  void validate_withHtmlTagTitle_shouldBeInvalid(String title, String expectedMessage) {
    // given
    UpdateCardCommand command = new UpdateCardCommand(
        new CardId(),
        title,
        "설명",
        new UserId());

    // when
    ValidationResult<UpdateCardCommand> result = updateCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).field()).isEqualTo("title");
    assertThat(result.getErrors().get(0).message()).isEqualTo(expectedMessage);
  }

  // ==================== 파라미터화 테스트 - 설명 검증 ====================

  @ParameterizedTest
  @DisplayName("유효한 설명으로 카드 수정 시 검증을 통과해야 한다")
  @MethodSource("validDescriptionTestData")
  void validate_withValidDescription_shouldBeValid(String description) {
    // given
    UpdateCardCommand command = new UpdateCardCommand(
        new CardId(),
        "유효한 카드 제목",
        description,
        new UserId());

    // when
    ValidationResult<UpdateCardCommand> result = updateCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }

  @ParameterizedTest
  @DisplayName("HTML 태그가 포함된 설명으로 카드 수정 시 검증에 실패해야 한다")
  @MethodSource("htmlTagDescriptionTestData")
  void validate_withHtmlTagDescription_shouldBeInvalid(String description, String expectedMessage) {
    // given
    UpdateCardCommand command = new UpdateCardCommand(
        new CardId(),
        "유효한 카드 제목",
        description,
        new UserId());

    // when
    ValidationResult<UpdateCardCommand> result = updateCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).field()).isEqualTo("description");
    assertThat(result.getErrors().get(0).message()).isEqualTo(expectedMessage);
  }

  @ParameterizedTest
  @DisplayName("너무 긴 설명으로 카드 수정 시 검증에 실패해야 한다")
  @MethodSource("tooLongDescriptionTestData")
  void validate_withTooLongDescription_shouldBeInvalid(String description, String expectedMessage) {
    // given
    UpdateCardCommand command = new UpdateCardCommand(
        new CardId(),
        "유효한 카드 제목",
        description,
        new UserId());

    // when
    ValidationResult<UpdateCardCommand> result = updateCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).field()).isEqualTo("description");
    assertThat(result.getErrors().get(0).message()).isEqualTo(expectedMessage);
  }

  // ==================== 필수 필드 검증 ====================

  @Test
  @DisplayName("cardId가 null이면 검증에 실패해야 한다")
  void validate_withNullCardId_shouldBeInvalid() {
    // given
    UpdateCardCommand command = new UpdateCardCommand(
        null,
        "유효한 카드 제목",
        "설명",
        new UserId());

    // when
    ValidationResult<UpdateCardCommand> result = updateCardValidator.validate(command);

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
    UpdateCardCommand command = new UpdateCardCommand(
        new CardId(),
        "유효한 카드 제목",
        "설명",
        null);

    // when
    ValidationResult<UpdateCardCommand> result = updateCardValidator.validate(command);

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
    UpdateCardCommand command = new UpdateCardCommand(
        null, // 카드 ID 없음
        null, // 제목 없음
        "<script>alert('test')</script>", // HTML 태그 포함
        null // 사용자 ID 없음
    );

    // when
    ValidationResult<UpdateCardCommand> result = updateCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(4);
    assertThat(result.getErrors()).extracting("field")
        .containsExactlyInAnyOrder("cardId", "title", "description", "userId");
  }

  @Test
  @DisplayName("제목에 여러 문제가 있으면 첫 번째 오류만 반환해야 한다")
  void validate_withMultipleTitleIssues_shouldReturnFirstError() {
    // given
    UpdateCardCommand command = new UpdateCardCommand(
        new CardId(),
        "", // 빈 제목 (첫 번째 오류)
        "설명",
        new UserId());

    // when
    ValidationResult<UpdateCardCommand> result = updateCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).field()).isEqualTo("title");
    assertThat(result.getErrors().get(0).message()).isEqualTo("Card title is required");
  }

  // ==================== 경계값 테스트 ====================

  @Test
  @DisplayName("제목이 정확히 최대 길이(200자)면 검증을 통과해야 한다")
  void validate_withMaxLengthTitle_shouldBeValid() {
    // given
    String maxLengthTitle = "a".repeat(200);
    UpdateCardCommand command = new UpdateCardCommand(
        new CardId(),
        maxLengthTitle,
        "설명",
        new UserId());

    // when
    ValidationResult<UpdateCardCommand> result = updateCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }

  @Test
  @DisplayName("제목이 1자면 검증을 통과해야 한다")
  void validate_withOneCharacterTitle_shouldBeValid() {
    // given
    UpdateCardCommand command = new UpdateCardCommand(
        new CardId(),
        "a",
        "설명",
        new UserId());

    // when
    ValidationResult<UpdateCardCommand> result = updateCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }

  @Test
  @DisplayName("설명이 정확히 최대 길이(2000자)면 검증을 통과해야 한다")
  void validate_withMaxLengthDescription_shouldBeValid() {
    // given
    String maxLengthDescription = "a".repeat(2000);
    UpdateCardCommand command = new UpdateCardCommand(
        new CardId(),
        "유효한 카드 제목",
        maxLengthDescription,
        new UserId());

    // when
    ValidationResult<UpdateCardCommand> result = updateCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }

  // ==================== 특수 케이스 테스트 ====================

  @Test
  @DisplayName("한글 제목은 검증을 통과해야 한다")
  void validate_withKoreanTitle_shouldBeValid() {
    // given
    UpdateCardCommand command = new UpdateCardCommand(
        new CardId(),
        "한글 카드 제목 테스트",
        "설명",
        new UserId());

    // when
    ValidationResult<UpdateCardCommand> result = updateCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }

  @Test
  @DisplayName("영어 제목은 검증을 통과해야 한다")
  void validate_withEnglishTitle_shouldBeValid() {
    // given
    UpdateCardCommand command = new UpdateCardCommand(
        new CardId(),
        "English Card Title Test",
        "설명",
        new UserId());

    // when
    ValidationResult<UpdateCardCommand> result = updateCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }

  @Test
  @DisplayName("숫자가 포함된 제목은 검증을 통과해야 한다")
  void validate_withNumbersInTitle_shouldBeValid() {
    // given
    UpdateCardCommand command = new UpdateCardCommand(
        new CardId(),
        "Task #123 - 버그 수정",
        "설명",
        new UserId());

    // when
    ValidationResult<UpdateCardCommand> result = updateCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }

  @Test
  @DisplayName("특수문자가 포함된 제목은 검증을 통과해야 한다")
  void validate_withSpecialCharactersInTitle_shouldBeValid() {
    // given
    UpdateCardCommand command = new UpdateCardCommand(
        new CardId(),
        "프로젝트-2024_v1.0 (최종)!",
        "설명",
        new UserId());

    // when
    ValidationResult<UpdateCardCommand> result = updateCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }

  @Test
  @DisplayName("마크다운 형식의 설명은 검증을 통과해야 한다")
  void validate_withMarkdownDescription_shouldBeValid() {
    // given
    UpdateCardCommand command = new UpdateCardCommand(
        new CardId(),
        "유효한 카드 제목",
        "# 제목\n\n- 항목 1\n- 항목 2\n\n**굵은 텍스트**와 *기울임 텍스트*",
        new UserId());

    // when
    ValidationResult<UpdateCardCommand> result = updateCardValidator.validate(command);

    // then
    assertThat(result.isValid()).isTrue();
  }
}
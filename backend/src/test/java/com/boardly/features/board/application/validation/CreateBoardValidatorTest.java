package com.boardly.features.board.application.validation;

import com.boardly.features.board.application.port.input.CreateBoardCommand;
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
class CreateBoardValidatorTest {

    private CreateBoardValidator createBoardValidator;

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
                    case "validation.title.required" -> "Board title is required";
                    case "validation.title.max.length" -> "Board title must be no more than 50 characters long";
                    case "validation.title.invalid" -> "Board title cannot contain HTML tags";
                    case "validation.description.invalid" -> "Board description cannot contain HTML tags";
                    case "validation.userId.required" -> "Board owner is required";
                    default -> key;
                };
            });

        ValidationMessageResolver messageResolver = new ValidationMessageResolver(messageSource);
        CommonValidationRules commonValidationRules = new CommonValidationRules(messageResolver);
        createBoardValidator = new CreateBoardValidator(commonValidationRules);
    }

    private CreateBoardCommand createValidCommand() {
        return new CreateBoardCommand(
            "유효한 보드 제목",
            "유효한 보드 설명",
            new UserId()
        );
    }

    // ==================== 파라미터화 테스트 데이터 제공 메서드들 ====================

    private static Stream<Arguments> invalidTitleTestData() {
        return Stream.of(
            Arguments.of(null, "Board title is required"),
            Arguments.of("", "Board title is required"),
            Arguments.of("   ", "Board title is required")
        );
    }

    private static Stream<Arguments> tooLongTitleTestData() {
        return Stream.of(
            Arguments.of("a".repeat(51), "Board title must be no more than 50 characters long")
        );
    }

    private static Stream<Arguments> htmlTagTitleTestData() {
        return Stream.of(
            Arguments.of("<script>", "Board title cannot contain HTML tags"),
            Arguments.of("<div>", "Board title cannot contain HTML tags"),
            Arguments.of("<p>", "Board title cannot contain HTML tags")
        );
    }

    private static Stream<Arguments> validTitleTestData() {
        return Stream.of(
            Arguments.of("프로젝트-2024_v1.0 (최종)!"),
            Arguments.of("한글 제목 테스트"),
            Arguments.of("English Title Test"),
            Arguments.of("프로젝트 2024 1차"),
            Arguments.of("a".repeat(50)), // 정확히 50자
            Arguments.of("a") // 1자
        );
    }

    private static Stream<Arguments> htmlTagDescriptionTestData() {
        return Stream.of(
            Arguments.of("<script>", "Board description cannot contain HTML tags"),
            Arguments.of("<div>", "Board description cannot contain HTML tags"),
            Arguments.of("<p>", "Board description cannot contain HTML tags")
        );
    }

    private static Stream<Arguments> validDescriptionTestData() {
        return Stream.of(
            Arguments.of("a".repeat(500)), // 정확히 500자
            Arguments.of("유효한 설명"),
            Arguments.of(""),
            Arguments.of((String) null)
        );
    }

    // ==================== 기본 테스트 ====================

    @Test
    @DisplayName("유효한 보드 생성 정보는 검증을 통과해야 한다")
    void validate_withValidData_shouldBeValid() {
        // given
        CreateBoardCommand command = createValidCommand();

        // when
        ValidationResult<CreateBoardCommand> result = createBoardValidator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("설명이 null이어도 검증을 통과해야 한다 (optional)")
    void validate_withNullDescription_shouldBeValid() {
        // given
        CreateBoardCommand command = new CreateBoardCommand(
            "유효한 보드 제목",
            null,
            new UserId()
        );

        // when
        ValidationResult<CreateBoardCommand> result = createBoardValidator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("설명이 빈 문자열이어도 검증을 통과해야 한다")
    void validate_withEmptyDescription_shouldBeValid() {
        // given
        CreateBoardCommand command = new CreateBoardCommand(
            "유효한 보드 제목",
            "",
            new UserId()
        );

        // when
        ValidationResult<CreateBoardCommand> result = createBoardValidator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    // ==================== 파라미터화 테스트 - 제목 검증 ====================

    @ParameterizedTest
    @DisplayName("유효한 제목으로 보드 생성 시 검증을 통과해야 한다")
    @MethodSource("validTitleTestData")
    void validate_withValidTitle_shouldBeValid(String title) {
        // given
        CreateBoardCommand command = new CreateBoardCommand(title, "설명", new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = createBoardValidator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @ParameterizedTest
    @DisplayName("유효하지 않은 제목으로 보드 생성 시 검증에 실패해야 한다")
    @MethodSource("invalidTitleTestData")
    void validate_withInvalidTitle_shouldBeInvalid(String title, String expectedMessage) {
        // given
        CreateBoardCommand command = new CreateBoardCommand(title, "설명", new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = createBoardValidator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("title");
        assertThat(result.getErrors().get(0).message()).isEqualTo(expectedMessage);
    }

    @ParameterizedTest
    @DisplayName("너무 긴 제목으로 보드 생성 시 검증에 실패해야 한다")
    @MethodSource("tooLongTitleTestData")
    void validate_withTooLongTitle_shouldBeInvalid(String title, String expectedMessage) {
        // given
        CreateBoardCommand command = new CreateBoardCommand(title, "설명", new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = createBoardValidator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("title");
        assertThat(result.getErrors().get(0).message()).isEqualTo(expectedMessage);
    }

    @ParameterizedTest
    @DisplayName("HTML 태그가 포함된 제목으로 보드 생성 시 검증에 실패해야 한다")
    @MethodSource("htmlTagTitleTestData")
    void validate_withHtmlTagTitle_shouldBeInvalid(String title, String expectedMessage) {
        // given
        CreateBoardCommand command = new CreateBoardCommand(title, "설명", new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = createBoardValidator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("title");
        assertThat(result.getErrors().get(0).message()).isEqualTo(expectedMessage);
    }

    // ==================== 파라미터화 테스트 - 설명 검증 ====================

    @ParameterizedTest
    @DisplayName("유효한 설명으로 보드 생성 시 검증을 통과해야 한다")
    @MethodSource("validDescriptionTestData")
    void validate_withValidDescription_shouldBeValid(String description) {
        // given
        CreateBoardCommand command = new CreateBoardCommand("유효한 보드 제목", description, new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = createBoardValidator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @ParameterizedTest
    @DisplayName("HTML 태그가 포함된 설명으로 보드 생성 시 검증에 실패해야 한다")
    @MethodSource("htmlTagDescriptionTestData")
    void validate_withHtmlTagDescription_shouldBeInvalid(String description, String expectedMessage) {
        // given
        CreateBoardCommand command = new CreateBoardCommand("제목", description, new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = createBoardValidator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("description");
        assertThat(result.getErrors().get(0).message()).isEqualTo(expectedMessage);
    }

    // ==================== 소유자 검증 ====================

    @Test
    @DisplayName("ownerId가 null이면 검증에 실패해야 한다")
    void validate_withNullOwnerId_shouldBeInvalid() {
        // given
        CreateBoardCommand command = new CreateBoardCommand("제목", "설명", null);

        // when
        ValidationResult<CreateBoardCommand> result = createBoardValidator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("userId");
        assertThat(result.getErrors().get(0).message()).isEqualTo("Board owner is required");
    }

    // ==================== 복합 검증 테스트 ====================

    @Test
    @DisplayName("여러 필드가 유효하지 않으면 모든 오류를 반환해야 한다")
    void validate_withMultipleInvalidFields_shouldReturnAllErrors() {
        // given
        String longTitle = "a".repeat(51);
        String htmlDescription = "<script>";
        CreateBoardCommand command = new CreateBoardCommand(longTitle, htmlDescription, null);

        // when
        ValidationResult<CreateBoardCommand> result = createBoardValidator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(3); // title, description, userId 모두 실패
        assertThat(result.getErrors()).extracting("field").containsExactlyInAnyOrder("title", "description", "userId");
    }

    @Test
    @DisplayName("제목에 여러 문제가 있으면 첫 번째 오류만 반환해야 한다")
    void validate_withMultipleTitleIssues_shouldReturnFirstError() {
        // given
        String longTitleWithHtml = "<script>" + "a".repeat(50); // HTML 태그 + 길이 초과
        CreateBoardCommand command = new CreateBoardCommand(longTitleWithHtml, "설명", new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = createBoardValidator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        // chain validator는 첫 번째 실패 시 중단되므로 최대 길이 오류가 먼저 나타남
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("title");
        assertThat(result.getErrors().get(0).message()).isEqualTo("Board title must be no more than 50 characters long");
    }

    // ==================== 경계값 테스트 ====================

    @Test
    @DisplayName("제목이 정확히 최대 길이면 검증을 통과해야 한다")
    void validate_withMaxLengthTitle_shouldBeValid() {
        // given
        String maxTitle = "a".repeat(50); // 정확히 50자
        CreateBoardCommand command = new CreateBoardCommand(maxTitle, "설명", new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = createBoardValidator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("제목이 1자면 검증을 통과해야 한다")
    void validate_withOneCharacterTitle_shouldBeValid() {
        // given
        CreateBoardCommand command = new CreateBoardCommand("a", "설명", new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = createBoardValidator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("한글 제목은 검증을 통과해야 한다")
    void validate_withKoreanTitle_shouldBeValid() {
        // given
        CreateBoardCommand command = new CreateBoardCommand("한글 제목 테스트", "설명", new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = createBoardValidator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("영어 제목은 검증을 통과해야 한다")
    void validate_withEnglishTitle_shouldBeValid() {
        // given
        CreateBoardCommand command = new CreateBoardCommand("English Title Test", "설명", new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = createBoardValidator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("숫자가 포함된 제목은 검증을 통과해야 한다")
    void validate_withNumbersInTitle_shouldBeValid() {
        // given
        CreateBoardCommand command = new CreateBoardCommand("프로젝트 2024 1차", "설명", new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = createBoardValidator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }
} 
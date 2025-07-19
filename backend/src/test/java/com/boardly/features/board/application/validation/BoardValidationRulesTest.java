package com.boardly.features.board.application.validation;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;
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
class BoardValidationRulesTest {

    @Mock
    private MessageSource messageSource;

    private ValidationMessageResolver messageResolver;
    private CommonValidationRules commonValidationRules;

    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(Locale.KOREAN);
        
        // MessageSource Mock 설정
        lenient().when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
            .thenAnswer(invocation -> {
                String key = invocation.getArgument(0);
                return switch (key) {
                    case "validation.title.required" -> "제목은 필수 입력 항목입니다";
                    case "validation.title.min.length" -> "제목은 최소 1자 이상 입력해야 합니다";
                    case "validation.title.max.length" -> "제목은 50자 이하로 입력해야 합니다";
                    case "validation.title.invalid" -> "제목에 HTML 태그는 허용되지 않습니다";
                    case "validation.description.max.length" -> "설명은 500자 이하로 입력해야 합니다";
                    case "validation.description.invalid" -> "설명에 HTML 태그는 허용되지 않습니다";
                    case "validation.boardId.required" -> "보드 ID는 필수 입력 항목입니다";
                    case "validation.userId.required" -> "사용자 ID는 필수 입력 항목입니다";
                    default -> key;
                };
            });

        messageResolver = new ValidationMessageResolver(messageSource);
        commonValidationRules = new CommonValidationRules(messageResolver);
    }

    // ==================== 테스트 데이터 제공 메서드들 ====================

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

    private static Stream<Arguments> invalidTitleTestData() {
        return Stream.of(
            Arguments.of("", "제목은 필수 입력 항목입니다"),
            Arguments.of("   ", "제목은 필수 입력 항목입니다"),
            Arguments.of("a".repeat(51), "제목은 50자 이하로 입력해야 합니다")
        );
    }

    private static Stream<Arguments> htmlTagTitleTestData() {
        return Stream.of(
            Arguments.of("<script>alert('test')</script>", "제목에 HTML 태그는 허용되지 않습니다"),
            Arguments.of("<div>제목</div>", "제목에 HTML 태그는 허용되지 않습니다"),
            Arguments.of("<p>테스트</p>", "제목에 HTML 태그는 허용되지 않습니다")
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

    private static Stream<Arguments> invalidDescriptionTestData() {
        return Stream.of(
            Arguments.of("a".repeat(501), "설명은 500자 이하로 입력해야 합니다")
        );
    }

    private static Stream<Arguments> htmlTagDescriptionTestData() {
        return Stream.of(
            Arguments.of("<script>alert('test')</script>", "설명에 HTML 태그는 허용되지 않습니다"),
            Arguments.of("<div>설명</div>", "설명에 HTML 태그는 허용되지 않습니다"),
            Arguments.of("<p>테스트</p>", "설명에 HTML 태그는 허용되지 않습니다")
        );
    }

    // ==================== 제목 검증 테스트 ====================

    @ParameterizedTest
    @DisplayName("유효한 제목으로 검증을 통과해야 한다")
    @MethodSource("validTitleTestData")
    void titleComplete_ValidTitle_ShouldPass(String title) {
        // given
        TestCommand command = new TestCommand(title, "설명", new BoardId("board-123"), new UserId("user-123"));
        Validator<TestCommand> validator = commonValidationRules.titleComplete(TestCommand::title);

        // when
        ValidationResult<TestCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @ParameterizedTest
    @DisplayName("유효하지 않은 제목으로 검증에 실패해야 한다")
    @MethodSource("invalidTitleTestData")
    void titleComplete_InvalidTitle_ShouldFail(String title, String expectedMessage) {
        // given
        TestCommand command = new TestCommand(title, "설명", new BoardId("board-123"), new UserId("user-123"));
        Validator<TestCommand> validator = commonValidationRules.titleComplete(TestCommand::title);

        // when
        ValidationResult<TestCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("title");
        assertThat(result.getErrors().get(0).message()).isEqualTo(expectedMessage);
    }

    @ParameterizedTest
    @DisplayName("HTML 태그가 포함된 제목으로 검증에 실패해야 한다")
    @MethodSource("htmlTagTitleTestData")
    void titleComplete_HtmlTagTitle_ShouldFail(String title, String expectedMessage) {
        // given
        TestCommand command = new TestCommand(title, "설명", new BoardId("board-123"), new UserId("user-123"));
        Validator<TestCommand> validator = commonValidationRules.titleComplete(TestCommand::title);

        // when
        ValidationResult<TestCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("title");
        assertThat(result.getErrors().get(0).message()).isEqualTo(expectedMessage);
    }

    // ==================== 설명 검증 테스트 ====================

    @ParameterizedTest
    @DisplayName("유효한 설명으로 검증을 통과해야 한다")
    @MethodSource("validDescriptionTestData")
    void descriptionComplete_ValidDescription_ShouldPass(String description) {
        // given
        TestCommand command = new TestCommand("제목", description, new BoardId("board-123"), new UserId("user-123"));
        Validator<TestCommand> validator = commonValidationRules.descriptionComplete(TestCommand::description);

        // when
        ValidationResult<TestCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @ParameterizedTest
    @DisplayName("유효하지 않은 설명으로 검증에 실패해야 한다")
    @MethodSource("invalidDescriptionTestData")
    void descriptionComplete_InvalidDescription_ShouldFail(String description, String expectedMessage) {
        // given
        TestCommand command = new TestCommand("제목", description, new BoardId("board-123"), new UserId("user-123"));
        Validator<TestCommand> validator = commonValidationRules.descriptionComplete(TestCommand::description);

        // when
        ValidationResult<TestCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("description");
        assertThat(result.getErrors().get(0).message()).isEqualTo(expectedMessage);
    }

    @ParameterizedTest
    @DisplayName("HTML 태그가 포함된 설명으로 검증에 실패해야 한다")
    @MethodSource("htmlTagDescriptionTestData")
    void descriptionComplete_HtmlTagDescription_ShouldFail(String description, String expectedMessage) {
        // given
        TestCommand command = new TestCommand("제목", description, new BoardId("board-123"), new UserId("user-123"));
        Validator<TestCommand> validator = commonValidationRules.descriptionComplete(TestCommand::description);

        // when
        ValidationResult<TestCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("description");
        assertThat(result.getErrors().get(0).message()).isEqualTo(expectedMessage);
    }

    // ==================== BoardId 검증 테스트 ====================

    @Test
    @DisplayName("유효한 BoardId로 검증을 통과해야 한다")
    void boardIdRequired_ValidBoardId_ShouldPass() {
        // given
        TestCommand command = new TestCommand("제목", "설명", new BoardId("board-123"), new UserId("user-123"));
        Validator<TestCommand> validator = commonValidationRules.boardIdRequired(TestCommand::boardId);

        // when
        ValidationResult<TestCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("null BoardId로 검증에 실패해야 한다")
    void boardIdRequired_NullBoardId_ShouldFail() {
        // given
        TestCommand command = new TestCommand("제목", "설명", null, new UserId("user-123"));
        Validator<TestCommand> validator = commonValidationRules.boardIdRequired(TestCommand::boardId);

        // when
        ValidationResult<TestCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("boardId");
        assertThat(result.getErrors().get(0).message()).isEqualTo("보드 ID는 필수 입력 항목입니다");
    }

    // ==================== UserId 검증 테스트 ====================

    @Test
    @DisplayName("유효한 UserId로 검증을 통과해야 한다")
    void userIdRequired_ValidUserId_ShouldPass() {
        // given
        TestCommand command = new TestCommand("제목", "설명", new BoardId("board-123"), new UserId("user-123"));
        Validator<TestCommand> validator = commonValidationRules.userIdRequired(TestCommand::userId);

        // when
        ValidationResult<TestCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("null UserId로 검증에 실패해야 한다")
    void userIdRequired_NullUserId_ShouldFail() {
        // given
        TestCommand command = new TestCommand("제목", "설명", new BoardId("board-123"), null);
        Validator<TestCommand> validator = commonValidationRules.userIdRequired(TestCommand::userId);

        // when
        ValidationResult<TestCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("userId");
        assertThat(result.getErrors().get(0).message()).isEqualTo("사용자 ID는 필수 입력 항목입니다");
    }

    // ==================== 상수 테스트 ====================

    @Test
    @DisplayName("TITLE_MAX_LENGTH 상수가 올바른 값을 가져야 한다")
    void titleMaxLength_ShouldBeCorrect() {
        assertThat(CommonValidationRules.TITLE_MAX_LENGTH).isEqualTo(50);
    }

    @Test
    @DisplayName("DESCRIPTION_MAX_LENGTH 상수가 올바른 값을 가져야 한다")
    void descriptionMaxLength_ShouldBeCorrect() {
        assertThat(CommonValidationRules.DESCRIPTION_MAX_LENGTH).isEqualTo(500);
    }

    // ==================== 테스트용 내부 클래스 ====================

    private static class TestCommand {
        private final String title;
        private final String description;
        private final BoardId boardId;
        private final UserId userId;

        public TestCommand(String title, String description, BoardId boardId, UserId userId) {
            this.title = title;
            this.description = description;
            this.boardId = boardId;
            this.userId = userId;
        }

        public String title() {
            return title;
        }

        public String description() {
            return description;
        }

        public BoardId boardId() {
            return boardId;
        }

        public UserId userId() {
            return userId;
        }
    }
} 
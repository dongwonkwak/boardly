package com.boardly.features.board.application.validation;

import com.boardly.features.board.application.port.input.CreateBoardCommand;
import com.boardly.features.user.domain.model.UserId;
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
class BoardValidatorTest {

    private BoardValidator boardValidator;

    @Mock
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(Locale.KOREAN);
        lenient().when(messageSource.getMessage(anyString(), any(Object[].class), any(Locale.class)))
            .thenAnswer(invocation -> {
                String code = invocation.getArgument(0);
                Object[] args = invocation.getArgument(1);
                StringBuilder message = new StringBuilder(code);
                if (args != null) {
                    for (Object arg : args) {
                        message.append(" ").append(arg);
                    }
                }
                return message.toString();
            });

        ValidationMessageResolver messageResolver = new ValidationMessageResolver(messageSource);
        boardValidator = new BoardValidator(messageResolver);
    }

    private CreateBoardCommand createValidCommand() {
        return new CreateBoardCommand(
            "유효한 보드 제목",
            "유효한 보드 설명",
            new UserId()
        );
    }

    @Test
    @DisplayName("유효한 보드 생성 정보는 검증을 통과해야 한다")
    void validateCreateBoard_withValidData_shouldBeValid() {
        // given
        CreateBoardCommand command = createValidCommand();

        // when
        ValidationResult<CreateBoardCommand> result = boardValidator.validateCreateBoard(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("설명이 null이어도 검증을 통과해야 한다 (optional)")
    void validateCreateBoard_withNullDescription_shouldBeValid() {
        // given
        CreateBoardCommand command = new CreateBoardCommand(
            "유효한 보드 제목",
            null,
            new UserId()
        );

        // when
        ValidationResult<CreateBoardCommand> result = boardValidator.validateCreateBoard(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("설명이 빈 문자열이어도 검증을 통과해야 한다")
    void validateCreateBoard_withEmptyDescription_shouldBeValid() {
        // given
        CreateBoardCommand command = new CreateBoardCommand(
            "유효한 보드 제목",
            "",
            new UserId()
        );

        // when
        ValidationResult<CreateBoardCommand> result = boardValidator.validateCreateBoard(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    // Title 검증 테스트들
    @Test
    @DisplayName("제목이 null이면 검증에 실패해야 한다")
    void validateCreateBoard_withNullTitle_shouldBeInvalid() {
        // given
        CreateBoardCommand command = new CreateBoardCommand(null, "설명", new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = boardValidator.validateCreateBoard(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("title");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.board.title.required");
    }

    @Test
    @DisplayName("제목이 빈 문자열이면 검증에 실패해야 한다")
    void validateCreateBoard_withEmptyTitle_shouldBeInvalid() {
        // given
        CreateBoardCommand command = new CreateBoardCommand("", "설명", new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = boardValidator.validateCreateBoard(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("title");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.board.title.required");
    }

    @Test
    @DisplayName("제목이 공백만 있으면 검증에 실패해야 한다")
    void validateCreateBoard_withWhitespaceTitle_shouldBeInvalid() {
        // given
        CreateBoardCommand command = new CreateBoardCommand("   ", "설명", new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = boardValidator.validateCreateBoard(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("title");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.board.title.required");
    }

    @Test
    @DisplayName("제목이 최대 길이를 초과하면 검증에 실패해야 한다")
    void validateCreateBoard_withTooLongTitle_shouldBeInvalid() {
        // given
        String longTitle = "a".repeat(51); // 50자 초과
        CreateBoardCommand command = new CreateBoardCommand(longTitle, "설명", new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = boardValidator.validateCreateBoard(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("title");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.board.title.max.length 50");
    }

    @Test
    @DisplayName("제목에 HTML 태그가 포함되면 검증에 실패해야 한다")
    void validateCreateBoard_withHtmlTagInTitle_shouldBeInvalid() {
        // given
        CreateBoardCommand command = new CreateBoardCommand("<script>", "설명", new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = boardValidator.validateCreateBoard(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("title");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.board.title.invalid");
    }

    @Test
    @DisplayName("제목에 허용되지 않은 특수문자가 포함되면 검증에 실패해야 한다")
    void validateCreateBoard_withInvalidSpecialCharactersInTitle_shouldBeInvalid() {
        // given
        CreateBoardCommand command = new CreateBoardCommand("제목@#$%", "설명", new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = boardValidator.validateCreateBoard(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("title");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.board.title.invalid");
    }

    @Test
    @DisplayName("제목에 허용되는 특수문자가 포함되면 검증을 통과해야 한다")
    void validateCreateBoard_withValidSpecialCharactersInTitle_shouldBeValid() {
        // given
        CreateBoardCommand command = new CreateBoardCommand("프로젝트-2024_v1.0 (최종)!", "설명", new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = boardValidator.validateCreateBoard(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    // Description 검증 테스트들
    @Test
    @DisplayName("설명이 최대 길이를 초과하면 검증에 실패해야 한다")
    void validateCreateBoard_withTooLongDescription_shouldBeInvalid() {
        // given
        String longDescription = "a".repeat(501); // 500자 초과
        CreateBoardCommand command = new CreateBoardCommand("제목", longDescription, new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = boardValidator.validateCreateBoard(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("description");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.board.description.max.length 500");
    }

    @Test
    @DisplayName("설명에 HTML 태그가 포함되면 검증에 실패해야 한다")
    void validateCreateBoard_withHtmlTagInDescription_shouldBeInvalid() {
        // given
        CreateBoardCommand command = new CreateBoardCommand("제목", "<div>", new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = boardValidator.validateCreateBoard(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("description");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.board.description.html.tag");
    }

    @Test
    @DisplayName("설명이 정확히 최대 길이면 검증을 통과해야 한다")
    void validateCreateBoard_withMaxLengthDescription_shouldBeValid() {
        // given
        String maxDescription = "a".repeat(500); // 정확히 500자
        CreateBoardCommand command = new CreateBoardCommand("제목", maxDescription, new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = boardValidator.validateCreateBoard(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    // 복합 검증 테스트들
    @Test
    @DisplayName("여러 필드가 유효하지 않으면 모든 오류를 반환해야 한다")
    void validateCreateBoard_withMultipleInvalidFields_shouldReturnAllErrors() {
        // given
        String longTitle = "a".repeat(51);
        String longDescription = "a".repeat(501);
        CreateBoardCommand command = new CreateBoardCommand(longTitle, longDescription, new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = boardValidator.validateCreateBoard(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(2);
        assertThat(result.getErrors()).extracting("field").containsExactlyInAnyOrder("title", "description");
    }

    @Test
    @DisplayName("제목에 여러 문제가 있으면 첫 번째 오류만 반환해야 한다")
    void validateCreateBoard_withMultipleTitleIssues_shouldReturnFirstError() {
        // given
        String longTitleWithHtml = "<script>" + "a".repeat(50); // HTML 태그 + 길이 초과
        CreateBoardCommand command = new CreateBoardCommand(longTitleWithHtml, "설명", new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = boardValidator.validateCreateBoard(command);

        // then
        assertThat(result.isValid()).isFalse();
        // chain validator는 첫 번째 실패 시 중단되므로 최대 길이 오류가 먼저 나타남
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("title");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.board.title.max.length 50");
    }

    // 경계값 테스트들
    @Test
    @DisplayName("제목이 정확히 최대 길이면 검증을 통과해야 한다")
    void validateCreateBoard_withMaxLengthTitle_shouldBeValid() {
        // given
        String maxTitle = "a".repeat(50); // 정확히 50자
        CreateBoardCommand command = new CreateBoardCommand(maxTitle, "설명", new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = boardValidator.validateCreateBoard(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("제목이 1자면 검증을 통과해야 한다")
    void validateCreateBoard_withOneCharacterTitle_shouldBeValid() {
        // given
        CreateBoardCommand command = new CreateBoardCommand("a", "설명", new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = boardValidator.validateCreateBoard(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("한글 제목은 검증을 통과해야 한다")
    void validateCreateBoard_withKoreanTitle_shouldBeValid() {
        // given
        CreateBoardCommand command = new CreateBoardCommand("한글 제목 테스트", "설명", new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = boardValidator.validateCreateBoard(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("영어 제목은 검증을 통과해야 한다")
    void validateCreateBoard_withEnglishTitle_shouldBeValid() {
        // given
        CreateBoardCommand command = new CreateBoardCommand("English Title Test", "설명", new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = boardValidator.validateCreateBoard(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("숫자가 포함된 제목은 검증을 통과해야 한다")
    void validateCreateBoard_withNumbersInTitle_shouldBeValid() {
        // given
        CreateBoardCommand command = new CreateBoardCommand("프로젝트 2024 1차", "설명", new UserId());

        // when
        ValidationResult<CreateBoardCommand> result = boardValidator.validateCreateBoard(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    // OwnerId 검증 테스트들
    @Test
    @DisplayName("ownerId가 null이면 검증에 실패해야 한다")
    void validateCreateBoard_withNullOwnerId_shouldBeInvalid() {
        // given
        CreateBoardCommand command = new CreateBoardCommand("제목", "설명", null);

        // when
        ValidationResult<CreateBoardCommand> result = boardValidator.validateCreateBoard(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("ownerId");
        assertThat(result.getErrors().get(0).message()).isEqualTo("validation.board.owner.required");
    }

    @Test
    @DisplayName("모든 필드가 유효하지 않으면 모든 오류를 반환해야 한다")
    void validateCreateBoard_withAllInvalidFields_shouldReturnAllErrors() {
        // given
        String longTitle = "a".repeat(51);
        String longDescription = "a".repeat(501);
        CreateBoardCommand command = new CreateBoardCommand(longTitle, longDescription, null);

        // when
        ValidationResult<CreateBoardCommand> result = boardValidator.validateCreateBoard(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(3);
        assertThat(result.getErrors()).extracting("field").containsExactlyInAnyOrder("title", "description", "ownerId");
    }
} 
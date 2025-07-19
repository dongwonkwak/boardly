package com.boardly.features.boardlist.application.validation;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.boardlist.application.port.input.CreateBoardListCommand;
import com.boardly.features.boardlist.domain.model.ListColor;
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
class CreateBoardListValidatorTest {

    @Mock
    private MessageSource messageSource;

    private CreateBoardListValidator validator;

    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(Locale.KOREAN);
        
        // MessageSource Mock 설정
        lenient().when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
            .thenAnswer(invocation -> {
                String key = invocation.getArgument(0);
                return switch (key) {
                    case "validation.boardlist.title.required" -> "List title is required";
                    case "validation.boardlist.title.max.length" -> "List title must be no more than 100 characters long";
                    case "validation.boardlist.title.invalid" -> "List title cannot contain HTML tags";
                    case "validation.boardlist.description.max.length" -> "List description must be no more than 500 characters long";
                    case "validation.boardlist.description.invalid" -> "List description cannot contain HTML tags";
                    case "validation.boardlist.boardId.required" -> "Board ID is required";
                    case "validation.boardlist.userId.required" -> "User ID is required";
                    case "validation.boardlist.color.required" -> "List color is required";
                    case "validation.boardlist.color.invalid" -> "Invalid color value";
                    default -> key;
                };
            });

        ValidationMessageResolver messageResolver = new ValidationMessageResolver(messageSource);
        validator = new CreateBoardListValidator(messageResolver);
    }

    @Test
    @DisplayName("유효한 CreateBoardListCommand는 검증을 통과해야 한다")
    void validate_ValidCommand_ShouldPass() {
        // given
        CreateBoardListCommand command = new CreateBoardListCommand(
            new BoardId("board-123"),
            new UserId("user-123"),
            "테스트 리스트",
            "테스트 설명",
            ListColor.of("#0079BF")
        );

        // when
        ValidationResult<CreateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("제목이 null인 경우 검증에 실패해야 한다")
    void validate_NullTitle_ShouldFail() {
        // given
        CreateBoardListCommand command = new CreateBoardListCommand(
            new BoardId("board-123"),
            new UserId("user-123"),
            null,
            "테스트 설명",
            ListColor.of("#0079BF")
        );

        // when
        ValidationResult<CreateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("title");
    }

    @Test
    @DisplayName("제목이 빈 문자열인 경우 검증에 실패해야 한다")
    void validate_EmptyTitle_ShouldFail() {
        // given
        CreateBoardListCommand command = new CreateBoardListCommand(
            new BoardId("board-123"),
            new UserId("user-123"),
            "",
            "테스트 설명",
            ListColor.of("#0079BF")
        );

        // when
        ValidationResult<CreateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("title");
    }

    @Test
    @DisplayName("제목이 공백만 있는 경우 검증에 실패해야 한다")
    void validate_BlankTitle_ShouldFail() {
        // given
        CreateBoardListCommand command = new CreateBoardListCommand(
            new BoardId("board-123"),
            new UserId("user-123"),
            "   ",
            "테스트 설명",
            ListColor.of("#0079BF")
        );

        // when
        ValidationResult<CreateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("title");
    }

    @Test
    @DisplayName("제목이 최대 길이를 초과하는 경우 검증에 실패해야 한다")
    void validate_TitleExceedsMaxLength_ShouldFail() {
        // given
        String longTitle = "a".repeat(101);
        CreateBoardListCommand command = new CreateBoardListCommand(
            new BoardId("board-123"),
            new UserId("user-123"),
            longTitle,
            "테스트 설명",
            ListColor.of("#0079BF")
        );

        // when
        ValidationResult<CreateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("title");
    }

    @Test
    @DisplayName("설명이 최대 길이를 초과하는 경우 검증에 실패해야 한다")
    void validate_DescriptionExceedsMaxLength_ShouldFail() {
        // given
        String longDescription = "a".repeat(501);
        CreateBoardListCommand command = new CreateBoardListCommand(
            new BoardId("board-123"),
            new UserId("user-123"),
            "테스트 리스트",
            longDescription,
            ListColor.of("#0079BF")
        );

        // when
        ValidationResult<CreateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("description");
    }

    @Test
    @DisplayName("boardId가 null인 경우 검증에 실패해야 한다")
    void validate_NullBoardId_ShouldFail() {
        // given
        CreateBoardListCommand command = new CreateBoardListCommand(
            null,
            new UserId("user-123"),
            "테스트 리스트",
            "테스트 설명",
            ListColor.of("#0079BF")
        );

        // when
        ValidationResult<CreateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("boardId");
    }

    @Test
    @DisplayName("userId가 null인 경우 검증에 실패해야 한다")
    void validate_NullUserId_ShouldFail() {
        // given
        CreateBoardListCommand command = new CreateBoardListCommand(
            new BoardId("board-123"),
            null,
            "테스트 리스트",
            "테스트 설명",
            ListColor.of("#0079BF")
        );

        // when
        ValidationResult<CreateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("userId");
    }

    @Test
    @DisplayName("color가 null인 경우 검증에 실패해야 한다")
    void validate_NullColor_ShouldFail() {
        // given
        CreateBoardListCommand command = new CreateBoardListCommand(
            new BoardId("board-123"),
            new UserId("user-123"),
            "테스트 리스트",
            "테스트 설명",
            null
        );

        // when
        ValidationResult<CreateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("color");
    }

    @Test
    @DisplayName("여러 필드가 유효하지 않은 경우 모든 오류가 반환되어야 한다")
    void validate_MultipleInvalidFields_ShouldReturnAllErrors() {
        // given
        CreateBoardListCommand command = new CreateBoardListCommand(
            null,
            null,
            "",
            "a".repeat(501),
            null
        );

        // when
        ValidationResult<CreateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(5);
    }

    @Test
    @DisplayName("설명이 null인 경우 검증을 통과해야 한다")
    void validate_NullDescription_ShouldPass() {
        // given
        CreateBoardListCommand command = new CreateBoardListCommand(
            new BoardId("board-123"),
            new UserId("user-123"),
            "테스트 리스트",
            null,
            ListColor.of("#0079BF")
        );

        // when
        ValidationResult<CreateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("제목에 HTML 태그가 포함된 경우 검증에 실패해야 한다")
    void validate_TitleWithHtmlTag_ShouldFail() {
        // given
        CreateBoardListCommand command = new CreateBoardListCommand(
            new BoardId("board-123"),
            new UserId("user-123"),
            "<script>alert('test')</script>",
            "테스트 설명",
            ListColor.of("#0079BF")
        );

        // when
        ValidationResult<CreateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("title");
    }

    @Test
    @DisplayName("설명에 HTML 태그가 포함된 경우 검증에 실패해야 한다")
    void validate_DescriptionWithHtmlTag_ShouldFail() {
        // given
        CreateBoardListCommand command = new CreateBoardListCommand(
            new BoardId("board-123"),
            new UserId("user-123"),
            "테스트 리스트",
            "<div>테스트 설명</div>",
            ListColor.of("#0079BF")
        );

        // when
        ValidationResult<CreateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("description");
    }

    @Test
    @DisplayName("유효하지 않은 색상으로 검증에 실패해야 한다")
    void validate_InvalidColor_ShouldFail() {
        // given
        // ListColor.of()는 유효하지 않은 색상을 기본 색상으로 변환하므로,
        // 직접 record를 생성하여 유효하지 않은 색상을 테스트
        CreateBoardListCommand command = new CreateBoardListCommand(
            new BoardId("board-123"),
            new UserId("user-123"),
            "테스트 리스트",
            "테스트 설명",
            new ListColor("#FFFFFF") // 유효하지 않은 색상
        );

        // when
        ValidationResult<CreateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("color");
    }

    @Test
    @DisplayName("다른 유효한 색상들로도 검증을 통과해야 한다")
    void validate_OtherValidColors_ShouldPass() {
        // given
        String[] validColors = {
            "#D29034", // Orange
            "#519839", // Green
            "#B04632", // Red
            "#89609E", // Purple
            "#CD5A91", // Pink
            "#4BBFDA", // Light Blue
            "#00AECC", // Teal
            "#838C91"  // Gray
        };

        for (String color : validColors) {
            CreateBoardListCommand command = new CreateBoardListCommand(
                new BoardId("board-123"),
                new UserId("user-123"),
                "테스트 리스트",
                "테스트 설명",
                ListColor.of(color)
            );

            // when
            ValidationResult<CreateBoardListCommand> result = validator.validate(command);

            // then
            assertThat(result.isValid()).as("Color: " + color).isTrue();
        }
    }
} 
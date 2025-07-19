package com.boardly.features.boardlist.application.validation;

import com.boardly.features.boardlist.application.port.input.UpdateBoardListCommand;
import com.boardly.features.boardlist.domain.model.ListColor;
import com.boardly.features.boardlist.domain.model.ListId;
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
class UpdateBoardListValidatorTest {

    @Mock
    private MessageSource messageSource;

    private UpdateBoardListValidator validator;

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
                    case "validation.boardlist.listId.required" -> "List ID is required";
                    case "validation.boardlist.userId.required" -> "User ID is required";
                    case "validation.boardlist.color.invalid" -> "Invalid color value";
                    default -> key;
                };
            });

        ValidationMessageResolver messageResolver = new ValidationMessageResolver(messageSource);
        validator = new UpdateBoardListValidator(messageResolver);
    }

    @Test
    @DisplayName("유효한 UpdateBoardListCommand는 검증을 통과해야 한다")
    void validate_ValidCommand_ShouldPass() {
        // given
        UpdateBoardListCommand command = new UpdateBoardListCommand(
            new ListId("list-123"),
            new UserId("user-123"),
            "테스트 리스트",
            "테스트 설명",
            ListColor.of("#0079BF")
        );

        // when
        ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("색상이 null인 경우에도 검증을 통과해야 한다")
    void validate_NullColor_ShouldPass() {
        // given
        UpdateBoardListCommand command = new UpdateBoardListCommand(
            new ListId("list-123"),
            new UserId("user-123"),
            "테스트 리스트",
            "테스트 설명",
            null
        );

        // when
        ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("설명이 null인 경우에도 검증을 통과해야 한다")
    void validate_NullDescription_ShouldPass() {
        // given
        UpdateBoardListCommand command = new UpdateBoardListCommand(
            new ListId("list-123"),
            new UserId("user-123"),
            "테스트 리스트",
            null,
            ListColor.of("#0079BF")
        );

        // when
        ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("제목이 null인 경우 검증에 실패해야 한다")
    void validate_NullTitle_ShouldFail() {
        // given
        UpdateBoardListCommand command = new UpdateBoardListCommand(
            new ListId("list-123"),
            new UserId("user-123"),
            null,
            "테스트 설명",
            ListColor.of("#0079BF")
        );

        // when
        ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("title");
    }

    @Test
    @DisplayName("제목이 빈 문자열인 경우 검증에 실패해야 한다")
    void validate_EmptyTitle_ShouldFail() {
        // given
        UpdateBoardListCommand command = new UpdateBoardListCommand(
            new ListId("list-123"),
            new UserId("user-123"),
            "",
            "테스트 설명",
            ListColor.of("#0079BF")
        );

        // when
        ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("title");
    }

    @Test
    @DisplayName("제목이 공백만 있는 경우 검증에 실패해야 한다")
    void validate_BlankTitle_ShouldFail() {
        // given
        UpdateBoardListCommand command = new UpdateBoardListCommand(
            new ListId("list-123"),
            new UserId("user-123"),
            "   ",
            "테스트 설명",
            ListColor.of("#0079BF")
        );

        // when
        ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

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
        UpdateBoardListCommand command = new UpdateBoardListCommand(
            new ListId("list-123"),
            new UserId("user-123"),
            longTitle,
            "테스트 설명",
            ListColor.of("#0079BF")
        );

        // when
        ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

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
        UpdateBoardListCommand command = new UpdateBoardListCommand(
            new ListId("list-123"),
            new UserId("user-123"),
            "테스트 리스트",
            longDescription,
            ListColor.of("#0079BF")
        );

        // when
        ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("description");
    }

    @Test
    @DisplayName("listId가 null인 경우 검증에 실패해야 한다")
    void validate_NullListId_ShouldFail() {
        // given
        UpdateBoardListCommand command = new UpdateBoardListCommand(
            null,
            new UserId("user-123"),
            "테스트 리스트",
            "테스트 설명",
            ListColor.of("#0079BF")
        );

        // when
        ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("listId");
    }

    @Test
    @DisplayName("userId가 null인 경우 검증에 실패해야 한다")
    void validate_NullUserId_ShouldFail() {
        // given
        UpdateBoardListCommand command = new UpdateBoardListCommand(
            new ListId("list-123"),
            null,
            "테스트 리스트",
            "테스트 설명",
            ListColor.of("#0079BF")
        );

        // when
        ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("userId");
    }

    @Test
    @DisplayName("여러 필드가 유효하지 않은 경우 모든 오류가 반환되어야 한다")
    void validate_MultipleInvalidFields_ShouldReturnAllErrors() {
        // given
        UpdateBoardListCommand command = new UpdateBoardListCommand(
            null,
            null,
            "",
            "a".repeat(501),
            null
        );

        // when
        ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(4);
        assertThat(result.getErrors().toJavaList().stream().map(error -> error.field()))
            .containsExactlyInAnyOrder("listId", "userId", "title", "description");
    }

    @Test
    @DisplayName("제목에 HTML 태그가 포함된 경우 검증에 실패해야 한다")
    void validate_TitleWithHtmlTag_ShouldFail() {
        // given
        UpdateBoardListCommand command = new UpdateBoardListCommand(
            new ListId("list-123"),
            new UserId("user-123"),
            "<script>alert('test')</script>테스트 리스트",
            "테스트 설명",
            ListColor.of("#0079BF")
        );

        // when
        ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("title");
    }

    @Test
    @DisplayName("설명에 HTML 태그가 포함된 경우 검증에 실패해야 한다")
    void validate_DescriptionWithHtmlTag_ShouldFail() {
        // given
        UpdateBoardListCommand command = new UpdateBoardListCommand(
            new ListId("list-123"),
            new UserId("user-123"),
            "테스트 리스트",
            "<p>테스트 설명</p>",
            ListColor.of("#0079BF")
        );

        // when
        ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("description");
    }

    @Test
    @DisplayName("유효하지 않은 색상으로 검증에 실패해야 한다")
    void validate_InvalidColor_ShouldFail() {
        // given
        UpdateBoardListCommand command = new UpdateBoardListCommand(
            new ListId("list-123"),
            new UserId("user-123"),
            "테스트 리스트",
            "테스트 설명",
            new ListColor("#invalid-color")
        );

        // when
        ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

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

        for (String colorValue : validColors) {
            UpdateBoardListCommand command = new UpdateBoardListCommand(
                new ListId("list-123"),
                new UserId("user-123"),
                "테스트 리스트",
                "테스트 설명",
                ListColor.of(colorValue)
            );

            // when
            ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

            // then
            assertThat(result.isValid())
                .withFailMessage("Color %s should be valid", colorValue)
                .isTrue();
        }
    }

    @Test
    @DisplayName("색상과 설명이 모두 null인 경우에도 검증을 통과해야 한다")
    void validate_NullColorAndDescription_ShouldPass() {
        // given
        UpdateBoardListCommand command = new UpdateBoardListCommand(
            new ListId("list-123"),
            new UserId("user-123"),
            "테스트 리스트",
            null,
            null
        );

        // when
        ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("제목에 허용된 특수문자가 포함된 경우 검증을 통과해야 한다")
    void validate_TitleWithAllowedSpecialCharacters_ShouldPass() {
        // given
        UpdateBoardListCommand command = new UpdateBoardListCommand(
            new ListId("list-123"),
            new UserId("user-123"),
            "테스트 리스트 (중요) - 버전 1.0",
            "테스트 설명",
            ListColor.of("#0079BF")
        );

        // when
        ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("제목에 허용되지 않은 특수문자가 포함된 경우 검증에 실패해야 한다")
    void validate_TitleWithDisallowedSpecialCharacters_ShouldFail() {
        // given
        UpdateBoardListCommand command = new UpdateBoardListCommand(
            new ListId("list-123"),
            new UserId("user-123"),
            "테스트 리스트 @#$%^&*",
            "테스트 설명",
            ListColor.of("#0079BF")
        );

        // when
        ValidationResult<UpdateBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("title");
    }
} 
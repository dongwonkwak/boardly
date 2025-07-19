package com.boardly.features.boardlist.application.validation;

import com.boardly.features.boardlist.application.port.input.DeleteBoardListCommand;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import io.vavr.collection.Seq;
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
class DeleteBoardListValidatorTest {

    private DeleteBoardListValidator validator;

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
                    case "validation.boardlist.listId.required" -> "List ID is required";
                    case "validation.boardlist.userId.required" -> "User ID is required";
                    default -> key;
                };
            });

        ValidationMessageResolver messageResolver = new ValidationMessageResolver(messageSource);
        validator = new DeleteBoardListValidator(messageResolver);
    }

    @Test
    @DisplayName("유효한 삭제 명령어는 검증을 통과해야 한다")
    void validate_ValidCommand_ShouldPass() {
        // given
        DeleteBoardListCommand command = new DeleteBoardListCommand(
                new ListId("list-123"),
                new UserId("user-123")
        );

        // when
        ValidationResult<DeleteBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("listId가 null인 경우 검증에 실패해야 한다")
    void validate_NullListId_ShouldFail() {
        // given
        DeleteBoardListCommand command = new DeleteBoardListCommand(
                null,
                new UserId("user-123")
        );

        // when
        ValidationResult<DeleteBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("listId");
    }

    @Test
    @DisplayName("userId가 null인 경우 검증에 실패해야 한다")
    void validate_NullUserId_ShouldFail() {
        // given
        DeleteBoardListCommand command = new DeleteBoardListCommand(
                new ListId("list-123"),
                null
        );

        // when
        ValidationResult<DeleteBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("userId");
    }

    @Test
    @DisplayName("listId와 userId가 모두 null인 경우 모든 오류가 반환되어야 한다")
    void validate_BothNullFields_ShouldReturnAllErrors() {
        // given
        DeleteBoardListCommand command = new DeleteBoardListCommand(
                null,
                null
        );

        // when
        ValidationResult<DeleteBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(2);
        
        var errors = result.getErrors();
        assertThat(errors).anyMatch(error -> error.field().equals("listId"));
        assertThat(errors).anyMatch(error -> error.field().equals("userId"));
    }

    @Test
    @DisplayName("유효한 ListId와 UserId 객체로 검증이 성공해야 한다")
    void validate_ValidObjects_ShouldPass() {
        // given
        ListId listId = new ListId("valid-list-id");
        UserId userId = new UserId("valid-user-id");
        DeleteBoardListCommand command = new DeleteBoardListCommand(listId, userId);

        // when
        ValidationResult<DeleteBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("빈 문자열 ID로 생성된 객체들도 유효해야 한다")
    void validate_EmptyStringIds_ShouldPass() {
        // given
        ListId listId = new ListId("");
        UserId userId = new UserId("");
        DeleteBoardListCommand command = new DeleteBoardListCommand(listId, userId);

        // when
        ValidationResult<DeleteBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("특수문자가 포함된 ID로 생성된 객체들도 유효해야 한다")
    void validate_SpecialCharacterIds_ShouldPass() {
        // given
        ListId listId = new ListId("list-123_456@test");
        UserId userId = new UserId("user-123_456@test");
        DeleteBoardListCommand command = new DeleteBoardListCommand(listId, userId);

        // when
        ValidationResult<DeleteBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("긴 ID로 생성된 객체들도 유효해야 한다")
    void validate_LongIds_ShouldPass() {
        // given
        String longId = "a".repeat(1000);
        ListId listId = new ListId(longId);
        UserId userId = new UserId(longId);
        DeleteBoardListCommand command = new DeleteBoardListCommand(listId, userId);

        // when
        ValidationResult<DeleteBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("한글이 포함된 ID로 생성된 객체들도 유효해야 한다")
    void validate_KoreanIds_ShouldPass() {
        // given
        ListId listId = new ListId("리스트-123");
        UserId userId = new UserId("사용자-123");
        DeleteBoardListCommand command = new DeleteBoardListCommand(listId, userId);

        // when
        ValidationResult<DeleteBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("UUID 형태의 ID로 생성된 객체들도 유효해야 한다")
    void validate_UuidIds_ShouldPass() {
        // given
        ListId listId = new ListId("550e8400-e29b-41d4-a716-446655440000");
        UserId userId = new UserId("550e8400-e29b-41d4-a716-446655440001");
        DeleteBoardListCommand command = new DeleteBoardListCommand(listId, userId);

        // when
        ValidationResult<DeleteBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }
} 
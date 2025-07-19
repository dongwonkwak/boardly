package com.boardly.features.boardlist.application.validation;

import com.boardly.features.boardlist.application.port.input.GetBoardListsCommand;
import com.boardly.features.board.domain.model.BoardId;
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
class GetBoardListsValidatorTest {

    private GetBoardListsValidator validator;

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
                    case "validation.boardlist.boardId.required" -> "Board ID is required";
                    case "validation.boardlist.userId.required" -> "User ID is required";
                    default -> key;
                };
            });

        ValidationMessageResolver messageResolver = new ValidationMessageResolver(messageSource);
        validator = new GetBoardListsValidator(messageResolver);
    }

    @Test
    @DisplayName("유효한 조회 명령어는 검증을 통과해야 한다")
    void validate_ValidCommand_ShouldPass() {
        // given
        GetBoardListsCommand command = new GetBoardListsCommand(
                new BoardId("board-123"),
                new UserId("user-123")
        );

        // when
        ValidationResult<GetBoardListsCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("boardId가 null인 경우 검증에 실패해야 한다")
    void validate_NullBoardId_ShouldFail() {
        // given
        GetBoardListsCommand command = new GetBoardListsCommand(
                null,
                new UserId("user-123")
        );

        // when
        ValidationResult<GetBoardListsCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("boardId");
    }

    @Test
    @DisplayName("userId가 null인 경우 검증에 실패해야 한다")
    void validate_NullUserId_ShouldFail() {
        // given
        GetBoardListsCommand command = new GetBoardListsCommand(
                new BoardId("board-123"),
                null
        );

        // when
        ValidationResult<GetBoardListsCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("userId");
    }

    @Test
    @DisplayName("boardId와 userId가 모두 null인 경우 모든 오류가 반환되어야 한다")
    void validate_BothNullFields_ShouldReturnAllErrors() {
        // given
        GetBoardListsCommand command = new GetBoardListsCommand(
                null,
                null
        );

        // when
        ValidationResult<GetBoardListsCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(2);
        
        var errors = result.getErrors();
        assertThat(errors).anyMatch(error -> error.field().equals("boardId"));
        assertThat(errors).anyMatch(error -> error.field().equals("userId"));
    }

    @Test
    @DisplayName("유효한 BoardId와 UserId 객체로 검증이 성공해야 한다")
    void validate_ValidObjects_ShouldPass() {
        // given
        BoardId boardId = new BoardId("valid-board-id");
        UserId userId = new UserId("valid-user-id");
        GetBoardListsCommand command = new GetBoardListsCommand(boardId, userId);

        // when
        ValidationResult<GetBoardListsCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("빈 문자열 ID로 생성된 객체들도 유효해야 한다")
    void validate_EmptyStringIds_ShouldPass() {
        // given
        BoardId boardId = new BoardId("");
        UserId userId = new UserId("");
        GetBoardListsCommand command = new GetBoardListsCommand(boardId, userId);

        // when
        ValidationResult<GetBoardListsCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("특수문자가 포함된 ID로 생성된 객체들도 유효해야 한다")
    void validate_SpecialCharacterIds_ShouldPass() {
        // given
        BoardId boardId = new BoardId("board-123_456@test");
        UserId userId = new UserId("user-123_456@test");
        GetBoardListsCommand command = new GetBoardListsCommand(boardId, userId);

        // when
        ValidationResult<GetBoardListsCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("긴 ID로 생성된 객체들도 유효해야 한다")
    void validate_LongIds_ShouldPass() {
        // given
        String longId = "a".repeat(1000);
        BoardId boardId = new BoardId(longId);
        UserId userId = new UserId(longId);
        GetBoardListsCommand command = new GetBoardListsCommand(boardId, userId);

        // when
        ValidationResult<GetBoardListsCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("한글이 포함된 ID로 생성된 객체들도 유효해야 한다")
    void validate_KoreanIds_ShouldPass() {
        // given
        BoardId boardId = new BoardId("보드-123");
        UserId userId = new UserId("사용자-123");
        GetBoardListsCommand command = new GetBoardListsCommand(boardId, userId);

        // when
        ValidationResult<GetBoardListsCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("UUID 형태의 ID로 생성된 객체들도 유효해야 한다")
    void validate_UuidIds_ShouldPass() {
        // given
        BoardId boardId = new BoardId("550e8400-e29b-41d4-a716-446655440000");
        UserId userId = new UserId("550e8400-e29b-41d4-a716-446655440001");
        GetBoardListsCommand command = new GetBoardListsCommand(boardId, userId);

        // when
        ValidationResult<GetBoardListsCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }
} 
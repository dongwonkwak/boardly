package com.boardly.features.boardlist.application.validation;

import com.boardly.features.boardlist.application.port.input.DeleteBoardListCommand;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class DeleteBoardListValidatorTest {

    @Mock
    private ValidationMessageResolver validationMessageResolver;

    private DeleteBoardListValidator validator;

    @BeforeEach
    void setUp() {
        // ValidationMessageResolver Mock 설정
        lenient().when(validationMessageResolver.getMessage("validation.boardlist.listId.required"))
                .thenReturn("리스트 ID는 필수입니다");
        lenient().when(validationMessageResolver.getMessage("validation.boardlist.userId.required"))
                .thenReturn("사용자 ID는 필수입니다");

        validator = new DeleteBoardListValidator(validationMessageResolver);
    }

    @Test
    @DisplayName("유효한 DeleteBoardListCommand는 검증을 통과해야 한다")
    void validate_ValidCommand_ShouldPass() {
        // given
        DeleteBoardListCommand command = new DeleteBoardListCommand(
                new ListId(),
                new UserId());

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
                new UserId());

        // when
        ValidationResult<DeleteBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("listId");
        assertThat(result.getErrors().get(0).message()).isEqualTo("리스트 ID는 필수입니다");
    }

    @Test
    @DisplayName("userId가 null인 경우 검증에 실패해야 한다")
    void validate_NullUserId_ShouldFail() {
        // given
        DeleteBoardListCommand command = new DeleteBoardListCommand(
                new ListId(),
                null);

        // when
        ValidationResult<DeleteBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).field()).isEqualTo("userId");
        assertThat(result.getErrors().get(0).message()).isEqualTo("사용자 ID는 필수입니다");
    }

    @Test
    @DisplayName("listId와 userId가 모두 null인 경우 모든 오류가 반환되어야 한다")
    void validate_BothNullFields_ShouldReturnAllErrors() {
        // given
        DeleteBoardListCommand command = new DeleteBoardListCommand(
                null,
                null);

        // when
        ValidationResult<DeleteBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(2);

        // listId 오류 확인
        assertThat(result.getErrors()).anyMatch(error -> error.field().equals("listId") &&
                error.message().equals("리스트 ID는 필수입니다"));

        // userId 오류 확인
        assertThat(result.getErrors()).anyMatch(error -> error.field().equals("userId") &&
                error.message().equals("사용자 ID는 필수입니다"));
    }

    @Test
    @DisplayName("유효한 ListId와 UserId 객체로 검증을 통과해야 한다")
    void validate_ValidObjects_ShouldPass() {
        // given
        ListId listId = new ListId();
        UserId userId = new UserId();
        DeleteBoardListCommand command = new DeleteBoardListCommand(listId, userId);

        // when
        ValidationResult<DeleteBoardListCommand> result = validator.validate(command);

        // then
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("다른 유효한 ListId와 UserId 조합으로도 검증을 통과해야 한다")
    void validate_DifferentValidObjects_ShouldPass() {
        // given
        ListId listId1 = new ListId();
        ListId listId2 = new ListId();
        UserId userId1 = new UserId();
        UserId userId2 = new UserId();

        DeleteBoardListCommand command1 = new DeleteBoardListCommand(listId1, userId1);
        DeleteBoardListCommand command2 = new DeleteBoardListCommand(listId2, userId2);

        // when
        ValidationResult<DeleteBoardListCommand> result1 = validator.validate(command1);
        ValidationResult<DeleteBoardListCommand> result2 = validator.validate(command2);

        // then
        assertThat(result1.isValid()).isTrue();
        assertThat(result2.isValid()).isTrue();
    }
}
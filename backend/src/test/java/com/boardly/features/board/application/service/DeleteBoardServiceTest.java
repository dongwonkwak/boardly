package com.boardly.features.board.application.service;

import com.boardly.features.board.application.port.input.DeleteBoardCommand;
import com.boardly.features.board.application.validation.DeleteBoardValidator;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.board.domain.repository.BoardMemberRepository;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteBoardServiceTest {

    private DeleteBoardService deleteBoardService;

    @Mock
    private DeleteBoardValidator deleteBoardValidator;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardListRepository boardListRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private BoardMemberRepository boardMemberRepository;

    @Mock
    private ValidationMessageResolver validationMessageResolver;

    private final BoardId boardId = new BoardId("board-1");
    private final UserId ownerId = new UserId("owner-1");
    private final UserId otherUserId = new UserId("other-1");

    @BeforeEach
    void setUp() {
        deleteBoardService = new DeleteBoardService(
                deleteBoardValidator,
                boardRepository,
                boardListRepository,
                cardRepository,
                boardMemberRepository,
                validationMessageResolver);
    }

    private DeleteBoardCommand createValidCommand() {
        return DeleteBoardCommand.of(boardId, ownerId);
    }

    private Board createBoard(UserId ownerId) {
        return Board.builder()
                .boardId(boardId)
                .title("테스트 보드")
                .description("테스트 설명")
                .isArchived(false)
                .ownerId(ownerId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("유효한 정보로 보드 삭제가 성공해야 한다")
    void deleteBoard_withValidData_shouldReturnSuccess() {
        // given
        DeleteBoardCommand command = createValidCommand();
        Board board = createBoard(ownerId);

        when(deleteBoardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));
        when(cardRepository.deleteByBoardId(boardId))
                .thenReturn(Either.right(null));
        when(boardMemberRepository.deleteByBoardId(boardId))
                .thenReturn(Either.right(null));
        when(boardRepository.delete(boardId))
                .thenReturn(Either.right(null));

        // when
        Either<Failure, Void> result = deleteBoardService.deleteBoard(command);

        // then
        assertThat(result.isRight()).isTrue();

        verify(deleteBoardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(cardRepository).deleteByBoardId(boardId);
        verify(boardListRepository).deleteByBoardId(boardId);
        verify(boardMemberRepository).deleteByBoardId(boardId);
        verify(boardRepository).delete(boardId);
    }

    @Test
    @DisplayName("입력 검증 실패 시 실패를 반환해야 한다")
    void deleteBoard_withInvalidInput_shouldReturnFailure() {
        // given
        DeleteBoardCommand command = createValidCommand();
        ValidationResult<DeleteBoardCommand> validationResult = ValidationResult.invalid("validation error",
                "INVALID_INPUT", null);

        when(deleteBoardValidator.validate(command))
                .thenReturn(validationResult);
        when(validationMessageResolver.getMessage("validation.input.invalid"))
                .thenReturn("입력 데이터가 올바르지 않습니다");

        // when
        Either<Failure, Void> result = deleteBoardService.deleteBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getMessage()).contains("입력 데이터가 올바르지 않습니다");

        verify(deleteBoardValidator).validate(command);
        verify(boardRepository, never()).findById(any());
    }

    @Test
    @DisplayName("보드가 존재하지 않을 때 실패를 반환해야 한다")
    void deleteBoard_withNonExistentBoard_shouldReturnFailure() {
        // given
        DeleteBoardCommand command = createValidCommand();

        when(deleteBoardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.empty());
        when(validationMessageResolver.getMessage("validation.board.not.found"))
                .thenReturn("보드를 찾을 수 없습니다");

        // when
        Either<Failure, Void> result = deleteBoardService.deleteBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getMessage()).contains("보드를 찾을 수 없습니다");

        verify(boardRepository).findById(boardId);
        verify(cardRepository, never()).deleteByBoardId(any());
    }

    @Test
    @DisplayName("보드 소유자가 아닌 사용자가 삭제를 시도할 때 실패를 반환해야 한다")
    void deleteBoard_withNonOwnerUser_shouldReturnFailure() {
        // given
        DeleteBoardCommand command = DeleteBoardCommand.of(boardId, otherUserId);
        Board board = createBoard(ownerId);

        when(deleteBoardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));
        when(validationMessageResolver.getMessage("validation.board.delete.access.denied"))
                .thenReturn("보드 삭제 권한이 없습니다");

        // when
        Either<Failure, Void> result = deleteBoardService.deleteBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getMessage()).contains("보드 삭제 권한이 없습니다");

        verify(boardRepository).findById(boardId);
        verify(cardRepository, never()).deleteByBoardId(any());
    }

    @Test
    @DisplayName("카드 삭제 실패 시 실패를 반환해야 한다")
    void deleteBoard_whenCardDeletionFails_shouldReturnFailure() {
        // given
        DeleteBoardCommand command = createValidCommand();
        Board board = createBoard(ownerId);
        Failure cardDeletionFailure = Failure.ofInternalServerError("카드 삭제 실패");

        when(deleteBoardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));
        when(cardRepository.deleteByBoardId(boardId))
                .thenReturn(Either.left(cardDeletionFailure));

        // when
        Either<Failure, Void> result = deleteBoardService.deleteBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(cardDeletionFailure);

        verify(cardRepository).deleteByBoardId(boardId);
        verify(boardListRepository, never()).deleteByBoardId(any());
    }

    @Test
    @DisplayName("멤버 삭제 실패 시 실패를 반환해야 한다")
    void deleteBoard_whenMemberDeletionFails_shouldReturnFailure() {
        // given
        DeleteBoardCommand command = createValidCommand();
        Board board = createBoard(ownerId);
        Failure memberDeletionFailure = Failure.ofInternalServerError("멤버 삭제 실패");

        when(deleteBoardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));
        when(cardRepository.deleteByBoardId(boardId))
                .thenReturn(Either.right(null));
        when(boardMemberRepository.deleteByBoardId(boardId))
                .thenReturn(Either.left(memberDeletionFailure));

        // when
        Either<Failure, Void> result = deleteBoardService.deleteBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(memberDeletionFailure);

        verify(cardRepository).deleteByBoardId(boardId);
        verify(boardListRepository).deleteByBoardId(boardId);
        verify(boardMemberRepository).deleteByBoardId(boardId);
        verify(boardRepository, never()).delete(any());
    }

    @Test
    @DisplayName("보드 삭제 실패 시 실패를 반환해야 한다")
    void deleteBoard_whenBoardDeletionFails_shouldReturnFailure() {
        // given
        DeleteBoardCommand command = createValidCommand();
        Board board = createBoard(ownerId);
        Failure boardDeletionFailure = Failure.ofInternalServerError("보드 삭제 실패");

        when(deleteBoardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));
        when(cardRepository.deleteByBoardId(boardId))
                .thenReturn(Either.right(null));
        when(boardMemberRepository.deleteByBoardId(boardId))
                .thenReturn(Either.right(null));
        when(boardRepository.delete(boardId))
                .thenReturn(Either.left(boardDeletionFailure));

        // when
        Either<Failure, Void> result = deleteBoardService.deleteBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(boardDeletionFailure);

        verify(cardRepository).deleteByBoardId(boardId);
        verify(boardListRepository).deleteByBoardId(boardId);
        verify(boardMemberRepository).deleteByBoardId(boardId);
        verify(boardRepository).delete(boardId);
    }
}
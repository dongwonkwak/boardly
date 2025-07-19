package com.boardly.features.boardlist.application.service;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.application.port.input.DeleteBoardListCommand;
import com.boardly.features.boardlist.application.validation.DeleteBoardListValidator;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListColor;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.user.domain.model.UserId;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteBoardListServiceTest {

    private DeleteBoardListService deleteBoardListService;

    @Mock
    private DeleteBoardListValidator deleteBoardListValidator;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardListRepository boardListRepository;

    @BeforeEach
    void setUp() {
        deleteBoardListService = new DeleteBoardListService(
                deleteBoardListValidator,
                boardRepository,
                boardListRepository
        );
    }

    private DeleteBoardListCommand createValidCommand() {
        return new DeleteBoardListCommand(
                new ListId("list-123"),
                new UserId("user-123")
        );
    }

    private Board createValidBoard(BoardId boardId, UserId ownerId) {
        return Board.builder()
                .boardId(boardId)
                .title("테스트 보드")
                .description("테스트 보드 설명")
                .isArchived(false)
                .ownerId(ownerId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private BoardList createValidBoardList(ListId listId, BoardId boardId, int position) {
        return BoardList.builder()
                .listId(listId)
                .title("삭제할 리스트")
                .description("삭제할 리스트 설명")
                .position(position)
                .color(ListColor.of("#0079BF"))
                .boardId(boardId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("유효한 정보로 리스트 삭제가 성공해야 한다")
    void deleteBoardList_withValidData_shouldReturnSuccess() {
        // given
        DeleteBoardListCommand command = createValidCommand();
        BoardId boardId = new BoardId("board-123");
        Board board = createValidBoard(boardId, command.userId());
        BoardList listToDelete = createValidBoardList(command.listId(), boardId, 1);

        when(deleteBoardListValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(command.listId()))
                .thenReturn(Optional.of(listToDelete));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));
        when(boardListRepository.findByBoardIdAndPositionGreaterThan(boardId, 1))
                .thenReturn(List.of());

        // when
        Either<Failure, Void> result = deleteBoardListService.deleteBoardList(command);

        // then
        assertThat(result.isRight()).isTrue();

        verify(deleteBoardListValidator).validate(command);
        verify(boardListRepository).findById(command.listId());
        verify(boardRepository).findById(boardId);
        verify(boardListRepository).deleteById(command.listId());
        verify(boardListRepository).findByBoardIdAndPositionGreaterThan(boardId, 1);
    }

    @Test
    @DisplayName("삭제 후 position 재정렬이 올바르게 동작해야 한다")
    void deleteBoardList_withRemainingLists_shouldReorderPositions() {
        // given
        DeleteBoardListCommand command = createValidCommand();
        BoardId boardId = new BoardId("board-123");
        Board board = createValidBoard(boardId, command.userId());
        BoardList listToDelete = createValidBoardList(command.listId(), boardId, 1);
        
        // 삭제 후 남은 리스트들
        BoardList remainingList1 = createValidBoardList(new ListId("list-456"), boardId, 2);
        BoardList remainingList2 = createValidBoardList(new ListId("list-789"), boardId, 3);
        List<BoardList> remainingLists = List.of(remainingList1, remainingList2);

        when(deleteBoardListValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(command.listId()))
                .thenReturn(Optional.of(listToDelete));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));
        when(boardListRepository.findByBoardIdAndPositionGreaterThan(boardId, 1))
                .thenReturn(remainingLists);
        when(boardListRepository.saveAll(any()))
                .thenReturn(remainingLists);

        // when
        Either<Failure, Void> result = deleteBoardListService.deleteBoardList(command);

        // then
        assertThat(result.isRight()).isTrue();

        verify(deleteBoardListValidator).validate(command);
        verify(boardListRepository).findById(command.listId());
        verify(boardRepository).findById(boardId);
        verify(boardListRepository).deleteById(command.listId());
        verify(boardListRepository).findByBoardIdAndPositionGreaterThan(boardId, 1);
        verify(boardListRepository).saveAll(remainingLists);

        // position이 올바르게 업데이트되었는지 확인
        assertThat(remainingList1.getPosition()).isEqualTo(1);
        assertThat(remainingList2.getPosition()).isEqualTo(2);
    }

    @Test
    @DisplayName("입력 검증 실패 시 검증 오류를 반환해야 한다")
    void deleteBoardList_withInvalidData_shouldReturnValidationFailure() {
        // given
        DeleteBoardListCommand command = createValidCommand();
        Failure.FieldViolation violation = Failure.FieldViolation.builder()
                .field("listId")
                .message("validation.boardlist.listId.required")
                .rejectedValue(command.listId())
                .build();
        ValidationResult<DeleteBoardListCommand> invalidResult = ValidationResult.invalid(violation);

        when(deleteBoardListValidator.validate(command))
                .thenReturn(invalidResult);

        // when
        Either<Failure, Void> result = deleteBoardListService.deleteBoardList(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ValidationFailure.class);
        Failure.ValidationFailure validationFailure = (Failure.ValidationFailure) result.getLeft();
        assertThat(validationFailure.message()).contains("INVALID_INPUT");

        verify(deleteBoardListValidator).validate(command);
        verify(boardListRepository, never()).findById(any(ListId.class));
        verify(boardListRepository, never()).deleteById(any(ListId.class));
    }

    @Test
    @DisplayName("리스트를 찾을 수 없는 경우 NOT_FOUND 오류를 반환해야 한다")
    void deleteBoardList_withNonExistentList_shouldReturnNotFoundFailure() {
        // given
        DeleteBoardListCommand command = createValidCommand();

        when(deleteBoardListValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(command.listId()))
                .thenReturn(Optional.empty());

        // when
        Either<Failure, Void> result = deleteBoardListService.deleteBoardList(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.NotFoundFailure.class);
        assertThat(result.getLeft().message()).isEqualTo("LIST_NOT_FOUND");

        verify(deleteBoardListValidator).validate(command);
        verify(boardListRepository).findById(command.listId());
        verify(boardRepository, never()).findById(any(BoardId.class));
        verify(boardListRepository, never()).deleteById(any(ListId.class));
    }

    @Test
    @DisplayName("보드를 찾을 수 없는 경우 NOT_FOUND 오류를 반환해야 한다")
    void deleteBoardList_withNonExistentBoard_shouldReturnNotFoundFailure() {
        // given
        DeleteBoardListCommand command = createValidCommand();
        BoardId boardId = new BoardId("board-123");
        BoardList listToDelete = createValidBoardList(command.listId(), boardId, 1);

        when(deleteBoardListValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(command.listId()))
                .thenReturn(Optional.of(listToDelete));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.empty());

        // when
        Either<Failure, Void> result = deleteBoardListService.deleteBoardList(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.NotFoundFailure.class);
        assertThat(result.getLeft().message()).isEqualTo("BOARD_NOT_FOUND");

        verify(deleteBoardListValidator).validate(command);
        verify(boardListRepository).findById(command.listId());
        verify(boardRepository).findById(boardId);
        verify(boardListRepository, never()).deleteById(any(ListId.class));
    }

    @Test
    @DisplayName("보드 소유자가 아닌 경우 권한 오류를 반환해야 한다")
    void deleteBoardList_withUnauthorizedUser_shouldReturnForbiddenFailure() {
        // given
        DeleteBoardListCommand command = createValidCommand();
        BoardId boardId = new BoardId("board-123");
        UserId differentUserId = new UserId("different-user");
        Board board = createValidBoard(boardId, differentUserId);
        BoardList listToDelete = createValidBoardList(command.listId(), boardId, 1);

        when(deleteBoardListValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(command.listId()))
                .thenReturn(Optional.of(listToDelete));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));

        // when
        Either<Failure, Void> result = deleteBoardListService.deleteBoardList(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ForbiddenFailure.class);
        assertThat(result.getLeft().message()).isEqualTo("UNAUTHORIZED_ACCESS");

        verify(deleteBoardListValidator).validate(command);
        verify(boardListRepository).findById(command.listId());
        verify(boardRepository).findById(boardId);
        verify(boardListRepository, never()).deleteById(any(ListId.class));
    }

    @Test
    @DisplayName("리스트 삭제 중 예외 발생 시 내부 서버 오류를 반환해야 한다")
    void deleteBoardList_withDeleteException_shouldReturnInternalServerError() {
        // given
        DeleteBoardListCommand command = createValidCommand();
        BoardId boardId = new BoardId("board-123");
        Board board = createValidBoard(boardId, command.userId());
        BoardList listToDelete = createValidBoardList(command.listId(), boardId, 1);

        when(deleteBoardListValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(command.listId()))
                .thenReturn(Optional.of(listToDelete));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));
        doThrow(new RuntimeException("데이터베이스 오류"))
                .when(boardListRepository).deleteById(command.listId());

        // when
        Either<Failure, Void> result = deleteBoardListService.deleteBoardList(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalServerError.class);
        assertThat(result.getLeft().message()).isEqualTo("데이터베이스 오류");

        verify(deleteBoardListValidator).validate(command);
        verify(boardListRepository).findById(command.listId());
        verify(boardRepository).findById(boardId);
        verify(boardListRepository).deleteById(command.listId());
    }

    @Test
    @DisplayName("position 재정렬 중 예외가 발생해도 삭제는 성공해야 한다")
    void deleteBoardList_withReorderException_shouldStillSucceed() {
        // given
        DeleteBoardListCommand command = createValidCommand();
        BoardId boardId = new BoardId("board-123");
        Board board = createValidBoard(boardId, command.userId());
        BoardList listToDelete = createValidBoardList(command.listId(), boardId, 1);
        
        // 삭제 후 남은 리스트들
        BoardList remainingList = createValidBoardList(new ListId("list-456"), boardId, 2);
        List<BoardList> remainingLists = List.of(remainingList);

        when(deleteBoardListValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(command.listId()))
                .thenReturn(Optional.of(listToDelete));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));
        when(boardListRepository.findByBoardIdAndPositionGreaterThan(boardId, 1))
                .thenReturn(remainingLists);
        doThrow(new RuntimeException("재정렬 오류"))
                .when(boardListRepository).saveAll(remainingLists);

        // when
        Either<Failure, Void> result = deleteBoardListService.deleteBoardList(command);

        // then
        assertThat(result.isRight()).isTrue(); // 삭제는 성공해야 함

        verify(deleteBoardListValidator).validate(command);
        verify(boardListRepository).findById(command.listId());
        verify(boardRepository).findById(boardId);
        verify(boardListRepository).deleteById(command.listId());
        verify(boardListRepository).findByBoardIdAndPositionGreaterThan(boardId, 1);
        verify(boardListRepository).saveAll(remainingLists);
    }

    @Test
    @DisplayName("여러 검증 오류가 있는 경우 모든 오류를 반환해야 한다")
    void deleteBoardList_withMultipleValidationErrors_shouldReturnAllErrors() {
        // given
        DeleteBoardListCommand command = createValidCommand();
        Failure.FieldViolation listIdViolation = Failure.FieldViolation.builder()
                .field("listId")
                .message("validation.boardlist.listId.required")
                .rejectedValue(command.listId())
                .build();
        Failure.FieldViolation userIdViolation = Failure.FieldViolation.builder()
                .field("userId")
                .message("validation.boardlist.userId.required")
                .rejectedValue(command.userId())
                .build();
        ValidationResult<DeleteBoardListCommand> invalidResult = ValidationResult.invalid(
                io.vavr.collection.List.of(listIdViolation, userIdViolation));

        when(deleteBoardListValidator.validate(command))
                .thenReturn(invalidResult);

        // when
        Either<Failure, Void> result = deleteBoardListService.deleteBoardList(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ValidationFailure.class);
        Failure.ValidationFailure validationFailure = (Failure.ValidationFailure) result.getLeft();
        assertThat(validationFailure.violations()).hasSize(2);

        verify(deleteBoardListValidator).validate(command);
        verify(boardListRepository, never()).findById(any(ListId.class));
        verify(boardListRepository, never()).deleteById(any(ListId.class));
    }

    @Test
    @DisplayName("마지막 리스트를 삭제할 때 position 재정렬이 필요하지 않아야 한다")
    void deleteBoardList_withLastList_shouldNotReorderPositions() {
        // given
        DeleteBoardListCommand command = createValidCommand();
        BoardId boardId = new BoardId("board-123");
        Board board = createValidBoard(boardId, command.userId());
        BoardList listToDelete = createValidBoardList(command.listId(), boardId, 0); // 첫 번째 리스트

        when(deleteBoardListValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(command.listId()))
                .thenReturn(Optional.of(listToDelete));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));
        when(boardListRepository.findByBoardIdAndPositionGreaterThan(boardId, 0))
                .thenReturn(List.of()); // 남은 리스트 없음

        // when
        Either<Failure, Void> result = deleteBoardListService.deleteBoardList(command);

        // then
        assertThat(result.isRight()).isTrue();

        verify(deleteBoardListValidator).validate(command);
        verify(boardListRepository).findById(command.listId());
        verify(boardRepository).findById(boardId);
        verify(boardListRepository).deleteById(command.listId());
        verify(boardListRepository).findByBoardIdAndPositionGreaterThan(boardId, 0);
        verify(boardListRepository, never()).saveAll(any()); // saveAll 호출되지 않음
    }
} 
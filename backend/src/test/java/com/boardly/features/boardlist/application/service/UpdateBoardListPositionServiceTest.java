package com.boardly.features.boardlist.application.service;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.application.port.input.UpdateBoardListPositionCommand;
import com.boardly.features.boardlist.application.validation.UpdateBoardListPositionValidator;
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
class UpdateBoardListPositionServiceTest {

    private UpdateBoardListPositionService updateBoardListPositionService;

    @Mock
    private UpdateBoardListPositionValidator updateBoardListPositionValidator;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardListRepository boardListRepository;

    @BeforeEach
    void setUp() {
        updateBoardListPositionService = new UpdateBoardListPositionService(
                updateBoardListPositionValidator,
                boardRepository,
                boardListRepository
        );
    }

    private UpdateBoardListPositionCommand createValidCommand() {
        return new UpdateBoardListPositionCommand(
                new ListId("list-2"),
                new UserId("user-123"),
                0
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
                .title("테스트 리스트 " + position)
                .description("테스트 리스트 설명 " + position)
                .position(position)
                .color(ListColor.of("#0079BF"))
                .boardId(boardId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("유효한 정보로 리스트 위치 변경이 성공해야 한다")
    void updateBoardListPosition_withValidData_shouldReturnUpdatedLists() {
        // given
        UpdateBoardListPositionCommand command = createValidCommand();
        BoardId boardId = new BoardId("board-123");
        Board board = createValidBoard(boardId, command.userId());
        
        // 3개의 리스트가 있는 상황에서 2번째 리스트(위치 1)를 첫 번째(위치 0)로 이동
        List<BoardList> originalLists = List.of(
                createValidBoardList(new ListId("list-1"), boardId, 0),
                createValidBoardList(command.listId(), boardId, 1), // 이동할 리스트
                createValidBoardList(new ListId("list-3"), boardId, 2)
        );

        when(updateBoardListPositionValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(command.listId()))
                .thenReturn(Optional.of(originalLists.get(1)));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));
        when(boardListRepository.findByBoardIdOrderByPosition(boardId))
                .thenReturn(originalLists);
        when(boardListRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Either<Failure, List<BoardList>> result = updateBoardListPositionService.updateBoardListPosition(command);

        // then
        assertThat(result.isRight()).isTrue();
        List<BoardList> updatedLists = result.get();
        assertThat(updatedLists).hasSize(3);
        
        // 위치가 올바르게 변경되었는지 확인
        assertThat(updatedLists.get(0).getListId()).isEqualTo(command.listId()); // 첫 번째 위치로 이동
        assertThat(updatedLists.get(0).getPosition()).isEqualTo(0);
        assertThat(updatedLists.get(1).getListId()).isEqualTo(new ListId("list-1")); // 한 칸 뒤로
        assertThat(updatedLists.get(1).getPosition()).isEqualTo(1);
        assertThat(updatedLists.get(2).getListId()).isEqualTo(new ListId("list-3")); // 그대로
        assertThat(updatedLists.get(2).getPosition()).isEqualTo(2);

        verify(updateBoardListPositionValidator).validate(command);
        verify(boardListRepository).findById(command.listId());
        verify(boardRepository).findById(boardId);
        verify(boardListRepository).findByBoardIdOrderByPosition(boardId);
        verify(boardListRepository).saveAll(any());
    }

    @Test
    @DisplayName("위치가 변경되지 않는 경우 기존 리스트를 반환해야 한다")
    void updateBoardListPosition_withSamePosition_shouldReturnOriginalLists() {
        // given
        UpdateBoardListPositionCommand command = new UpdateBoardListPositionCommand(
                new ListId("list-1"),
                new UserId("user-123"),
                0 // 이미 0번 위치에 있는 리스트
        );
        BoardId boardId = new BoardId("board-123");
        Board board = createValidBoard(boardId, command.userId());
        BoardList list = createValidBoardList(command.listId(), boardId, 0);
        List<BoardList> originalLists = List.of(list);

        when(updateBoardListPositionValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(command.listId()))
                .thenReturn(Optional.of(list));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));
        when(boardListRepository.findByBoardIdOrderByPosition(boardId))
                .thenReturn(originalLists);

        // when
        Either<Failure, List<BoardList>> result = updateBoardListPositionService.updateBoardListPosition(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isEqualTo(originalLists);

        verify(updateBoardListPositionValidator).validate(command);
        verify(boardListRepository).findById(command.listId());
        verify(boardRepository).findById(boardId);
        verify(boardListRepository).findByBoardIdOrderByPosition(boardId);
        verify(boardListRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("입력 검증 실패 시 검증 오류를 반환해야 한다")
    void updateBoardListPosition_withInvalidData_shouldReturnValidationFailure() {
        // given
        UpdateBoardListPositionCommand command = createValidCommand();
        Failure.FieldViolation violation = Failure.FieldViolation.builder()
                .field("newPosition")
                .message("validation.boardlist.position.invalid")
                .rejectedValue(command.newPosition())
                .build();
        ValidationResult<UpdateBoardListPositionCommand> invalidResult = ValidationResult.invalid(violation);

        when(updateBoardListPositionValidator.validate(command))
                .thenReturn(invalidResult);

        // when
        Either<Failure, List<BoardList>> result = updateBoardListPositionService.updateBoardListPosition(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ValidationFailure.class);
        Failure.ValidationFailure validationFailure = (Failure.ValidationFailure) result.getLeft();
        assertThat(validationFailure.message()).contains("INVALID_INPUT");

        verify(updateBoardListPositionValidator).validate(command);
        verify(boardListRepository, never()).findById(any(ListId.class));
        verify(boardListRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("리스트를 찾을 수 없는 경우 NOT_FOUND 오류를 반환해야 한다")
    void updateBoardListPosition_withNonExistentList_shouldReturnNotFoundFailure() {
        // given
        UpdateBoardListPositionCommand command = createValidCommand();

        when(updateBoardListPositionValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(command.listId()))
                .thenReturn(Optional.empty());

        // when
        Either<Failure, List<BoardList>> result = updateBoardListPositionService.updateBoardListPosition(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.NotFoundFailure.class);
        assertThat(result.getLeft().message()).isEqualTo("LIST_NOT_FOUND");

        verify(updateBoardListPositionValidator).validate(command);
        verify(boardListRepository).findById(command.listId());
        verify(boardRepository, never()).findById(any(BoardId.class));
        verify(boardListRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("보드를 찾을 수 없는 경우 NOT_FOUND 오류를 반환해야 한다")
    void updateBoardListPosition_withNonExistentBoard_shouldReturnNotFoundFailure() {
        // given
        UpdateBoardListPositionCommand command = createValidCommand();
        BoardId boardId = new BoardId("board-123");
        BoardList list = createValidBoardList(command.listId(), boardId, 1);

        when(updateBoardListPositionValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(command.listId()))
                .thenReturn(Optional.of(list));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.empty());

        // when
        Either<Failure, List<BoardList>> result = updateBoardListPositionService.updateBoardListPosition(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.NotFoundFailure.class);
        assertThat(result.getLeft().message()).isEqualTo("BOARD_NOT_FOUND");

        verify(updateBoardListPositionValidator).validate(command);
        verify(boardListRepository).findById(command.listId());
        verify(boardRepository).findById(boardId);
        verify(boardListRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("보드 소유자가 아닌 경우 권한 오류를 반환해야 한다")
    void updateBoardListPosition_withUnauthorizedUser_shouldReturnForbiddenFailure() {
        // given
        UpdateBoardListPositionCommand command = createValidCommand();
        BoardId boardId = new BoardId("board-123");
        UserId differentUserId = new UserId("different-user");
        Board board = createValidBoard(boardId, differentUserId);
        BoardList list = createValidBoardList(command.listId(), boardId, 1);

        when(updateBoardListPositionValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(command.listId()))
                .thenReturn(Optional.of(list));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));

        // when
        Either<Failure, List<BoardList>> result = updateBoardListPositionService.updateBoardListPosition(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ForbiddenFailure.class);
        assertThat(result.getLeft().message()).isEqualTo("UNAUTHORIZED_ACCESS");

        verify(updateBoardListPositionValidator).validate(command);
        verify(boardListRepository).findById(command.listId());
        verify(boardRepository).findById(boardId);
        verify(boardListRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("새로운 위치가 리스트 개수를 초과하는 경우 오류를 반환해야 한다")
    void updateBoardListPosition_withInvalidPosition_shouldReturnConflictFailure() {
        // given
        UpdateBoardListPositionCommand command = new UpdateBoardListPositionCommand(
                new ListId("list-1"),
                new UserId("user-123"),
                3 // 3개 리스트가 있는데 3번 위치로 이동 시도
        );
        BoardId boardId = new BoardId("board-123");
        Board board = createValidBoard(boardId, command.userId());
        BoardList list = createValidBoardList(command.listId(), boardId, 0);
        List<BoardList> originalLists = List.of(
                list,
                createValidBoardList(new ListId("list-2"), boardId, 1),
                createValidBoardList(new ListId("list-3"), boardId, 2)
        );

        when(updateBoardListPositionValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(command.listId()))
                .thenReturn(Optional.of(list));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));
        when(boardListRepository.findByBoardIdOrderByPosition(boardId))
                .thenReturn(originalLists);

        // when
        Either<Failure, List<BoardList>> result = updateBoardListPositionService.updateBoardListPosition(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ConflictFailure.class);
        assertThat(result.getLeft().message()).isEqualTo("INVALID_POSITION");

        verify(updateBoardListPositionValidator).validate(command);
        verify(boardListRepository).findById(command.listId());
        verify(boardRepository).findById(boardId);
        verify(boardListRepository).findByBoardIdOrderByPosition(boardId);
        verify(boardListRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("위치 변경 중 예외 발생 시 내부 서버 오류를 반환해야 한다")
    void updateBoardListPosition_withRepositoryException_shouldReturnInternalServerError() {
        // given
        UpdateBoardListPositionCommand command = createValidCommand();
        BoardId boardId = new BoardId("board-123");
        Board board = createValidBoard(boardId, command.userId());
        BoardList list = createValidBoardList(command.listId(), boardId, 1);
        List<BoardList> originalLists = List.of(
                createValidBoardList(new ListId("list-1"), boardId, 0),
                list,
                createValidBoardList(new ListId("list-3"), boardId, 2)
        );

        when(updateBoardListPositionValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(command.listId()))
                .thenReturn(Optional.of(list));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));
        when(boardListRepository.findByBoardIdOrderByPosition(boardId))
                .thenReturn(originalLists);
        when(boardListRepository.saveAll(any()))
                .thenThrow(new RuntimeException("데이터베이스 오류"));

        // when
        Either<Failure, List<BoardList>> result = updateBoardListPositionService.updateBoardListPosition(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalServerError.class);
        assertThat(result.getLeft().message()).isEqualTo("데이터베이스 오류");

        verify(updateBoardListPositionValidator).validate(command);
        verify(boardListRepository).findById(command.listId());
        verify(boardRepository).findById(boardId);
        verify(boardListRepository).findByBoardIdOrderByPosition(boardId);
        verify(boardListRepository).saveAll(any());
    }

    @Test
    @DisplayName("리스트를 뒤로 이동하는 경우 올바르게 처리되어야 한다")
    void updateBoardListPosition_moveBackward_shouldReorderCorrectly() {
        // given
        UpdateBoardListPositionCommand command = new UpdateBoardListPositionCommand(
                new ListId("list-1"),
                new UserId("user-123"),
                2 // 첫 번째 리스트를 세 번째 위치로 이동
        );
        BoardId boardId = new BoardId("board-123");
        Board board = createValidBoard(boardId, command.userId());
        
        List<BoardList> originalLists = List.of(
                createValidBoardList(command.listId(), boardId, 0), // 이동할 리스트
                createValidBoardList(new ListId("list-2"), boardId, 1),
                createValidBoardList(new ListId("list-3"), boardId, 2)
        );

        when(updateBoardListPositionValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(command.listId()))
                .thenReturn(Optional.of(originalLists.get(0)));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));
        when(boardListRepository.findByBoardIdOrderByPosition(boardId))
                .thenReturn(originalLists);
        when(boardListRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Either<Failure, List<BoardList>> result = updateBoardListPositionService.updateBoardListPosition(command);

        // then
        assertThat(result.isRight()).isTrue();
        List<BoardList> updatedLists = result.get();
        assertThat(updatedLists).hasSize(3);
        
        // 위치가 올바르게 변경되었는지 확인
        assertThat(updatedLists.get(0).getListId()).isEqualTo(new ListId("list-2")); // 한 칸 앞으로
        assertThat(updatedLists.get(0).getPosition()).isEqualTo(0);
        assertThat(updatedLists.get(1).getListId()).isEqualTo(new ListId("list-3")); // 한 칸 앞으로
        assertThat(updatedLists.get(1).getPosition()).isEqualTo(1);
        assertThat(updatedLists.get(2).getListId()).isEqualTo(command.listId()); // 마지막 위치로 이동
        assertThat(updatedLists.get(2).getPosition()).isEqualTo(2);

        verify(updateBoardListPositionValidator).validate(command);
        verify(boardListRepository).findById(command.listId());
        verify(boardRepository).findById(boardId);
        verify(boardListRepository).findByBoardIdOrderByPosition(boardId);
        verify(boardListRepository).saveAll(any());
    }
} 
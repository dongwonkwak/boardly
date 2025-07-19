package com.boardly.features.boardlist.application.service;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.application.port.input.GetBoardListsCommand;
import com.boardly.features.boardlist.application.validation.GetBoardListsValidator;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListColor;
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
class GetBoardListsServiceTest {

    private GetBoardListsService getBoardListsService;

    @Mock
    private GetBoardListsValidator getBoardListsValidator;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardListRepository boardListRepository;

    @BeforeEach
    void setUp() {
        getBoardListsService = new GetBoardListsService(
                getBoardListsValidator,
                boardRepository,
                boardListRepository
        );
    }

    private GetBoardListsCommand createValidCommand() {
        return new GetBoardListsCommand(
                new BoardId("board-123"),
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

    private BoardList createValidBoardList(BoardId boardId, int position) {
        return BoardList.builder()
                .listId(new com.boardly.features.boardlist.domain.model.ListId("list-" + position))
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
    @DisplayName("유효한 정보로 보드 리스트 조회가 성공해야 한다")
    void getBoardLists_withValidData_shouldReturnBoardLists() {
        // given
        GetBoardListsCommand command = createValidCommand();
        BoardId boardId = command.boardId();
        Board board = createValidBoard(boardId, command.userId());
        
        List<BoardList> expectedLists = List.of(
                createValidBoardList(boardId, 0),
                createValidBoardList(boardId, 1),
                createValidBoardList(boardId, 2)
        );

        when(getBoardListsValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));
        when(boardListRepository.findByBoardIdOrderByPosition(boardId))
                .thenReturn(expectedLists);

        // when
        Either<Failure, List<BoardList>> result = getBoardListsService.getBoardLists(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).hasSize(3);
        assertThat(result.get()).isEqualTo(expectedLists);

        verify(getBoardListsValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardListRepository).findByBoardIdOrderByPosition(boardId);
    }

    @Test
    @DisplayName("빈 리스트가 반환되어도 성공해야 한다")
    void getBoardLists_withEmptyList_shouldReturnEmptyList() {
        // given
        GetBoardListsCommand command = createValidCommand();
        BoardId boardId = command.boardId();
        Board board = createValidBoard(boardId, command.userId());

        when(getBoardListsValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));
        when(boardListRepository.findByBoardIdOrderByPosition(boardId))
                .thenReturn(List.of());

        // when
        Either<Failure, List<BoardList>> result = getBoardListsService.getBoardLists(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isEmpty();

        verify(getBoardListsValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardListRepository).findByBoardIdOrderByPosition(boardId);
    }

    @Test
    @DisplayName("입력 검증 실패 시 검증 오류를 반환해야 한다")
    void getBoardLists_withInvalidData_shouldReturnValidationFailure() {
        // given
        GetBoardListsCommand command = createValidCommand();
        Failure.FieldViolation violation = Failure.FieldViolation.builder()
                .field("boardId")
                .message("validation.boardlist.boardId.required")
                .rejectedValue(command.boardId())
                .build();
        ValidationResult<GetBoardListsCommand> invalidResult = ValidationResult.invalid(violation);

        when(getBoardListsValidator.validate(command))
                .thenReturn(invalidResult);

        // when
        Either<Failure, List<BoardList>> result = getBoardListsService.getBoardLists(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ValidationFailure.class);
        Failure.ValidationFailure validationFailure = (Failure.ValidationFailure) result.getLeft();
        assertThat(validationFailure.message()).contains("INVALID_INPUT");

        verify(getBoardListsValidator).validate(command);
        verify(boardRepository, never()).findById(any(BoardId.class));
        verify(boardListRepository, never()).findByBoardIdOrderByPosition(any(BoardId.class));
    }

    @Test
    @DisplayName("보드를 찾을 수 없는 경우 NOT_FOUND 오류를 반환해야 한다")
    void getBoardLists_withNonExistentBoard_shouldReturnNotFoundFailure() {
        // given
        GetBoardListsCommand command = createValidCommand();
        BoardId boardId = command.boardId();

        when(getBoardListsValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.empty());

        // when
        Either<Failure, List<BoardList>> result = getBoardListsService.getBoardLists(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.NotFoundFailure.class);
        assertThat(result.getLeft().message()).isEqualTo("BOARD_NOT_FOUND");

        verify(getBoardListsValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardListRepository, never()).findByBoardIdOrderByPosition(any(BoardId.class));
    }

    @Test
    @DisplayName("보드 소유자가 아닌 경우 권한 오류를 반환해야 한다")
    void getBoardLists_withUnauthorizedUser_shouldReturnForbiddenFailure() {
        // given
        GetBoardListsCommand command = createValidCommand();
        BoardId boardId = command.boardId();
        UserId differentUserId = new UserId("different-user");
        Board board = createValidBoard(boardId, differentUserId);

        when(getBoardListsValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));

        // when
        Either<Failure, List<BoardList>> result = getBoardListsService.getBoardLists(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ForbiddenFailure.class);
        assertThat(result.getLeft().message()).isEqualTo("UNAUTHORIZED_ACCESS");

        verify(getBoardListsValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardListRepository, never()).findByBoardIdOrderByPosition(any(BoardId.class));
    }

    @Test
    @DisplayName("리스트 조회 중 예외 발생 시 내부 서버 오류를 반환해야 한다")
    void getBoardLists_withRepositoryException_shouldReturnInternalServerError() {
        // given
        GetBoardListsCommand command = createValidCommand();
        BoardId boardId = command.boardId();
        Board board = createValidBoard(boardId, command.userId());

        when(getBoardListsValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));
        when(boardListRepository.findByBoardIdOrderByPosition(boardId))
                .thenThrow(new RuntimeException("데이터베이스 오류"));

        // when
        Either<Failure, List<BoardList>> result = getBoardListsService.getBoardLists(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalServerError.class);
        assertThat(result.getLeft().message()).isEqualTo("데이터베이스 오류");

        verify(getBoardListsValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardListRepository).findByBoardIdOrderByPosition(boardId);
    }

    @Test
    @DisplayName("여러 검증 오류가 있는 경우 모든 오류를 반환해야 한다")
    void getBoardLists_withMultipleValidationErrors_shouldReturnAllErrors() {
        // given
        GetBoardListsCommand command = createValidCommand();
        Failure.FieldViolation boardIdViolation = Failure.FieldViolation.builder()
                .field("boardId")
                .message("validation.boardlist.boardId.required")
                .rejectedValue(command.boardId())
                .build();
        Failure.FieldViolation userIdViolation = Failure.FieldViolation.builder()
                .field("userId")
                .message("validation.boardlist.userId.required")
                .rejectedValue(command.userId())
                .build();
        ValidationResult<GetBoardListsCommand> invalidResult = ValidationResult.invalid(
                io.vavr.collection.List.of(boardIdViolation, userIdViolation));

        when(getBoardListsValidator.validate(command))
                .thenReturn(invalidResult);

        // when
        Either<Failure, List<BoardList>> result = getBoardListsService.getBoardLists(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ValidationFailure.class);
        Failure.ValidationFailure validationFailure = (Failure.ValidationFailure) result.getLeft();
        assertThat(validationFailure.violations()).hasSize(2);

        verify(getBoardListsValidator).validate(command);
        verify(boardRepository, never()).findById(any(BoardId.class));
        verify(boardListRepository, never()).findByBoardIdOrderByPosition(any(BoardId.class));
    }

    @Test
    @DisplayName("position 순서대로 정렬된 리스트가 반환되어야 한다")
    void getBoardLists_shouldReturnSortedLists() {
        // given
        GetBoardListsCommand command = createValidCommand();
        BoardId boardId = command.boardId();
        Board board = createValidBoard(boardId, command.userId());
        
        // position 순서대로 정렬된 리스트들
        List<BoardList> expectedLists = List.of(
                createValidBoardList(boardId, 0),
                createValidBoardList(boardId, 1),
                createValidBoardList(boardId, 2)
        );

        when(getBoardListsValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));
        when(boardListRepository.findByBoardIdOrderByPosition(boardId))
                .thenReturn(expectedLists);

        // when
        Either<Failure, List<BoardList>> result = getBoardListsService.getBoardLists(command);

        // then
        assertThat(result.isRight()).isTrue();
        List<BoardList> actualLists = result.get();
        
        // position 순서대로 정렬되어 있는지 확인
        for (int i = 0; i < actualLists.size(); i++) {
            assertThat(actualLists.get(i).getPosition()).isEqualTo(i);
        }

        verify(getBoardListsValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardListRepository).findByBoardIdOrderByPosition(boardId);
    }
} 
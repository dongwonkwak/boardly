package com.boardly.features.boardlist.application.service;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.application.port.input.UpdateBoardListCommand;
import com.boardly.features.boardlist.application.validation.UpdateBoardListValidator;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateBoardListServiceTest {

    private UpdateBoardListService updateBoardListService;

    @Mock
    private UpdateBoardListValidator updateBoardListValidator;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardListRepository boardListRepository;

    @BeforeEach
    void setUp() {
        updateBoardListService = new UpdateBoardListService(
                updateBoardListValidator,
                boardRepository,
                boardListRepository
        );
    }

    private UpdateBoardListCommand createValidCommand() {
        return new UpdateBoardListCommand(
                new ListId("list-123"),
                new UserId("user-123"),
                "수정된 리스트",
                "수정된 설명",
                ListColor.of("#D29034")
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

    private BoardList createValidBoardList(ListId listId, BoardId boardId) {
        return BoardList.builder()
                .listId(listId)
                .title("기존 리스트")
                .description("기존 설명")
                .position(0)
                .color(ListColor.of("#0079BF"))
                .boardId(boardId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("유효한 정보로 리스트 수정이 성공해야 한다")
    void updateBoardList_withValidData_shouldReturnUpdatedBoardList() {
        // given
        UpdateBoardListCommand command = createValidCommand();
        BoardId boardId = new BoardId("board-123");
        Board board = createValidBoard(boardId, command.userId());
        BoardList existingList = createValidBoardList(command.listId(), boardId);
        BoardList updatedList = createValidBoardList(command.listId(), boardId);
        updatedList.updateTitle(command.title());
        updatedList.updateDescription(command.description());
        updatedList.updateColor(command.color());

        when(updateBoardListValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(command.listId()))
                .thenReturn(Optional.of(existingList));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));
        when(boardListRepository.save(any(BoardList.class)))
                .thenReturn(updatedList);

        // when
        Either<Failure, BoardList> result = updateBoardListService.updateBoardList(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().getTitle()).isEqualTo(command.title());
        assertThat(result.get().getDescription()).isEqualTo(command.description());
        assertThat(result.get().getColor()).isEqualTo(command.color());

        verify(updateBoardListValidator).validate(command);
        verify(boardListRepository).findById(command.listId());
        verify(boardRepository).findById(boardId);
        verify(boardListRepository).save(any(BoardList.class));
    }

    @Test
    @DisplayName("설명이 null인 경우에도 리스트 수정이 성공해야 한다")
    void updateBoardList_withNullDescription_shouldReturnUpdatedBoardList() {
        // given
        UpdateBoardListCommand command = new UpdateBoardListCommand(
                new ListId("list-123"),
                new UserId("user-123"),
                "수정된 리스트",
                null,
                ListColor.of("#D29034")
        );
        BoardId boardId = new BoardId("board-123");
        Board board = createValidBoard(boardId, command.userId());
        BoardList existingList = createValidBoardList(command.listId(), boardId);
        BoardList updatedList = createValidBoardList(command.listId(), boardId);
        updatedList.updateTitle(command.title());
        updatedList.updateColor(command.color());

        when(updateBoardListValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(command.listId()))
                .thenReturn(Optional.of(existingList));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));
        when(boardListRepository.save(any(BoardList.class)))
                .thenReturn(updatedList);

        // when
        Either<Failure, BoardList> result = updateBoardListService.updateBoardList(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().getTitle()).isEqualTo(command.title());
        assertThat(result.get().getDescription()).isEqualTo("기존 설명"); // 기존 설명 유지

        verify(updateBoardListValidator).validate(command);
        verify(boardListRepository).findById(command.listId());
        verify(boardRepository).findById(boardId);
        verify(boardListRepository).save(any(BoardList.class));
    }

    @Test
    @DisplayName("색상이 null인 경우에도 리스트 수정이 성공해야 한다")
    void updateBoardList_withNullColor_shouldReturnUpdatedBoardList() {
        // given
        UpdateBoardListCommand command = new UpdateBoardListCommand(
                new ListId("list-123"),
                new UserId("user-123"),
                "수정된 리스트",
                "수정된 설명",
                null
        );
        BoardId boardId = new BoardId("board-123");
        Board board = createValidBoard(boardId, command.userId());
        BoardList existingList = createValidBoardList(command.listId(), boardId);
        BoardList updatedList = createValidBoardList(command.listId(), boardId);
        updatedList.updateTitle(command.title());
        updatedList.updateDescription(command.description());

        when(updateBoardListValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(command.listId()))
                .thenReturn(Optional.of(existingList));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));
        when(boardListRepository.save(any(BoardList.class)))
                .thenReturn(updatedList);

        // when
        Either<Failure, BoardList> result = updateBoardListService.updateBoardList(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().getTitle()).isEqualTo(command.title());
        assertThat(result.get().getDescription()).isEqualTo(command.description());
        assertThat(result.get().getColor()).isEqualTo(ListColor.of("#0079BF")); // 기존 색상 유지

        verify(updateBoardListValidator).validate(command);
        verify(boardListRepository).findById(command.listId());
        verify(boardRepository).findById(boardId);
        verify(boardListRepository).save(any(BoardList.class));
    }

    @Test
    @DisplayName("입력 검증 실패 시 검증 오류를 반환해야 한다")
    void updateBoardList_withInvalidData_shouldReturnValidationFailure() {
        // given
        UpdateBoardListCommand command = createValidCommand();
        Failure.FieldViolation violation = Failure.FieldViolation.builder()
                .field("title")
                .message("validation.boardlist.title.required")
                .rejectedValue(command.title())
                .build();
        ValidationResult<UpdateBoardListCommand> invalidResult = ValidationResult.invalid(violation);

        when(updateBoardListValidator.validate(command))
                .thenReturn(invalidResult);

        // when
        Either<Failure, BoardList> result = updateBoardListService.updateBoardList(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ValidationFailure.class);
        Failure.ValidationFailure validationFailure = (Failure.ValidationFailure) result.getLeft();
        assertThat(validationFailure.message()).contains("INVALID_INPUT");

        verify(updateBoardListValidator).validate(command);
        verify(boardListRepository, never()).findById(any(ListId.class));
        verify(boardListRepository, never()).save(any(BoardList.class));
    }

    @Test
    @DisplayName("리스트를 찾을 수 없는 경우 NOT_FOUND 오류를 반환해야 한다")
    void updateBoardList_withNonExistentList_shouldReturnNotFoundFailure() {
        // given
        UpdateBoardListCommand command = createValidCommand();

        when(updateBoardListValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(command.listId()))
                .thenReturn(Optional.empty());

        // when
        Either<Failure, BoardList> result = updateBoardListService.updateBoardList(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.NotFoundFailure.class);
        assertThat(result.getLeft().message()).isEqualTo("LIST_NOT_FOUND");

        verify(updateBoardListValidator).validate(command);
        verify(boardListRepository).findById(command.listId());
        verify(boardRepository, never()).findById(any(BoardId.class));
        verify(boardListRepository, never()).save(any(BoardList.class));
    }

    @Test
    @DisplayName("보드를 찾을 수 없는 경우 NOT_FOUND 오류를 반환해야 한다")
    void updateBoardList_withNonExistentBoard_shouldReturnNotFoundFailure() {
        // given
        UpdateBoardListCommand command = createValidCommand();
        BoardId boardId = new BoardId("board-123");
        BoardList existingList = createValidBoardList(command.listId(), boardId);

        when(updateBoardListValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(command.listId()))
                .thenReturn(Optional.of(existingList));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.empty());

        // when
        Either<Failure, BoardList> result = updateBoardListService.updateBoardList(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.NotFoundFailure.class);
        assertThat(result.getLeft().message()).isEqualTo("BOARD_NOT_FOUND");

        verify(updateBoardListValidator).validate(command);
        verify(boardListRepository).findById(command.listId());
        verify(boardRepository).findById(boardId);
        verify(boardListRepository, never()).save(any(BoardList.class));
    }

    @Test
    @DisplayName("보드 소유자가 아닌 경우 권한 오류를 반환해야 한다")
    void updateBoardList_withUnauthorizedUser_shouldReturnForbiddenFailure() {
        // given
        UpdateBoardListCommand command = createValidCommand();
        BoardId boardId = new BoardId("board-123");
        UserId differentUserId = new UserId("different-user");
        Board board = createValidBoard(boardId, differentUserId);
        BoardList existingList = createValidBoardList(command.listId(), boardId);

        when(updateBoardListValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(command.listId()))
                .thenReturn(Optional.of(existingList));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));

        // when
        Either<Failure, BoardList> result = updateBoardListService.updateBoardList(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ForbiddenFailure.class);
        assertThat(result.getLeft().message()).isEqualTo("UNAUTHORIZED_ACCESS");

        verify(updateBoardListValidator).validate(command);
        verify(boardListRepository).findById(command.listId());
        verify(boardRepository).findById(boardId);
        verify(boardListRepository, never()).save(any(BoardList.class));
    }

    @Test
    @DisplayName("리스트 저장 중 예외 발생 시 내부 서버 오류를 반환해야 한다")
    void updateBoardList_withSaveException_shouldReturnInternalServerError() {
        // given
        UpdateBoardListCommand command = createValidCommand();
        BoardId boardId = new BoardId("board-123");
        Board board = createValidBoard(boardId, command.userId());
        BoardList existingList = createValidBoardList(command.listId(), boardId);

        when(updateBoardListValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(command.listId()))
                .thenReturn(Optional.of(existingList));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));
        when(boardListRepository.save(any(BoardList.class)))
                .thenThrow(new RuntimeException("데이터베이스 오류"));

        // when
        Either<Failure, BoardList> result = updateBoardListService.updateBoardList(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalServerError.class);
        assertThat(result.getLeft().message()).isEqualTo("데이터베이스 오류");

        verify(updateBoardListValidator).validate(command);
        verify(boardListRepository).findById(command.listId());
        verify(boardRepository).findById(boardId);
        verify(boardListRepository).save(any(BoardList.class));
    }

    @Test
    @DisplayName("여러 검증 오류가 있는 경우 모든 오류를 반환해야 한다")
    void updateBoardList_withMultipleValidationErrors_shouldReturnAllErrors() {
        // given
        UpdateBoardListCommand command = createValidCommand();
        Failure.FieldViolation titleViolation = Failure.FieldViolation.builder()
                .field("title")
                .message("validation.boardlist.title.required")
                .rejectedValue(command.title())
                .build();
        Failure.FieldViolation listIdViolation = Failure.FieldViolation.builder()
                .field("listId")
                .message("validation.boardlist.listId.required")
                .rejectedValue(command.listId())
                .build();
        ValidationResult<UpdateBoardListCommand> invalidResult = ValidationResult.invalid(
                io.vavr.collection.List.of(titleViolation, listIdViolation));

        when(updateBoardListValidator.validate(command))
                .thenReturn(invalidResult);

        // when
        Either<Failure, BoardList> result = updateBoardListService.updateBoardList(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ValidationFailure.class);
        Failure.ValidationFailure validationFailure = (Failure.ValidationFailure) result.getLeft();
        assertThat(validationFailure.violations()).hasSize(2);

        verify(updateBoardListValidator).validate(command);
        verify(boardListRepository, never()).findById(any(ListId.class));
        verify(boardListRepository, never()).save(any(BoardList.class));
    }

    @Test
    @DisplayName("부분 업데이트가 올바르게 동작해야 한다")
    void updateBoardList_withPartialUpdate_shouldUpdateOnlyProvidedFields() {
        // given
        UpdateBoardListCommand command = new UpdateBoardListCommand(
                new ListId("list-123"),
                new UserId("user-123"),
                "수정된 리스트",
                null,
                null
        );
        BoardId boardId = new BoardId("board-123");
        Board board = createValidBoard(boardId, command.userId());
        BoardList existingList = createValidBoardList(command.listId(), boardId);
        BoardList updatedList = createValidBoardList(command.listId(), boardId);
        updatedList.updateTitle(command.title());

        when(updateBoardListValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardListRepository.findById(command.listId()))
                .thenReturn(Optional.of(existingList));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(board));
        when(boardListRepository.save(any(BoardList.class)))
                .thenReturn(updatedList);

        // when
        Either<Failure, BoardList> result = updateBoardListService.updateBoardList(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().getTitle()).isEqualTo(command.title());
        assertThat(result.get().getDescription()).isEqualTo("기존 설명"); // 기존 값 유지
        assertThat(result.get().getColor()).isEqualTo(ListColor.of("#0079BF")); // 기존 값 유지

        verify(updateBoardListValidator).validate(command);
        verify(boardListRepository).findById(command.listId());
        verify(boardRepository).findById(boardId);
        verify(boardListRepository).save(any(BoardList.class));
    }
} 
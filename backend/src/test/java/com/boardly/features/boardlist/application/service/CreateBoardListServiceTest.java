package com.boardly.features.boardlist.application.service;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.application.port.input.CreateBoardListCommand;
import com.boardly.features.boardlist.application.validation.CreateBoardListValidator;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListColor;
import com.boardly.features.boardlist.domain.policy.ListLimitPolicy;
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
class CreateBoardListServiceTest {

    private CreateBoardListService createBoardListService;

    @Mock
    private CreateBoardListValidator createBoardListValidator;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardListRepository boardListRepository;

    @Mock
    private ListLimitPolicy listLimitPolicy;

    @BeforeEach
    void setUp() {
        createBoardListService = new CreateBoardListService(
                createBoardListValidator,
                boardRepository,
                boardListRepository,
                listLimitPolicy
        );
    }

    private CreateBoardListCommand createValidCommand() {
        return new CreateBoardListCommand(
                new BoardId(),
                new UserId(),
                "테스트 리스트",
                "테스트 리스트 설명",
                ListColor.of("#0079BF")
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

    private BoardList createValidBoardList(CreateBoardListCommand command, int position) {
        return BoardList.create(
                command.title(),
                command.description(),
                position,
                command.color(),
                command.boardId()
        );
    }

    @Test
    @DisplayName("유효한 정보로 리스트 생성이 성공해야 한다")
    void createBoardList_withValidData_shouldReturnBoardList() {
        // given
        CreateBoardListCommand command = createValidCommand();
        Board board = createValidBoard(command.boardId(), command.userId());
        BoardList savedList = createValidBoardList(command, 0);

        when(createBoardListValidator.validateCreateBoardList(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(command.boardId()))
                .thenReturn(Optional.of(board));
        when(listLimitPolicy.canCreateList(0))
                .thenReturn(true);
        when(boardListRepository.countByBoardId(command.boardId()))
                .thenReturn(0L);
        when(boardListRepository.findMaxPositionByBoardId(command.boardId()))
                .thenReturn(Optional.empty());
        when(boardListRepository.save(any(BoardList.class)))
                .thenReturn(savedList);

        // when
        Either<Failure, BoardList> result = createBoardListService.createBoardList(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().getTitle()).isEqualTo(command.title());
        assertThat(result.get().getDescription()).isEqualTo(command.description());
        assertThat(result.get().getColor()).isEqualTo(command.color());
        assertThat(result.get().getBoardId()).isEqualTo(command.boardId());
        assertThat(result.get().getPosition()).isEqualTo(0);

        verify(createBoardListValidator).validateCreateBoardList(command);
        verify(boardRepository).findById(command.boardId());
        verify(listLimitPolicy).canCreateList(0);
        verify(boardListRepository).countByBoardId(command.boardId());
        verify(boardListRepository).findMaxPositionByBoardId(command.boardId());
        verify(boardListRepository).save(any(BoardList.class));
    }

    @Test
    @DisplayName("설명이 null인 경우에도 리스트 생성이 성공해야 한다")
    void createBoardList_withNullDescription_shouldReturnBoardList() {
        // given
        CreateBoardListCommand command = new CreateBoardListCommand(
                new BoardId(),
                new UserId(),
                "테스트 리스트",
                null,
                ListColor.of("#0079BF")
        );
        Board board = createValidBoard(command.boardId(), command.userId());
        BoardList savedList = createValidBoardList(command, 0);

        when(createBoardListValidator.validateCreateBoardList(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(command.boardId()))
                .thenReturn(Optional.of(board));
        when(listLimitPolicy.canCreateList(0))
                .thenReturn(true);
        when(boardListRepository.countByBoardId(command.boardId()))
                .thenReturn(0L);
        when(boardListRepository.findMaxPositionByBoardId(command.boardId()))
                .thenReturn(Optional.empty());
        when(boardListRepository.save(any(BoardList.class)))
                .thenReturn(savedList);

        // when
        Either<Failure, BoardList> result = createBoardListService.createBoardList(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().getTitle()).isEqualTo(command.title());
        assertThat(result.get().getDescription()).isEqualTo(null);
        assertThat(result.get().getColor()).isEqualTo(command.color());

        verify(createBoardListValidator).validateCreateBoardList(command);
        verify(boardRepository).findById(command.boardId());
        verify(listLimitPolicy).canCreateList(0);
        verify(boardListRepository).countByBoardId(command.boardId());
        verify(boardListRepository).findMaxPositionByBoardId(command.boardId());
        verify(boardListRepository).save(any(BoardList.class));
    }

    @Test
    @DisplayName("입력 검증 실패 시 검증 오류를 반환해야 한다")
    void createBoardList_withInvalidData_shouldReturnValidationFailure() {
        // given
        CreateBoardListCommand command = createValidCommand();
        Failure.FieldViolation violation = Failure.FieldViolation.builder()
                .field("title")
                .message("validation.boardlist.title.required")
                .rejectedValue(command.title())
                .build();
        ValidationResult<CreateBoardListCommand> invalidResult = ValidationResult.invalid(violation);

        when(createBoardListValidator.validateCreateBoardList(command))
                .thenReturn(invalidResult);

        // when
        Either<Failure, BoardList> result = createBoardListService.createBoardList(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ValidationFailure.class);
        Failure.ValidationFailure validationFailure = (Failure.ValidationFailure) result.getLeft();
        assertThat(validationFailure.message()).contains("INVALID_INPUT");

        verify(createBoardListValidator).validateCreateBoardList(command);
        verify(boardRepository, never()).findById(any(BoardId.class));
        verify(boardListRepository, never()).save(any(BoardList.class));
    }

    @Test
    @DisplayName("보드를 찾을 수 없는 경우 NOT_FOUND 오류를 반환해야 한다")
    void createBoardList_withNonExistentBoard_shouldReturnNotFoundFailure() {
        // given
        CreateBoardListCommand command = createValidCommand();

        when(createBoardListValidator.validateCreateBoardList(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(command.boardId()))
                .thenReturn(Optional.empty());

        // when
        Either<Failure, BoardList> result = createBoardListService.createBoardList(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.NotFoundFailure.class);
        assertThat(result.getLeft().message()).isEqualTo("BOARD_NOT_FOUND");

        verify(createBoardListValidator).validateCreateBoardList(command);
        verify(boardRepository).findById(command.boardId());
        verify(boardListRepository, never()).save(any(BoardList.class));
    }

    @Test
    @DisplayName("보드 소유자가 아닌 경우 권한 오류를 반환해야 한다")
    void createBoardList_withUnauthorizedUser_shouldReturnForbiddenFailure() {
        // given
        CreateBoardListCommand command = createValidCommand();
        UserId differentUserId = new UserId();
        Board board = createValidBoard(command.boardId(), differentUserId);

        when(createBoardListValidator.validateCreateBoardList(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(command.boardId()))
                .thenReturn(Optional.of(board));

        // when
        Either<Failure, BoardList> result = createBoardListService.createBoardList(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ForbiddenFailure.class);
        assertThat(result.getLeft().message()).isEqualTo("UNAUTHORIZED_ACCESS");

        verify(createBoardListValidator).validateCreateBoardList(command);
        verify(boardRepository).findById(command.boardId());
        verify(boardListRepository, never()).save(any(BoardList.class));
    }

    @Test
    @DisplayName("리스트 생성 한도 초과 시 FORBIDDEN 오류를 반환해야 한다")
    void createBoardList_withLimitExceeded_shouldReturnForbiddenFailure() {
        // given
        CreateBoardListCommand command = createValidCommand();
        Board board = createValidBoard(command.boardId(), command.userId());

        when(createBoardListValidator.validateCreateBoardList(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(command.boardId()))
                .thenReturn(Optional.of(board));
        when(boardListRepository.countByBoardId(command.boardId()))
                .thenReturn(10L);
        when(listLimitPolicy.canCreateList(10))
                .thenReturn(false);

        // when
        Either<Failure, BoardList> result = createBoardListService.createBoardList(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ForbiddenFailure.class);
        assertThat(result.getLeft().message()).isEqualTo("LIST_LIMIT_EXCEEDED");

        verify(createBoardListValidator).validateCreateBoardList(command);
        verify(boardRepository).findById(command.boardId());
        verify(boardListRepository).countByBoardId(command.boardId());
        verify(listLimitPolicy).canCreateList(10);
        verify(boardListRepository, never()).save(any(BoardList.class));
    }

    @Test
    @DisplayName("기존 리스트가 있는 경우 다음 위치에 생성해야 한다")
    void createBoardList_withExistingLists_shouldCreateAtNextPosition() {
        // given
        CreateBoardListCommand command = createValidCommand();
        Board board = createValidBoard(command.boardId(), command.userId());
        BoardList savedList = createValidBoardList(command, 3);

        when(createBoardListValidator.validateCreateBoardList(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(command.boardId()))
                .thenReturn(Optional.of(board));
        when(listLimitPolicy.canCreateList(3))
                .thenReturn(true);
        when(boardListRepository.countByBoardId(command.boardId()))
                .thenReturn(3L);
        when(boardListRepository.findMaxPositionByBoardId(command.boardId()))
                .thenReturn(Optional.of(2));
        when(boardListRepository.save(any(BoardList.class)))
                .thenReturn(savedList);

        // when
        Either<Failure, BoardList> result = createBoardListService.createBoardList(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().getPosition()).isEqualTo(3);

        verify(createBoardListValidator).validateCreateBoardList(command);
        verify(boardRepository).findById(command.boardId());
        verify(listLimitPolicy).canCreateList(3);
        verify(boardListRepository).countByBoardId(command.boardId());
        verify(boardListRepository).findMaxPositionByBoardId(command.boardId());
        verify(boardListRepository).save(any(BoardList.class));
    }

    @Test
    @DisplayName("리스트 저장 중 예외 발생 시 내부 서버 오류를 반환해야 한다")
    void createBoardList_withSaveException_shouldReturnInternalServerError() {
        // given
        CreateBoardListCommand command = createValidCommand();
        Board board = createValidBoard(command.boardId(), command.userId());

        when(createBoardListValidator.validateCreateBoardList(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(command.boardId()))
                .thenReturn(Optional.of(board));
        when(listLimitPolicy.canCreateList(0))
                .thenReturn(true);
        when(boardListRepository.countByBoardId(command.boardId()))
                .thenReturn(0L);
        when(boardListRepository.findMaxPositionByBoardId(command.boardId()))
                .thenReturn(Optional.empty());
        when(boardListRepository.save(any(BoardList.class)))
                .thenThrow(new RuntimeException("데이터베이스 오류"));

        // when
        Either<Failure, BoardList> result = createBoardListService.createBoardList(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalServerError.class);
        assertThat(result.getLeft().message()).isEqualTo("데이터베이스 오류");

        verify(createBoardListValidator).validateCreateBoardList(command);
        verify(boardRepository).findById(command.boardId());
        verify(listLimitPolicy).canCreateList(0);
        verify(boardListRepository).countByBoardId(command.boardId());
        verify(boardListRepository).findMaxPositionByBoardId(command.boardId());
        verify(boardListRepository).save(any(BoardList.class));
    }

    @Test
    @DisplayName("여러 검증 오류가 있는 경우 모든 오류를 반환해야 한다")
    void createBoardList_withMultipleValidationErrors_shouldReturnAllErrors() {
        // given
        CreateBoardListCommand command = createValidCommand();
        Failure.FieldViolation titleViolation = Failure.FieldViolation.builder()
                .field("title")
                .message("validation.boardlist.title.required")
                .rejectedValue(command.title())
                .build();
        Failure.FieldViolation colorViolation = Failure.FieldViolation.builder()
                .field("color")
                .message("validation.boardlist.color.required")
                .rejectedValue(command.color())
                .build();
        ValidationResult<CreateBoardListCommand> invalidResult = ValidationResult.invalid(
                io.vavr.collection.List.of(titleViolation, colorViolation));

        when(createBoardListValidator.validateCreateBoardList(command))
                .thenReturn(invalidResult);

        // when
        Either<Failure, BoardList> result = createBoardListService.createBoardList(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ValidationFailure.class);
        Failure.ValidationFailure validationFailure = (Failure.ValidationFailure) result.getLeft();
        assertThat(validationFailure.violations()).hasSize(2);

        verify(createBoardListValidator).validateCreateBoardList(command);
        verify(boardRepository, never()).findById(any(BoardId.class));
        verify(boardListRepository, never()).save(any(BoardList.class));
    }

    @Test
    @DisplayName("생성된 리스트는 올바른 초기 상태를 가져야 한다")
    void createBoardList_shouldCreateListWithCorrectInitialState() {
        // given
        CreateBoardListCommand command = createValidCommand();
        Board board = createValidBoard(command.boardId(), command.userId());
        BoardList savedList = createValidBoardList(command, 0);

        when(createBoardListValidator.validateCreateBoardList(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(command.boardId()))
                .thenReturn(Optional.of(board));
        when(listLimitPolicy.canCreateList(0))
                .thenReturn(true);
        when(boardListRepository.countByBoardId(command.boardId()))
                .thenReturn(0L);
        when(boardListRepository.findMaxPositionByBoardId(command.boardId()))
                .thenReturn(Optional.empty());
        when(boardListRepository.save(any(BoardList.class)))
                .thenReturn(savedList);

        // when
        Either<Failure, BoardList> result = createBoardListService.createBoardList(command);

        // then
        assertThat(result.isRight()).isTrue();
        BoardList createdList = result.get();
        assertThat(createdList.getListId()).isNotNull();
        assertThat(createdList.getTitle()).isEqualTo(command.title());
        assertThat(createdList.getDescription()).isEqualTo(command.description());
        assertThat(createdList.getColor()).isEqualTo(command.color());
        assertThat(createdList.getBoardId()).isEqualTo(command.boardId());
        assertThat(createdList.getPosition()).isEqualTo(0);

        verify(createBoardListValidator).validateCreateBoardList(command);
        verify(boardRepository).findById(command.boardId());
        verify(listLimitPolicy).canCreateList(0);
        verify(boardListRepository).countByBoardId(command.boardId());
        verify(boardListRepository).findMaxPositionByBoardId(command.boardId());
        verify(boardListRepository).save(any(BoardList.class));
    }
} 
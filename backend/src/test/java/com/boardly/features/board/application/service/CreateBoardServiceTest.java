package com.boardly.features.board.application.service;

import com.boardly.features.board.application.port.input.CreateBoardCommand;
import com.boardly.features.board.application.validation.CreateBoardValidator;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateBoardServiceTest {

    private CreateBoardService createBoardService;

    @Mock
    private CreateBoardValidator createBoardValidator;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private ValidationMessageResolver validationMessageResolver;

    private final UserId ownerId = new UserId();

    @BeforeEach
    void setUp() {
        createBoardService = new CreateBoardService(createBoardValidator, boardRepository, validationMessageResolver);
    }

    private CreateBoardCommand createValidCommand() {
        return new CreateBoardCommand("테스트 보드", "테스트 보드 설명", ownerId);
    }

    private CreateBoardCommand createValidCommandWithNullDescription() {
        return new CreateBoardCommand("테스트 보드", null, ownerId);
    }

    private Board createValidBoard(String title, String description, UserId ownerId) {
        return Board.builder()
                .boardId(new BoardId())
                .title(title)
                .description(description)
                .isArchived(false)
                .ownerId(ownerId)
                .isStarred(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("유효한 정보로 보드 생성이 성공해야 한다")
    void createBoard_withValidData_shouldReturnBoard() {
        // given
        CreateBoardCommand command = createValidCommand();
        Board savedBoard = createValidBoard(command.title(), command.description(), command.ownerId());

        when(createBoardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.save(any(Board.class)))
                .thenReturn(Either.right(savedBoard));

        // when
        Either<Failure, Board> result = createBoardService.createBoard(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().getTitle()).isEqualTo(command.title());
        assertThat(result.get().getDescription()).isEqualTo(command.description());
        assertThat(result.get().getOwnerId()).isEqualTo(command.ownerId());
        assertThat(result.get().isArchived()).isFalse();
        assertThat(result.get().isStarred()).isFalse();

        verify(createBoardValidator).validate(command);
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    @DisplayName("설명이 null인 경우에도 보드 생성이 성공해야 한다")
    void createBoard_withNullDescription_shouldReturnBoard() {
        // given
        CreateBoardCommand command = createValidCommandWithNullDescription();
        Board savedBoard = createValidBoard(command.title(), command.description(), command.ownerId());

        when(createBoardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.save(any(Board.class)))
                .thenReturn(Either.right(savedBoard));

        // when
        Either<Failure, Board> result = createBoardService.createBoard(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().getTitle()).isEqualTo(command.title());
        assertThat(result.get().getDescription()).isEqualTo(command.description());
        assertThat(result.get().getOwnerId()).isEqualTo(command.ownerId());

        verify(createBoardValidator).validate(command);
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    @DisplayName("입력 검증 실패 시 검증 오류를 반환해야 한다")
    void createBoard_withInvalidData_shouldReturnValidationFailure() {
        // given
        CreateBoardCommand command = createValidCommand();
        Failure.FieldViolation violation = Failure.FieldViolation.builder()
                .field("title")
                .message("validation.board.title.required")
                .rejectedValue(command.title())
                .build();
        ValidationResult<CreateBoardCommand> invalidResult = ValidationResult.invalid(violation);

        when(validationMessageResolver.getMessage("validation.input.invalid"))
                .thenReturn("입력 데이터가 올바르지 않습니다");
        when(createBoardValidator.validate(command))
                .thenReturn(invalidResult);

        // when
        Either<Failure, Board> result = createBoardService.createBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
        Failure.InputError inputError = (Failure.InputError) result.getLeft();
        assertThat(inputError.getMessage()).isEqualTo("입력 데이터가 올바르지 않습니다");
        assertThat(inputError.getViolations()).hasSize(1);

        verify(createBoardValidator).validate(command);
        verify(validationMessageResolver).getMessage("validation.input.invalid");
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("여러 검증 오류가 있는 경우 모든 오류를 반환해야 한다")
    void createBoard_withMultipleValidationErrors_shouldReturnAllErrors() {
        // given
        CreateBoardCommand command = createValidCommand();
        Failure.FieldViolation titleViolation = Failure.FieldViolation.builder()
                .field("title")
                .message("validation.board.title.required")
                .rejectedValue(command.title())
                .build();
        Failure.FieldViolation ownerViolation = Failure.FieldViolation.builder()
                .field("ownerId")
                .message("validation.board.owner.required")
                .rejectedValue(command.ownerId())
                .build();
        ValidationResult<CreateBoardCommand> invalidResult = ValidationResult.invalid(
                io.vavr.collection.List.of(titleViolation, ownerViolation));

        when(validationMessageResolver.getMessage("validation.input.invalid"))
                .thenReturn("입력 데이터가 올바르지 않습니다");
        when(createBoardValidator.validate(command))
                .thenReturn(invalidResult);

        // when
        Either<Failure, Board> result = createBoardService.createBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
        Failure.InputError inputError = (Failure.InputError) result.getLeft();
        assertThat(inputError.getMessage()).isEqualTo("입력 데이터가 올바르지 않습니다");
        assertThat(inputError.getViolations()).hasSize(2);

        verify(createBoardValidator).validate(command);
        verify(validationMessageResolver).getMessage("validation.input.invalid");
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("보드 저장 실패 시 저장소 오류를 반환해야 한다")
    void createBoard_withSaveFailure_shouldReturnRepositoryFailure() {
        // given
        CreateBoardCommand command = createValidCommand();
        Failure saveFailure = Failure.ofInternalError("데이터베이스 연결 오류", "DB_CONNECTION_ERROR", null);

        when(createBoardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.save(any(Board.class)))
                .thenReturn(Either.left(saveFailure));

        // when
        Either<Failure, Board> result = createBoardService.createBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
        assertThat(result.getLeft().getMessage()).isEqualTo("데이터베이스 연결 오류");

        verify(createBoardValidator).validate(command);
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    @DisplayName("보드 저장 시 제약 조건 위반 오류를 반환해야 한다")
    void createBoard_withConstraintViolation_shouldReturnConflictFailure() {
        // given
        CreateBoardCommand command = createValidCommand();
        Failure constraintFailure = Failure.ofResourceConflict("BOARD_CONSTRAINT_VIOLATION", "CONSTRAINT_VIOLATION",
                null);

        when(createBoardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.save(any(Board.class)))
                .thenReturn(Either.left(constraintFailure));

        // when
        Either<Failure, Board> result = createBoardService.createBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ResourceConflict.class);
        assertThat(result.getLeft().getMessage()).isEqualTo("BOARD_CONSTRAINT_VIOLATION");

        verify(createBoardValidator).validate(command);
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    @DisplayName("런타임 예외 발생 시 내부 서버 오류를 반환해야 한다")
    void createBoard_withRuntimeException_shouldReturnInternalServerError() {
        // given
        CreateBoardCommand command = createValidCommand();

        when(createBoardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.save(any(Board.class)))
                .thenThrow(new RuntimeException("예상치 못한 오류"));

        // when
        Either<Failure, Board> result = createBoardService.createBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
        assertThat(result.getLeft().getMessage()).isEqualTo("예상치 못한 오류");

        verify(createBoardValidator).validate(command);
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    @DisplayName("생성된 보드는 올바른 초기 상태를 가져야 한다")
    void createBoard_shouldCreateBoardWithCorrectInitialState() {
        // given
        CreateBoardCommand command = createValidCommand();
        Board savedBoard = createValidBoard(command.title(), command.description(), command.ownerId());

        when(createBoardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.save(any(Board.class)))
                .thenReturn(Either.right(savedBoard));

        // when
        Either<Failure, Board> result = createBoardService.createBoard(command);

        // then
        assertThat(result.isRight()).isTrue();

        // 저장되는 Board 객체의 초기 상태가 올바른지 확인
        verify(boardRepository).save(argThat(board -> board.getTitle().equals(command.title()) &&
                board.getDescription().equals(command.description()) &&
                board.getOwnerId().equals(command.ownerId()) &&
                !board.isArchived() &&
                !board.isStarred() &&
                board.getBoardId() != null &&
                board.getCreatedAt() != null &&
                board.getUpdatedAt() != null));
    }

    @Test
    @DisplayName("보드 ID는 자동으로 생성되어야 한다")
    void createBoard_shouldGenerateBoardId() {
        // given
        CreateBoardCommand command = createValidCommand();
        Board savedBoard = createValidBoard(command.title(), command.description(), command.ownerId());

        when(createBoardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.save(any(Board.class)))
                .thenReturn(Either.right(savedBoard));

        // when
        Either<Failure, Board> result = createBoardService.createBoard(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().getBoardId()).isNotNull();

        // 저장되는 Board 객체에 BoardId가 설정되어 있는지 확인
        verify(boardRepository).save(argThat(board -> board.getBoardId() != null &&
                board.getBoardId().getId() != null &&
                !board.getBoardId().getId().isEmpty()));
    }

    @Test
    @DisplayName("보드 생성 시 생성 시간이 설정되어야 한다")
    void createBoard_shouldSetCreationTime() {
        // given
        CreateBoardCommand command = createValidCommand();
        Board savedBoard = createValidBoard(command.title(), command.description(), command.ownerId());

        when(createBoardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.save(any(Board.class)))
                .thenReturn(Either.right(savedBoard));

        // when
        Either<Failure, Board> result = createBoardService.createBoard(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().getCreatedAt()).isNotNull();
        assertThat(result.get().getUpdatedAt()).isNotNull();

        // 저장되는 Board 객체에 생성 시간이 설정되어 있는지 확인
        verify(boardRepository).save(argThat(board -> board.getCreatedAt() != null &&
                board.getUpdatedAt() != null));
    }

    @Test
    @DisplayName("보드 생성 시 즐겨찾기 상태는 false로 초기화되어야 한다")
    void createBoard_shouldInitializeStarredAsFalse() {
        // given
        CreateBoardCommand command = createValidCommand();
        Board savedBoard = createValidBoard(command.title(), command.description(), command.ownerId());

        when(createBoardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.save(any(Board.class)))
                .thenReturn(Either.right(savedBoard));

        // when
        Either<Failure, Board> result = createBoardService.createBoard(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().isStarred()).isFalse();

        // 저장되는 Board 객체에 즐겨찾기 상태가 false로 설정되어 있는지 확인
        verify(boardRepository).save(argThat(board -> !board.isStarred()));
    }

    @Test
    @DisplayName("보드 생성 시 아카이브 상태는 false로 초기화되어야 한다")
    void createBoard_shouldInitializeArchivedAsFalse() {
        // given
        CreateBoardCommand command = createValidCommand();
        Board savedBoard = createValidBoard(command.title(), command.description(), command.ownerId());

        when(createBoardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.save(any(Board.class)))
                .thenReturn(Either.right(savedBoard));

        // when
        Either<Failure, Board> result = createBoardService.createBoard(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().isArchived()).isFalse();

        // 저장되는 Board 객체에 아카이브 상태가 false로 설정되어 있는지 확인
        verify(boardRepository).save(argThat(board -> !board.isArchived()));
    }

    @Test
    @DisplayName("Board.create 메서드가 올바른 파라미터로 호출되어야 한다")
    void createBoard_shouldCallBoardCreateWithCorrectParameters() {
        // given
        CreateBoardCommand command = createValidCommand();
        Board savedBoard = createValidBoard(command.title(), command.description(), command.ownerId());

        when(createBoardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.save(any(Board.class)))
                .thenReturn(Either.right(savedBoard));

        // when
        Either<Failure, Board> result = createBoardService.createBoard(command);

        // then
        assertThat(result.isRight()).isTrue();

        // Board.create 메서드가 올바른 파라미터로 호출되었는지 확인
        verify(boardRepository).save(argThat(board -> board.getTitle().equals(command.title()) &&
                board.getDescription().equals(command.description()) &&
                board.getOwnerId().equals(command.ownerId())));
    }

    @Test
    @DisplayName("Try.of를 사용한 예외 처리가 올바르게 동작해야 한다")
    void createBoard_shouldHandleExceptionsWithTry() {
        // given
        CreateBoardCommand command = createValidCommand();
        String errorMessage = "데이터베이스 연결 실패";

        when(createBoardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.save(any(Board.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // when
        Either<Failure, Board> result = createBoardService.createBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
        assertThat(result.getLeft().getMessage()).isEqualTo(errorMessage);
        assertThat(((Failure.InternalError) result.getLeft()).getErrorCode()).isEqualTo("BOARD_CREATION_ERROR");

        verify(createBoardValidator).validate(command);
        verify(boardRepository).save(any(Board.class));
    }
}
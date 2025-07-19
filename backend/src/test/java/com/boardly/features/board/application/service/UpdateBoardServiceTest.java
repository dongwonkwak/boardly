package com.boardly.features.board.application.service;

import com.boardly.features.board.application.port.input.UpdateBoardCommand;
import com.boardly.features.board.application.validation.UpdateBoardValidator;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateBoardServiceTest {

    private UpdateBoardService updateBoardService;

    @Mock
    private UpdateBoardValidator boardValidator;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private ValidationMessageResolver messageResolver;

    private final BoardId boardId = new BoardId();
    private final UserId ownerId = new UserId();
    private final UserId otherUserId = new UserId();

    @BeforeEach
    void setUp() {
        updateBoardService = new UpdateBoardService(boardValidator, boardRepository, messageResolver);
        
        // 메시지 모킹 설정 (lenient로 설정하여 사용되지 않는 스텁도 허용)
        lenient().when(messageResolver.getMessage("validation.board.not.found"))
                .thenReturn("보드를 찾을 수 없습니다.");
        lenient().when(messageResolver.getMessage("validation.board.modification.access.denied"))
                .thenReturn("보드 수정 권한이 없습니다.");
        lenient().when(messageResolver.getMessage("validation.board.archived.modification.denied"))
                .thenReturn("아카이브된 보드는 수정할 수 없습니다.");
        lenient().when(messageResolver.getMessage("validation.board.modification.error"))
                .thenReturn("보드 수정 중 오류가 발생했습니다.");
        lenient().when(messageResolver.getMessage("validation.board.update.error"))
                .thenReturn("보드 업데이트 중 오류가 발생했습니다.");
    }

    private UpdateBoardCommand createValidCommand() {
        return new UpdateBoardCommand(
                boardId,
                "업데이트된 제목",
                "업데이트된 설명",
                ownerId
        );
    }

    private Board createValidBoard() {
        return Board.builder()
                .boardId(boardId)
                .title("Original Title")
                .description("Original Description")
                .isArchived(false)
                .ownerId(ownerId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private Board createArchivedBoard() {
        return Board.builder()
                .boardId(boardId)
                .title("Archived Board")
                .description("Archived Description")
                .isArchived(true)
                .ownerId(ownerId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("유효한 정보로 보드 업데이트가 성공해야 한다")
    void updateBoard_withValidData_shouldReturnUpdatedBoard() {
        // given
        UpdateBoardCommand command = createValidCommand();
        Board existingBoard = createValidBoard();
        Board updatedBoard = createValidBoard();
        updatedBoard.updateTitle(command.title());
        updatedBoard.updateDescription(command.description());
        
        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(existingBoard));
        when(boardRepository.save(any(Board.class)))
                .thenReturn(Either.right(updatedBoard));

        // when
        Either<Failure, Board> result = updateBoardService.updateBoard(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().getTitle()).isEqualTo(command.title());
        assertThat(result.get().getDescription()).isEqualTo(command.description());
        assertThat(result.get().getOwnerId()).isEqualTo(ownerId);

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        // 수정된 로직에서는 변경사항이 있으면 save()가 호출됨
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    @DisplayName("제목만 업데이트하는 경우 성공해야 한다")
    void updateBoard_withTitleOnly_shouldReturnUpdatedBoard() {
        // given
        UpdateBoardCommand command = new UpdateBoardCommand(
                boardId,
                "업데이트된 제목",
                null,
                ownerId
        );
        Board existingBoard = createValidBoard();
        Board updatedBoard = createValidBoard();
        updatedBoard.updateTitle(command.title());

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(existingBoard));
        when(boardRepository.save(any(Board.class)))
                .thenReturn(Either.right(updatedBoard));

        // when
        Either<Failure, Board> result = updateBoardService.updateBoard(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().getTitle()).isEqualTo(command.title());
        assertThat(result.get().getDescription()).isEqualTo("Original Description");

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    @DisplayName("설명만 업데이트하는 경우 성공해야 한다")
    void updateBoard_withDescriptionOnly_shouldReturnUpdatedBoard() {
        // given
        UpdateBoardCommand command = new UpdateBoardCommand(
                boardId,
                null,
                "업데이트된 설명",
                ownerId
        );
        Board existingBoard = createValidBoard();
        Board updatedBoard = createValidBoard();
        updatedBoard.updateDescription(command.description());

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(existingBoard));
        when(boardRepository.save(any(Board.class)))
                .thenReturn(Either.right(updatedBoard));

        // when
        Either<Failure, Board> result = updateBoardService.updateBoard(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().getTitle()).isEqualTo("Original Title");
        assertThat(result.get().getDescription()).isEqualTo(command.description());

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    @DisplayName("입력 검증 실패 시 검증 오류를 반환해야 한다")
    void updateBoard_withInvalidData_shouldReturnValidationFailure() {
        // given
        UpdateBoardCommand command = createValidCommand();
        Failure.FieldViolation violation = Failure.FieldViolation.builder()
                .field("title")
                .message("validation.board.title.max.length")
                .rejectedValue(command.title())
                .build();
        ValidationResult<UpdateBoardCommand> invalidResult = ValidationResult.invalid(violation);

        when(boardValidator.validate(command))
                .thenReturn(invalidResult);

        // when
        Either<Failure, Board> result = updateBoardService.updateBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ValidationFailure.class);
        Failure.ValidationFailure validationFailure = (Failure.ValidationFailure) result.getLeft();
        assertThat(validationFailure.message()).contains("INVALID_INPUT");

        verify(boardValidator).validate(command);
        verify(boardRepository, never()).findById(any());
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("보드를 찾을 수 없는 경우 NotFound 오류를 반환해야 한다")
    void updateBoard_withNonExistentBoard_shouldReturnNotFoundFailure() {
        // given
        UpdateBoardCommand command = createValidCommand();

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.empty());

        // when
        Either<Failure, Board> result = updateBoardService.updateBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.NotFoundFailure.class);
        assertThat(result.getLeft().message()).isEqualTo("보드를 찾을 수 없습니다.");

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(messageResolver).getMessage("validation.board.not.found");
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("권한이 없는 사용자가 보드를 수정하려는 경우 UnAuthorized 오류를 반환해야 한다")
    void updateBoard_withUnauthorizedUser_shouldReturnUnAuthorizedFailure() {
        // given
        UpdateBoardCommand command = new UpdateBoardCommand(
                boardId,
                "업데이트된 제목",
                "업데이트된 설명",
                otherUserId  // 다른 사용자가 요청
        );
        Board existingBoard = createValidBoard();  // ownerId가 소유한 보드

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(existingBoard));

        // when
        Either<Failure, Board> result = updateBoardService.updateBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ForbiddenFailure.class);
        assertThat(result.getLeft().message()).isEqualTo("보드 수정 권한이 없습니다.");

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(messageResolver).getMessage("validation.board.modification.access.denied");
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("아카이브된 보드를 수정하려는 경우 Conflict 오류를 반환해야 한다")
    void updateBoard_withArchivedBoard_shouldReturnConflictFailure() {
        // given
        UpdateBoardCommand command = createValidCommand();
        Board archivedBoard = createArchivedBoard();

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(archivedBoard));

        // when
        Either<Failure, Board> result = updateBoardService.updateBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ConflictFailure.class);
        assertThat(result.getLeft().message()).isEqualTo("아카이브된 보드는 수정할 수 없습니다.");

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(messageResolver).getMessage("validation.board.archived.modification.denied");
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("변경 사항이 없는 경우 저장하지 않고 기존 보드를 반환해야 한다")
    void updateBoard_withNoChanges_shouldReturnExistingBoard() {
        // given
        UpdateBoardCommand command = new UpdateBoardCommand(
                boardId,
                "Original Title",  // 기존 보드와 같은 제목
                "Original Description",  // 기존 보드와 같은 설명
                ownerId
        );
        Board existingBoard = createValidBoard();

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(existingBoard));

        // when
        Either<Failure, Board> result = updateBoardService.updateBoard(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isEqualTo(existingBoard);

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("null 값으로 업데이트하는 경우 변경 사항이 없으면 저장하지 않아야 한다")
    void updateBoard_withNullValues_shouldNotSaveIfNoChanges() {
        // given
        UpdateBoardCommand command = new UpdateBoardCommand(
                boardId,
                null,
                null,
                ownerId
        );
        Board existingBoard = createValidBoard();

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(existingBoard));

        // when
        Either<Failure, Board> result = updateBoardService.updateBoard(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isEqualTo(existingBoard);

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("보드 저장 실패 시 저장소 오류를 반환해야 한다")
    void updateBoard_withSaveFailure_shouldReturnRepositoryFailure() {
        // given
        UpdateBoardCommand command = createValidCommand();
        Board existingBoard = createValidBoard();
        Failure saveFailure = Failure.ofInternalServerError("데이터베이스 연결 오류");

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(existingBoard));
        when(boardRepository.save(any(Board.class)))
                .thenReturn(Either.left(saveFailure));

        // when
        Either<Failure, Board> result = updateBoardService.updateBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalServerError.class);
        assertThat(result.getLeft().message()).isEqualTo("데이터베이스 연결 오류");

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    @DisplayName("여러 검증 오류가 있는 경우 모든 오류를 반환해야 한다")
    void updateBoard_withMultipleValidationErrors_shouldReturnAllErrors() {
        // given
        UpdateBoardCommand command = createValidCommand();
        Failure.FieldViolation titleViolation = Failure.FieldViolation.builder()
                .field("title")
                .message("validation.board.title.max.length")
                .rejectedValue(command.title())
                .build();
        Failure.FieldViolation descriptionViolation = Failure.FieldViolation.builder()
                .field("description")
                .message("validation.board.description.invalid")
                .rejectedValue(command.description())
                .build();
        ValidationResult<UpdateBoardCommand> invalidResult = ValidationResult.invalid(
                io.vavr.collection.List.of(titleViolation, descriptionViolation));

        when(boardValidator.validate(command))
                .thenReturn(invalidResult);

        // when
        Either<Failure, Board> result = updateBoardService.updateBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ValidationFailure.class);
        Failure.ValidationFailure validationFailure = (Failure.ValidationFailure) result.getLeft();
        assertThat(validationFailure.violations()).hasSize(2);

        verify(boardValidator).validate(command);
        verify(boardRepository, never()).findById(any());
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("런타임 예외 발생 시 내부 서버 오류를 반환해야 한다")
    void updateBoard_withRuntimeException_shouldReturnInternalServerError() {
        // given
        UpdateBoardCommand command = createValidCommand();
        Board existingBoard = createValidBoard();

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(existingBoard));
        when(boardRepository.save(any(Board.class)))
                .thenThrow(new RuntimeException("예상치 못한 오류"));

        // when
        Either<Failure, Board> result = updateBoardService.updateBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalServerError.class);
        assertThat(result.getLeft().message()).isEqualTo("보드 업데이트 중 오류가 발생했습니다.");

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(messageResolver).getMessage("validation.board.update.error");
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    @DisplayName("보드 변경 사항 적용 중 예외 발생 시 내부 서버 오류를 반환해야 한다")
    void updateBoard_withApplyChangesException_shouldReturnInternalServerError() {
        // given
        UpdateBoardCommand command = createValidCommand();
        Board existingBoard = spy(createValidBoard());

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(existingBoard));
        
        // Board의 updateTitle 메서드에서 예외 발생하도록 설정
        doThrow(new RuntimeException("제목 업데이트 중 오류"))
                .when(existingBoard).updateTitle(anyString());

        // when
        Either<Failure, Board> result = updateBoardService.updateBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalServerError.class);
        assertThat(result.getLeft().message()).isEqualTo("보드 수정 중 오류가 발생했습니다.");

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(messageResolver).getMessage("validation.board.modification.error");
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("제목과 설명을 모두 업데이트하는 경우 두 필드 모두 변경되어야 한다")
    void updateBoard_withBothTitleAndDescription_shouldUpdateBothFields() {
        // given
        UpdateBoardCommand command = createValidCommand();
        Board existingBoard = createValidBoard();
        Board updatedBoard = createValidBoard();
        updatedBoard.updateTitle(command.title());
        updatedBoard.updateDescription(command.description());

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(existingBoard));
        when(boardRepository.save(any(Board.class)))
                .thenReturn(Either.right(updatedBoard));

        // when
        Either<Failure, Board> result = updateBoardService.updateBoard(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().getTitle()).isEqualTo(command.title());
        assertThat(result.get().getDescription()).isEqualTo(command.description());

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    @DisplayName("빈 문자열로 업데이트하는 경우 정상적으로 처리되어야 한다")
    void updateBoard_withEmptyString_shouldUpdateCorrectly() {
        // given
        UpdateBoardCommand command = new UpdateBoardCommand(
                boardId,
                "",
                "",
                ownerId
        );
        Board existingBoard = createValidBoard();
        Board updatedBoard = createValidBoard();
        updatedBoard.updateTitle("");
        updatedBoard.updateDescription("");

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(existingBoard));
        when(boardRepository.save(any(Board.class)))
                .thenReturn(Either.right(updatedBoard));

        // when
        Either<Failure, Board> result = updateBoardService.updateBoard(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().getTitle()).isEqualTo("");
        assertThat(result.get().getDescription()).isEqualTo("");

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardRepository).save(any(Board.class));
    }
} 
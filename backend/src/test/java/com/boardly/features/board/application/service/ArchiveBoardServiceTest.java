package com.boardly.features.board.application.service;

import com.boardly.features.board.application.port.input.ArchiveBoardCommand;
import com.boardly.features.board.application.validation.ArchiveBoardValidator;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArchiveBoardServiceTest {

    private ArchiveBoardService archiveBoardService;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private ArchiveBoardValidator boardValidator;

    @Mock
    private ValidationMessageResolver messageResolver;

    private final BoardId boardId = new BoardId();
    private final UserId ownerId = new UserId();
    private final UserId otherUserId = new UserId();

    @BeforeEach
    void setUp() {
        archiveBoardService = new ArchiveBoardService(boardRepository, boardValidator, messageResolver);
        
        // 메시지 모킹 설정 (lenient로 설정하여 사용되지 않는 스텁도 허용)
        lenient().when(messageResolver.getMessage("validation.board.not.found"))
                .thenReturn("보드를 찾을 수 없습니다.");
        lenient().when(messageResolver.getMessage("validation.board.archive.access.denied"))
                .thenReturn("보드 아카이브 권한이 없습니다.");
        lenient().when(messageResolver.getMessage("validation.board.archive.status.change.error"))
                .thenReturn("보드 아카이브 상태 변경 중 오류가 발생했습니다.");
        lenient().when(messageResolver.getMessage("validation.board.archive.error"))
                .thenReturn("보드 아카이브 중 오류가 발생했습니다.");
        lenient().when(messageResolver.getMessage("validation.board.unarchive.error"))
                .thenReturn("보드 언아카이브 중 오류가 발생했습니다.");
    }

    private ArchiveBoardCommand createValidCommand() {
        return new ArchiveBoardCommand(boardId, ownerId);
    }

    private Board createActiveBoard(UserId ownerId) {
        return Board.builder()
                .boardId(boardId)
                .title("테스트 보드")
                .description("테스트 설명")
                .isArchived(false)
                .ownerId(ownerId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private Board createArchivedBoard(UserId ownerId) {
        return Board.builder()
                .boardId(boardId)
                .title("테스트 보드")
                .description("테스트 설명")
                .isArchived(true)
                .ownerId(ownerId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("유효한 정보로 보드 아카이브가 성공해야 한다")
    void archiveBoard_withValidData_shouldReturnArchivedBoard() {
        // given
        ArchiveBoardCommand command = createValidCommand();
        Board activeBoard = createActiveBoard(ownerId);
        Board archivedBoard = createArchivedBoard(ownerId);
        
        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(activeBoard));
        when(boardRepository.save(any(Board.class)))
                .thenReturn(Either.right(archivedBoard));

        // when
        Either<Failure, Board> result = archiveBoardService.archiveBoard(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().isArchived()).isTrue();
        assertThat(result.get().getOwnerId()).isEqualTo(ownerId);

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    @DisplayName("유효한 정보로 보드 언아카이브가 성공해야 한다")
    void unarchiveBoard_withValidData_shouldReturnActiveBoard() {
        // given
        ArchiveBoardCommand command = createValidCommand();
        Board archivedBoard = createArchivedBoard(ownerId);
        Board activeBoard = createActiveBoard(ownerId);
        
        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(archivedBoard));
        when(boardRepository.save(any(Board.class)))
                .thenReturn(Either.right(activeBoard));

        // when
        Either<Failure, Board> result = archiveBoardService.unarchiveBoard(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().isArchived()).isFalse();
        assertThat(result.get().getOwnerId()).isEqualTo(ownerId);

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    @DisplayName("입력 검증 실패 시 검증 오류를 반환해야 한다")
    void archiveBoard_withInvalidData_shouldReturnValidationFailure() {
        // given
        ArchiveBoardCommand command = createValidCommand();
        Failure.FieldViolation violation = Failure.FieldViolation.builder()
                .field("boardId")
                .message("validation.board.id.required")
                .rejectedValue(command.boardId())
                .build();
        ValidationResult<ArchiveBoardCommand> invalidResult = ValidationResult.invalid(violation);

        when(boardValidator.validate(command))
                .thenReturn(invalidResult);

        // when
        Either<Failure, Board> result = archiveBoardService.archiveBoard(command);

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
    void archiveBoard_withNonExistentBoard_shouldReturnNotFoundFailure() {
        // given
        ArchiveBoardCommand command = createValidCommand();

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.empty());

        // when
        Either<Failure, Board> result = archiveBoardService.archiveBoard(command);

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
    @DisplayName("권한이 없는 사용자가 보드를 아카이브하려는 경우 UnAuthorized 오류를 반환해야 한다")
    void archiveBoard_withUnauthorizedUser_shouldReturnUnAuthorizedFailure() {
        // given
        ArchiveBoardCommand command = new ArchiveBoardCommand(boardId, otherUserId);
        Board activeBoard = createActiveBoard(ownerId);

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(activeBoard));

        // when
        Either<Failure, Board> result = archiveBoardService.archiveBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.Unauthorized.class);
        assertThat(result.getLeft().message()).isEqualTo("보드 아카이브 권한이 없습니다.");

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(messageResolver).getMessage("validation.board.archive.access.denied");
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 아카이브된 보드를 다시 아카이브하는 경우 변경 없이 반환해야 한다")
    void archiveBoard_withAlreadyArchivedBoard_shouldReturnExistingBoard() {
        // given
        ArchiveBoardCommand command = createValidCommand();
        Board archivedBoard = createArchivedBoard(ownerId);

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(archivedBoard));

        // when
        Either<Failure, Board> result = archiveBoardService.archiveBoard(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isEqualTo(archivedBoard);
        assertThat(result.get().isArchived()).isTrue();

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 활성화된 보드를 다시 언아카이브하는 경우 변경 없이 반환해야 한다")
    void unarchiveBoard_withAlreadyActiveBoard_shouldReturnExistingBoard() {
        // given
        ArchiveBoardCommand command = createValidCommand();
        Board activeBoard = createActiveBoard(ownerId);

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(activeBoard));

        // when
        Either<Failure, Board> result = archiveBoardService.unarchiveBoard(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isEqualTo(activeBoard);
        assertThat(result.get().isArchived()).isFalse();

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("아카이브 상태 변경 중 예외 발생 시 내부 서버 오류를 반환해야 한다")
    void archiveBoard_withArchiveException_shouldReturnInternalServerError() {
        // given
        ArchiveBoardCommand command = createValidCommand();
        Board activeBoard = spy(createActiveBoard(ownerId));

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(activeBoard));
        
        // Board의 archive 메서드에서 예외 발생하도록 설정
        doThrow(new RuntimeException("아카이브 처리 중 오류"))
                .when(activeBoard).archive();

        // when
        Either<Failure, Board> result = archiveBoardService.archiveBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalServerError.class);
        assertThat(result.getLeft().message()).isEqualTo("보드 아카이브 상태 변경 중 오류가 발생했습니다.");

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(messageResolver).getMessage("validation.board.archive.status.change.error");
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("보드 저장 실패 시 저장소 오류를 반환해야 한다 - 아카이브")
    void archiveBoard_withSaveFailure_shouldReturnRepositoryFailure() {
        // given
        ArchiveBoardCommand command = createValidCommand();
        Board activeBoard = createActiveBoard(ownerId);
        Failure saveFailure = Failure.ofInternalServerError("데이터베이스 연결 오류");

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(activeBoard));
        when(boardRepository.save(any(Board.class)))
                .thenReturn(Either.left(saveFailure));

        // when
        Either<Failure, Board> result = archiveBoardService.archiveBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalServerError.class);
        assertThat(result.getLeft().message()).isEqualTo("데이터베이스 연결 오류");

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    @DisplayName("보드 저장 실패 시 저장소 오류를 반환해야 한다 - 언아카이브")
    void unarchiveBoard_withSaveFailure_shouldReturnRepositoryFailure() {
        // given
        ArchiveBoardCommand command = createValidCommand();
        Board archivedBoard = createArchivedBoard(ownerId);
        Failure saveFailure = Failure.ofInternalServerError("데이터베이스 연결 오류");

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(archivedBoard));
        when(boardRepository.save(any(Board.class)))
                .thenReturn(Either.left(saveFailure));

        // when
        Either<Failure, Board> result = archiveBoardService.unarchiveBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalServerError.class);
        assertThat(result.getLeft().message()).isEqualTo("데이터베이스 연결 오류");

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    @DisplayName("런타임 예외 발생 시 내부 서버 오류를 반환해야 한다 - 아카이브")
    void archiveBoard_withRuntimeException_shouldReturnInternalServerError() {
        // given
        ArchiveBoardCommand command = createValidCommand();
        Board activeBoard = createActiveBoard(ownerId);

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(activeBoard));
        when(boardRepository.save(any(Board.class)))
                .thenThrow(new RuntimeException("예상치 못한 오류"));

        // when
        Either<Failure, Board> result = archiveBoardService.archiveBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalServerError.class);
        assertThat(result.getLeft().message()).isEqualTo("보드 아카이브 중 오류가 발생했습니다.");

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(messageResolver).getMessage("validation.board.archive.error");
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    @DisplayName("런타임 예외 발생 시 내부 서버 오류를 반환해야 한다 - 언아카이브")
    void unarchiveBoard_withRuntimeException_shouldReturnInternalServerError() {
        // given
        ArchiveBoardCommand command = createValidCommand();
        Board archivedBoard = createArchivedBoard(ownerId);

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(archivedBoard));
        when(boardRepository.save(any(Board.class)))
                .thenThrow(new RuntimeException("예상치 못한 오류"));

        // when
        Either<Failure, Board> result = archiveBoardService.unarchiveBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalServerError.class);
        assertThat(result.getLeft().message()).isEqualTo("보드 언아카이브 중 오류가 발생했습니다.");

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(messageResolver).getMessage("validation.board.unarchive.error");
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    @DisplayName("여러 검증 오류가 있는 경우 모든 오류를 반환해야 한다")
    void archiveBoard_withMultipleValidationErrors_shouldReturnAllErrors() {
        // given
        ArchiveBoardCommand command = createValidCommand();
        Failure.FieldViolation boardIdViolation = Failure.FieldViolation.builder()
                .field("boardId")
                .message("validation.board.id.required")
                .rejectedValue(command.boardId())
                .build();
        Failure.FieldViolation userIdViolation = Failure.FieldViolation.builder()
                .field("requestedBy")
                .message("validation.user.id.required")
                .rejectedValue(command.requestedBy())
                .build();
        ValidationResult<ArchiveBoardCommand> invalidResult = ValidationResult.invalid(
                io.vavr.collection.List.of(boardIdViolation, userIdViolation));

        when(boardValidator.validate(command))
                .thenReturn(invalidResult);

        // when
        Either<Failure, Board> result = archiveBoardService.archiveBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ValidationFailure.class);
        Failure.ValidationFailure validationFailure = (Failure.ValidationFailure) result.getLeft();
        assertThat(validationFailure.violations()).hasSize(2);

        verify(boardValidator).validate(command);
        verify(boardRepository, never()).findById(any());
        verify(boardRepository, never()).save(any());
    }
} 
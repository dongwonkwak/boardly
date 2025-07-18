package com.boardly.features.board.application.service;

import com.boardly.features.board.application.port.input.ToggleStarBoardCommand;
import com.boardly.features.board.application.validation.ToggleStarBoardValidator;
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
class ToggleStarBoardServiceTest {

    private ToggleStarBoardService toggleStarBoardService;

    @Mock
    private ToggleStarBoardValidator boardValidator;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private ValidationMessageResolver messageResolver;

    private BoardId boardId;
    private UserId ownerId;

    @BeforeEach
    void setUp() {
        // ID 객체들을 BeforeEach에서 생성하여 안정성 확보
        boardId = new BoardId();
        ownerId = new UserId();
        
        toggleStarBoardService = new ToggleStarBoardService(boardValidator, boardRepository, messageResolver);
        
        // 메시지 모킹 설정 (lenient로 설정하여 사용되지 않는 스텁도 허용)
        lenient().when(messageResolver.getMessage("validation.board.not.found"))
                .thenReturn("보드를 찾을 수 없습니다.");
        lenient().when(messageResolver.getMessage("validation.board.modification.access.denied"))
                .thenReturn("보드 수정 권한이 없습니다.");
        lenient().when(messageResolver.getMessage("validation.board.star.toggle.error"))
                .thenReturn("보드 즐겨찾기 상태 변경 중 오류가 발생했습니다.");
        lenient().when(messageResolver.getMessage("validation.board.star.save.error"))
                .thenReturn("보드 즐겨찾기 상태 저장 중 오류가 발생했습니다.");
    }

    private ToggleStarBoardCommand createValidCommand() {
        return new ToggleStarBoardCommand(boardId, ownerId);
    }

    private Board createUnstarredBoard() {
        return Board.builder()
                .boardId(boardId)
                .title("Test Board")
                .description("Test Description")
                .isArchived(false)
                .ownerId(ownerId)
                .isStarred(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private Board createStarredBoard() {
        return Board.builder()
                .boardId(boardId)
                .title("Test Board")
                .description("Test Description")
                .isArchived(false)
                .ownerId(ownerId)
                .isStarred(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("즐겨찾기가 아닌 보드를 즐겨찾기로 추가해야 한다")
    void starringBoard_withUnstarredBoard_shouldReturnStarredBoard() {
        // given
        ToggleStarBoardCommand command = createValidCommand();
        Board unstarredBoard = createUnstarredBoard();
        Board starredBoard = createStarredBoard();

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(unstarredBoard));
        when(boardRepository.save(any(Board.class)))
                .thenReturn(Either.right(starredBoard));

        // when
        Either<Failure, Board> result = toggleStarBoardService.starringBoard(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().isStarred()).isTrue();
        assertThat(unstarredBoard.isStarred()).isTrue(); // 원본 객체가 변경됨

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardRepository).save(unstarredBoard);
    }

    @Test
    @DisplayName("즐겨찾기인 보드를 즐겨찾기에서 제거해야 한다")
    void unstarringBoard_withStarredBoard_shouldReturnUnstarredBoard() {
        // given
        ToggleStarBoardCommand command = createValidCommand();
        Board starredBoard = createStarredBoard();
        Board unstarredBoard = createUnstarredBoard();

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(starredBoard));
        when(boardRepository.save(any(Board.class)))
                .thenReturn(Either.right(unstarredBoard));

        // when
        Either<Failure, Board> result = toggleStarBoardService.unstarringBoard(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().isStarred()).isFalse();
        assertThat(starredBoard.isStarred()).isFalse(); // 원본 객체가 변경됨

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardRepository).save(starredBoard);
    }

    @Test
    @DisplayName("이미 즐겨찾기인 보드를 즐겨찾기 추가하려는 경우 변경사항 없이 성공해야 한다")
    void starringBoard_withAlreadyStarredBoard_shouldReturnBoardWithoutChanges() {
        // given
        ToggleStarBoardCommand command = createValidCommand();
        Board starredBoard = createStarredBoard();

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(starredBoard));

        // when
        Either<Failure, Board> result = toggleStarBoardService.starringBoard(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().isStarred()).isTrue();

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardRepository, never()).save(any(Board.class)); // 저장이 호출되지 않아야 함
    }

    @Test
    @DisplayName("즐겨찾기가 아닌 보드를 즐겨찾기 제거하려는 경우 변경사항 없이 성공해야 한다")
    void unstarringBoard_withAlreadyUnstarredBoard_shouldReturnBoardWithoutChanges() {
        // given
        ToggleStarBoardCommand command = createValidCommand();
        Board unstarredBoard = createUnstarredBoard();

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(unstarredBoard));

        // when
        Either<Failure, Board> result = toggleStarBoardService.unstarringBoard(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().isStarred()).isFalse();

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardRepository, never()).save(any(Board.class)); // 저장이 호출되지 않아야 함
    }

    @Test
    @DisplayName("검증 실패 시 실패를 반환해야 한다")
    void starringBoard_withInvalidCommand_shouldReturnValidationFailure() {
        // given
        ToggleStarBoardCommand command = createValidCommand();
        Failure.FieldViolation violation = Failure.FieldViolation.builder()
                .field("boardId")
                .message("보드 ID는 필수입니다.")
                .rejectedValue(null)
                .build();
        ValidationResult<ToggleStarBoardCommand> invalidResult = ValidationResult.invalid(violation);

        when(boardValidator.validate(command))
                .thenReturn(invalidResult);

        // when
        Either<Failure, Board> result = toggleStarBoardService.starringBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ValidationFailure.class);

        verify(boardValidator).validate(command);
        verify(boardRepository, never()).findById(any());
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 보드인 경우 NOT_FOUND 실패를 반환해야 한다")
    void starringBoard_withNonExistentBoard_shouldReturnNotFoundFailure() {
        // given
        ToggleStarBoardCommand command = createValidCommand();

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.empty());

        // when
        Either<Failure, Board> result = toggleStarBoardService.starringBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.NotFoundFailure.class);
        assertThat(result.getLeft().message()).isEqualTo("보드를 찾을 수 없습니다.");

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("보드 저장 실패 시 INTERNAL_SERVER_ERROR 실패를 반환해야 한다")
    void starringBoard_withSaveFailure_shouldReturnInternalServerErrorFailure() {
        // given
        ToggleStarBoardCommand command = createValidCommand();
        Board unstarredBoard = createUnstarredBoard();
        Failure saveFailure = Failure.ofInternalServerError("저장 실패");

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(unstarredBoard));
        when(boardRepository.save(any(Board.class)))
                .thenReturn(Either.left(saveFailure));

        // when
        Either<Failure, Board> result = toggleStarBoardService.starringBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalServerError.class);

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardRepository).save(unstarredBoard);
    }

    @Test
    @DisplayName("보드 저장 중 예외 발생 시 INTERNAL_SERVER_ERROR 실패를 반환해야 한다")
    void starringBoard_withSaveException_shouldReturnInternalServerErrorFailure() {
        // given
        ToggleStarBoardCommand command = createValidCommand();
        Board unstarredBoard = createUnstarredBoard();

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(unstarredBoard));
        when(boardRepository.save(any(Board.class)))
                .thenThrow(new RuntimeException("Database error"));

        // when
        Either<Failure, Board> result = toggleStarBoardService.starringBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalServerError.class);
        assertThat(result.getLeft().message()).isEqualTo("보드 즐겨찾기 상태 저장 중 오류가 발생했습니다.");

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardRepository).save(unstarredBoard);
    }

    @Test
    @DisplayName("unstarringBoard - 검증 실패 시 실패를 반환해야 한다")
    void unstarringBoard_withInvalidCommand_shouldReturnValidationFailure() {
        // given
        ToggleStarBoardCommand command = createValidCommand();
        Failure.FieldViolation violation = Failure.FieldViolation.builder()
                .field("requestedBy")
                .message("요청자 ID는 필수입니다.")
                .rejectedValue(null)
                .build();
        ValidationResult<ToggleStarBoardCommand> invalidResult = ValidationResult.invalid(violation);

        when(boardValidator.validate(command))
                .thenReturn(invalidResult);

        // when
        Either<Failure, Board> result = toggleStarBoardService.unstarringBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ValidationFailure.class);

        verify(boardValidator).validate(command);
        verify(boardRepository, never()).findById(any());
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("unstarringBoard - 존재하지 않는 보드인 경우 NOT_FOUND 실패를 반환해야 한다")
    void unstarringBoard_withNonExistentBoard_shouldReturnNotFoundFailure() {
        // given
        ToggleStarBoardCommand command = createValidCommand();

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.empty());

        // when
        Either<Failure, Board> result = toggleStarBoardService.unstarringBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.NotFoundFailure.class);
        assertThat(result.getLeft().message()).isEqualTo("보드를 찾을 수 없습니다.");

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("starringBoard - 다른 사용자의 보드인 경우 FORBIDDEN 실패를 반환해야 한다")
    void starringBoard_withOtherUsersBoard_shouldReturnForbiddenFailure() {
        // given
        ToggleStarBoardCommand command = createValidCommand();
        UserId differentOwnerId = new UserId(); // 다른 사용자 ID 생성
        Board boardWithDifferentOwner = Board.builder()
                .boardId(boardId)
                .title("Other User's Board")
                .description("Other User's Description")
                .isArchived(false)
                .ownerId(differentOwnerId) // 다른 소유자
                .isStarred(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(boardWithDifferentOwner));

        // when
        Either<Failure, Board> result = toggleStarBoardService.starringBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ForbiddenFailure.class);
        assertThat(result.getLeft().message()).isEqualTo("보드 수정 권한이 없습니다.");

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("unstarringBoard - 다른 사용자의 보드인 경우 FORBIDDEN 실패를 반환해야 한다")
    void unstarringBoard_withOtherUsersBoard_shouldReturnForbiddenFailure() {
        // given
        ToggleStarBoardCommand command = createValidCommand();
        UserId differentOwnerId = new UserId(); // 다른 사용자 ID 생성
        Board boardWithDifferentOwner = Board.builder()
                .boardId(boardId)
                .title("Other User's Board")
                .description("Other User's Description")
                .isArchived(false)
                .ownerId(differentOwnerId) // 다른 소유자
                .isStarred(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(boardValidator.validate(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(boardId))
                .thenReturn(Optional.of(boardWithDifferentOwner));

        // when
        Either<Failure, Board> result = toggleStarBoardService.unstarringBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ForbiddenFailure.class);
        assertThat(result.getLeft().message()).isEqualTo("보드 수정 권한이 없습니다.");

        verify(boardValidator).validate(command);
        verify(boardRepository).findById(boardId);
        verify(boardRepository, never()).save(any());
    }
} 
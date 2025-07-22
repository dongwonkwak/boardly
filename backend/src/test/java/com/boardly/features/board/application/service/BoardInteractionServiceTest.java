package com.boardly.features.board.application.service;

import com.boardly.features.board.application.port.input.ToggleStarBoardCommand;
import com.boardly.features.board.application.validation.BoardValidator;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.user.application.service.UserFinder;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardInteractionServiceTest {

    private BoardInteractionService boardInteractionService;

    @Mock
    private BoardValidator boardValidator;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private ValidationMessageResolver messageResolver;

    @Mock
    private UserFinder userFinder;

    @BeforeEach
    void setUp() {
        boardInteractionService = new BoardInteractionService(
                boardValidator,
                boardRepository,
                messageResolver,
                userFinder);
    }

    private ToggleStarBoardCommand createValidCommand() {
        return ToggleStarBoardCommand.of(
                new BoardId(),
                new UserId());
    }

    private Board createValidBoard(BoardId boardId, UserId ownerId, boolean isStarred) {
        return Board.builder()
                .boardId(boardId)
                .title("테스트 보드")
                .description("테스트 보드 설명")
                .isArchived(false)
                .ownerId(ownerId)
                .isStarred(isStarred)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // ==================== STAR BOARD TESTS ====================

    @Test
    @DisplayName("유효한 정보로 보드 즐겨찾기 추가가 성공해야 한다")
    void starringBoard_withValidData_shouldReturnStarredBoard() {
        // given
        ToggleStarBoardCommand command = createValidCommand();
        Board unstarredBoard = createValidBoard(command.boardId(), command.requestedBy(), false);
        Board starredBoard = createValidBoard(command.boardId(), command.requestedBy(), true);

        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
        when(boardValidator.validateToggleStar(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(command.boardId()))
                .thenReturn(java.util.Optional.of(unstarredBoard));
        when(boardRepository.save(any(Board.class)))
                .thenReturn(Either.right(starredBoard));

        // when
        Either<Failure, Board> result = boardInteractionService.starringBoard(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().isStarred()).isTrue();
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    @DisplayName("이미 즐겨찾기된 보드에 즐겨찾기 추가 시도 시 기존 상태를 유지해야 한다")
    void starringBoard_withAlreadyStarredBoard_shouldReturnSameBoard() {
        // given
        ToggleStarBoardCommand command = createValidCommand();
        Board alreadyStarredBoard = createValidBoard(command.boardId(), command.requestedBy(), true);

        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
        when(boardValidator.validateToggleStar(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(command.boardId()))
                .thenReturn(java.util.Optional.of(alreadyStarredBoard));

        // when
        Either<Failure, Board> result = boardInteractionService.starringBoard(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().isStarred()).isTrue();
        verify(boardRepository, never()).save(any(Board.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 즐겨찾기 추가 시도 시 NotFound 오류를 반환해야 한다")
    void starringBoard_withNonExistentUser_shouldReturnNotFoundFailure() {
        // given
        ToggleStarBoardCommand command = createValidCommand();
        String errorMessage = "사용자를 찾을 수 없습니다";

        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(false);
        when(messageResolver.getMessage("validation.user.not.found")).thenReturn(errorMessage);

        // when
        Either<Failure, Board> result = boardInteractionService.starringBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getMessage()).isEqualTo(errorMessage);
        verify(boardRepository, never()).findById(any());
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("입력 검증 실패 시 InputError를 반환해야 한다")
    void starringBoard_withInvalidData_shouldReturnInputError() {
        // given
        ToggleStarBoardCommand command = createValidCommand();
        String errorMessage = "입력 데이터가 유효하지 않습니다";
        List<Failure.FieldViolation> violations = List.of(
                Failure.FieldViolation.builder()
                        .field("boardId")
                        .message("보드 ID는 필수입니다")
                        .rejectedValue(null)
                        .build());

        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
        when(boardValidator.validateToggleStar(command))
                .thenReturn(ValidationResult.invalid(io.vavr.collection.List.ofAll(violations)));
        when(messageResolver.getMessage("validation.input.invalid")).thenReturn(errorMessage);

        // when
        Either<Failure, Board> result = boardInteractionService.starringBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getMessage()).isEqualTo(errorMessage);
        assertThat(((Failure.InputError) result.getLeft()).getViolations()).containsExactlyElementsOf(violations);
        verify(boardRepository, never()).findById(any());
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 보드에 즐겨찾기 추가 시도 시 NotFound 오류를 반환해야 한다")
    void starringBoard_withNonExistentBoard_shouldReturnNotFoundFailure() {
        // given
        ToggleStarBoardCommand command = createValidCommand();
        String errorMessage = "보드를 찾을 수 없습니다";

        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
        when(boardValidator.validateToggleStar(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(command.boardId()))
                .thenReturn(java.util.Optional.empty());
        when(messageResolver.getMessage("validation.board.not.found")).thenReturn(errorMessage);

        // when
        Either<Failure, Board> result = boardInteractionService.starringBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getMessage()).isEqualTo(errorMessage);
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("보드 저장 중 예외 발생 시 InternalServerError를 반환해야 한다")
    void starringBoard_withSaveException_shouldReturnInternalServerError() {
        // given
        ToggleStarBoardCommand command = createValidCommand();
        Board unstarredBoard = createValidBoard(command.boardId(), command.requestedBy(), false);
        String errorMessage = "보드 저장 중 오류가 발생했습니다";

        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
        when(boardValidator.validateToggleStar(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(command.boardId()))
                .thenReturn(java.util.Optional.of(unstarredBoard));
        when(boardRepository.save(any(Board.class)))
                .thenThrow(new RuntimeException("Database error"));
        when(messageResolver.getMessage("validation.board.star.save.error")).thenReturn(errorMessage);

        // when
        Either<Failure, Board> result = boardInteractionService.starringBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getMessage()).isEqualTo(errorMessage);
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
    }

    // ==================== UNSTAR BOARD TESTS ====================

    @Test
    @DisplayName("유효한 정보로 보드 즐겨찾기 제거가 성공해야 한다")
    void unstarringBoard_withValidData_shouldReturnUnstarredBoard() {
        // given
        ToggleStarBoardCommand command = createValidCommand();
        Board starredBoard = createValidBoard(command.boardId(), command.requestedBy(), true);
        Board unstarredBoard = createValidBoard(command.boardId(), command.requestedBy(), false);

        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
        when(boardValidator.validateToggleStar(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(command.boardId()))
                .thenReturn(java.util.Optional.of(starredBoard));
        when(boardRepository.save(any(Board.class)))
                .thenReturn(Either.right(unstarredBoard));

        // when
        Either<Failure, Board> result = boardInteractionService.unstarringBoard(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().isStarred()).isFalse();
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    @DisplayName("이미 즐겨찾기되지 않은 보드에 즐겨찾기 제거 시도 시 기존 상태를 유지해야 한다")
    void unstarringBoard_withAlreadyUnstarredBoard_shouldReturnSameBoard() {
        // given
        ToggleStarBoardCommand command = createValidCommand();
        Board alreadyUnstarredBoard = createValidBoard(command.boardId(), command.requestedBy(), false);

        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
        when(boardValidator.validateToggleStar(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(command.boardId()))
                .thenReturn(java.util.Optional.of(alreadyUnstarredBoard));

        // when
        Either<Failure, Board> result = boardInteractionService.unstarringBoard(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().isStarred()).isFalse();
        verify(boardRepository, never()).save(any(Board.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 즐겨찾기 제거 시도 시 NotFound 오류를 반환해야 한다")
    void unstarringBoard_withNonExistentUser_shouldReturnNotFoundFailure() {
        // given
        ToggleStarBoardCommand command = createValidCommand();
        String errorMessage = "사용자를 찾을 수 없습니다";

        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(false);
        when(messageResolver.getMessage("validation.user.not.found")).thenReturn(errorMessage);

        // when
        Either<Failure, Board> result = boardInteractionService.unstarringBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getMessage()).isEqualTo(errorMessage);
        verify(boardRepository, never()).findById(any());
        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 보드에 즐겨찾기 제거 시도 시 NotFound 오류를 반환해야 한다")
    void unstarringBoard_withNonExistentBoard_shouldReturnNotFoundFailure() {
        // given
        ToggleStarBoardCommand command = createValidCommand();
        String errorMessage = "보드를 찾을 수 없습니다";

        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
        when(boardValidator.validateToggleStar(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(command.boardId()))
                .thenReturn(java.util.Optional.empty());
        when(messageResolver.getMessage("validation.board.not.found")).thenReturn(errorMessage);

        // when
        Either<Failure, Board> result = boardInteractionService.unstarringBoard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getMessage()).isEqualTo(errorMessage);
        verify(boardRepository, never()).save(any());
    }

    // ==================== INTEGRATION TESTS ====================

    @Test
    @DisplayName("동일한 상태로 토글 시도 시 저장하지 않고 기존 보드를 반환해야 한다")
    void toggleStarBoard_sameStateToggle_shouldNotSaveAndReturnExistingBoard() {
        // given
        ToggleStarBoardCommand command = createValidCommand();
        Board starredBoard = createValidBoard(command.boardId(), command.requestedBy(), true);

        when(userFinder.checkUserExists(command.requestedBy())).thenReturn(true);
        when(boardValidator.validateToggleStar(command))
                .thenReturn(ValidationResult.valid(command));
        when(boardRepository.findById(command.boardId()))
                .thenReturn(java.util.Optional.of(starredBoard));

        // when - 이미 즐겨찾기된 보드에 다시 즐겨찾기 추가
        Either<Failure, Board> result = boardInteractionService.starringBoard(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().isStarred()).isTrue();
        verify(boardRepository, never()).save(any(Board.class));
    }
}
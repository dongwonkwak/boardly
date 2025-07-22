package com.boardly.features.board.application.service;

import com.boardly.features.board.application.port.input.GetUserBoardsCommand;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.user.application.service.UserFinder;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BoardQueryServiceTest {

    private BoardQueryService boardQueryService;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private UserFinder userFinder;

    @Mock
    private ValidationMessageResolver validationMessageResolver;

    @BeforeEach
    void setUp() {
        boardQueryService = new BoardQueryService(
                boardRepository,
                userFinder,
                validationMessageResolver);
    }

    // ==================== HELPER METHODS ====================

    private GetUserBoardsCommand createValidCommand(UserId ownerId, boolean includeArchived) {
        return new GetUserBoardsCommand(ownerId, includeArchived);
    }

    private Board createValidBoard(BoardId boardId, UserId ownerId, boolean isArchived, Instant updatedAt) {
        return Board.builder()
                .boardId(boardId)
                .title("테스트 보드")
                .description("테스트 보드 설명")
                .isArchived(isArchived)
                .ownerId(ownerId)
                .isStarred(false)
                .createdAt(Instant.now())
                .updatedAt(updatedAt)
                .build();
    }

    private List<Board> createTestBoards(UserId ownerId) {
        Instant now = Instant.now();
        Instant oneHourAgo = now.minusSeconds(3600);
        Instant twoHoursAgo = now.minusSeconds(7200);

        Board board1 = createValidBoard(new BoardId(), ownerId, false, oneHourAgo);
        Board board2 = createValidBoard(new BoardId(), ownerId, false, now);
        Board board3 = createValidBoard(new BoardId(), ownerId, true, twoHoursAgo);

        return List.of(board1, board2, board3);
    }

    // ==================== SUCCESS CASES ====================

    @Test
    @DisplayName("유효한 정보로 활성 보드 목록 조회가 성공해야 한다")
    void getUserBoards_withValidActiveOnlyCommand_shouldReturnActiveBoards() {
        // given
        UserId ownerId = new UserId();
        GetUserBoardsCommand command = createValidCommand(ownerId, false);
        List<Board> expectedBoards = createTestBoards(ownerId).stream()
                .filter(board -> !board.isArchived())
                .toList();

        when(validationMessageResolver.getMessage("validation.input.invalid"))
                .thenReturn("입력이 유효하지 않습니다");
        when(validationMessageResolver.getMessage("validation.user.id.required"))
                .thenReturn("사용자 ID는 필수입니다");
        when(validationMessageResolver.getMessage("validation.user.not.found"))
                .thenReturn("사용자를 찾을 수 없습니다");
        when(userFinder.checkUserExists(ownerId)).thenReturn(true);
        when(boardRepository.findActiveByOwnerId(ownerId)).thenReturn(expectedBoards);

        // when
        Either<Failure, List<Board>> result = boardQueryService.getUserBoards(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).hasSize(2);
        assertThat(result.get()).allMatch(board -> !board.isArchived());
        assertThat(result.get()).isSortedAccordingTo(
                (b1, b2) -> b2.getUpdatedAt().compareTo(b1.getUpdatedAt()));

        verify(userFinder).checkUserExists(ownerId);
        verify(boardRepository).findActiveByOwnerId(ownerId);
        verify(boardRepository, never()).findByOwnerId(any());
    }

    @Test
    @DisplayName("유효한 정보로 모든 보드 목록 조회가 성공해야 한다")
    void getUserBoards_withValidAllCommand_shouldReturnAllBoards() {
        // given
        UserId ownerId = new UserId();
        GetUserBoardsCommand command = createValidCommand(ownerId, true);
        List<Board> expectedBoards = createTestBoards(ownerId);

        when(validationMessageResolver.getMessage("validation.input.invalid"))
                .thenReturn("입력이 유효하지 않습니다");
        when(validationMessageResolver.getMessage("validation.user.id.required"))
                .thenReturn("사용자 ID는 필수입니다");
        when(validationMessageResolver.getMessage("validation.user.not.found"))
                .thenReturn("사용자를 찾을 수 없습니다");
        when(userFinder.checkUserExists(ownerId)).thenReturn(true);
        when(boardRepository.findByOwnerId(ownerId)).thenReturn(expectedBoards);

        // when
        Either<Failure, List<Board>> result = boardQueryService.getUserBoards(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).hasSize(3);
        assertThat(result.get()).isSortedAccordingTo(
                (b1, b2) -> b2.getUpdatedAt().compareTo(b1.getUpdatedAt()));

        verify(userFinder).checkUserExists(ownerId);
        verify(boardRepository).findByOwnerId(ownerId);
        verify(boardRepository, never()).findActiveByOwnerId(any());
    }

    @Test
    @DisplayName("보드가 없는 사용자의 보드 목록 조회 시 빈 리스트를 반환해야 한다")
    void getUserBoards_withNoBoards_shouldReturnEmptyList() {
        // given
        UserId ownerId = new UserId();
        GetUserBoardsCommand command = createValidCommand(ownerId, false);

        when(validationMessageResolver.getMessage("validation.input.invalid"))
                .thenReturn("입력이 유효하지 않습니다");
        when(validationMessageResolver.getMessage("validation.user.id.required"))
                .thenReturn("사용자 ID는 필수입니다");
        when(validationMessageResolver.getMessage("validation.user.not.found"))
                .thenReturn("사용자를 찾을 수 없습니다");
        when(userFinder.checkUserExists(ownerId)).thenReturn(true);
        when(boardRepository.findActiveByOwnerId(ownerId)).thenReturn(List.of());

        // when
        Either<Failure, List<Board>> result = boardQueryService.getUserBoards(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isEmpty();

        verify(userFinder).checkUserExists(ownerId);
        verify(boardRepository).findActiveByOwnerId(ownerId);
    }

    // ==================== FAILURE CASES ====================

    @Test
    @DisplayName("null 커맨드로 조회 시도 시 InputError를 반환해야 한다")
    void getUserBoards_withNullCommand_shouldReturnInputError() {
        // given
        when(validationMessageResolver.getMessage("validation.input.invalid"))
                .thenReturn("입력이 유효하지 않습니다");

        // when
        Either<Failure, List<Board>> result = boardQueryService.getUserBoards(null);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
        assertThat(((Failure.InputError) result.getLeft()).getErrorCode()).isEqualTo("INVALID_COMMAND");
        assertThat(result.getLeft().getMessage()).isEqualTo("입력이 유효하지 않습니다");
        assertThat(((Failure.InputError) result.getLeft()).getViolations()).hasSize(1);
        assertThat(((Failure.InputError) result.getLeft()).getViolations().get(0).field()).isEqualTo("command");

        verify(userFinder, never()).checkUserExists(any());
        verify(boardRepository, never()).findActiveByOwnerId(any());
        verify(boardRepository, never()).findByOwnerId(any());
    }

    @Test
    @DisplayName("null ownerId로 조회 시도 시 InputError를 반환해야 한다")
    void getUserBoards_withNullOwnerId_shouldReturnInputError() {
        // given
        GetUserBoardsCommand command = new GetUserBoardsCommand(null, false);

        when(validationMessageResolver.getMessage("validation.input.invalid"))
                .thenReturn("입력이 유효하지 않습니다");
        when(validationMessageResolver.getMessage("validation.user.id.required"))
                .thenReturn("사용자 ID는 필수입니다");

        // when
        Either<Failure, List<Board>> result = boardQueryService.getUserBoards(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
        assertThat(((Failure.InputError) result.getLeft()).getErrorCode()).isEqualTo("INVALID_INPUT");
        assertThat(result.getLeft().getMessage()).isEqualTo("입력이 유효하지 않습니다");
        assertThat(((Failure.InputError) result.getLeft()).getViolations()).hasSize(1);
        assertThat(((Failure.InputError) result.getLeft()).getViolations().get(0).field()).isEqualTo("userId");

        verify(userFinder, never()).checkUserExists(any());
        verify(boardRepository, never()).findActiveByOwnerId(any());
        verify(boardRepository, never()).findByOwnerId(any());
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 조회 시도 시 NotFound 오류를 반환해야 한다")
    void getUserBoards_withNonExistentUser_shouldReturnNotFoundFailure() {
        // given
        UserId ownerId = new UserId();
        GetUserBoardsCommand command = createValidCommand(ownerId, false);

        when(validationMessageResolver.getMessage("validation.input.invalid"))
                .thenReturn("입력이 유효하지 않습니다");
        when(validationMessageResolver.getMessage("validation.user.id.required"))
                .thenReturn("사용자 ID는 필수입니다");
        when(validationMessageResolver.getMessage("validation.user.not.found"))
                .thenReturn("사용자를 찾을 수 없습니다");
        when(userFinder.checkUserExists(ownerId)).thenReturn(false);

        // when
        Either<Failure, List<Board>> result = boardQueryService.getUserBoards(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
        assertThat(((Failure.NotFound) result.getLeft()).getErrorCode()).isEqualTo("NOT_FOUND");
        assertThat(result.getLeft().getMessage()).isEqualTo("사용자를 찾을 수 없습니다");

        verify(userFinder).checkUserExists(ownerId);
        verify(boardRepository, never()).findActiveByOwnerId(any());
        verify(boardRepository, never()).findByOwnerId(any());
    }

    // ==================== EXCEPTION CASES ====================

    @Test
    @DisplayName("보드 저장소에서 예외 발생 시 InternalServerError를 반환해야 한다")
    void getUserBoards_withRepositoryException_shouldReturnInternalServerError() {
        // given
        UserId ownerId = new UserId();
        GetUserBoardsCommand command = createValidCommand(ownerId, false);
        RuntimeException exception = new RuntimeException("데이터베이스 연결 오류");

        when(validationMessageResolver.getMessage("validation.input.invalid"))
                .thenReturn("입력이 유효하지 않습니다");
        when(validationMessageResolver.getMessage("validation.user.id.required"))
                .thenReturn("사용자 ID는 필수입니다");
        when(validationMessageResolver.getMessage("validation.user.not.found"))
                .thenReturn("사용자를 찾을 수 없습니다");
        when(userFinder.checkUserExists(ownerId)).thenReturn(true);
        when(boardRepository.findActiveByOwnerId(ownerId)).thenThrow(exception);

        // when
        Either<Failure, List<Board>> result = boardQueryService.getUserBoards(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
        assertThat(((Failure.InternalError) result.getLeft()).getErrorCode()).isEqualTo("BOARD_QUERY_ERROR");
        assertThat(result.getLeft().getMessage()).isEqualTo("데이터베이스 연결 오류");

        verify(userFinder).checkUserExists(ownerId);
        verify(boardRepository).findActiveByOwnerId(ownerId);
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("활성 보드만 조회할 때 아카이브된 보드가 제외되어야 한다")
    void getUserBoards_activeOnly_shouldExcludeArchivedBoards() {
        // given
        UserId ownerId = new UserId();
        GetUserBoardsCommand command = createValidCommand(ownerId, false);
        List<Board> allBoards = createTestBoards(ownerId);
        List<Board> activeBoards = allBoards.stream()
                .filter(board -> !board.isArchived())
                .toList();

        when(validationMessageResolver.getMessage("validation.input.invalid"))
                .thenReturn("입력이 유효하지 않습니다");
        when(validationMessageResolver.getMessage("validation.user.id.required"))
                .thenReturn("사용자 ID는 필수입니다");
        when(validationMessageResolver.getMessage("validation.user.not.found"))
                .thenReturn("사용자를 찾을 수 없습니다");
        when(userFinder.checkUserExists(ownerId)).thenReturn(true);
        when(boardRepository.findActiveByOwnerId(ownerId)).thenReturn(activeBoards);

        // when
        Either<Failure, List<Board>> result = boardQueryService.getUserBoards(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).hasSize(2);
        assertThat(result.get()).allMatch(board -> !board.isArchived());

        verify(boardRepository).findActiveByOwnerId(ownerId);
        verify(boardRepository, never()).findByOwnerId(any());
    }

    @Test
    @DisplayName("모든 보드 조회 시 아카이브된 보드도 포함되어야 한다")
    void getUserBoards_allBoards_shouldIncludeArchivedBoards() {
        // given
        UserId ownerId = new UserId();
        GetUserBoardsCommand command = createValidCommand(ownerId, true);
        List<Board> allBoards = createTestBoards(ownerId);

        when(validationMessageResolver.getMessage("validation.input.invalid"))
                .thenReturn("입력이 유효하지 않습니다");
        when(validationMessageResolver.getMessage("validation.user.id.required"))
                .thenReturn("사용자 ID는 필수입니다");
        when(validationMessageResolver.getMessage("validation.user.not.found"))
                .thenReturn("사용자를 찾을 수 없습니다");
        when(userFinder.checkUserExists(ownerId)).thenReturn(true);
        when(boardRepository.findByOwnerId(ownerId)).thenReturn(allBoards);

        // when
        Either<Failure, List<Board>> result = boardQueryService.getUserBoards(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).hasSize(3);
        assertThat(result.get()).anyMatch(Board::isArchived);

        verify(boardRepository).findByOwnerId(ownerId);
        verify(boardRepository, never()).findActiveByOwnerId(any());
    }

    @Test
    @DisplayName("보드 목록이 수정 시간 역순으로 정렬되어야 한다")
    void getUserBoards_shouldReturnBoardsSortedByUpdatedAtDesc() {
        // given
        UserId ownerId = new UserId();
        GetUserBoardsCommand command = createValidCommand(ownerId, false);

        Instant now = Instant.now();
        Instant oneHourAgo = now.minusSeconds(3600);
        Instant twoHoursAgo = now.minusSeconds(7200);

        Board oldBoard = createValidBoard(new BoardId(), ownerId, false, twoHoursAgo);
        Board newBoard = createValidBoard(new BoardId(), ownerId, false, now);
        Board middleBoard = createValidBoard(new BoardId(), ownerId, false, oneHourAgo);

        List<Board> unsortedBoards = List.of(oldBoard, newBoard, middleBoard);

        when(validationMessageResolver.getMessage("validation.input.invalid"))
                .thenReturn("입력이 유효하지 않습니다");
        when(validationMessageResolver.getMessage("validation.user.id.required"))
                .thenReturn("사용자 ID는 필수입니다");
        when(validationMessageResolver.getMessage("validation.user.not.found"))
                .thenReturn("사용자를 찾을 수 없습니다");
        when(userFinder.checkUserExists(ownerId)).thenReturn(true);
        when(boardRepository.findActiveByOwnerId(ownerId)).thenReturn(unsortedBoards);

        // when
        Either<Failure, List<Board>> result = boardQueryService.getUserBoards(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).hasSize(3);
        assertThat(result.get().get(0).getUpdatedAt()).isEqualTo(now);
        assertThat(result.get().get(1).getUpdatedAt()).isEqualTo(oneHourAgo);
        assertThat(result.get().get(2).getUpdatedAt()).isEqualTo(twoHoursAgo);
    }

    // ==================== COMMAND FACTORY METHODS ====================

    @Test
    @DisplayName("activeOnly 팩토리 메서드로 생성된 커맨드로 조회가 성공해야 한다")
    void getUserBoards_withActiveOnlyFactoryMethod_shouldReturnActiveBoards() {
        // given
        UserId ownerId = new UserId();
        GetUserBoardsCommand command = GetUserBoardsCommand.activeOnly(ownerId);
        List<Board> expectedBoards = createTestBoards(ownerId).stream()
                .filter(board -> !board.isArchived())
                .toList();

        when(validationMessageResolver.getMessage("validation.input.invalid"))
                .thenReturn("입력이 유효하지 않습니다");
        when(validationMessageResolver.getMessage("validation.user.id.required"))
                .thenReturn("사용자 ID는 필수입니다");
        when(validationMessageResolver.getMessage("validation.user.not.found"))
                .thenReturn("사용자를 찾을 수 없습니다");
        when(userFinder.checkUserExists(ownerId)).thenReturn(true);
        when(boardRepository.findActiveByOwnerId(ownerId)).thenReturn(expectedBoards);

        // when
        Either<Failure, List<Board>> result = boardQueryService.getUserBoards(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).hasSize(2);
        assertThat(command.includeArchived()).isFalse();

        verify(boardRepository).findActiveByOwnerId(ownerId);
    }

    @Test
    @DisplayName("all 팩토리 메서드로 생성된 커맨드로 조회가 성공해야 한다")
    void getUserBoards_withAllFactoryMethod_shouldReturnAllBoards() {
        // given
        UserId ownerId = new UserId();
        GetUserBoardsCommand command = GetUserBoardsCommand.all(ownerId);
        List<Board> expectedBoards = createTestBoards(ownerId);

        when(validationMessageResolver.getMessage("validation.input.invalid"))
                .thenReturn("입력이 유효하지 않습니다");
        when(validationMessageResolver.getMessage("validation.user.id.required"))
                .thenReturn("사용자 ID는 필수입니다");
        when(validationMessageResolver.getMessage("validation.user.not.found"))
                .thenReturn("사용자를 찾을 수 없습니다");
        when(userFinder.checkUserExists(ownerId)).thenReturn(true);
        when(boardRepository.findByOwnerId(ownerId)).thenReturn(expectedBoards);

        // when
        Either<Failure, List<Board>> result = boardQueryService.getUserBoards(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).hasSize(3);
        assertThat(command.includeArchived()).isTrue();

        verify(boardRepository).findByOwnerId(ownerId);
    }
}
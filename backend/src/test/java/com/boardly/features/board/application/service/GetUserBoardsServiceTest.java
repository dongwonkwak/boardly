package com.boardly.features.board.application.service;

import com.boardly.features.board.application.port.input.GetUserBoardsCommand;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetUserBoardsService 테스트")
class GetUserBoardsServiceTest {

  @Mock
  private BoardRepository boardRepository;

  @Mock
  private ValidationMessageResolver validationMessageResolver;

  @InjectMocks
  private GetUserBoardsService getUserBoardsService;

  private UserId testUserId;
  private Board activeBoard1;
  private Board activeBoard2;
  private Board archivedBoard;

  @BeforeEach
  void setUp() {
    testUserId = new UserId("test-user-123");

    Instant now = Instant.now();

    activeBoard1 = Board.builder()
        .boardId(new BoardId())
        .title("활성 보드 1")
        .description("첫 번째 활성 보드")
        .isArchived(false)
        .ownerId(testUserId)
        .createdAt(now.minus(2, ChronoUnit.HOURS))
        .updatedAt(now.minus(30, ChronoUnit.MINUTES))
        .build();

    activeBoard2 = Board.builder()
        .boardId(new BoardId())
        .title("활성 보드 2")
        .description("두 번째 활성 보드")
        .isArchived(false)
        .ownerId(testUserId)
        .createdAt(now.minus(1, ChronoUnit.HOURS))
        .updatedAt(now) // 가장 최근 수정
        .build();

    archivedBoard = Board.builder()
        .boardId(new BoardId())
        .title("아카이브된 보드")
        .description("아카이브된 보드")
        .isArchived(true)
        .ownerId(testUserId)
        .createdAt(now.minus(3, ChronoUnit.HOURS))
        .updatedAt(now.minus(1, ChronoUnit.HOURS))
        .build();
  }

  @Test
  @DisplayName("활성 보드만 조회 - 성공")
  void getUserBoards_ActiveOnly_Success() {
    // given
    GetUserBoardsCommand command = GetUserBoardsCommand.activeOnly(testUserId);
    List<Board> activeBoards = List.of(activeBoard1, activeBoard2);

    when(boardRepository.findActiveByOwnerId(testUserId)).thenReturn(activeBoards);

    // when
    Either<Failure, List<Board>> result = getUserBoardsService.getUserBoards(command);

    // then
    assertThat(result.isRight()).isTrue();
    List<Board> boards = result.get();
    assertThat(boards).hasSize(2);
    // 최신 수정 시간 순으로 정렬되어야 함
    assertThat(boards.get(0)).isEqualTo(activeBoard2); // 가장 최근 수정
    assertThat(boards.get(1)).isEqualTo(activeBoard1);

    verify(boardRepository).findActiveByOwnerId(testUserId);
    verify(boardRepository, never()).findByOwnerId(any());
  }

  @Test
  @DisplayName("모든 보드 조회 - 성공")
  void getUserBoards_All_Success() {
    // given
    GetUserBoardsCommand command = GetUserBoardsCommand.all(testUserId);
    List<Board> allBoards = List.of(activeBoard1, activeBoard2, archivedBoard);

    when(boardRepository.findByOwnerId(testUserId)).thenReturn(allBoards);

    // when
    Either<Failure, List<Board>> result = getUserBoardsService.getUserBoards(command);

    // then
    assertThat(result.isRight()).isTrue();
    List<Board> boards = result.get();
    assertThat(boards).hasSize(3);
    // 최신 수정 시간 순으로 정렬되어야 함
    assertThat(boards.get(0)).isEqualTo(activeBoard2); // 가장 최근 수정
    assertThat(boards.get(1)).isEqualTo(activeBoard1);
    assertThat(boards.get(2)).isEqualTo(archivedBoard);

    verify(boardRepository).findByOwnerId(testUserId);
    verify(boardRepository, never()).findActiveByOwnerId(any());
  }

  @Test
  @DisplayName("빈 목록 조회 - 성공")
  void getUserBoards_EmptyList_Success() {
    // given
    GetUserBoardsCommand command = GetUserBoardsCommand.activeOnly(testUserId);

    when(boardRepository.findActiveByOwnerId(testUserId)).thenReturn(List.of());

    // when
    Either<Failure, List<Board>> result = getUserBoardsService.getUserBoards(command);

    // then
    assertThat(result.isRight()).isTrue();
    assertThat(result.get()).isEmpty();

    verify(boardRepository).findActiveByOwnerId(testUserId);
  }

  @Test
  @DisplayName("Command가 null인 경우 - 검증 실패")
  void getUserBoards_NullCommand_ValidationFailure() {
    // given
    when(validationMessageResolver.getMessage("validation.input.invalid"))
        .thenReturn("입력 데이터가 올바르지 않습니다");

    // when
    Either<Failure, List<Board>> result = getUserBoardsService.getUserBoards(null);

    // then
    assertThat(result.isLeft()).isTrue();
    Failure failure = result.getLeft();
    assertThat(failure).isInstanceOf(Failure.InputError.class);

    Failure.InputError inputError = (Failure.InputError) failure;
    assertThat(inputError.getMessage()).isEqualTo("입력 데이터가 올바르지 않습니다");
    assertThat(inputError.getErrorCode()).isEqualTo("INVALID_COMMAND");
    assertThat(inputError.getViolations()).hasSize(1);

    Failure.FieldViolation violation = inputError.getViolations().iterator().next();
    assertThat(violation.field()).isEqualTo("command");
    assertThat(violation.message()).isEqualTo("GetUserBoardsCommand is null");
    assertThat(violation.rejectedValue()).isNull();

    verify(boardRepository, never()).findActiveByOwnerId(any());
    verify(boardRepository, never()).findByOwnerId(any());
  }

  @Test
  @DisplayName("소유자 ID가 null인 경우 - 검증 실패")
  void getUserBoards_NullOwnerId_ValidationFailure() {
    // given
    GetUserBoardsCommand command = new GetUserBoardsCommand(null, false);

    when(validationMessageResolver.getMessage("validation.input.invalid"))
        .thenReturn("입력 데이터가 올바르지 않습니다");
    when(validationMessageResolver.getMessage("validation.user.id.required"))
        .thenReturn("사용자 ID는 필수 입력 항목입니다");

    // when
    Either<Failure, List<Board>> result = getUserBoardsService.getUserBoards(command);

    // then
    assertThat(result.isLeft()).isTrue();
    Failure failure = result.getLeft();
    assertThat(failure).isInstanceOf(Failure.InputError.class);

    Failure.InputError inputError = (Failure.InputError) failure;
    assertThat(inputError.getMessage()).isEqualTo("입력 데이터가 올바르지 않습니다");
    assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
    assertThat(inputError.getViolations()).hasSize(1);

    Failure.FieldViolation violation = inputError.getViolations().iterator().next();
    assertThat(violation.field()).isEqualTo("userId");
    assertThat(violation.message()).isEqualTo("사용자 ID는 필수 입력 항목입니다");
    assertThat(violation.rejectedValue()).isNull();

    verify(boardRepository, never()).findActiveByOwnerId(any());
    verify(boardRepository, never()).findByOwnerId(any());
  }

  @Test
  @DisplayName("Repository에서 예외 발생 - 내부 서버 오류")
  void getUserBoards_RepositoryException_InternalServerError() {
    // given
    GetUserBoardsCommand command = GetUserBoardsCommand.activeOnly(testUserId);
    RuntimeException exception = new RuntimeException("Database connection failed");

    when(boardRepository.findActiveByOwnerId(testUserId))
        .thenThrow(exception);

    // when
    Either<Failure, List<Board>> result = getUserBoardsService.getUserBoards(command);

    // then
    assertThat(result.isLeft()).isTrue();
    Failure failure = result.getLeft();
    assertThat(failure).isInstanceOf(Failure.InternalError.class);

    Failure.InternalError internalError = (Failure.InternalError) failure;
    assertThat(internalError.getMessage()).isEqualTo("Database connection failed");
    assertThat(internalError.getErrorCode()).isEqualTo("BOARD_QUERY_ERROR");
    assertThat(internalError.getContext()).isNull();

    verify(boardRepository).findActiveByOwnerId(testUserId);
  }

  @Test
  @DisplayName("활성 보드 조회 시 정렬 확인")
  void getUserBoards_ActiveOnly_SortingVerification() {
    // given
    GetUserBoardsCommand command = GetUserBoardsCommand.activeOnly(testUserId);
    // 정렬되지 않은 순서로 반환
    List<Board> unsortedBoards = List.of(activeBoard1, activeBoard2);

    when(boardRepository.findActiveByOwnerId(testUserId)).thenReturn(unsortedBoards);

    // when
    Either<Failure, List<Board>> result = getUserBoardsService.getUserBoards(command);

    // then
    assertThat(result.isRight()).isTrue();
    List<Board> boards = result.get();
    assertThat(boards).hasSize(2);

    // updatedAt 기준 내림차순 정렬 확인
    assertThat(boards.get(0).getUpdatedAt()).isAfter(boards.get(1).getUpdatedAt());
    assertThat(boards.get(0)).isEqualTo(activeBoard2); // 가장 최근 수정
    assertThat(boards.get(1)).isEqualTo(activeBoard1);
  }

  @Test
  @DisplayName("전체 보드 조회 시 정렬 확인")
  void getUserBoards_All_SortingVerification() {
    // given
    GetUserBoardsCommand command = GetUserBoardsCommand.all(testUserId);
    // 정렬되지 않은 순서로 반환
    List<Board> unsortedBoards = List.of(archivedBoard, activeBoard1, activeBoard2);

    when(boardRepository.findByOwnerId(testUserId)).thenReturn(unsortedBoards);

    // when
    Either<Failure, List<Board>> result = getUserBoardsService.getUserBoards(command);

    // then
    assertThat(result.isRight()).isTrue();
    List<Board> boards = result.get();
    assertThat(boards).hasSize(3);

    // updatedAt 기준 내림차순 정렬 확인
    assertThat(boards.get(0).getUpdatedAt()).isAfter(boards.get(1).getUpdatedAt());
    assertThat(boards.get(1).getUpdatedAt()).isAfter(boards.get(2).getUpdatedAt());
    assertThat(boards.get(0)).isEqualTo(activeBoard2); // 가장 최근 수정
    assertThat(boards.get(1)).isEqualTo(activeBoard1);
    assertThat(boards.get(2)).isEqualTo(archivedBoard);
  }
}
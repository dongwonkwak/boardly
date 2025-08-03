package com.boardly.features.board.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.boardly.features.board.application.dto.BoardDetailDto;
import com.boardly.features.board.application.port.input.GetBoardDetailCommand;
import com.boardly.features.board.application.port.input.GetUserBoardsCommand;
import com.boardly.features.board.application.port.output.GetBoardDetailPort;
import com.boardly.features.board.application.port.output.GetBoardDetailPort.BoardDetailData;
import com.boardly.features.board.application.validation.BoardValidator;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.model.BoardMember;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.label.domain.model.Label;
import com.boardly.features.user.application.service.UserFinder;
import com.boardly.features.user.domain.model.User;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("BoardQueryService 테스트")
class BoardQueryServiceTest {

        @Mock
        private BoardRepository boardRepository;

        @Mock
        private UserFinder userFinder;

        @Mock
        private GetBoardDetailPort getBoardDetailPort;

        @Mock
        private ValidationMessageResolver validationMessageResolver;

        @Mock
        private BoardValidator boardValidator;

        private BoardQueryService boardQueryService;

        @BeforeEach
        void setUp() {
                boardQueryService = new BoardQueryService(
                                boardRepository,
                                userFinder,
                                getBoardDetailPort,
                                validationMessageResolver,
                                boardValidator);
        }

        // ==================== HELPER METHODS ====================

        private GetUserBoardsCommand createValidGetUserBoardsCommand(UserId ownerId, boolean includeArchived) {
                return new GetUserBoardsCommand(ownerId, includeArchived);
        }

        private GetBoardDetailCommand createValidGetBoardDetailCommand(BoardId boardId, UserId userId) {
                return new GetBoardDetailCommand(boardId, userId);
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

        private BoardDetailData createValidBoardDetailData() {
                BoardId boardId = new BoardId();
                UserId ownerId = new UserId();

                Board board = createValidBoard(boardId, ownerId, false, Instant.now());
                List<BoardList> boardLists = List.of();
                List<BoardMember> boardMembers = List.of();
                List<Label> labels = List.of();
                Map<ListId, List<Card>> cards = Map.of();
                Map<UserId, User> users = Map.of();

                return new BoardDetailData(board, boardLists, boardMembers, labels, cards, users);
        }

        // ==================== GET USER BOARDS TESTS ====================

        @Test
        @DisplayName("유효한 정보로 활성 보드 목록 조회가 성공해야 한다")
        void getUserBoards_withValidActiveOnlyCommand_shouldReturnActiveBoards() {
                // given
                UserId ownerId = new UserId();
                GetUserBoardsCommand command = createValidGetUserBoardsCommand(ownerId, false);
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
                GetUserBoardsCommand command = createValidGetUserBoardsCommand(ownerId, true);
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
                GetUserBoardsCommand command = createValidGetUserBoardsCommand(ownerId, false);

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

        @Test
        @DisplayName("보드 저장소에서 예외 발생 시 InternalServerError를 반환해야 한다")
        void getUserBoards_withRepositoryException_shouldReturnInternalServerError() {
                // given
                UserId ownerId = new UserId();
                GetUserBoardsCommand command = createValidGetUserBoardsCommand(ownerId, false);
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

        // ==================== GET BOARD DETAIL TESTS ====================

        @Test
        @DisplayName("유효한 정보로 보드 상세 조회가 성공해야 한다")
        void getBoardDetail_withValidCommand_shouldReturnBoardDetailDto() {
                // given
                BoardId boardId = new BoardId();
                UserId userId = new UserId();
                GetBoardDetailCommand command = createValidGetBoardDetailCommand(boardId, userId);
                BoardDetailData boardDetailData = createValidBoardDetailData();

                when(boardValidator.validateGetDetail(command))
                                .thenReturn(ValidationResult.valid(command));
                when(getBoardDetailPort.getBoardDetail(boardId, userId))
                                .thenReturn(Either.right(boardDetailData));

                // when
                Either<Failure, BoardDetailDto> result = boardQueryService.getBoardDetail(command);

                // then
                assertThat(result.isRight()).isTrue();
                assertThat(result.get()).isNotNull();
                assertThat(result.get()).isInstanceOf(BoardDetailDto.class);

                verify(boardValidator).validateGetDetail(command);
                verify(getBoardDetailPort).getBoardDetail(boardId, userId);
        }

        @Test
        @DisplayName("보드 상세 조회 입력 검증 실패 시 InputError를 반환해야 한다")
        void getBoardDetail_withInvalidInput_shouldReturnInputError() {
                // given
                BoardId boardId = new BoardId();
                UserId userId = new UserId();
                GetBoardDetailCommand command = createValidGetBoardDetailCommand(boardId, userId);

                Failure.FieldViolation violation = Failure.FieldViolation.builder()
                                .field("boardId")
                                .message("보드 ID는 필수입니다")
                                .rejectedValue(null)
                                .build();

                when(boardValidator.validateGetDetail(command))
                                .thenReturn(ValidationResult.invalid(violation));
                when(validationMessageResolver.getMessage("validation.input.invalid"))
                                .thenReturn("입력이 유효하지 않습니다");

                // when
                Either<Failure, BoardDetailDto> result = boardQueryService.getBoardDetail(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
                assertThat(((Failure.InputError) result.getLeft()).getErrorCode()).isEqualTo("INVALID_INPUT");
                assertThat(result.getLeft().getMessage()).isEqualTo("입력이 유효하지 않습니다");
                assertThat(((Failure.InputError) result.getLeft()).getViolations()).hasSize(1);

                verify(boardValidator).validateGetDetail(command);
                verify(getBoardDetailPort, never()).getBoardDetail(any(), any());
        }

        @Test
        @DisplayName("보드 상세 데이터 조회 실패 시 포트에서 반환된 실패를 그대로 반환해야 한다")
        void getBoardDetail_whenPortReturnsFailure_shouldReturnFailure() {
                // given
                BoardId boardId = new BoardId();
                UserId userId = new UserId();
                GetBoardDetailCommand command = createValidGetBoardDetailCommand(boardId, userId);
                Failure expectedFailure = Failure.ofNotFound("보드를 찾을 수 없습니다");

                when(boardValidator.validateGetDetail(command))
                                .thenReturn(ValidationResult.valid(command));
                when(getBoardDetailPort.getBoardDetail(boardId, userId))
                                .thenReturn(Either.left(expectedFailure));

                // when
                Either<Failure, BoardDetailDto> result = boardQueryService.getBoardDetail(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isEqualTo(expectedFailure);

                verify(boardValidator).validateGetDetail(command);
                verify(getBoardDetailPort).getBoardDetail(boardId, userId);
        }

        @Test
        @DisplayName("보드 상세 조회 중 예외 발생 시 InternalServerError를 반환해야 한다")
        void getBoardDetail_whenExceptionOccurs_shouldReturnInternalServerError() {
                // given
                BoardId boardId = new BoardId();
                UserId userId = new UserId();
                GetBoardDetailCommand command = createValidGetBoardDetailCommand(boardId, userId);
                RuntimeException exception = new RuntimeException("데이터베이스 오류");

                when(boardValidator.validateGetDetail(command))
                                .thenReturn(ValidationResult.valid(command));
                when(getBoardDetailPort.getBoardDetail(boardId, userId))
                                .thenThrow(exception);
                when(validationMessageResolver.getMessage("board.detail.get.error", "보드 상세 조회 중 오류가 발생했습니다."))
                                .thenReturn("보드 상세 조회 중 오류가 발생했습니다.");

                // when
                Either<Failure, BoardDetailDto> result = boardQueryService.getBoardDetail(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
                assertThat(result.getLeft().getMessage()).isEqualTo("보드 상세 조회 중 오류가 발생했습니다.");

                verify(boardValidator).validateGetDetail(command);
                verify(getBoardDetailPort).getBoardDetail(boardId, userId);
        }

        @Test
        @DisplayName("BoardDetailDto 생성 중 예외 발생 시 InternalError를 반환해야 한다")
        void getBoardDetail_whenDtoCreationFails_shouldReturnInternalError() {
                // given
                BoardId boardId = new BoardId();
                UserId userId = new UserId();
                GetBoardDetailCommand command = createValidGetBoardDetailCommand(boardId, userId);

                // null 데이터를 전달하여 NPE 발생시키기
                BoardDetailData boardDetailData = new BoardDetailData(null, null, null, null, null, null);

                when(boardValidator.validateGetDetail(command))
                                .thenReturn(ValidationResult.valid(command));
                when(getBoardDetailPort.getBoardDetail(boardId, userId))
                                .thenReturn(Either.right(boardDetailData));

                // when
                Either<Failure, BoardDetailDto> result = boardQueryService.getBoardDetail(command);

                // then
                assertThat(result.isLeft()).isTrue();
                assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
                assertThat(result.getLeft().getMessage()).isEqualTo("보드 상세 정보 생성 중 오류가 발생했습니다.");

                verify(boardValidator).validateGetDetail(command);
                verify(getBoardDetailPort).getBoardDetail(boardId, userId);
        }

        // ==================== EDGE CASES ====================

        @Test
        @DisplayName("활성 보드만 조회할 때 아카이브된 보드가 제외되어야 한다")
        void getUserBoards_activeOnly_shouldExcludeArchivedBoards() {
                // given
                UserId ownerId = new UserId();
                GetUserBoardsCommand command = createValidGetUserBoardsCommand(ownerId, false);
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
                GetUserBoardsCommand command = createValidGetUserBoardsCommand(ownerId, true);
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
                GetUserBoardsCommand command = createValidGetUserBoardsCommand(ownerId, false);

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
package com.boardly.features.dashboard.application.service;

import com.boardly.features.activity.application.port.input.GetActivityQuery;
import com.boardly.features.activity.application.port.output.ActivityResponse;
import com.boardly.features.activity.application.service.ActivityReadService;
import com.boardly.features.board.application.dto.BoardSummaryDto;
import com.boardly.features.board.application.port.input.GetUserBoardsCommand;
import com.boardly.features.board.application.service.BoardQueryService;
import com.boardly.features.board.application.service.BoardPermissionService;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.model.BoardRole;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.dashboard.application.dto.DashboardResponse;
import com.boardly.features.dashboard.application.dto.DashboardStatisticsDto;
import com.boardly.features.dashboard.application.port.input.GetDashboardCommand;
import com.boardly.features.user.application.service.UserFinder;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("DashboardService 테스트")
class DashboardServiceTest {

    private DashboardService dashboardService;

    @Mock
    private BoardQueryService boardQueryService;

    @Mock
    private ActivityReadService activityReadService;

    @Mock
    private UserFinder userFinder;

    @Mock
    private ValidationMessageResolver validationMessageResolver;

    @Mock
    private BoardListRepository boardListRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private BoardPermissionService boardPermissionService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(
                boardQueryService,
                activityReadService,
                userFinder,
                validationMessageResolver,
                boardListRepository,
                cardRepository,
                boardPermissionService);

        // 공통 메시지 모킹 설정
        lenient().when(validationMessageResolver.getMessage("validation.dashboard.command.null"))
                .thenReturn("대시보드 명령이 null입니다");
        lenient().when(validationMessageResolver.getMessage("validation.input.invalid"))
                .thenReturn("입력 데이터가 유효하지 않습니다");
        lenient().when(validationMessageResolver.getMessage("validation.user.id.required"))
                .thenReturn("사용자 ID가 필요합니다");
        lenient().when(validationMessageResolver.getMessage("validation.user.not.found"))
                .thenReturn("사용자를 찾을 수 없습니다");
        lenient().when(validationMessageResolver.getMessage("validation.dashboard.internal.server.error"))
                .thenReturn("대시보드 조회 중 내부 서버 오류가 발생했습니다");
    }

    // ==================== HELPER METHODS ====================

    private UserId createValidUserId() {
        return new UserId("test-user-id");
    }

    private BoardId createValidBoardId() {
        return new BoardId("test-board-id");
    }

    private ListId createValidListId() {
        return new ListId("test-list-id");
    }

    private GetDashboardCommand createValidCommand(UserId userId) {
        return new GetDashboardCommand(userId);
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

    private BoardList createValidBoardList(ListId listId, BoardId boardId) {
        return BoardList.builder()
                .listId(listId)
                .title("테스트 리스트")
                .description("테스트 리스트 설명")
                .position(1)
                .boardId(boardId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private Card createValidCard(ListId listId) {
        return Card.builder()
                .cardId(new com.boardly.features.card.domain.model.CardId("test-card-id"))
                .title("테스트 카드")
                .description("테스트 카드 설명")
                .position(1)
                .listId(listId)
                .createdBy(new UserId("test-user"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private ActivityResponse createValidActivityResponse() {
        return ActivityResponse.builder()
                .id("test-activity-id")
                .type(com.boardly.features.activity.domain.model.ActivityType.BOARD_CREATE)
                .actor(com.boardly.features.activity.application.port.output.ActorResponse.builder()
                        .id("test-user-id")
                        .firstName("홍")
                        .lastName("길동")
                        .profileImageUrl("https://example.com/profile.jpg")
                        .build())
                .timestamp(Instant.now())
                .payload(java.util.Map.of("title", "테스트 보드"))
                .build();
    }

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("성공 케이스")
    class SuccessCases {

        @Test
        @DisplayName("유효한 명령으로 대시보드 조회 시 성공해야 한다")
        void getDashboard_WithValidCommand_ShouldSucceed() {
            // given
            UserId userId = createValidUserId();
            GetDashboardCommand command = createValidCommand(userId);
            BoardId boardId = createValidBoardId();
            ListId listId = createValidListId();

            Board board = createValidBoard(boardId, userId, false);
            BoardList boardList = createValidBoardList(listId, boardId);
            Card card = createValidCard(listId);
            ActivityResponse activity = createValidActivityResponse();

            // Mock 설정
            when(userFinder.checkUserExists(userId)).thenReturn(true);
            when(boardQueryService.getUserBoards(any(GetUserBoardsCommand.class)))
                    .thenReturn(Either.right(List.of(board)));
            when(activityReadService.getActivities(any(GetActivityQuery.class)))
                    .thenReturn(Either
                            .right(com.boardly.features.activity.application.port.output.ActivityListResponse.builder()
                                    .activities(List.of(activity))
                                    .totalCount(1)
                                    .currentPage(0)
                                    .totalPages(1)
                                    .hasNextPage(false)
                                    .hasPreviousPage(false)
                                    .build()));
            when(boardListRepository.findByBoardId(boardId))
                    .thenReturn(List.of(boardList));
            when(cardRepository.findByListIdIn(List.of(listId)))
                    .thenReturn(List.of(card));
            when(boardPermissionService.getUserBoardRole(boardId, userId))
                    .thenReturn(Either.right(BoardRole.OWNER));

            // when
            Either<Failure, DashboardResponse> result = dashboardService.getDashboard(command);

            // then
            assertThat(result.isRight()).isTrue();
            DashboardResponse response = result.get();

            assertThat(response.boards()).hasSize(1);
            assertThat(response.recentActivity()).hasSize(1);
            assertThat(response.statistics()).isNotNull();
            assertThat(response.statistics().totalBoards()).isEqualTo(1);
            assertThat(response.statistics().totalCards()).isEqualTo(1);
            assertThat(response.statistics().starredBoards()).isEqualTo(0);

            // 검증
            verify(userFinder, atLeastOnce()).checkUserExists(userId);
            verify(boardQueryService, atLeastOnce()).getUserBoards(any(GetUserBoardsCommand.class));
            verify(activityReadService, atLeastOnce()).getActivities(any(GetActivityQuery.class));
            verify(boardListRepository, atLeastOnce()).findByBoardId(boardId);
            verify(cardRepository, atLeastOnce()).findByListIdIn(List.of(listId));
            verify(boardPermissionService, atLeastOnce()).getUserBoardRole(boardId, userId);
        }

        @Test
        @DisplayName("스타된 보드가 있는 경우 통계에 반영되어야 한다")
        void getDashboard_WithStarredBoards_ShouldIncludeInStatistics() {
            // given
            UserId userId = createValidUserId();
            GetDashboardCommand command = createValidCommand(userId);
            BoardId boardId1 = createValidBoardId();
            BoardId boardId2 = new BoardId("test-board-id-2");

            Board board1 = createValidBoard(boardId1, userId, true); // 스타된 보드
            Board board2 = createValidBoard(boardId2, userId, false); // 스타되지 않은 보드

            // Mock 설정
            when(userFinder.checkUserExists(userId)).thenReturn(true);
            when(boardQueryService.getUserBoards(any(GetUserBoardsCommand.class)))
                    .thenReturn(Either.right(List.of(board1, board2)));
            when(activityReadService.getActivities(any(GetActivityQuery.class)))
                    .thenReturn(Either
                            .right(com.boardly.features.activity.application.port.output.ActivityListResponse.builder()
                                    .activities(List.of())
                                    .totalCount(0)
                                    .currentPage(0)
                                    .totalPages(1)
                                    .hasNextPage(false)
                                    .hasPreviousPage(false)
                                    .build()));
            when(boardListRepository.findByBoardId(any(BoardId.class)))
                    .thenReturn(List.of());
            when(boardPermissionService.getUserBoardRole(any(BoardId.class), eq(userId)))
                    .thenReturn(Either.right(BoardRole.OWNER));

            // when
            Either<Failure, DashboardResponse> result = dashboardService.getDashboard(command);

            // then
            assertThat(result.isRight()).isTrue();
            DashboardResponse response = result.get();

            assertThat(response.statistics().totalBoards()).isEqualTo(2);
            assertThat(response.statistics().starredBoards()).isEqualTo(1);
            assertThat(response.statistics().totalCards()).isEqualTo(0);
        }

        @Test
        @DisplayName("보드가 없는 경우 빈 목록과 통계를 반환해야 한다")
        void getDashboard_WithNoBoards_ShouldReturnEmptyLists() {
            // given
            UserId userId = createValidUserId();
            GetDashboardCommand command = createValidCommand(userId);

            // Mock 설정
            when(userFinder.checkUserExists(userId)).thenReturn(true);
            when(boardQueryService.getUserBoards(any(GetUserBoardsCommand.class)))
                    .thenReturn(Either.right(List.of()));
            when(activityReadService.getActivities(any(GetActivityQuery.class)))
                    .thenReturn(Either
                            .right(com.boardly.features.activity.application.port.output.ActivityListResponse.builder()
                                    .activities(List.of())
                                    .totalCount(0)
                                    .currentPage(0)
                                    .totalPages(1)
                                    .hasNextPage(false)
                                    .hasPreviousPage(false)
                                    .build()));

            // when
            Either<Failure, DashboardResponse> result = dashboardService.getDashboard(command);

            // then
            assertThat(result.isRight()).isTrue();
            DashboardResponse response = result.get();

            assertThat(response.boards()).isEmpty();
            assertThat(response.recentActivity()).isEmpty();
            assertThat(response.statistics().totalBoards()).isEqualTo(0);
            assertThat(response.statistics().totalCards()).isEqualTo(0);
            assertThat(response.statistics().starredBoards()).isEqualTo(0);
        }
    }

    // ==================== VALIDATION FAILURE CASES ====================

    @Nested
    @DisplayName("검증 실패 케이스")
    class ValidationFailureCases {

        @Test
        @DisplayName("명령이 null인 경우 실패해야 한다")
        void getDashboard_WithNullCommand_ShouldFail() {
            // when
            Either<Failure, DashboardResponse> result = dashboardService.getDashboard(null);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.InputError.class);
            Failure.InputError inputError = (Failure.InputError) failure;
            assertThat(inputError.getErrorCode()).isEqualTo("INVALID_COMMAND");
            assertThat(inputError.getViolations()).hasSize(1);
            assertThat(inputError.getViolations().get(0).field()).isEqualTo("command");

            verify(validationMessageResolver).getMessage("validation.dashboard.command.null");
            verify(validationMessageResolver).getMessage("validation.input.invalid");
        }

        @Test
        @DisplayName("사용자 ID가 null인 경우 실패해야 한다")
        void getDashboard_WithNullUserId_ShouldFail() {
            // given
            // DashboardService에서 userId가 null인 경우를 테스트
            // 실제로는 command.userId()가 null이 아닌데 getId()가 null을 반환하는 경우를 테스트
            GetDashboardCommand command = mock(GetDashboardCommand.class);
            UserId mockUserId = mock(UserId.class);
            when(mockUserId.getId()).thenReturn(null);
            when(command.userId()).thenReturn(mockUserId);

            // when
            Either<Failure, DashboardResponse> result = dashboardService.getDashboard(command);

            // then
            // 실제로는 DashboardService에서 userId.getId()가 null인 경우를 처리하지 않으므로
            // 이 테스트는 현재 구현에서는 실패할 수 있음
            // 향후 DashboardService에서 이 케이스를 처리하도록 개선이 필요함
            assertThat(result.isLeft()).isTrue();
        }
    }

    // ==================== NOT FOUND CASES ====================

    @Nested
    @DisplayName("찾을 수 없음 케이스")
    class NotFoundCases {

        @Test
        @DisplayName("존재하지 않는 사용자로 조회 시 실패해야 한다")
        void getDashboard_WithNonExistentUser_ShouldFail() {
            // given
            UserId userId = createValidUserId();
            GetDashboardCommand command = createValidCommand(userId);

            when(userFinder.checkUserExists(userId)).thenReturn(false);

            // when
            Either<Failure, DashboardResponse> result = dashboardService.getDashboard(command);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.NotFound.class);

            verify(userFinder).checkUserExists(userId);
            verify(validationMessageResolver).getMessage("validation.user.not.found");
        }
    }

    // ==================== DEPENDENCY FAILURE CASES ====================

    @Nested
    @DisplayName("의존성 실패 케이스")
    class DependencyFailureCases {

        @Test
        @DisplayName("보드 조회 실패 시 실패를 반환해야 한다")
        void getDashboard_WhenBoardQueryFails_ShouldReturnFailure() {
            // given
            UserId userId = createValidUserId();
            GetDashboardCommand command = createValidCommand(userId);
            Failure boardQueryFailure = Failure.ofNotFound("보드를 찾을 수 없습니다");

            when(userFinder.checkUserExists(userId)).thenReturn(true);
            when(boardQueryService.getUserBoards(any(GetUserBoardsCommand.class)))
                    .thenReturn(Either.left(boardQueryFailure));

            // when
            Either<Failure, DashboardResponse> result = dashboardService.getDashboard(command);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isEqualTo(boardQueryFailure);

            verify(userFinder).checkUserExists(userId);
            verify(boardQueryService).getUserBoards(any(GetUserBoardsCommand.class));
            verify(activityReadService, never()).getActivities(any(GetActivityQuery.class));
        }

        @Test
        @DisplayName("활동 조회 실패 시 실패를 반환해야 한다")
        void getDashboard_WhenActivityQueryFails_ShouldReturnFailure() {
            // given
            UserId userId = createValidUserId();
            GetDashboardCommand command = createValidCommand(userId);
            BoardId boardId = createValidBoardId();
            Board board = createValidBoard(boardId, userId, false);
            Failure activityQueryFailure = Failure.ofNotFound("활동을 찾을 수 없습니다");

            when(userFinder.checkUserExists(userId)).thenReturn(true);
            when(boardQueryService.getUserBoards(any(GetUserBoardsCommand.class)))
                    .thenReturn(Either.right(List.of(board)));
            when(activityReadService.getActivities(any(GetActivityQuery.class)))
                    .thenReturn(Either.left(activityQueryFailure));

            // when
            Either<Failure, DashboardResponse> result = dashboardService.getDashboard(command);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isEqualTo(activityQueryFailure);

            verify(userFinder).checkUserExists(userId);
            verify(boardQueryService).getUserBoards(any(GetUserBoardsCommand.class));
            verify(activityReadService).getActivities(any(GetActivityQuery.class));
        }
    }

    // ==================== EXCEPTION CASES ====================

    @Nested
    @DisplayName("예외 케이스")
    class ExceptionCases {

        @Test
        @DisplayName("보드 조회 중 예외 발생 시 내부 서버 오류를 반환해야 한다")
        void getDashboard_WhenBoardQueryThrowsException_ShouldReturnInternalServerError() {
            // given
            UserId userId = createValidUserId();
            GetDashboardCommand command = createValidCommand(userId);

            when(userFinder.checkUserExists(userId)).thenReturn(true);
            when(boardQueryService.getUserBoards(any(GetUserBoardsCommand.class)))
                    .thenThrow(new RuntimeException("데이터베이스 연결 오류"));

            // when
            Either<Failure, DashboardResponse> result = dashboardService.getDashboard(command);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.InternalError.class);

            verify(userFinder).checkUserExists(userId);
            verify(boardQueryService).getUserBoards(any(GetUserBoardsCommand.class));
            verify(validationMessageResolver).getMessage("validation.dashboard.internal.server.error");
        }

        @Test
        @DisplayName("활동 조회 중 예외 발생 시 내부 서버 오류를 반환해야 한다")
        void getDashboard_WhenActivityQueryThrowsException_ShouldReturnInternalServerError() {
            // given
            UserId userId = createValidUserId();
            GetDashboardCommand command = createValidCommand(userId);
            BoardId boardId = createValidBoardId();
            Board board = createValidBoard(boardId, userId, false);

            when(userFinder.checkUserExists(userId)).thenReturn(true);
            when(boardQueryService.getUserBoards(any(GetUserBoardsCommand.class)))
                    .thenReturn(Either.right(List.of(board)));
            when(activityReadService.getActivities(any(GetActivityQuery.class)))
                    .thenThrow(new RuntimeException("활동 조회 오류"));

            // when
            Either<Failure, DashboardResponse> result = dashboardService.getDashboard(command);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.InternalError.class);

            verify(userFinder).checkUserExists(userId);
            verify(boardQueryService).getUserBoards(any(GetUserBoardsCommand.class));
            verify(activityReadService).getActivities(any(GetActivityQuery.class));
            verify(validationMessageResolver).getMessage("validation.dashboard.internal.server.error");
        }
    }

    // ==================== BOARD SUMMARY CONVERSION TESTS ====================

    @Nested
    @DisplayName("보드 요약 변환 테스트")
    class BoardSummaryConversionTests {

        @Test
        @DisplayName("보드 요약 정보가 올바르게 변환되어야 한다")
        void convertToBoardSummaryDtos_ShouldConvertCorrectly() {
            // given
            UserId userId = createValidUserId();
            GetDashboardCommand command = createValidCommand(userId);
            BoardId boardId = createValidBoardId();
            ListId listId = createValidListId();

            Board board = createValidBoard(boardId, userId, true);
            BoardList boardList = createValidBoardList(listId, boardId);
            Card card = createValidCard(listId);
            ActivityResponse activity = createValidActivityResponse();

            // Mock 설정
            when(userFinder.checkUserExists(userId)).thenReturn(true);
            when(boardQueryService.getUserBoards(any(GetUserBoardsCommand.class)))
                    .thenReturn(Either.right(List.of(board)));
            when(activityReadService.getActivities(any(GetActivityQuery.class)))
                    .thenReturn(Either
                            .right(com.boardly.features.activity.application.port.output.ActivityListResponse.builder()
                                    .activities(List.of(activity))
                                    .totalCount(1)
                                    .currentPage(0)
                                    .totalPages(1)
                                    .hasNextPage(false)
                                    .hasPreviousPage(false)
                                    .build()));
            when(boardListRepository.findByBoardId(boardId))
                    .thenReturn(List.of(boardList));
            when(cardRepository.findByListIdIn(List.of(listId)))
                    .thenReturn(List.of(card));
            when(boardPermissionService.getUserBoardRole(boardId, userId))
                    .thenReturn(Either.right(BoardRole.OWNER));

            // when
            Either<Failure, DashboardResponse> result = dashboardService.getDashboard(command);

            // then
            assertThat(result.isRight()).isTrue();
            DashboardResponse response = result.get();

            assertThat(response.boards()).hasSize(1);
            BoardSummaryDto boardSummary = response.boards().get(0);

            assertThat(boardSummary.id()).isEqualTo(boardId.getId());
            assertThat(boardSummary.title()).isEqualTo("테스트 보드");
            assertThat(boardSummary.description()).isEqualTo("테스트 보드 설명");
            assertThat(boardSummary.isStarred()).isTrue();
            assertThat(boardSummary.listCount()).isEqualTo(1);
            assertThat(boardSummary.cardCount()).isEqualTo(1);
            assertThat(boardSummary.role()).isEqualTo("owner");
            assertThat(boardSummary.color()).isNotNull();
        }

        @Test
        @DisplayName("보드 역할 조회 실패 시 unknown으로 설정되어야 한다")
        void convertToBoardSummaryDtos_WhenRoleQueryFails_ShouldSetUnknownRole() {
            // given
            UserId userId = createValidUserId();
            GetDashboardCommand command = createValidCommand(userId);
            BoardId boardId = createValidBoardId();

            Board board = createValidBoard(boardId, userId, false);

            // Mock 설정
            when(userFinder.checkUserExists(userId)).thenReturn(true);
            when(boardQueryService.getUserBoards(any(GetUserBoardsCommand.class)))
                    .thenReturn(Either.right(List.of(board)));
            when(activityReadService.getActivities(any(GetActivityQuery.class)))
                    .thenReturn(Either
                            .right(com.boardly.features.activity.application.port.output.ActivityListResponse.builder()
                                    .activities(List.of())
                                    .totalCount(0)
                                    .currentPage(0)
                                    .totalPages(1)
                                    .hasNextPage(false)
                                    .hasPreviousPage(false)
                                    .build()));
            when(boardListRepository.findByBoardId(boardId))
                    .thenReturn(List.of());
            when(boardPermissionService.getUserBoardRole(boardId, userId))
                    .thenReturn(Either.left(Failure.ofNotFound("역할을 찾을 수 없습니다")));

            // when
            Either<Failure, DashboardResponse> result = dashboardService.getDashboard(command);

            // then
            assertThat(result.isRight()).isTrue();
            DashboardResponse response = result.get();

            assertThat(response.boards()).hasSize(1);
            BoardSummaryDto boardSummary = response.boards().get(0);
            assertThat(boardSummary.role()).isEqualTo("unknown");
        }
    }

    // ==================== STATISTICS CALCULATION TESTS ====================

    @Nested
    @DisplayName("통계 계산 테스트")
    class StatisticsCalculationTests {

        @Test
        @DisplayName("여러 보드와 카드가 있는 경우 통계가 올바르게 계산되어야 한다")
        void calculateStatistics_WithMultipleBoardsAndCards_ShouldCalculateCorrectly() {
            // given
            UserId userId = createValidUserId();
            GetDashboardCommand command = createValidCommand(userId);
            BoardId boardId1 = createValidBoardId();
            BoardId boardId2 = new BoardId("test-board-id-2");
            ListId listId1 = createValidListId();
            ListId listId2 = new ListId("test-list-id-2");

            Board board1 = createValidBoard(boardId1, userId, true); // 스타된 보드
            Board board2 = createValidBoard(boardId2, userId, false); // 스타되지 않은 보드

            BoardList boardList1 = createValidBoardList(listId1, boardId1);
            BoardList boardList2 = createValidBoardList(listId2, boardId2);

            Card card1 = createValidCard(listId1);
            Card card2 = createValidCard(listId2);
            Card card3 = createValidCard(listId2); // 두 번째 리스트에 카드 2개

            ActivityResponse activity = createValidActivityResponse();

            // Mock 설정
            when(userFinder.checkUserExists(userId)).thenReturn(true);
            when(boardQueryService.getUserBoards(any(GetUserBoardsCommand.class)))
                    .thenReturn(Either.right(List.of(board1, board2)));
            when(activityReadService.getActivities(any(GetActivityQuery.class)))
                    .thenReturn(Either
                            .right(com.boardly.features.activity.application.port.output.ActivityListResponse.builder()
                                    .activities(List.of(activity))
                                    .totalCount(1)
                                    .currentPage(0)
                                    .totalPages(1)
                                    .hasNextPage(false)
                                    .hasPreviousPage(false)
                                    .build()));
            when(boardListRepository.findByBoardId(boardId1))
                    .thenReturn(List.of(boardList1));
            when(boardListRepository.findByBoardId(boardId2))
                    .thenReturn(List.of(boardList2));
            when(cardRepository.findByListIdIn(Arrays.asList(listId1, listId2)))
                    .thenReturn(List.of(card1, card2, card3));
            when(boardPermissionService.getUserBoardRole(any(BoardId.class), eq(userId)))
                    .thenReturn(Either.right(BoardRole.OWNER));

            // when
            Either<Failure, DashboardResponse> result = dashboardService.getDashboard(command);

            // then
            assertThat(result.isRight()).isTrue();
            DashboardResponse response = result.get();
            DashboardStatisticsDto statistics = response.statistics();

            assertThat(statistics.totalBoards()).isEqualTo(2);
            assertThat(statistics.starredBoards()).isEqualTo(1);
            assertThat(statistics.totalCards()).isEqualTo(3);
            assertThat(statistics.archivedBoards()).isEqualTo(0);
        }

        @Test
        @DisplayName("카드 계산 중 예외 발생 시 0을 반환해야 한다")
        void calculateTotalCards_WhenExceptionOccurs_ShouldReturnZero() {
            // given
            UserId userId = createValidUserId();
            GetDashboardCommand command = createValidCommand(userId);
            BoardId boardId = createValidBoardId();

            Board board = createValidBoard(boardId, userId, false);

            // Mock 설정
            when(userFinder.checkUserExists(userId)).thenReturn(true);
            when(boardQueryService.getUserBoards(any(GetUserBoardsCommand.class)))
                    .thenReturn(Either.right(List.of(board)));
            when(activityReadService.getActivities(any(GetActivityQuery.class)))
                    .thenReturn(Either
                            .right(com.boardly.features.activity.application.port.output.ActivityListResponse.builder()
                                    .activities(List.of())
                                    .totalCount(0)
                                    .currentPage(0)
                                    .totalPages(1)
                                    .hasNextPage(false)
                                    .hasPreviousPage(false)
                                    .build()));
            when(boardListRepository.findByBoardId(boardId))
                    .thenThrow(new RuntimeException("리스트 조회 오류"));
            when(boardPermissionService.getUserBoardRole(any(BoardId.class), eq(userId)))
                    .thenReturn(Either.right(BoardRole.OWNER));

            // when
            Either<Failure, DashboardResponse> result = dashboardService.getDashboard(command);

            // then
            assertThat(result.isRight()).isTrue();
            DashboardResponse response = result.get();
            DashboardStatisticsDto statistics = response.statistics();

            assertThat(statistics.totalBoards()).isEqualTo(1);
            assertThat(statistics.totalCards()).isEqualTo(0);
        }
    }
}
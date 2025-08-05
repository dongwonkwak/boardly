package com.boardly.features.dashboard.application.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardly.features.activity.application.port.input.GetActivityQuery;
import com.boardly.features.activity.application.port.output.ActivityResponse;
import com.boardly.features.activity.application.service.ActivityReadService;
import com.boardly.features.board.application.dto.BoardSummaryDto;
import com.boardly.features.board.application.port.input.GetUserBoardsCommand;
import com.boardly.features.board.application.service.BoardPermissionService;
import com.boardly.features.board.application.service.BoardQueryService;
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
import com.boardly.features.dashboard.application.usecase.GetDashboardUseCase;
import com.boardly.features.user.application.service.UserFinder;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 대시보드 서비스
 * 
 * <p>
 * 대시보드 조회 관련 작업을 담당하는 통합 서비스입니다.
 * 보드 목록, 최근 활동, 통계 정보를 조합하여 대시보드 데이터를 제공합니다.
 * </p>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardService implements GetDashboardUseCase {

    private final BoardQueryService boardQueryService;
    private final ActivityReadService activityReadService;
    private final UserFinder userFinder;
    private final ValidationMessageResolver validationMessageResolver;
    private final BoardListRepository boardListRepository;
    private final CardRepository cardRepository;
    private final BoardPermissionService boardPermissionService;

    @Override
    public Either<Failure, DashboardResponse> getDashboard(GetDashboardCommand command) {
        if (command == null) {
            var violation = Failure.FieldViolation.builder()
                    .field("command")
                    .message(validationMessageResolver.getMessage("validation.dashboard.command.null"))
                    .rejectedValue(null)
                    .build();
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.input.invalid"),
                    "INVALID_COMMAND",
                    List.of(violation)));
        }

        log.info("대시보드 조회 시작: userId={}", command.userId().getId());

        // 1. 입력 검증
        if (command.userId() == null) {
            var violation = Failure.FieldViolation.builder()
                    .field("userId")
                    .message(validationMessageResolver.getMessage("validation.user.id.required"))
                    .rejectedValue(null)
                    .build();
            log.warn("대시보드 조회 검증 실패: userId=null");
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.of(violation)));
        }

        // 2. 사용자 존재 확인
        if (!userFinder.checkUserExists(command.userId())) {
            return Either.left(Failure
                    .ofNotFound(validationMessageResolver.getMessage("validation.user.not.found")));
        }

        // 3. 대시보드 데이터 조회
        return Try.of(() -> {
            // 3-1. 보드 목록 조회
            Either<Failure, List<Board>> boardsResult = boardQueryService.getUserBoards(
                    GetUserBoardsCommand.activeOnly(command.userId()));

            if (boardsResult.isLeft()) {
                return Either.<Failure, DashboardResponse>left(boardsResult.getLeft());
            }

            List<Board> boards = boardsResult.get();

            // 3-2. 최근 활동 조회
            Either<Failure, List<ActivityResponse>> activitiesResult = getRecentActivities(command.userId());
            if (activitiesResult.isLeft()) {
                return Either.<Failure, DashboardResponse>left(activitiesResult.getLeft());
            }

            List<ActivityResponse> activities = activitiesResult.get();

            // 3-3. 통계 정보 계산
            DashboardStatisticsDto statistics = calculateStatistics(boards);

            // 3-4. 응답 생성
            DashboardResponse response = DashboardResponse.builder()
                    .boards(convertToBoardSummaryDtos(boards))
                    .recentActivity(activities)
                    .statistics(statistics)
                    .build();

            log.info("대시보드 조회 완료: userId={}, boardCount={}, activityCount={}",
                    command.userId().getId(), boards.size(), activities.size());

            return Either.<Failure, DashboardResponse>right(response);
        })
                .fold(
                        throwable -> {
                            log.error("대시보드 조회 중 예외 발생: userId={}, error={}",
                                    command.userId().getId(), throwable.getMessage(), throwable);
                            return Either.left(Failure.ofInternalServerError(
                                    validationMessageResolver
                                            .getMessage("validation.dashboard.internal.server.error")));
                        },
                        result -> result);
    }

    /**
     * 최근 활동 목록을 조회합니다.
     */
    private Either<Failure, List<ActivityResponse>> getRecentActivities(
            com.boardly.features.user.domain.model.UserId userId) {
        GetActivityQuery query = GetActivityQuery.forUserWithPagination(userId, 0, 20);

        return activityReadService.getActivities(query)
                .map(response -> response.activities());
    }

    /**
     * 통계 정보를 계산합니다.
     */
    private DashboardStatisticsDto calculateStatistics(List<Board> boards) {
        int totalBoards = boards.size();
        int totalCards = calculateTotalCards(boards);
        int starredBoards = (int) boards.stream()
                .filter(Board::isStarred)
                .count();
        int archivedBoards = 0; // 현재는 아카이브된 보드는 조회하지 않음

        return DashboardStatisticsDto.builder()
                .totalBoards(totalBoards)
                .totalCards(totalCards)
                .starredBoards(starredBoards)
                .archivedBoards(archivedBoards)
                .build();
    }

    /**
     * 모든 보드의 카드 수 합계를 계산합니다.
     */
    private int calculateTotalCards(List<Board> boards) {
        if (boards.isEmpty()) {
            return 0;
        }

        try {
            // 1. 모든 보드 ID 수집
            List<BoardId> boardIds = boards.stream()
                    .map(Board::getBoardId)
                    .collect(Collectors.toList());

            // 2. 모든 보드의 리스트들을 한 번에 조회
            List<BoardList> allBoardLists = new ArrayList<>();
            for (BoardId boardId : boardIds) {
                List<BoardList> boardLists = boardListRepository.findByBoardId(boardId);
                allBoardLists.addAll(boardLists);
            }

            if (allBoardLists.isEmpty()) {
                return 0;
            }

            // 3. 모든 리스트의 카드들을 한 번에 조회
            List<ListId> listIds = allBoardLists.stream()
                    .map(BoardList::getListId)
                    .collect(Collectors.toList());

            List<Card> allCards = cardRepository.findByListIdIn(listIds);

            log.info("전체 카드 수 계산 완료: boardCount={}, listCount={}, totalCards={}",
                    boards.size(), allBoardLists.size(), allCards.size());

            return allCards.size();

        } catch (Exception e) {
            log.error("전체 카드 수 계산 중 오류 발생: error={}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Board 엔티티를 BoardSummaryDto로 변환합니다.
     */
    private List<BoardSummaryDto> convertToBoardSummaryDtos(List<Board> boards) {
        // 모든 보드의 리스트와 카드 데이터를 미리 로드
        Map<BoardId, List<BoardList>> boardListsMap = loadBoardListsMap(boards);
        Map<ListId, List<Card>> listCardsMap = loadListCardsMap(boardListsMap);

        // 모든 보드의 사용자 역할을 미리 로드
        Map<BoardId, String> boardRoleMap = loadBoardRoleMap(boards);

        return boards.stream()
                .map(board -> {
                    // 보드별 리스트 수와 카드 수 계산
                    List<BoardList> boardLists = boardListsMap.getOrDefault(board.getBoardId(), List.of());
                    int listCount = boardLists.size();
                    int cardCount = calculateBoardCardCountFromMap(boardLists, listCardsMap);

                    // 사용자의 보드 역할 조회
                    String userRole = boardRoleMap.getOrDefault(board.getBoardId(), "unknown");

                    return BoardSummaryDto.builder()
                            .id(board.getBoardId().getId())
                            .title(board.getTitle())
                            .description(board.getDescription())
                            .createdAt(board.getCreatedAt())
                            .listCount(listCount)
                            .cardCount(cardCount)
                            .isStarred(board.isStarred())
                            .color(getColorClass(board.getBoardId().getId()))
                            .role(userRole)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 모든 보드의 리스트 데이터를 맵으로 로드합니다.
     */
    private Map<BoardId, List<BoardList>> loadBoardListsMap(
            List<Board> boards) {
        Map<BoardId, List<BoardList>> boardListsMap = new HashMap<>();

        for (Board board : boards) {
            try {
                List<BoardList> boardLists = boardListRepository.findByBoardId(board.getBoardId());
                boardListsMap.put(board.getBoardId(), boardLists);
            } catch (Exception e) {
                log.warn("보드 리스트 조회 중 오류 발생: boardId={}, error={}",
                        board.getBoardId().getId(), e.getMessage());
                boardListsMap.put(board.getBoardId(), List.of());
            }
        }

        return boardListsMap;
    }

    /**
     * 모든 리스트의 카드 데이터를 맵으로 로드합니다.
     */
    private Map<ListId, List<Card>> loadListCardsMap(
            Map<BoardId, List<BoardList>> boardListsMap) {

        // 모든 리스트 ID 수집
        List<ListId> allListIds = boardListsMap.values().stream()
                .flatMap(List::stream)
                .map(BoardList::getListId)
                .collect(Collectors.toList());

        if (allListIds.isEmpty()) {
            return Map.of();
        }

        try {
            // 모든 카드를 한 번에 조회
            List<Card> allCards = cardRepository.findByListIdIn(allListIds);

            // 리스트 ID별로 카드들을 그룹화
            return allCards.stream()
                    .collect(Collectors.groupingBy(Card::getListId));

        } catch (Exception e) {
            log.warn("리스트 카드 조회 중 오류 발생: error={}", e.getMessage());
            return Map.of();
        }
    }

    /**
     * 맵에서 보드의 카드 수를 계산합니다.
     */
    private int calculateBoardCardCountFromMap(List<BoardList> boardLists,
            Map<ListId, List<Card>> listCardsMap) {

        return boardLists.stream()
                .mapToInt(boardList -> {
                    List<Card> cards = listCardsMap.getOrDefault(boardList.getListId(), List.of());
                    return cards.size();
                })
                .sum();
    }

    /**
     * 모든 보드의 사용자 역할을 맵으로 로드합니다.
     */
    private Map<BoardId, String> loadBoardRoleMap(List<Board> boards) {
        Map<BoardId, String> boardRoleMap = new HashMap<>();

        for (Board board : boards) {
            try {
                String role = getUserBoardRole(board.getBoardId(), board.getOwnerId());
                boardRoleMap.put(board.getBoardId(), role);
            } catch (Exception e) {
                log.warn("보드 역할 조회 중 오류 발생: boardId={}, error={}",
                        board.getBoardId().getId(), e.getMessage());
                boardRoleMap.put(board.getBoardId(), "unknown");
            }
        }

        return boardRoleMap;
    }

    /**
     * 사용자의 보드 역할을 조회합니다.
     */
    private String getUserBoardRole(BoardId boardId, com.boardly.features.user.domain.model.UserId userId) {
        try {
            Either<Failure, BoardRole> roleResult = boardPermissionService.getUserBoardRole(boardId, userId);

            if (roleResult.isLeft()) {
                log.warn("보드 역할 조회 실패: boardId={}, userId={}, error={}",
                        boardId.getId(), userId.getId(), roleResult.getLeft().getMessage());
                return "unknown"; // 역할 조회 실패 시 기본값
            }

            BoardRole role = roleResult.get();
            if (role == null) {
                return "owner"; // 소유자인 경우
            }

            return role.name().toLowerCase(); // ADMIN -> admin, MEMBER -> member 등

        } catch (Exception e) {
            log.warn("보드 역할 조회 중 예외 발생: boardId={}, userId={}, error={}",
                    boardId.getId(), userId.getId(), e.getMessage());
            return "unknown";
        }
    }

    private String getColorClass(String boardId) {
        // 보드 ID 해시를 기반으로 색상 클래스 생성
        String[] colors = {
                "blue-purple", "green-teal", "orange-red",
                "purple-pink", "gray-slate", "indigo-blue"
        };
        int index = Math.abs(boardId.hashCode()) % colors.length;
        return colors[index];
    }
}
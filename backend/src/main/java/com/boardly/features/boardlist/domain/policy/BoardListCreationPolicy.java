package com.boardly.features.boardlist.domain.policy;

import org.springframework.stereotype.Component;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 보드 리스트 생성 정책
 * 
 * <p>
 * 보드 리스트 생성과 관련된 비즈니스 규칙을 정의하고 검증합니다.
 * 보드당 리스트 개수 제한, 생성 권한 등의 정책을 관리합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BoardListCreationPolicy {

    private final BoardListRepository boardListRepository;
    private final BoardListPolicyConfig policyConfig;

    /**
     * 보드 리스트 생성이 가능한지 검증합니다.
     */
    public Either<Failure, Void> canCreateBoardList(BoardId boardId) {
        log.debug("보드 리스트 생성 정책 검증 시작: boardId={}", boardId.getId());

        return checkListCountLimit(boardId)
                .peek(v -> log.debug("보드 리스트 생성 정책 검증 성공: boardId={}", boardId.getId()));
    }

    /**
     * 보드당 리스트 개수 제한을 확인합니다.
     */
    private Either<Failure, Void> checkListCountLimit(BoardId boardId) {
        long currentCount = boardListRepository.countByBoardId(boardId);
        int maxLists = policyConfig.getMaxListsPerBoard();

        log.debug("현재 리스트 개수 확인: boardId={}, currentCount={}, maxCount={}",
                boardId.getId(), currentCount, maxLists);

        if (currentCount >= maxLists) {
            log.warn("보드 리스트 개수 제한 초과: boardId={}, currentCount={}, maxCount={}",
                    boardId.getId(), currentCount, maxLists);
            return Either.left(Failure.ofPermissionDenied(
                    String.format("보드당 최대 %d개의 리스트만 생성할 수 있습니다. (현재: %d개)",
                            maxLists, currentCount)));
        }

        return Either.right(null);
    }

    /**
     * 최대 리스트 개수를 반환합니다.
     */
    public int getMaxListsPerBoard() {
        return policyConfig.getMaxListsPerBoard();
    }

    /**
     * 권장 리스트 개수를 반환합니다.
     */
    public int getRecommendedListsPerBoard() {
        return policyConfig.getRecommendedListsPerBoard();
    }

    /**
     * 경고 임계값을 반환합니다.
     */
    public int getWarningThreshold() {
        return policyConfig.getWarningThreshold();
    }

    /**
     * 추가 생성 가능한 리스트 개수를 반환합니다.
     */
    public long getAvailableListSlots(BoardId boardId) {
        long currentCount = boardListRepository.countByBoardId(boardId);
        int maxLists = policyConfig.getMaxListsPerBoard();
        return Math.max(0, maxLists - currentCount);
    }

    /**
     * 현재 리스트 개수에 대한 상태를 반환합니다.
     */
    public ListCountStatus getStatus(BoardId boardId) {
        long currentCount = boardListRepository.countByBoardId(boardId);
        int maxLists = policyConfig.getMaxListsPerBoard();
        int warningThreshold = policyConfig.getWarningThreshold();
        int recommendedLists = policyConfig.getRecommendedListsPerBoard();

        if (currentCount >= maxLists) {
            return ListCountStatus.LIMIT_REACHED;
        } else if (currentCount >= warningThreshold) {
            return ListCountStatus.WARNING;
        } else if (currentCount > recommendedLists) {
            return ListCountStatus.ABOVE_RECOMMENDED;
        } else {
            return ListCountStatus.NORMAL;
        }
    }

    /**
     * 경고 임계값을 초과했는지 확인합니다.
     */
    public boolean shouldShowWarning(BoardId boardId) {
        long currentCount = boardListRepository.countByBoardId(boardId);
        return currentCount >= policyConfig.getWarningThreshold();
    }

    /**
     * 권장 개수를 초과했는지 확인합니다.
     */
    public boolean exceedsRecommended(BoardId boardId) {
        long currentCount = boardListRepository.countByBoardId(boardId);
        return currentCount > policyConfig.getRecommendedListsPerBoard();
    }

    /**
     * 기본 최대 리스트 개수를 반환합니다.
     */
    public static int getDefaultMaxListsPerBoard() {
        return BoardListPolicyConfig.Defaults.MAX_LISTS_PER_BOARD;
    }

    /**
     * 리스트 개수 상태를 나타내는 열거형
     */
    public enum ListCountStatus {
        /**
         * 정상 범위 (권장 개수 이하)
         */
        NORMAL("정상", "리스트 개수가 적절합니다."),

        /**
         * 권장 개수 초과 (하지만 경고 임계값 미만)
         */
        ABOVE_RECOMMENDED("권장 초과", "권장 개수를 초과했습니다. 리스트를 정리하는 것을 고려해보세요."),

        /**
         * 경고 임계값 초과 (하지만 최대 개수 미만)
         */
        WARNING("경고", "리스트가 너무 많습니다. 성능에 영향을 줄 수 있습니다."),

        /**
         * 최대 개수 도달 (더 이상 생성 불가)
         */
        LIMIT_REACHED("제한 도달", "최대 리스트 개수에 도달했습니다. 새 리스트를 생성하려면 기존 리스트를 삭제해주세요.");

        private final String displayName;
        private final String message;

        ListCountStatus(String displayName, String message) {
            this.displayName = displayName;
            this.message = message;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getMessage() {
            return message;
        }

        /**
         * 리스트 생성이 가능한 상태인지 확인합니다.
         */
        public boolean canCreateList() {
            return this != LIMIT_REACHED;
        }

        /**
         * 사용자에게 알림을 표시해야 하는 상태인지 확인합니다.
         */
        public boolean requiresNotification() {
            return this == WARNING || this == LIMIT_REACHED;
        }
    }
}
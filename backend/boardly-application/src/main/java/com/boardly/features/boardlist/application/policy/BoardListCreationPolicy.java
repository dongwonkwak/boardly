package com.boardly.features.boardlist.application.policy;

import com.boardly.features.boardlist.domain.config.BoardListPolicyConfig;
import com.boardly.features.boardlist.domain.port.BoardListRepository;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.common.value.BoardId;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoardListCreationPolicy {

    private final BoardListRepository boardListRepository;
    private final BoardListPolicyConfig policyConfig;

    public Either<Failure, Void> canCreateBoardList(BoardId boardId) {
        return checkListCountLimit(boardId);
    }

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

    public enum ListCountStatus {
        NORMAL("정상", "리스트 개수가 적절합니다."),
        ABOVE_RECOMMENDED("권장 초과", "권장 개수를 초과했습니다. 리스트를 정리하는 것을 고려해보세요."),
        WARNING("경고", "리스트가 너무 많습니다. 성능에 영향을 줄 수 있습니다."),
        LIMIT_REACHED("제한 도달", "최대 리스트 개수에 도달했습니다. 새 리스트를 생성하려면 기존 리스트를 삭제해주세요.");

        private final String displayName;
        private final String message;

        ListCountStatus(String displayName, String message) {
            this.displayName = displayName;
            this.message = message;
        }

        public String getDisplayName() { return displayName; }
        public String getMessage() { return message; }
        public boolean requiresNotification() { return this == WARNING || this == LIMIT_REACHED; }
    }

    private Either<Failure, Void> checkListCountLimit(BoardId boardId) {
        long currentCount = boardListRepository.countByBoardId(boardId);
        int maxLists = policyConfig.getMaxListsPerBoard();
        if (currentCount >= maxLists) {
            return Either.left(Failure.ofPermissionDenied(
                String.format("보드당 최대 %d개의 리스트만 생성할 수 있습니다. (현재: %d개)", maxLists, currentCount)
            ));
        }
        return Either.right(null);
    }
}

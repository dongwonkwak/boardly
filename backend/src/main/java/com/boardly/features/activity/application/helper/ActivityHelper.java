package com.boardly.features.activity.application.helper;

import java.util.Map;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boardly.features.activity.application.port.input.CreateActivityCommand;
import com.boardly.features.activity.application.usecase.CreateActivityUseCase;
import com.boardly.features.activity.domain.model.ActivityType;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.user.domain.model.UserId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityHelper {

    private final CreateActivityUseCase createActivityUseCase;

    /**
     * 활동 로그 비동기 생성
     */
    @Async
    public void logActivity(ActivityType type, UserId actorId, Map<String, Object> payload,
            BoardId boardId, ListId listId, CardId cardId) {

        try {
            var command = CreateActivityCommand.of(type, actorId, payload, boardId, listId, cardId);
            createActivityUseCase.createActivity(command)
                    .peek(activity -> log.debug("Activity log created: {}", activity.getId()))
                    .peekLeft(failure -> log.error("Failed to create activity log: {}", failure.getMessage()));
        } catch (Exception e) {
            log.error("Failed to log activity: {}", e.getMessage(), e);
        }
    }

    /**
     * 활동 로그 동기 생성
     */
    public void logActivitySync(ActivityType type, UserId actorId, Map<String, Object> payload,
            BoardId boardId, ListId listId, CardId cardId) {
        try {
            var command = CreateActivityCommand.of(type, actorId, payload, boardId, listId, cardId);
            createActivityUseCase.createActivity(command)
                    .peek(activity -> log.debug("Activity log created: {}", activity.getId()))
                    .peekLeft(failure -> log.error("Failed to create activity log: {}", failure.getMessage()));
        } catch (Exception e) {
            log.error("Failed to log activity: {}", e.getMessage(), e);
        }
    }

    // 자주 사용되는 활동 로그 생성 메서드

    /**
     * 카드 생성 활동 로그
     */
    @Async
    public void logCardCreate(UserId actorId, String listName, String cardTitle,
            BoardId boardId, ListId listId, CardId cardId) {
        var payload = Map.<String, Object>of(
                "listName", listName,
                "cardTitle", cardTitle,
                "listId", listId.getId(),
                "cardId", cardId.getId());

        logActivity(ActivityType.CARD_CREATE, actorId, payload, boardId, listId, cardId);
    }

    /**
     * 카드 이동 활동 로그
     */
    @Async
    public void logCardMove(UserId actorId, String cardTitle, String sourceListName, String destListName,
            BoardId boardId, ListId sourceListId, ListId destListId, CardId cardId) {
        var payload = Map.<String, Object>of(
                "cardTitle", cardTitle,
                "sourceListName", sourceListName,
                "destListName", destListName,
                "cardId", cardId.getId(),
                "sourceListId", sourceListId.getId(),
                "destListId", destListId.getId());

        logActivity(ActivityType.CARD_MOVE, actorId, payload, boardId, destListId, cardId);
    }

    /**
     * 리스트 생성 활동 로그
     */
    @Async
    public void logListCreate(UserId actorId, String listName, String boardName,
            BoardId boardId, ListId listId) {
        var payload = Map.<String, Object>of(
                "listName", listName,
                "listId", listId.getId(),
                "boardName", boardName);

        logActivity(ActivityType.LIST_CREATE, actorId, payload, boardId, listId, null);
    }

    /**
     * 보드 생성 활동 로그
     */
    @Async
    public void logBoardCreate(UserId actorId, String boardName, BoardId boardId) {
        var payload = Map.<String, Object>of(
                "boardName", boardName,
                "boardId", boardId.getId());

        logActivity(ActivityType.BOARD_CREATE, actorId, payload, boardId, null, null);
    }

    /**
     * 카드 관련 활동 로그 (카드 ID 기반)
     */
    @Async
    public void logCardActivity(ActivityType type, UserId actorId, Map<String, Object> payload,
            BoardId boardId, ListId listId, CardId cardId) {
        if (!isCardActivityType(type)) {
            log.warn("Invalid card activity type: {}", type);
            return;
        }
        logActivity(type, actorId, payload, boardId, listId, cardId);
    }

    /**
     * 리스트 관련 활동 로그
     */
    @Async
    public void logListActivity(ActivityType type, UserId actorId, Map<String, Object> payload,
            BoardId boardId, ListId listId) {
        if (!isListActivityType(type)) {
            log.warn("Invalid list activity type: {}", type);
            return;
        }
        logActivity(type, actorId, payload, boardId, listId, null);
    }

    /**
     * 보드 관련 활동 로그
     */
    @Async
    public void logBoardActivity(ActivityType type, UserId actorId, Map<String, Object> payload,
            BoardId boardId) {
        if (!isBoardActivityType(type)) {
            log.warn("Invalid board activity type: {}", type);
            return;
        }
        logActivity(type, actorId, payload, boardId, null, null);
    }

    /**
     * 사용자 관련 활동 로그
     */
    @Async
    public void logUserActivity(ActivityType type, UserId actorId, Map<String, Object> payload) {
        if (!isUserActivityType(type)) {
            log.warn("Invalid user activity type: {}", type);
            return;
        }
        logActivity(type, actorId, payload, null, null, null);
    }

    // =================================================================
    // 🛡️ 검증 메서드들
    // =================================================================

    boolean isCardActivityType(ActivityType type) {
        return type.name().startsWith("CARD_");
    }

    boolean isListActivityType(ActivityType type) {
        return type.name().startsWith("LIST_");
    }

    boolean isBoardActivityType(ActivityType type) {
        return type.name().startsWith("BOARD_");
    }

    boolean isUserActivityType(ActivityType type) {
        return type.name().startsWith("USER_");
    }
}

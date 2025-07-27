package com.boardly.features.activity.application.port.input;

import java.util.Map;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.activity.domain.model.ActivityType;
import com.boardly.features.user.domain.model.UserId;

/**
 * 활동 생성 커맨드
 * 
 * @param type    활동 유형
 * @param actorId 활동 생성자 ID
 * @param payload 활동 데이터
 * @param boardId 보드 ID (선택)
 * @param listId  리스트 ID (선택)
 * @param cardId  카드 ID (선택)
 */
public record CreateActivityCommand(
        ActivityType type,
        UserId actorId,
        Map<String, Object> payload,
        String boardName,
        BoardId boardId, // optional
        ListId listId, // optional
        CardId cardId // optional
) {

    public static CreateActivityCommand of(
            ActivityType type,
            UserId actorId,
            Map<String, Object> payload,
            String boardName,
            BoardId boardId,
            ListId listId,
            CardId cardId) {
        return new CreateActivityCommand(type, actorId, payload, boardName, boardId, listId, cardId);
    }

    public static CreateActivityCommand forCard(
            ActivityType type,
            UserId actorId,
            Map<String, Object> payload,
            String boardName,
            BoardId boardId,
            ListId listId,
            CardId cardId) {
        return new CreateActivityCommand(type, actorId, payload, boardName, boardId, listId, cardId);
    }

    public static CreateActivityCommand forList(
            ActivityType type,
            UserId actorId,
            Map<String, Object> payload,
            String boardName,
            BoardId boardId,
            ListId listId) {
        return new CreateActivityCommand(type, actorId, payload, boardName, boardId, listId, null);
    }

    public static CreateActivityCommand forBoard(
            ActivityType type,
            UserId actorId,
            Map<String, Object> payload,
            String boardName,
            BoardId boardId) {
        return new CreateActivityCommand(type, actorId, payload, boardName, boardId, null, null);
    }

    public static CreateActivityCommand forUser(
            ActivityType type,
            UserId actorId,
            Map<String, Object> payload,
            String boardName,
            UserId userId) {
        return new CreateActivityCommand(type, actorId, payload, boardName, null, null, null);
    }
}

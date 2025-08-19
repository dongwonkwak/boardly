package com.boardly.features.activity.domain;

import java.time.Instant;

import com.boardly.shared.common.value.ActivityId;
import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.CardId;
import com.boardly.shared.common.value.ListId;
import com.boardly.shared.common.value.WorkspaceId;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
public class Activity {

    @NonNull
    ActivityId id;

    @NonNull
    ActivityType type;

    @NonNull
    Actor actor;

    @NonNull
    Instant timestamp;

    @NonNull
    Payload payload;

    @NonNull
    WorkspaceId workspaceId;

    String boardName;

    // 선택적 엔티티 ID들 (활동이 연관된 엔티티)
    BoardId boardId;
    ListId listId;
    CardId cardId;

    @Builder
    private Activity(
            ActivityId id,
            ActivityType type,
            Actor actor,
            Instant timestamp,
            Payload payload,
            WorkspaceId workspaceId,
            String boardName,
            BoardId boardId,
            ListId listId,
            CardId cardId) {
        this.id = id;
        this.type = type;
        this.actor = actor;
        this.timestamp = timestamp;
        this.payload = payload;
        this.workspaceId = workspaceId;
        this.boardName = boardName;
        this.boardId = boardId;
        this.listId = listId;
        this.cardId = cardId;
    }

    public static Activity create(
            ActivityType type,
            Actor actor,
            Payload payload,
            WorkspaceId workspaceId,
            String boardName,
            BoardId boardId,
            ListId listId,
            CardId cardId) {
        return Activity.builder()
                .id(new ActivityId())
                .type(type)
                .actor(actor)
                .timestamp(Instant.now())
                .payload(payload)
                .workspaceId(workspaceId)
                .boardName(boardName)
                .boardId(boardId)
                .listId(listId)
                .cardId(cardId)
                .build();
    }
}

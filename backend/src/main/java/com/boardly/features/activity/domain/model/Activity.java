package com.boardly.features.activity.domain.model;

import java.time.Instant;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.card.domain.model.CardId;

import io.micrometer.common.lang.NonNull;
import lombok.Builder;
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

    // 선택적 엔티티 ID들 (활동이 연관된 엔티티)
    BoardId boardId;
    ListId listId;
    CardId cardId;

    @Builder
    private Activity(ActivityId id, ActivityType type, Actor actor, Instant timestamp, Payload payload, BoardId boardId,
            ListId listId, CardId cardId) {
        this.id = id;
        this.type = type;
        this.actor = actor;
        this.timestamp = timestamp;
        this.payload = payload;
        this.boardId = boardId;
        this.listId = listId;
        this.cardId = cardId;
    }

    public static Activity create(
            ActivityType type,
            Actor actor,
            Payload payload,
            BoardId boardId,
            ListId listId,
            CardId cardId) {
        return Activity.builder()
                .id(new ActivityId())
                .type(type)
                .actor(actor)
                .timestamp(Instant.now())
                .payload(payload)
                .boardId(boardId)
                .listId(listId)
                .cardId(cardId)
                .build();
    }
}

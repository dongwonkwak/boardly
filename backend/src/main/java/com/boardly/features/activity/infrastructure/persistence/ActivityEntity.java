package com.boardly.features.activity.infrastructure.persistence;

import java.time.Instant;
import java.util.Map;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.boardly.features.activity.domain.model.Activity;
import com.boardly.features.activity.domain.model.ActivityId;
import com.boardly.features.activity.domain.model.ActivityType;
import com.boardly.features.activity.domain.model.Actor;
import com.boardly.features.activity.domain.model.Payload;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.card.domain.model.CardId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_activity", indexes = {
        @Index(name = "idx_activity_board_id", columnList = "board_id"),
        @Index(name = "idx_activity_actor_id", columnList = "actor_id"),
        @Index(name = "idx_activity_timestamp", columnList = "created_at"),
        @Index(name = "idx_activity_board_timestamp", columnList = "board_id, created_at")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ActivityEntity {
    @Id
    @Column(name = "activity_id", nullable = false, length = 50)
    private String activityId; // 활동 ID

    @Column(name = "actor_id", nullable = false, length = 50)
    private String actorId; // 활동 생성자 ID

    @Column(name = "board_id", length = 50)
    private String boardId; // 보드 ID

    @Column(name = "list_id", length = 50)
    private String listId; // 리스트 ID

    @Column(name = "card_id", length = 50)
    private String cardId; // 카드 ID

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 50)
    private ActivityType activityType; // 활동 유형

    @Column(name = "actor_first_name", nullable = false, length = 50)
    private String actorFirstName; // 활동 생성자 이름

    @Column(name = "actor_last_name", nullable = false, length = 50)
    private String actorLastName;

    @Column(name = "actor_profile_image_url", nullable = true, length = 500)
    private String actorProfileImageUrl;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ActivityEntity fromDomainEntity(Activity activity) {
        try {
            String payloadJson = objectMapper.writeValueAsString(activity.getPayload().getData());

            return ActivityEntity.builder()
                    .activityId(activity.getId().getId())
                    .actorId(activity.getActor().getId())
                    .boardId(activity.getBoardId() != null ? activity.getBoardId().getId() : null)
                    .listId(activity.getListId() != null ? activity.getListId().getId() : null)
                    .cardId(activity.getCardId() != null ? activity.getCardId().getId() : null)
                    .activityType(activity.getType())
                    .actorFirstName(activity.getActor().getFirstName())
                    .actorLastName(activity.getActor().getLastName())
                    .actorProfileImageUrl(activity.getActor().getProfileImageUrl())
                    .payload(payloadJson)
                    .createdAt(activity.getTimestamp())
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }
    }

    public Activity toDomainEntity() {
        try {
            Map<String, Object> payloadData = objectMapper.readValue(
                    payload,
                    new TypeReference<Map<String, Object>>() {
                    });

            var actor = Actor.of(actorId, actorFirstName, actorLastName, actorProfileImageUrl);
            var payloadObj = new Payload(payloadData);

            return Activity.builder()
                    .id(new ActivityId(activityId))
                    .type(activityType)
                    .actor(actor)
                    .timestamp(getCreatedAt())
                    .payload(payloadObj)
                    .boardId(boardId != null ? new BoardId(boardId) : null)
                    .listId(listId != null ? new ListId(listId) : null)
                    .cardId(cardId != null ? new CardId(cardId) : null)
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize payload", e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        ActivityEntity that = (ActivityEntity) obj;
        return activityId != null && activityId.equals(that.activityId);
    }

    @Override
    public int hashCode() {
        return activityId != null ? activityId.hashCode() : 0;
    }
}

package com.boardly.infrastructure.adapters.out.persistence.activity;

import com.boardly.features.activity.domain.Activity;
import com.boardly.features.activity.domain.ActivityType;
import com.boardly.features.activity.domain.Actor;
import com.boardly.features.activity.domain.Payload;
import com.boardly.shared.common.value.ActivityId;
import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.CardId;
import com.boardly.shared.common.value.ListId;
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
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
    name = "user_activity",
    indexes = {
      @Index(name = "idx_activity_board_id", columnList = "board_id"),
      @Index(name = "idx_activity_actor_id", columnList = "actor_id"),
      @Index(name = "idx_activity_created_at", columnList = "created_at"),
      @Index(name = "idx_activity_board_timestamp", columnList = "board_id, created_at")
    }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ActivityEntity {
  @Id
  @Column(name = "activity_id", nullable = false, length = 50)
  private String activityId;

  @Column(name = "actor_id", nullable = false, length = 50)
  private String actorId;

  @Column(name = "board_id", length = 50)
  private String boardId;

  @Column(name = "list_id", length = 50)
  private String listId;

  @Column(name = "card_id", length = 50)
  private String cardId;

  @Enumerated(EnumType.STRING)
  @Column(name = "activity_type", nullable = false, length = 50)
  private ActivityType activityType;

  @Column(name = "actor_first_name", nullable = false, length = 50)
  private String actorFirstName;

  @Column(name = "actor_last_name", nullable = false, length = 50)
  private String actorLastName;

  @Column(name = "actor_profile_image_url", length = 500)
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
      Map<String, Object> payloadData =
          objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {});

      Actor actor = Actor.of(actorId, actorFirstName, actorLastName, actorProfileImageUrl);
      Payload payloadObj = new Payload(payloadData);

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
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    ActivityEntity that = (ActivityEntity) obj;
    return activityId != null && activityId.equals(that.activityId);
  }

  @Override
  public int hashCode() {
    return activityId != null ? activityId.hashCode() : 0;
  }
}

package com.boardly.infrastructure.adapters.out.persistence.activity;

import java.time.Instant;
import java.util.Map;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.boardly.features.activity.domain.Activity;
import com.boardly.features.activity.domain.ActivityType;
import com.boardly.features.activity.domain.Actor;
import com.boardly.features.activity.domain.Payload;
import com.boardly.shared.common.value.ActivityId;
import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.WorkspaceId;
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
@Table(name = "activities", indexes = {
    @Index(name = "idx_activity_board_id", columnList = "board_id"),
    @Index(name = "idx_activity_actor_id", columnList = "actor_id"),
    @Index(name = "idx_activity_created_at", columnList = "created_at"),
    @Index(name = "idx_activity_board_timestamp", columnList = "board_id, created_at"),
    @Index(name = "idx_activities_workspace_board", columnList = "workspace_id, board_id"),
    @Index(name = "idx_activities_user_created", columnList = "workspace_id, actor_id, created_at")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ActivityEntity {
  @Id
  @Column(name = "id", nullable = false)
  private String activityId;

  @Column(name = "workspace_id", nullable = false)
  private String workspaceId;

  @Column(name = "board_id", length = 50)
  private String boardId;

  @Column(name = "actor_id", length = 50)
  private String actorId;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 50)
  private ActivityType activityType;

  @Column(name = "payload_json", columnDefinition = "CLOB")
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
          .workspaceId(activity.getWorkspaceId().getId())
          .boardId(activity.getBoardId() != null ? activity.getBoardId().getId() : null)
          .actorId(activity.getActor().getId())
          .activityType(activity.getType())
          .payload(payloadJson)
          .createdAt(activity.getTimestamp())
          .build();
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize payload", e);
    }
  }

  public Activity toDomainEntity() {
    try {
      Map<String, Object> payloadData = objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {
      });

      Actor actor = Actor.of(actorId, "", "", ""); // Activity 도메인에서 actor 정보를 별도로 관리
      Payload payloadObj = new Payload(payloadData);

      return Activity.builder()
          .id(new ActivityId(activityId))
          .type(activityType)
          .actor(actor)
          .timestamp(getCreatedAt())
          .payload(payloadObj)
          .workspaceId(new WorkspaceId(workspaceId))
          .boardId(boardId != null ? new BoardId(boardId) : null)
          .listId(null) // Activity 도메인에서 listId는 별도로 관리
          .cardId(null) // Activity 도메인에서 cardId는 별도로 관리
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

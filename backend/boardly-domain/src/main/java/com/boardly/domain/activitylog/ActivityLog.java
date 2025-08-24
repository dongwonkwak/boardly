package com.boardly.domain.activitylog;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * 활동 로그 도메인 엔티티
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivityLog {

    private ActivityLogId id;
    private Actor actor;
    private ActivityType activityType;
    private Map<String, Object> details;
    private Instant createdAt;

    @Builder
    public ActivityLog(ActivityLogId id, Actor actor, ActivityType activityType,
            Map<String, Object> details, Instant createdAt) {
        this.id = id;
        this.actor = actor;
        this.activityType = activityType;
        this.details = details;
        this.createdAt = createdAt;
    }

    /**
     * 활동 로그 생성 팩토리 메서드
     */
    public static ActivityLog create(Actor actor, ActivityType activityType, Map<String, Object> details) {
        return ActivityLog.builder()
                .id(ActivityLogId.generate())
                .actor(actor)
                .activityType(activityType)
                .details(details)
                .createdAt(Instant.now())
                .build();
    }

    /**
     * User 객체로부터 활동 로그 생성
     */
    public static ActivityLog create(com.boardly.domain.user.User user, ActivityType activityType,
            Map<String, Object> details) {
        Actor actor = Actor.from(user);
        return create(actor, activityType, details);
    }
}

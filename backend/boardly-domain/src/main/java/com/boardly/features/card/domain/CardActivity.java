package com.boardly.features.card.domain;

import com.boardly.shared.domain.BaseEntity;
import com.boardly.shared.common.value.ActivityId;
import com.boardly.features.user.domain.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 카드 활동 내역 도메인 모델
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CardActivity extends BaseEntity {

    private ActivityId activityId;
    private String type;
    private String description;
    private Instant createdAt;
    private User createdBy;
    private User targetUser;

    /**
     * 카드 활동 내역 생성
     */
    public static CardActivity of(
            ActivityId activityId,
            String type,
            String description,
            Instant createdAt,
            User createdBy,
            User targetUser) {

        return CardActivity.builder()
                .activityId(activityId)
                .type(type)
                .description(description)
                .createdAt(createdAt)
                .createdBy(createdBy)
                .targetUser(targetUser)
                .build();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        CardActivity that = (CardActivity) obj;
        return activityId != null && activityId.equals(that.activityId);
    }

    @Override
    public int hashCode() {
        return activityId != null ? activityId.hashCode() : 0;
    }
}
package com.boardly.features.activity.domain.model;

import com.boardly.shared.domain.valueobject.EntityId;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ActivityId extends EntityId {
    public ActivityId(String activityId) {
        super(activityId);
    }

    public ActivityId() {
        super();
    }
}

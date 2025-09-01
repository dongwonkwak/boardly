package com.boardly.shared.common.value;

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

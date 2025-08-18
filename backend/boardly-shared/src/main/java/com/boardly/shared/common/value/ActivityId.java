package com.boardly.shared.common.value;

import com.github.f4b6a3.ulid.UlidCreator;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ActivityId extends EntityId {

    public ActivityId(String activityId) {
        super(activityId);
    }

    public ActivityId() {
        super("act_" + UlidCreator.getUlid().toString());
    }
}

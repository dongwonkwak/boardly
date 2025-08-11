package com.boardly.shared.common.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class LabelId extends EntityId {

    public LabelId(String labelId) {
        super(labelId);
    }

    public LabelId() {
        super();
    }
}

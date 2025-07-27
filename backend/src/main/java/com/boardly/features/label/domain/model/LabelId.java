package com.boardly.features.label.domain.model;

import com.boardly.shared.domain.valueobject.EntityId;

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

package com.boardly.shared.common.value;

import com.github.f4b6a3.ulid.UlidCreator;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class LabelId extends EntityId {

    public LabelId(String labelId) {
        super(labelId);
    }

    public LabelId() {
        super("lbl_" + UlidCreator.getUlid().toString());
    }
}

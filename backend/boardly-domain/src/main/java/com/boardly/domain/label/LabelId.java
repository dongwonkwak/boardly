package com.boardly.domain.label;

import com.boardly.shared.EntityId;

/**
 * 라벨 ID 도메인 객체
 */
public class LabelId extends EntityId {

    public LabelId() {
        super();
    }

    public LabelId(String value) {
        super(value);
    }

    public static LabelId generate() {
        return new LabelId(generateWithPrefix("lbl_"));
    }

    public static LabelId of(String value) {
        return new LabelId(value);
    }

    @Override
    protected String getPrefix() {
        return "lbl_";
    }
}

package com.boardly.domain.checklist;

import com.boardly.shared.EntityId;

/**
 * 체크리스트 ID 도메인 객체
 */
public class ChecklistId extends EntityId {

    public ChecklistId() {
        super();
    }

    public ChecklistId(String value) {
        super(value);
    }

    public static ChecklistId generate() {
        return new ChecklistId(generateWithPrefix("cl_"));
    }

    public static ChecklistId of(String value) {
        return new ChecklistId(value);
    }

    @Override
    protected String getPrefix() {
        return "cl_";
    }
}

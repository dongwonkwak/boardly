package com.boardly.domain.checklist;

import com.boardly.shared.EntityId;

/**
 * 체크리스트 아이템 ID 도메인 객체
 */
public class ChecklistItemId extends EntityId {

    public ChecklistItemId() {
        super();
    }

    public ChecklistItemId(String value) {
        super(value);
    }

    public static ChecklistItemId generate() {
        return new ChecklistItemId(generateWithPrefix("cli_"));
    }

    public static ChecklistItemId of(String value) {
        return new ChecklistItemId(value);
    }

    @Override
    protected String getPrefix() {
        return "cli_";
    }
}

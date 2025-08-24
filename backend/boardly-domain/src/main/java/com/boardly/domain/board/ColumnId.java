package com.boardly.domain.board;

import com.boardly.shared.EntityId;

/**
 * 컬럼 ID 도메인 객체
 */
public class ColumnId extends EntityId {

    public ColumnId() {
        super();
    }

    public ColumnId(String value) {
        super(value);
    }

    public static ColumnId generate() {
        return new ColumnId(generateWithPrefix("col_"));
    }

    public static ColumnId of(String value) {
        return new ColumnId(value);
    }

    @Override
    protected String getPrefix() {
        return "col_";
    }
}

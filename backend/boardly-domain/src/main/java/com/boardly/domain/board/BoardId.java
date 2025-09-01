package com.boardly.domain.board;

import com.boardly.shared.EntityId;

/**
 * 보드 ID 도메인 객체
 */
public class BoardId extends EntityId {

    public BoardId() {
        super();
    }

    public BoardId(String value) {
        super(value);
    }

    public static BoardId generate() {
        return new BoardId(generateWithPrefix("bd_"));
    }

    public static BoardId of(String value) {
        return new BoardId(value);
    }

    @Override
    protected String getPrefix() {
        return "bd_";
    }
}

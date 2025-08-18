package com.boardly.shared.common.value;

import com.github.f4b6a3.ulid.UlidCreator;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class BoardId extends EntityId {

    public BoardId(String boardId) {
        super(boardId);
    }

    public BoardId() {
        super("brd_" + UlidCreator.getUlid().toString());
    }
}

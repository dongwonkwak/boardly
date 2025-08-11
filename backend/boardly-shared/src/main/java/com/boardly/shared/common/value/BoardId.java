package com.boardly.shared.common.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class BoardId extends EntityId {

    public BoardId(String boardId) {
        super(boardId);
    }

    public BoardId() {
        super();
    }
}

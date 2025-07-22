package com.boardly.features.board.domain.model;

import com.boardly.shared.domain.valueobject.EntityId;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class BoardMemberId extends EntityId {

    public BoardMemberId(String boardMemberId) {
        super(boardMemberId);
    }

    public BoardMemberId() {
        super();
    }
}
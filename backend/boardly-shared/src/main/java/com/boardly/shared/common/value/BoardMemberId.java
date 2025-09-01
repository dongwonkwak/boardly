package com.boardly.shared.common.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class BoardMemberId extends EntityId {

    public BoardMemberId(String memberId) {
        super(memberId);
    }

    public BoardMemberId() {
        super();
    }
}

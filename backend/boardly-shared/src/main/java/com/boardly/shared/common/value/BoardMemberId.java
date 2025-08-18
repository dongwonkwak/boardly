package com.boardly.shared.common.value;

import com.github.f4b6a3.ulid.UlidCreator;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class BoardMemberId extends EntityId {

    public BoardMemberId(String memberId) {
        super(memberId);
    }

    public BoardMemberId() {
        super("bm_" + UlidCreator.getUlid().toString());
    }
}

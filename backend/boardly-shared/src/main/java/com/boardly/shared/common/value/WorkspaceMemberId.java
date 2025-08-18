package com.boardly.shared.common.value;

import com.github.f4b6a3.ulid.UlidCreator;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class WorkspaceMemberId extends EntityId {

    public WorkspaceMemberId(String id) {
        super(id);
    }

    public WorkspaceMemberId() {
        super("wsm_" + UlidCreator.getUlid().toString());
    }
}

package com.boardly.shared.common.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class WorkspaceMemberId extends EntityId {

    public WorkspaceMemberId(String id) {
        super(id);
    }

    public WorkspaceMemberId() {
        super();
    }
}

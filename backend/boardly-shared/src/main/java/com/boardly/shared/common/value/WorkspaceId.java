package com.boardly.shared.common.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class WorkspaceId extends EntityId {

    public WorkspaceId(String workspaceId) {
        super(workspaceId);
    }

    public WorkspaceId() {
        super();
    }
}

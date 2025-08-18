package com.boardly.shared.common.value;

import com.github.f4b6a3.ulid.UlidCreator;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class WorkspaceId extends EntityId {

    public WorkspaceId(String workspaceId) {
        super(workspaceId);
    }

    public WorkspaceId() {
        super("wsp_" + UlidCreator.getUlid().toString());
    }
}

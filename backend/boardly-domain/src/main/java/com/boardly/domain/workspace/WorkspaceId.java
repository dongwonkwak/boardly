package com.boardly.domain.workspace;

import com.boardly.shared.EntityId;

/**
 * 워크스페이스 ID 도메인 객체
 */
public class WorkspaceId extends EntityId {

    public WorkspaceId() {
        super();
    }

    public WorkspaceId(String value) {
        super(value);
    }

    public static WorkspaceId generate() {
        return new WorkspaceId(generateWithPrefix("ws_"));
    }

    public static WorkspaceId of(String value) {
        return new WorkspaceId(value);
    }

    @Override
    protected String getPrefix() {
        return "ws_";
    }
}

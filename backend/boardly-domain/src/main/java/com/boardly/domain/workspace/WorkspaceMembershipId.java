package com.boardly.domain.workspace;

import com.boardly.shared.EntityId;

/**
 * 워크스페이스 멤버십 ID 도메인 객체
 */
public class WorkspaceMembershipId extends EntityId {

    public WorkspaceMembershipId() {
        super();
    }

    public WorkspaceMembershipId(String value) {
        super(value);
    }

    public static WorkspaceMembershipId generate() {
        return new WorkspaceMembershipId(generateWithPrefix("wsm_"));
    }

    public static WorkspaceMembershipId of(String value) {
        return new WorkspaceMembershipId(value);
    }

    @Override
    protected String getPrefix() {
        return "wsm_";
    }
}

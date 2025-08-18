package com.boardly.shared.common.value;

/**
 * 워크스페이스 멤버의 역할 정의
 */
public enum WorkspaceRole {
    OWNER(true, true, true),
    ADMIN(true, true, false),
    MEMBER(true, false, false),
    GUEST(false, false, false);

    private final boolean canManage;
    private final boolean canWrite;
    private final boolean canRead;

    WorkspaceRole(boolean canManage, boolean canWrite, boolean canRead) {
        this.canManage = canManage;
        this.canWrite = canWrite;
        this.canRead = canRead;
    }

    public boolean canManageWorkspace() { return canManage; }
    public boolean canWriteWorkspace() { return canWrite; }
    public boolean canReadWorkspace() { return canRead; }

    public int getPriority() {
        return switch (this) {
            case OWNER -> 4;
            case ADMIN -> 3;
            case MEMBER -> 2;
            case GUEST -> 1;
        };
    }

    public boolean hasHigherPermissionThan(WorkspaceRole other) {
        return this.getPriority() > other.getPriority();
    }
}

package com.boardly.features.workspace.domain;

import java.time.Instant;
import java.util.Objects;

import com.boardly.shared.common.value.UserId;
import com.boardly.shared.common.value.WorkspaceId;
import com.boardly.shared.common.value.WorkspaceMemberId;
import com.boardly.shared.common.value.WorkspaceRole;
import com.boardly.shared.domain.BaseEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkspaceMember extends BaseEntity {

    private WorkspaceMemberId memberId;
    private WorkspaceId workspaceId;
    private UserId userId;
    private WorkspaceRole role;
    private boolean isActive;

    @Builder
    private WorkspaceMember(
        WorkspaceMemberId memberId,
        WorkspaceId workspaceId,
        UserId userId,
        WorkspaceRole role,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt
    ) {
        super(createdAt, updatedAt);
        this.memberId = memberId;
        this.workspaceId = workspaceId;
        this.userId = userId;
        this.role = role;
        this.isActive = isActive;
    }

    public static WorkspaceMember create(WorkspaceId workspaceId, UserId userId, WorkspaceRole role) {
        Instant now = Instant.now();
        return WorkspaceMember.builder()
            .memberId(new WorkspaceMemberId())
            .workspaceId(workspaceId)
            .userId(userId)
            .role(role)
            .isActive(true)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    public void changeRole(WorkspaceRole newRole) {
        this.role = newRole;
        markAsUpdated();
    }

    public void deactivate() {
        this.isActive = false;
        markAsUpdated();
    }

    public boolean canRead() { return isActive && role.canReadWorkspace(); }
    public boolean canWrite() { return isActive && role.canWriteWorkspace(); }
    public boolean canManage() { return isActive && role.canManageWorkspace(); }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        WorkspaceMember that = (WorkspaceMember) obj;
        return Objects.equals(memberId, that.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId);
    }

    @Override
    public String toString() {
        return String.format("WorkspaceMember{memberId=%s, workspaceId=%s, userId=%s, role=%s, isActive=%s, createdAt=%s, updatedAt=%s}",
            memberId, workspaceId, userId, role, isActive, getCreatedAt(), getUpdatedAt());
    }
}

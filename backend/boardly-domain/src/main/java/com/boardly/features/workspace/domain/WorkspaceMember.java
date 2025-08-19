package com.boardly.features.workspace.domain;

import com.boardly.shared.common.value.UserId;
import com.boardly.shared.common.value.WorkspaceId;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkspaceMember {

    public enum Role {
        OWNER, ADMIN, MEMBER;

        public boolean hasAdminPermission() {
            return this == OWNER || this == ADMIN;
        }

        public boolean hasWritePermission() {
            return this == OWNER || this == ADMIN || this == MEMBER;
        }

        public boolean hasReadPermission() {
            return true; // 모든 역할이 읽기 권한을 가짐
        }
    }

    public enum InviteStatus {
        PENDING, ACCEPTED, DECLINED, EXPIRED
    }

    private WorkspaceId workspaceId;
    private UserId userId;
    private Role role;
    private InviteStatus inviteStatus;
    private UserId invitedBy;
    private Instant invitedAt;
    private Instant joinedAt;

    @Builder
    private WorkspaceMember(
            WorkspaceId workspaceId,
            UserId userId,
            Role role,
            InviteStatus inviteStatus,
            UserId invitedBy,
            Instant invitedAt,
            Instant joinedAt) {
        this.workspaceId = workspaceId;
        this.userId = userId;
        this.role = role;
        this.inviteStatus = inviteStatus;
        this.invitedBy = invitedBy;
        this.invitedAt = invitedAt;
        this.joinedAt = joinedAt;
    }

    public static WorkspaceMember createOwner(WorkspaceId workspaceId, UserId userId) {
        return WorkspaceMember.builder()
                .workspaceId(workspaceId)
                .userId(userId)
                .role(Role.OWNER)
                .inviteStatus(InviteStatus.ACCEPTED)
                .invitedAt(Instant.now())
                .joinedAt(Instant.now())
                .build();
    }

    public static WorkspaceMember createInvitation(
            WorkspaceId workspaceId,
            UserId userId,
            Role role,
            UserId invitedBy) {
        return WorkspaceMember.builder()
                .workspaceId(workspaceId)
                .userId(userId)
                .role(role)
                .inviteStatus(InviteStatus.PENDING)
                .invitedBy(invitedBy)
                .invitedAt(Instant.now())
                .build();
    }

    public void acceptInvitation() {
        this.inviteStatus = InviteStatus.ACCEPTED;
        this.joinedAt = Instant.now();
    }

    public void declineInvitation() {
        this.inviteStatus = InviteStatus.DECLINED;
    }

    public void expireInvitation() {
        this.inviteStatus = InviteStatus.EXPIRED;
    }

    public void changeRole(Role newRole) {
        this.role = newRole;
    }

    public boolean isActive() {
        return inviteStatus == InviteStatus.ACCEPTED;
    }

    public boolean isPending() {
        return inviteStatus == InviteStatus.PENDING;
    }

    public boolean canManageWorkspace() {
        return role.hasAdminPermission() && isActive();
    }

    public boolean canInviteMembers() {
        return role.hasAdminPermission() && isActive();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        WorkspaceMember that = (WorkspaceMember) obj;
        return Objects.equals(workspaceId, that.workspaceId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId, userId);
    }

    @Override
    public String toString() {
        return String.format("WorkspaceMember{workspaceId=%s, userId=%s, role=%s, status=%s}",
                workspaceId, userId, role, inviteStatus);
    }
}

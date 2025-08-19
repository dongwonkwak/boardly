package com.boardly.infrastructure.adapters.out.persistence.workspace;

import java.time.Instant;
import java.util.Objects;

import com.boardly.features.workspace.domain.WorkspaceMember;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.common.value.WorkspaceId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "workspace_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkspaceMemberEntity {

    @Id
    @Column(name = "workspace_id", nullable = false)
    private String workspaceId;

    @Id
    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private WorkspaceMember.Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "invite_status", nullable = false, length = 20)
    private WorkspaceMember.InviteStatus inviteStatus;

    @Column(name = "invited_by", length = 50)
    private String invitedBy;

    @Column(name = "invited_at", nullable = false)
    private Instant invitedAt;

    @Column(name = "joined_at")
    private Instant joinedAt;

    @Builder
    private WorkspaceMemberEntity(
            String workspaceId,
            String userId,
            WorkspaceMember.Role role,
            WorkspaceMember.InviteStatus inviteStatus,
            String invitedBy,
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

    /**
     * Domain WorkspaceMember 객체로 변환
     */
    public WorkspaceMember toDomainEntity() {
        return WorkspaceMember.builder()
                .workspaceId(new WorkspaceId(this.workspaceId))
                .userId(new UserId(this.userId))
                .role(this.role)
                .inviteStatus(this.inviteStatus)
                .invitedBy(this.invitedBy != null ? new UserId(this.invitedBy) : null)
                .invitedAt(this.invitedAt)
                .joinedAt(this.joinedAt)
                .build();
    }

    /**
     * Domain WorkspaceMember 객체로부터 Entity 생성
     */
    public static WorkspaceMemberEntity fromDomainEntity(WorkspaceMember workspaceMember) {
        return WorkspaceMemberEntity.builder()
                .workspaceId(workspaceMember.getWorkspaceId().getId())
                .userId(workspaceMember.getUserId().getId())
                .role(workspaceMember.getRole())
                .inviteStatus(workspaceMember.getInviteStatus())
                .invitedBy(workspaceMember.getInvitedBy() != null ? workspaceMember.getInvitedBy().getId() : null)
                .invitedAt(workspaceMember.getInvitedAt())
                .joinedAt(workspaceMember.getJoinedAt())
                .build();
    }

    /**
     * 도메인 객체의 변경사항을 반영
     */
    public void updateFromDomainEntity(WorkspaceMember workspaceMember) {
        this.role = workspaceMember.getRole();
        this.inviteStatus = workspaceMember.getInviteStatus();
        this.joinedAt = workspaceMember.getJoinedAt();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        WorkspaceMemberEntity that = (WorkspaceMemberEntity) obj;
        return workspaceId != null && workspaceId.equals(that.workspaceId) &&
                userId != null && userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId, userId);
    }

    @Override
    public String toString() {
        return String.format("WorkspaceMemberEntity{workspaceId=%s, userId=%s, role=%s, status=%s}",
                workspaceId, userId, role, inviteStatus);
    }
}

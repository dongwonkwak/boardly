package com.boardly.infrastructure.adapters.out.persistence.workspace;

import com.boardly.features.workspace.domain.WorkspaceMember;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.common.value.WorkspaceId;
import com.boardly.shared.common.value.WorkspaceMemberId;
import com.boardly.shared.common.value.WorkspaceRole;
import com.boardly.shared.common.value.InviteStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "workspace_members",
    indexes = {
        @Index(name = "idx_workspace_members_user_workspace", columnList = "user_id, workspace_id"),
        @Index(name = "idx_workspace_members_status", columnList = "invite_status")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkspaceMemberEntity {

    @Id
    @Column(name = "member_id", nullable = false, length = 50)
    private String memberId;

    @Column(name = "workspace_id", nullable = false)
    private String workspaceId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private WorkspaceRole role;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "invite_status")
    private InviteStatus inviteStatus;

    @Column(name = "invited_by")
    private String invitedBy;

    @Column(name = "invited_at")
    private Instant invitedAt;

    @Column(name = "joined_at")
    private Instant joinedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    @Builder
    private WorkspaceMemberEntity(
        String memberId,
        String workspaceId,
        String userId,
        WorkspaceRole role,
        boolean isActive,
        InviteStatus inviteStatus,
        String invitedBy,
        Instant invitedAt,
        Instant joinedAt,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.memberId = memberId;
        this.workspaceId = workspaceId;
        this.userId = userId;
        this.role = role;
        this.isActive = isActive;
        this.inviteStatus = inviteStatus;
        this.invitedBy = invitedBy;
        this.invitedAt = invitedAt;
        this.joinedAt = joinedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public WorkspaceMember toDomainEntity() {
        return WorkspaceMember.builder()
            .memberId(new WorkspaceMemberId(memberId))
            .workspaceId(new WorkspaceId(workspaceId))
            .userId(new UserId(userId))
            .role(role)
            .isActive(isActive)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    }

    public static WorkspaceMemberEntity fromDomainEntity(WorkspaceMember member) {
        return WorkspaceMemberEntity.builder()
            .memberId(member.getMemberId().getId())
            .workspaceId(member.getWorkspaceId().getId())
            .userId(member.getUserId().getId())
            .role(member.getRole())
            .isActive(member.isActive())
            .createdAt(member.getCreatedAt())
            .updatedAt(member.getUpdatedAt())
            .build();
    }

    public enum InviteStatus { PENDING, ACCEPTED, DECLINED, EXPIRED }
}

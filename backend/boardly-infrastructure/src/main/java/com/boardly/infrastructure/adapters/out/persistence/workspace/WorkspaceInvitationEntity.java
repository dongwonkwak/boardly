package com.boardly.infrastructure.adapters.out.persistence.workspace;

import com.boardly.features.workspace.domain.WorkspaceInvitation;
import com.boardly.features.workspace.domain.WorkspaceMember;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.common.value.WorkspaceId;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "workspace_invitations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkspaceInvitationEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "workspace_id", nullable = false)
    private String workspaceId;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private WorkspaceMember.Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WorkspaceInvitation.Status status;

    @Column(name = "invited_by", length = 50)
    private String invitedBy;

    @Column(name = "invited_at", nullable = false)
    private Instant invitedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Builder
    private WorkspaceInvitationEntity(
            String id,
            String workspaceId,
            String email,
            WorkspaceMember.Role role,
            WorkspaceInvitation.Status status,
            String invitedBy,
            Instant invitedAt,
            Instant expiresAt,
            Instant acceptedAt) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.email = email;
        this.role = role;
        this.status = status;
        this.invitedBy = invitedBy;
        this.invitedAt = invitedAt;
        this.expiresAt = expiresAt;
        this.acceptedAt = acceptedAt;
    }

    /**
     * Domain WorkspaceInvitation 객체로 변환
     */
    public WorkspaceInvitation toDomainEntity() {
        return WorkspaceInvitation.builder()
                .id(this.id)
                .workspaceId(new WorkspaceId(this.workspaceId))
                .email(this.email)
                .role(this.role)
                .status(this.status)
                .invitedBy(this.invitedBy != null ? new UserId(this.invitedBy) : null)
                .invitedAt(this.invitedAt)
                .expiresAt(this.expiresAt)
                .acceptedAt(this.acceptedAt)
                .build();
    }

    /**
     * Domain WorkspaceInvitation 객체로부터 Entity 생성
     */
    public static WorkspaceInvitationEntity fromDomainEntity(WorkspaceInvitation invitation) {
        return WorkspaceInvitationEntity.builder()
                .id(invitation.getId())
                .workspaceId(invitation.getWorkspaceId().getId())
                .email(invitation.getEmail())
                .role(invitation.getRole())
                .status(invitation.getStatus())
                .invitedBy(invitation.getInvitedBy() != null ? invitation.getInvitedBy().getId() : null)
                .invitedAt(invitation.getInvitedAt())
                .expiresAt(invitation.getExpiresAt())
                .acceptedAt(invitation.getAcceptedAt())
                .build();
    }

    /**
     * 도메인 객체의 변경사항을 반영
     */
    public void updateFromDomainEntity(WorkspaceInvitation invitation) {
        this.status = invitation.getStatus();
        this.acceptedAt = invitation.getAcceptedAt();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        WorkspaceInvitationEntity that = (WorkspaceInvitationEntity) obj;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("WorkspaceInvitationEntity{id=%s, workspaceId=%s, email='%s', role=%s, status=%s}",
                id, workspaceId, email, role, status);
    }
}

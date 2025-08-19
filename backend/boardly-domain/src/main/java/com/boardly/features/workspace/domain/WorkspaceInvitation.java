package com.boardly.features.workspace.domain;

import java.time.Instant;
import java.util.Objects;

import com.boardly.shared.common.value.UserId;
import com.boardly.shared.common.value.WorkspaceId;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkspaceInvitation {

    public enum Status {
        PENDING, ACCEPTED, DECLINED, EXPIRED
    }

    private String id;
    private WorkspaceId workspaceId;
    private String email;
    private WorkspaceMember.Role role;
    private Status status;
    private UserId invitedBy;
    private Instant invitedAt;
    private Instant expiresAt;
    private Instant acceptedAt;

    @Builder
    private WorkspaceInvitation(
            String id,
            WorkspaceId workspaceId,
            String email,
            WorkspaceMember.Role role,
            Status status,
            UserId invitedBy,
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

    public static WorkspaceInvitation create(
            WorkspaceId workspaceId,
            String email,
            WorkspaceMember.Role role,
            UserId invitedBy) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(7 * 24 * 60 * 60); // 7일 후 만료

        return WorkspaceInvitation.builder()
                .workspaceId(workspaceId)
                .email(email.toLowerCase().trim())
                .role(role)
                .status(Status.PENDING)
                .invitedBy(invitedBy)
                .invitedAt(now)
                .expiresAt(expiresAt)
                .build();
    }

    public void accept() {
        this.status = Status.ACCEPTED;
        this.acceptedAt = Instant.now();
    }

    public void decline() {
        this.status = Status.DECLINED;
    }

    public void expire() {
        this.status = Status.EXPIRED;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isPending() {
        return status == Status.PENDING && !isExpired();
    }

    public boolean canBeAccepted() {
        return isPending();
    }

    public boolean canBeDeclined() {
        return isPending();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        WorkspaceInvitation that = (WorkspaceInvitation) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("WorkspaceInvitation{id=%s, workspaceId=%s, email='%s', role=%s, status=%s}",
                id, workspaceId, email, role, status);
    }
}

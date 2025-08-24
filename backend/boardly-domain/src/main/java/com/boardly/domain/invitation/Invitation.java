package com.boardly.domain.invitation;

import com.boardly.domain.board.BoardId;
import com.boardly.domain.common.Domain;
import com.boardly.domain.user.UserId;
import com.boardly.domain.workspace.WorkspaceId;
import lombok.Builder;

import java.time.Instant;

public sealed interface Invitation extends Domain {
    InvitationStatus status();

    Instant expiresAt();

    @Builder
    record WorkspaceInvitation(
            InvitationId id,
            String email,
            InvitationStatus status,
            WorkspaceId workspaceId,
            UserId invitedBy,
            Instant expiresAt,
            Instant createdAt,
            Instant updatedAt) implements Invitation {
    }

    @Builder
    record BoardInvitation(
            InvitationId id,
            String email,
            InvitationStatus status,
            BoardId boardId,
            UserId invitedBy,
            Instant expiresAt,
            Instant createdAt,
            Instant updatedAt) implements Invitation {
    }

    default boolean isExpired() {
        return expiresAt().isBefore(Instant.now());
    }

    default boolean isActionable() {
        return status() == InvitationStatus.PENDING && !isExpired();
    }

    // 초대를 수락할 수 있는지 확인
    default boolean canBeAccepted() {
        return isActionable();
    }

    // 초대를 거절할 수 있는지 확인
    default boolean canBeDeclined() {
        return isActionable();
    }

}

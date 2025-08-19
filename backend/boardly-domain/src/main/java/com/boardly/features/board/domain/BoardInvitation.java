package com.boardly.features.board.domain;

import java.time.Instant;
import java.util.Objects;

import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.UserId;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardInvitation {

    public enum Role {
        BOARD_ADMIN, BOARD_EDITOR, BOARD_VIEWER;

        public boolean hasAdminPermission() {
            return this == BOARD_ADMIN;
        }

        public boolean hasWritePermission() {
            return this == BOARD_ADMIN || this == BOARD_EDITOR;
        }

        public boolean hasReadPermission() {
            return true; // 모든 역할이 읽기 권한을 가짐
        }
    }

    public enum Status {
        PENDING, ACCEPTED, DECLINED, EXPIRED
    }

    private String id;
    private BoardId boardId;
    private String email;
    private Role role;
    private Status status;
    private UserId invitedBy;
    private Instant invitedAt;
    private Instant expiresAt;
    private Instant acceptedAt;

    @Builder
    private BoardInvitation(
            String id,
            BoardId boardId,
            String email,
            Role role,
            Status status,
            UserId invitedBy,
            Instant invitedAt,
            Instant expiresAt,
            Instant acceptedAt) {
        this.id = id;
        this.boardId = boardId;
        this.email = email;
        this.role = role;
        this.status = status;
        this.invitedBy = invitedBy;
        this.invitedAt = invitedAt;
        this.expiresAt = expiresAt;
        this.acceptedAt = acceptedAt;
    }

    public static BoardInvitation create(
            BoardId boardId,
            String email,
            Role role,
            UserId invitedBy) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(7 * 24 * 60 * 60); // 7일 후 만료

        return BoardInvitation.builder()
                .boardId(boardId)
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
        BoardInvitation that = (BoardInvitation) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("BoardInvitation{id=%s, boardId=%s, email='%s', role=%s, status=%s}",
                id, boardId, email, role, status);
    }
}

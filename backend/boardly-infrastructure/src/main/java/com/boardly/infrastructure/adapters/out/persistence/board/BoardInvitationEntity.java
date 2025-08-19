package com.boardly.infrastructure.adapters.out.persistence.board;

import java.time.Instant;

import com.boardly.features.board.domain.BoardInvitation;
import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.UserId;

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
@Table(name = "board_invitations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardInvitationEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "board_id", nullable = false, length = 50)
    private String boardId;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private BoardInvitation.Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BoardInvitation.Status status;

    @Column(name = "invited_by", length = 50)
    private String invitedBy;

    @Column(name = "invited_at", nullable = false)
    private Instant invitedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Builder
    private BoardInvitationEntity(
            String id,
            String boardId,
            String email,
            BoardInvitation.Role role,
            BoardInvitation.Status status,
            String invitedBy,
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

    /**
     * Domain BoardInvitation 객체로 변환
     */
    public BoardInvitation toDomainEntity() {
        return BoardInvitation.builder()
                .id(this.id)
                .boardId(new BoardId(this.boardId))
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
     * Domain BoardInvitation 객체로부터 Entity 생성
     */
    public static BoardInvitationEntity fromDomainEntity(BoardInvitation invitation) {
        return BoardInvitationEntity.builder()
                .id(invitation.getId())
                .boardId(invitation.getBoardId().getId())
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
    public void updateFromDomainEntity(BoardInvitation invitation) {
        this.status = invitation.getStatus();
        this.acceptedAt = invitation.getAcceptedAt();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        BoardInvitationEntity that = (BoardInvitationEntity) obj;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("BoardInvitationEntity{id=%s, boardId=%s, email='%s', role=%s, status=%s}",
                id, boardId, email, role, status);
    }
}

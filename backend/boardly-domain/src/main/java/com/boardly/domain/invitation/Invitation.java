package com.boardly.domain.invitation;

import com.boardly.domain.board.BoardId;
import com.boardly.domain.user.UserId;
import com.boardly.domain.workspace.WorkspaceId;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 초대장 도메인 엔티티
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invitation {

    private InvitationId id;
    private String email;
    private InvitationType type;
    private InvitationStatus status;
    private WorkspaceId workspaceId;
    private BoardId boardId;
    private UserId invitedBy;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;

    @Builder
    public Invitation(InvitationId id, String email, InvitationType type, InvitationStatus status,
            WorkspaceId workspaceId, BoardId boardId, UserId invitedBy,
            Instant expiresAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.type = type;
        this.status = status;
        this.workspaceId = workspaceId;
        this.boardId = boardId;
        this.invitedBy = invitedBy;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 워크스페이스 초대장 생성 팩토리 메서드
     */
    public static Invitation createWorkspaceInvitation(String email, WorkspaceId workspaceId,
            UserId invitedBy, Instant expiresAt) {
        return Invitation.builder()
                .id(InvitationId.generate())
                .email(email)
                .type(InvitationType.WORKSPACE)
                .status(InvitationStatus.PENDING)
                .workspaceId(workspaceId)
                .invitedBy(invitedBy)
                .expiresAt(expiresAt)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * 보드 초대장 생성 팩토리 메서드
     */
    public static Invitation createBoardInvitation(String email, BoardId boardId,
            UserId invitedBy, Instant expiresAt) {
        return Invitation.builder()
                .id(InvitationId.generate())
                .email(email)
                .type(InvitationType.BOARD)
                .status(InvitationStatus.PENDING)
                .boardId(boardId)
                .invitedBy(invitedBy)
                .expiresAt(expiresAt)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * 초대장 수락
     */
    public void accept() {
        this.status = InvitationStatus.ACCEPTED;
        this.updatedAt = Instant.now();
    }

    /**
     * 초대장 거절
     */
    public void decline() {
        this.status = InvitationStatus.DECLINED;
        this.updatedAt = Instant.now();
    }

    /**
     * 초대장 취소
     */
    public void cancel() {
        this.status = InvitationStatus.EXPIRED;
        this.updatedAt = Instant.now();
    }

    /**
     * 초대장이 만료되었는지 확인
     */
    public boolean isExpired() {
        return this.expiresAt != null && this.expiresAt.isBefore(Instant.now());
    }

    /**
     * 워크스페이스 초대장인지 확인
     */
    public boolean isWorkspaceInvitation() {
        return this.type == InvitationType.WORKSPACE;
    }

    /**
     * 보드 초대장인지 확인
     */
    public boolean isBoardInvitation() {
        return this.type == InvitationType.BOARD;
    }
}

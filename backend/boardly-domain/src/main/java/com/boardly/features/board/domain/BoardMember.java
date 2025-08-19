package com.boardly.features.board.domain;

import java.time.Instant;
import java.util.Objects;

import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.BoardMemberId;
import com.boardly.shared.common.value.BoardRole;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.domain.BaseEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BoardMember extends BaseEntity {

    public enum InviteStatus {
        PENDING, ACCEPTED, DECLINED, EXPIRED
    }

    private BoardMemberId memberId;
    private BoardId boardId;
    private UserId userId;
    private BoardRole role;
    private boolean isActive;
    private InviteStatus inviteStatus;
    private UserId invitedBy;
    private Instant invitedAt;
    private Instant joinedAt;

    @Builder
    private BoardMember(
            BoardMemberId memberId,
            BoardId boardId,
            UserId userId,
            BoardRole role,
            boolean isActive,
            InviteStatus inviteStatus,
            UserId invitedBy,
            Instant invitedAt,
            Instant joinedAt,
            Instant createdAt,
            Instant updatedAt) {
        super(createdAt, updatedAt);
        this.memberId = memberId;
        this.boardId = boardId;
        this.userId = userId;
        this.role = role;
        this.isActive = isActive;
        this.inviteStatus = inviteStatus;
        this.invitedBy = invitedBy;
        this.invitedAt = invitedAt;
        this.joinedAt = joinedAt;
    }

    /**
     * 새로운 보드 멤버를 생성합니다.
     */
    public static BoardMember create(
            BoardId boardId,
            UserId userId,
            BoardRole role) {
        Instant now = Instant.now();
        return BoardMember.builder()
                .memberId(new BoardMemberId())
                .boardId(boardId)
                .userId(userId)
                .role(role)
                .isActive(true)
                .inviteStatus(InviteStatus.ACCEPTED)
                .invitedAt(now)
                .joinedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * 초대를 통해 새로운 보드 멤버를 생성합니다.
     */
    public static BoardMember createInvitation(
            BoardId boardId,
            UserId userId,
            BoardRole role,
            UserId invitedBy) {
        Instant now = Instant.now();
        return BoardMember.builder()
                .memberId(new BoardMemberId())
                .boardId(boardId)
                .userId(userId)
                .role(role)
                .isActive(false)
                .inviteStatus(InviteStatus.PENDING)
                .invitedBy(invitedBy)
                .invitedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * 멤버의 역할을 변경합니다.
     */
    public void changeRole(BoardRole newRole) {
        this.role = newRole;
        markAsUpdated();
    }

    /**
     * 멤버를 비활성화합니다.
     */
    public void deactivate() {
        this.isActive = false;
        markAsUpdated();
    }

    /**
     * 멤버를 활성화합니다.
     */
    public void activate() {
        this.isActive = true;
        markAsUpdated();
    }

    /**
     * 초대를 수락합니다.
     */
    public void acceptInvitation() {
        this.inviteStatus = InviteStatus.ACCEPTED;
        this.isActive = true;
        this.joinedAt = Instant.now();
        markAsUpdated();
    }

    /**
     * 초대를 거절합니다.
     */
    public void declineInvitation() {
        this.inviteStatus = InviteStatus.DECLINED;
        this.isActive = false;
        markAsUpdated();
    }

    /**
     * 초대를 만료시킵니다.
     */
    public void expireInvitation() {
        this.inviteStatus = InviteStatus.EXPIRED;
        this.isActive = false;
        markAsUpdated();
    }

    /**
     * 사용자가 보드에 읽기 권한이 있는지 확인합니다.
     */
    public boolean canRead() {
        return isActive && inviteStatus == InviteStatus.ACCEPTED && role.hasReadPermission();
    }

    /**
     * 사용자가 보드에 쓰기 권한이 있는지 확인합니다.
     */
    public boolean canWrite() {
        return isActive && inviteStatus == InviteStatus.ACCEPTED && role.hasWritePermission();
    }

    /**
     * 사용자가 보드에 관리 권한이 있는지 확인합니다.
     */
    public boolean canAdmin() {
        return isActive && inviteStatus == InviteStatus.ACCEPTED && role.hasAdminPermission();
    }

    /**
     * 사용자가 보드에 소유자 권한이 있는지 확인합니다.
     */
    public boolean canOwn() {
        return isActive && inviteStatus == InviteStatus.ACCEPTED && role.hasOwnerPermission();
    }

    /**
     * 초대가 대기 중인지 확인합니다.
     */
    public boolean isPending() {
        return inviteStatus == InviteStatus.PENDING;
    }

    /**
     * 초대가 수락되었는지 확인합니다.
     */
    public boolean isAccepted() {
        return inviteStatus == InviteStatus.ACCEPTED;
    }

    /**
     * 초대가 거절되었는지 확인합니다.
     */
    public boolean isDeclined() {
        return inviteStatus == InviteStatus.DECLINED;
    }

    /**
     * 초대가 만료되었는지 확인합니다.
     */
    public boolean isExpired() {
        return inviteStatus == InviteStatus.EXPIRED;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        BoardMember that = (BoardMember) obj;
        return Objects.equals(memberId, that.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId);
    }

    @Override
    public String toString() {
        return String.format("BoardMember{memberId=%s, boardId=%s, userId=%s, role=%s, status=%s}",
                memberId, boardId, userId, role, inviteStatus);
    }
}

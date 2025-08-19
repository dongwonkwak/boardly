package com.boardly.infrastructure.adapters.out.persistence.board;

import java.time.Instant;
import java.util.Objects;

import com.boardly.features.board.domain.BoardMember;
import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.BoardMemberId;
import com.boardly.shared.common.value.BoardRole;
import com.boardly.shared.common.value.UserId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "board_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardMemberEntity {

    @Id
    @Column(name = "member_id", nullable = false, length = 50)
    private String memberId;

    @Column(name = "board_id", nullable = false, length = 50)
    private String boardId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private BoardRole role;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    // 초대/멤버십 관리 (schema.md 반영)
    @Enumerated(EnumType.STRING)
    @Column(name = "invite_status")
    private BoardMember.InviteStatus inviteStatus;

    @Column(name = "invited_by", length = 50)
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
    private BoardMemberEntity(
            String memberId,
            String boardId,
            String userId,
            BoardRole role,
            boolean isActive,
            BoardMember.InviteStatus inviteStatus,
            String invitedBy,
            Instant invitedAt,
            Instant joinedAt,
            Instant createdAt,
            Instant updatedAt) {
        this.memberId = memberId;
        this.boardId = boardId;
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

    /**
     * Domain BoardMember 객체로 변환
     */
    public BoardMember toDomainEntity() {
        return BoardMember.builder()
                .memberId(new BoardMemberId(memberId))
                .boardId(new BoardId(boardId))
                .userId(new UserId(userId))
                .role(role)
                .isActive(isActive)
                .inviteStatus(inviteStatus)
                .invitedBy(invitedBy != null ? new UserId(invitedBy) : null)
                .invitedAt(invitedAt)
                .joinedAt(joinedAt)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    /**
     * Domain BoardMember 객체로부터 엔티티 생성
     */
    public static BoardMemberEntity fromDomainEntity(BoardMember boardMember) {
        return BoardMemberEntity.builder()
                .memberId(boardMember.getMemberId().getId())
                .boardId(boardMember.getBoardId().getId())
                .userId(boardMember.getUserId().getId())
                .role(boardMember.getRole())
                .isActive(boardMember.isActive())
                .inviteStatus(boardMember.getInviteStatus())
                .invitedBy(boardMember.getInvitedBy() != null ? boardMember.getInvitedBy().getId() : null)
                .invitedAt(boardMember.getInvitedAt())
                .joinedAt(boardMember.getJoinedAt())
                .createdAt(boardMember.getCreatedAt())
                .updatedAt(boardMember.getUpdatedAt())
                .build();
    }

    /**
     * 엔티티 업데이트
     */
    public void updateFromDomain(BoardMember boardMember) {
        this.role = boardMember.getRole();
        this.isActive = boardMember.isActive();
        this.inviteStatus = boardMember.getInviteStatus();
        this.joinedAt = boardMember.getJoinedAt();
        this.updatedAt = boardMember.getUpdatedAt();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        BoardMemberEntity that = (BoardMemberEntity) obj;
        return Objects.equals(memberId, that.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId);
    }

    @Override
    public String toString() {
        return String.format("BoardMemberEntity{memberId=%s, boardId=%s, userId=%s, role=%s, status=%s}",
                memberId, boardId, userId, role, inviteStatus);
    }
}

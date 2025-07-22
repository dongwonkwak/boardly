package com.boardly.features.board.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.BaseEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardMember extends BaseEntity {

    private BoardMemberId memberId;
    private BoardId boardId;
    private UserId userId;
    private BoardRole role;
    private boolean isActive;

    @Builder
    private BoardMember(BoardMemberId memberId, BoardId boardId, UserId userId,
            BoardRole role, boolean isActive, Instant createdAt, Instant updatedAt) {

        super(createdAt, updatedAt);
        this.memberId = memberId;
        this.boardId = boardId;
        this.userId = userId;
        this.role = role;
        this.isActive = isActive;
    }

    /**
     * 새로운 보드 멤버를 생성합니다.
     */
    public static BoardMember create(BoardId boardId, UserId userId, BoardRole role) {
        Instant now = Instant.now();
        return BoardMember.builder()
                .memberId(new BoardMemberId())
                .boardId(boardId)
                .userId(userId)
                .role(role)
                .isActive(true)
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
     * 사용자가 보드에 읽기 권한이 있는지 확인합니다.
     */
    public boolean canRead() {
        return isActive && role.hasReadPermission();
    }

    /**
     * 사용자가 보드에 쓰기 권한이 있는지 확인합니다.
     */
    public boolean canWrite() {
        return isActive && role.hasWritePermission();
    }

    /**
     * 사용자가 보드에 관리 권한이 있는지 확인합니다.
     */
    public boolean canAdmin() {
        return isActive && role.hasAdminPermission();
    }

    /**
     * 사용자가 보드에 소유자 권한이 있는지 확인합니다.
     */
    public boolean canOwn() {
        return isActive && role.hasOwnerPermission();
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
}
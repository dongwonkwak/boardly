package com.boardly.domain.workspace;

import com.boardly.domain.user.UserId;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

/**
 * 워크스페이스 멤버십 도메인 엔티티
 * 합성키 (workspaceId + userId)를 사용하여 식별
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = { "workspaceId", "userId" })
public class WorkspaceMembership {

    private WorkspaceId workspaceId;
    private UserId userId;
    private WorkspaceRole role;
    private Instant joinedAt;

    @Builder
    public WorkspaceMembership(WorkspaceId workspaceId, UserId userId,
            WorkspaceRole role, Instant joinedAt) {
        this.workspaceId = workspaceId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    /**
     * 워크스페이스 멤버십 생성 팩토리 메서드
     */
    public static WorkspaceMembership create(WorkspaceId workspaceId, UserId userId, WorkspaceRole role) {
        return WorkspaceMembership.builder()
                .workspaceId(workspaceId)
                .userId(userId)
                .role(role)
                .joinedAt(Instant.now())
                .build();
    }

    /**
     * 역할 변경
     */
    public void changeRole(WorkspaceRole newRole) {
        this.role = newRole;
    }

    /**
     * 관리자인지 확인
     */
    public boolean isAdmin() {
        return this.role == WorkspaceRole.ADMIN;
    }

    /**
     * 멤버인지 확인
     */
    public boolean isMember() {
        return this.role == WorkspaceRole.MEMBER;
    }
}

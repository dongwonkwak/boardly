package com.boardly.domain.workspace;

import com.boardly.domain.user.UserId;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 워크스페이스 멤버십 도메인 엔티티
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkspaceMembership {

    private WorkspaceMembershipId id;
    private WorkspaceId workspaceId;
    private UserId userId;
    private WorkspaceRole role;
    private Instant joinedAt;

    @Builder
    public WorkspaceMembership(WorkspaceMembershipId id, WorkspaceId workspaceId, UserId userId,
            WorkspaceRole role, Instant joinedAt) {
        this.id = id;
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
                .id(WorkspaceMembershipId.generate())
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

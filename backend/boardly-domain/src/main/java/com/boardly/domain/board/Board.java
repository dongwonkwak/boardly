package com.boardly.domain.board;

import com.boardly.domain.user.UserId;
import com.boardly.domain.workspace.WorkspaceId;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 보드 도메인 엔티티
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board {

    private BoardId id;
    private String title;
    private String description;
    private WorkspaceId workspaceId;
    private UserId ownerId;
    private boolean isPublic;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastAccessedAt;

    @Builder
    public Board(BoardId id, String title, String description, WorkspaceId workspaceId,
            UserId ownerId, boolean isPublic, Instant createdAt, Instant updatedAt, Instant lastAccessedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.workspaceId = workspaceId;
        this.ownerId = ownerId;
        this.isPublic = isPublic;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastAccessedAt = lastAccessedAt;
    }

    /**
     * 보드 생성 팩토리 메서드
     */
    public static Board create(String title, String description, WorkspaceId workspaceId, UserId ownerId,
            boolean isPublic) {
        Instant now = Instant.now();
        return Board.builder()
                .id(BoardId.generate())
                .title(title)
                .description(description)
                .workspaceId(workspaceId)
                .ownerId(ownerId)
                .isPublic(isPublic)
                .createdAt(now)
                .updatedAt(now)
                .lastAccessedAt(now)
                .build();
    }

    /**
     * 보드 정보 업데이트
     */
    public void updateInfo(String title, String description, boolean isPublic) {
        this.title = title;
        this.description = description;
        this.isPublic = isPublic;
        this.updatedAt = Instant.now();
    }

    /**
     * 공개 보드인지 확인
     */
    public boolean isPublic() {
        return this.isPublic;
    }

    /**
     * 보드 제목 유효성 검증
     */
    public boolean isValidTitle() {
        return title != null && !title.trim().isEmpty() && title.length() <= 100;
    }

    /**
     * 보드 설명 유효성 검증
     */
    public boolean isValidDescription() {
        return description == null || description.length() <= 500;
    }

    /**
     * 보드가 특정 워크스페이스에 속하는지 확인
     */
    public boolean belongsToWorkspace(WorkspaceId workspaceId) {
        return this.workspaceId.equals(workspaceId);
    }

    /**
     * 최근 접근 시간 업데이트
     */
    public void updateLastAccessedAt() {
        this.lastAccessedAt = Instant.now();
    }

    /**
     * 특정 사용자가 보드 소유자인지 확인
     */
    public boolean isOwnedBy(UserId userId) {
        return this.ownerId.equals(userId);
    }

    /**
     * 보드 소유권 이전
     */
    public void transferOwnership(UserId newOwnerId) {
        this.ownerId = newOwnerId;
        this.updatedAt = Instant.now();
    }
}

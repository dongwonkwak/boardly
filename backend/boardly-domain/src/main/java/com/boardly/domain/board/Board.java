package com.boardly.domain.board;

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
    private boolean isPublic;
    private Instant createdAt;
    private Instant updatedAt;

    @Builder
    public Board(BoardId id, String title, String description, WorkspaceId workspaceId,
            boolean isPublic, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.workspaceId = workspaceId;
        this.isPublic = isPublic;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 보드 생성 팩토리 메서드
     */
    public static Board create(String title, String description, WorkspaceId workspaceId, boolean isPublic) {
        return Board.builder()
                .id(BoardId.generate())
                .title(title)
                .description(description)
                .workspaceId(workspaceId)
                .isPublic(isPublic)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
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
}

package com.boardly.domain.workspace;

import com.boardly.domain.user.UserId;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 워크스페이스 도메인 엔티티
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Workspace {

    private WorkspaceId id;
    private String name;
    private String description;
    private WorkspaceType type;
    private boolean isPublic;
    private UserId ownerId;
    private Instant createdAt;
    private Instant updatedAt;

    @Builder
    public Workspace(WorkspaceId id, String name, String description, WorkspaceType type,
            boolean isPublic, UserId ownerId, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.isPublic = isPublic;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 워크스페이스 생성 팩토리 메서드
     */
    public static Workspace create(String name, String description, WorkspaceType type, boolean isPublic,
            UserId ownerId) {
        return Workspace.builder()
                .id(WorkspaceId.generate())
                .name(name)
                .description(description)
                .type(type)
                .isPublic(isPublic)
                .ownerId(ownerId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * 워크스페이스 정보 업데이트
     */
    public void updateInfo(String name, String description, boolean isPublic) {
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
        this.updatedAt = Instant.now();
    }

    /**
     * 워크스페이스 타입 변경
     */
    public void changeType(WorkspaceType newType) {
        this.type = newType;
        this.updatedAt = Instant.now();
    }

    /**
     * 공개 워크스페이스인지 확인
     */
    public boolean isPublic() {
        return this.isPublic;
    }
}

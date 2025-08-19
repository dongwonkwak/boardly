package com.boardly.infrastructure.adapters.out.persistence.workspace;

import java.time.Instant;

import com.boardly.features.workspace.domain.Workspace;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.common.value.WorkspaceId;

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
@Table(name = "workspaces")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkspaceEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String workspaceId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50)
    private Workspace.Type type;

    @Column(name = "owner_user_id", length = 50)
    private String ownerUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    @Builder
    private WorkspaceEntity(
            String workspaceId,
            String name,
            String description,
            Workspace.Type type,
            String ownerUserId,
            Instant createdAt,
            Instant updatedAt) {
        this.workspaceId = workspaceId;
        this.name = name;
        this.description = description;
        this.type = type;
        this.ownerUserId = ownerUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Domain Workspace 객체로 변환
     */
    public Workspace toDomainEntity() {
        return Workspace.builder()
                .workspaceId(new WorkspaceId(this.workspaceId))
                .name(this.name)
                .description(this.description)
                .type(this.type)
                .ownerUserId(this.ownerUserId != null ? new UserId(this.ownerUserId) : null)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    /**
     * Domain Workspace 객체로부터 Entity 생성
     */
    public static WorkspaceEntity fromDomainEntity(Workspace workspace) {
        return WorkspaceEntity.builder()
                .workspaceId(workspace.getWorkspaceId().getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .type(workspace.getType())
                .ownerUserId(workspace.getOwnerUserId() != null ? workspace.getOwnerUserId().getId() : null)
                .createdAt(workspace.getCreatedAt())
                .updatedAt(workspace.getUpdatedAt())
                .build();
    }

    /**
     * 도메인 객체의 변경사항을 반영
     */
    public void updateFromDomainEntity(Workspace workspace) {
        this.name = workspace.getName();
        this.description = workspace.getDescription();
        this.type = workspace.getType();
        this.ownerUserId = workspace.getOwnerUserId() != null ? workspace.getOwnerUserId().getId() : null;
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        WorkspaceEntity that = (WorkspaceEntity) obj;
        return workspaceId != null && workspaceId.equals(that.workspaceId);
    }

    @Override
    public int hashCode() {
        return workspaceId != null ? workspaceId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("WorkspaceEntity{id=%s, name='%s', type=%s, owner=%s}",
                workspaceId, name, type, ownerUserId);
    }
}

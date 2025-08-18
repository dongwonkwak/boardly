package com.boardly.infrastructure.adapters.out.persistence.workspace;

import com.boardly.features.workspace.domain.Workspace;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.common.value.WorkspaceId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "workspaces",
    indexes = {
        @Index(name = "idx_workspaces_owner_user_id", columnList = "owner_user_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkspaceEntity {

    @Id
    @Column(name = "id", nullable = false, length = 50)
    private String id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "type", length = 50)
    private String type;

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
        String id,
        String name,
        String description,
        String type,
        String ownerUserId,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.ownerUserId = ownerUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Workspace toDomainEntity() {
        return Workspace.builder()
            .workspaceId(new WorkspaceId(this.id))
            .name(this.name)
            .description(this.description)
            .type(Workspace.Type.valueOf(this.type != null ? this.type : "DEFAULT"))
            .ownerUserId(this.ownerUserId != null ? new UserId(this.ownerUserId) : null)
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .build();
    }

    public static WorkspaceEntity fromDomainEntity(Workspace workspace) {
        return WorkspaceEntity.builder()
            .id(workspace.getWorkspaceId().getId())
            .name(workspace.getName())
            .description(workspace.getDescription())
            .type(workspace.getType() != null ? workspace.getType().name() : null)
            .ownerUserId(workspace.getOwnerUserId() != null ? workspace.getOwnerUserId().getId() : null)
            .createdAt(workspace.getCreatedAt())
            .updatedAt(workspace.getUpdatedAt())
            .build();
    }
}

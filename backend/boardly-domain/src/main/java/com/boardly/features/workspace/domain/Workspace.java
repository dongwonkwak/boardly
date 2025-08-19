package com.boardly.features.workspace.domain;

import java.time.Instant;
import java.util.Objects;

import com.boardly.shared.common.value.UserId;
import com.boardly.shared.common.value.WorkspaceId;
import com.boardly.shared.domain.BaseEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Workspace extends BaseEntity {

    public enum Type {
        PERSONAL, TEAM, DEFAULT
    }

    private WorkspaceId workspaceId;
    private String name;
    private String description;
    private Type type;
    private UserId ownerUserId;

    @Builder
    private Workspace(
            WorkspaceId workspaceId,
            String name,
            String description,
            Type type,
            UserId ownerUserId,
            Instant createdAt,
            Instant updatedAt) {
        super(createdAt, updatedAt);
        this.workspaceId = workspaceId;
        this.name = name;
        this.description = description;
        this.type = type;
        this.ownerUserId = ownerUserId;
    }

    public static Workspace create(String name, String description, Type type, UserId ownerUserId) {
        Instant now = Instant.now();
        return Workspace.builder()
                .workspaceId(new WorkspaceId())
                .name(name)
                .description(description)
                .type(type)
                .ownerUserId(ownerUserId)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void rename(String newName) {
        this.name = newName;
        markAsUpdated();
    }

    public void changeDescription(String newDescription) {
        this.description = newDescription;
        markAsUpdated();
    }

    public void changeType(Type newType) {
        this.type = newType;
        markAsUpdated();
    }

    public boolean isOwner(UserId userId) {
        return ownerUserId != null && ownerUserId.equals(userId);
    }

    /**
     * 워크스페이스가 개인용인지 확인합니다.
     */
    public boolean isPersonal() {
        return type == Type.PERSONAL;
    }

    /**
     * 워크스페이스가 팀용인지 확인합니다.
     */
    public boolean isTeam() {
        return type == Type.TEAM;
    }

    /**
     * 워크스페이스가 기본 워크스페이스인지 확인합니다.
     */
    public boolean isDefault() {
        return type == Type.DEFAULT;
    }

    /**
     * 사용자가 워크스페이스를 관리할 수 있는지 확인합니다.
     */
    public boolean canManage(UserId userId) {
        return isOwner(userId);
    }

    /**
     * 사용자가 워크스페이스에 멤버를 초대할 수 있는지 확인합니다.
     */
    public boolean canInviteMembers(UserId userId) {
        return isOwner(userId);
    }

    /**
     * 사용자가 워크스페이스를 삭제할 수 있는지 확인합니다.
     */
    public boolean canDelete(UserId userId) {
        return isOwner(userId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Workspace that = (Workspace) obj;
        return Objects.equals(workspaceId, that.workspaceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId);
    }

    @Override
    public String toString() {
        return String.format("Workspace{id=%s, name='%s', type=%s, owner=%s, createdAt=%s, updatedAt=%s}",
                workspaceId, name, type, ownerUserId, getCreatedAt(), getUpdatedAt());
    }
}

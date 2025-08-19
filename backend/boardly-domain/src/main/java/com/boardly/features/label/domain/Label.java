package com.boardly.features.label.domain;

import java.time.Instant;

import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.LabelId;
import com.boardly.shared.common.value.WorkspaceId;
import com.boardly.shared.domain.BaseEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Label extends BaseEntity {

    private LabelId labelId;
    private BoardId boardId;
    private WorkspaceId workspaceId;
    private String name;
    private String color;

    @Builder
    private Label(LabelId labelId, BoardId boardId, WorkspaceId workspaceId, String name, String color,
            Instant createdAt, Instant updatedAt) {
        super(createdAt, updatedAt);
        this.labelId = labelId;
        this.boardId = boardId;
        this.workspaceId = workspaceId;
        this.name = name.trim();
        this.color = color.toUpperCase();
    }

    /**
     * 새 라벨 생성 (팩토리 메서드)
     */
    public static Label create(BoardId boardId, WorkspaceId workspaceId, String name, String color) {
        return Label.builder()
                .labelId(new LabelId())
                .boardId(boardId)
                .workspaceId(workspaceId)
                .name(name)
                .color(color)
                .build();
    }

    /**
     * 기존 라벨 복원 (리포지토리용)
     */
    public static Label restore(LabelId labelId, BoardId boardId, WorkspaceId workspaceId, String name, String color,
            Instant createdAt, Instant updatedAt) {
        return Label.builder()
                .labelId(labelId)
                .boardId(boardId)
                .workspaceId(workspaceId)
                .name(name)
                .color(color)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    /**
     * 라벨명 변경
     */
    public void updateName(String newName) {
        this.name = newName.trim();
        markAsUpdated();
    }

    /**
     * 라벨 색상 변경
     */
    public void updateColor(String newColor) {
        this.color = newColor.toUpperCase();
        markAsUpdated();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Label other = (Label) obj;
        return labelId != null && labelId.equals(other.labelId);
    }

    @Override
    public int hashCode() {
        return labelId != null ? labelId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("Label{labelId='%s', boardId='%s', name='%s', color='%s'}",
                labelId, boardId, name, color);
    }
}

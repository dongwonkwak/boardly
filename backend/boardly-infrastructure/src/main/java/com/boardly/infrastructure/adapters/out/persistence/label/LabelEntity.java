package com.boardly.infrastructure.adapters.out.persistence.label;

import java.time.Instant;

import com.boardly.features.label.domain.Label;
import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.LabelId;
import com.boardly.shared.common.value.WorkspaceId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "labels", uniqueConstraints = @UniqueConstraint(columnNames = { "board_id", "name" }), indexes = {
        @Index(name = "idx_label_board_id", columnList = "board_id"),
        @Index(name = "idx_label_name", columnList = "board_id, name"),
        @Index(name = "idx_labels_workspace_id", columnList = "workspace_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LabelEntity {

    @Id
    @Column(name = "label_id", nullable = false, length = 50)
    private String labelId;

    @Column(name = "board_id", nullable = false, length = 50)
    private String boardId;

    // 멀티테넌시를 위한 워크스페이스 식별자 (schema.md 반영)
    @Column(name = "workspace_id")
    private String workspaceId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "color", nullable = false, length = 7)
    private String color;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    @Builder
    private LabelEntity(
            String labelId,
            String boardId,
            String workspaceId,
            String name,
            String color,
            Instant createdAt,
            Instant updatedAt) {
        this.labelId = labelId;
        this.boardId = boardId;
        this.workspaceId = workspaceId;
        this.name = name;
        this.color = color;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 도메인 객체로부터 엔티티 생성
     */
    public static LabelEntity from(Label label) {
        return LabelEntity.builder()
                .labelId(label.getLabelId().getId())
                .boardId(label.getBoardId().getId())
                .workspaceId(label.getWorkspaceId().getId())
                .name(label.getName())
                .color(label.getColor())
                .createdAt(label.getCreatedAt())
                .updatedAt(label.getUpdatedAt())
                .build();
    }

    /**
     * 엔티티를 도메인 객체로 변환
     */
    public Label toDomainEntity() {
        return Label.restore(
                new LabelId(labelId),
                new BoardId(boardId),
                new WorkspaceId(workspaceId),
                name,
                color,
                createdAt,
                updatedAt);
    }

    /**
     * 도메인 객체의 변경사항을 반영
     */
    public void updateFromDomainEntity(Label label) {
        this.name = label.getName();
        this.color = label.getColor();
        this.workspaceId = label.getWorkspaceId().getId();
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        LabelEntity that = (LabelEntity) obj;
        return labelId != null && labelId.equals(that.labelId);
    }

    @Override
    public int hashCode() {
        return labelId != null ? labelId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("LabelEntity{labelId='%s', boardId='%s', workspaceId='%s', name='%s', color='%s'}",
                labelId, boardId, workspaceId, name, color);
    }
}

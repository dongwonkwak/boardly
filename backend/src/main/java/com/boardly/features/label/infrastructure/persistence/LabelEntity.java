package com.boardly.features.label.infrastructure.persistence;

import java.time.Instant;

import com.boardly.features.label.domain.model.Label;
import com.boardly.features.label.domain.model.LabelId;
import com.boardly.features.board.domain.model.BoardId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Index;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
@Table(name = "labels", uniqueConstraints = @UniqueConstraint(columnNames = { "board_id", "name" }), indexes = {
        @Index(name = "idx_label_board_id", columnList = "board_id"),
        @Index(name = "idx_label_name", columnList = "board_id, name")
})
public class LabelEntity {

    @Id
    @Column(name = "label_id", nullable = false, length = 50)
    private String labelId;

    @Column(name = "board_id", nullable = false, length = 50)
    private String boardId;

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

    // 기본 생성자 (JPA 필수)
    protected LabelEntity() {
    }

    /**
     * 도메인 객체로부터 엔티티 생성
     */
    public static LabelEntity from(Label label) {
        LabelEntity entity = new LabelEntity();
        entity.labelId = label.getLabelId().getId();
        entity.boardId = label.getBoardId().getId();
        entity.name = label.getName();
        entity.color = label.getColor();
        entity.createdAt = label.getCreatedAt();
        entity.updatedAt = label.getUpdatedAt();
        return entity;
    }

    /**
     * 엔티티를 도메인 객체로 변환
     */
    public Label toDomainEntity() {
        return Label.restore(
                new LabelId(labelId),
                new BoardId(boardId),
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
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
    public String getLabelId() {
        return labelId;
    }

    public void setLabelId(String labelId) {
        this.labelId = labelId;
    }

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
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
        return String.format("LabelEntity{labelId='%s', boardId='%s', name='%s', color='%s'}",
                labelId, boardId, name, color);
    }
}

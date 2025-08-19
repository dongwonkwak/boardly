package com.boardly.infrastructure.adapters.out.persistence.boardlist;

import java.time.Instant;

import com.boardly.features.boardlist.domain.BoardList;
import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.ListColor;
import com.boardly.shared.common.value.ListId;
import com.boardly.shared.common.value.WorkspaceId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lists")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardListEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String listId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "color", nullable = false, length = 7)
    private String color;

    @Column(name = "board_id", nullable = false)
    private String boardId;

    // 멀티테넌시를 위한 워크스페이스 식별자 (schema.md 반영)
    @Column(name = "workspace_id", nullable = false)
    private String workspaceId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    @Builder
    private BoardListEntity(
            String listId,
            String title,
            String description,
            int position,
            String color,
            String boardId,
            String workspaceId,
            Instant createdAt,
            Instant updatedAt) {
        this.listId = listId;
        this.title = title;
        this.description = description;
        this.position = position;
        this.color = color;
        this.boardId = boardId;
        this.workspaceId = workspaceId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Domain BoardList 객체로 변환
     */
    public BoardList toDomainEntity() {
        return BoardList.builder()
                .listId(new ListId(this.listId))
                .title(this.title)
                .description(this.description)
                .position(this.position)
                .color(ListColor.of(this.color))
                .boardId(new BoardId(this.boardId))
                .workspaceId(new WorkspaceId(this.workspaceId))
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    /**
     * Domain BoardList 객체로부터 Entity 생성
     */
    public static BoardListEntity fromDomainEntity(BoardList boardList) {
        return BoardListEntity.builder()
                .listId(boardList.getListId().getId())
                .title(boardList.getTitle())
                .description(boardList.getDescription())
                .position(boardList.getPosition())
                .color(boardList.getColor().color())
                .boardId(boardList.getBoardId().getId())
                .workspaceId(boardList.getWorkspaceId().getId())
                .createdAt(boardList.getCreatedAt())
                .updatedAt(boardList.getUpdatedAt())
                .build();
    }

    /**
     * 도메인 객체의 변경사항을 반영
     */
    public void updateFromDomainEntity(BoardList boardList) {
        this.title = boardList.getTitle();
        this.description = boardList.getDescription();
        this.position = boardList.getPosition();
        this.color = boardList.getColor().color();
        this.workspaceId = boardList.getWorkspaceId().getId();
        this.updatedAt = Instant.now();
    }

    /**
     * 제목 업데이트
     */
    public void updateTitle(String title) {
        this.title = title;
        this.updatedAt = Instant.now();
    }

    /**
     * 설명 업데이트
     */
    public void updateDescription(String description) {
        this.description = description;
        this.updatedAt = Instant.now();
    }

    /**
     * 위치 업데이트
     */
    public void updatePosition(int position) {
        this.position = position;
        this.updatedAt = Instant.now();
    }

    /**
     * 색상 업데이트
     */
    public void updateColor(String color) {
        this.color = color;
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        BoardListEntity that = (BoardListEntity) obj;
        return listId != null && listId.equals(that.listId);
    }

    @Override
    public int hashCode() {
        return listId != null ? listId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format(
                "BoardListEntity{listId='%s', title='%s', position=%d, color='%s', boardId='%s', createdAt=%s, updatedAt=%s}",
                listId,
                title,
                position,
                color,
                boardId,
                createdAt,
                updatedAt);
    }
}

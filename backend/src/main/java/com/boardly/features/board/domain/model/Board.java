package com.boardly.features.board.domain.model;

import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.BaseEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board extends BaseEntity {

    private BoardId boardId;
    private String title;
    private String description;
    private boolean isArchived;
    private UserId ownerId;

    @Builder
    private Board(
        BoardId boardId, String title, String description, 
        boolean isArchived, UserId ownerId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        
        super(createdAt, updatedAt);
        this.boardId = boardId;
        this.title = title;
        this.description = description;
        this.isArchived = isArchived;
        this.ownerId = ownerId;
    }

    /**
     * 새로운 보드를 생성합니다.
     */
    public static Board create(String title, String description, UserId ownerId) {
        
        return Board.builder()
            .boardId(new BoardId())
            .title(title)
            .description(description != null ? description : "")
            .isArchived(false)
            .ownerId(ownerId)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    /**
     * 보드 제목을 수정합니다.
     */
    public void updateTitle(String title) {
        this.title = title;
        markAsUpdated();
    }

    /**
     * 보드 설명을 수정합니다.
     */
    public void updateDescription(String description) {
        this.description = description != null ? description : "";
        markAsUpdated();
    }

    /**
     * 보드를 아카이브합니다.
     */
    public void archive() {
        this.isArchived = true;
        markAsUpdated();
    }

    /**
     * 보드를 언아카이브합니다.
     */
    public void unarchive() {
        this.isArchived = false;
        markAsUpdated();
    }

    /**
     * 보드가 활성 상태인지 확인합니다.
     */
    public boolean isActive() {
        return !isArchived;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Board board = (Board) o;
        return Objects.equals(boardId, board.boardId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(boardId);
    }

    @Override
    public String toString() {
        return "Board{" +
            "boardId=" + boardId +
            ", title='" + title + '\'' +
            ", isArchived=" + isArchived +
            '}';
    }
}

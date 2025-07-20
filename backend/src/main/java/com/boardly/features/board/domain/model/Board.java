package com.boardly.features.board.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.BaseEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board extends BaseEntity {

    private BoardId boardId;
    private String title;
    private String description;
    private boolean isArchived;
    private UserId ownerId;
    private boolean isStarred;

    @Builder
    private Board(BoardId boardId, String title, String description,
            boolean isArchived, UserId ownerId, boolean isStarred,
            Instant createdAt, Instant updatedAt) {

        super(createdAt, updatedAt);
        this.boardId = boardId;
        this.title = title;
        this.description = description;
        this.isArchived = isArchived;
        this.ownerId = ownerId;
        this.isStarred = isStarred;
    }

    /**
     * 새로운 보드를 생성합니다. (UTC 기준)
     */
    public static Board create(String title, String description, UserId ownerId) {
        Instant now = Instant.now();
        return Board.builder()
                .boardId(new BoardId())
                .title(title)
                .description(description)
                .isArchived(false)
                .ownerId(ownerId)
                .isStarred(false)
                .createdAt(now)
                .updatedAt(now)
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
        this.description = description;
        markAsUpdated();
    }

    /**
     * 보드 즐겨찾기 상태를 수정합니다.
     */
    public void updateStarred(boolean isStarred) {
        this.isStarred = isStarred;
        markAsUpdated();
    }

    /**
     * 보드를 수정합니다.
     */
    public void update(String title, String description) {
        this.title = title;
        this.description = description;
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

    /**
     * 보드가 수정된 적이 있는지 확인
     * (생성 시간과 수정 시간이 다른 경우)
     */
    public boolean hasBeenModified() {
        return !getCreatedAt().equals(getUpdatedAt());
    }

    /**
     * 사용자가 이 보드에 접근할 수 있는지 확인
     */
    public boolean canAccess(UserId userId) {
        return this.ownerId.equals(userId);
    }

    /**
     * 사용자가 이 보드를 수정할 수 있는지 확인
     */
    public boolean canModify(UserId userId) {
        return canAccess(userId) && !isArchived;
    }

    /**
     * 사용자가 이 보드를 아카이브할 수 있는지 확인
     */
    public boolean canArchive(UserId userId) {
        return canAccess(userId);
    }

    /**
     * 사용자가 이 보드의 즐겨찾기를 변경할 수 있는지 확인
     */
    public boolean canToggleStar(UserId userId) {
        return canAccess(userId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Board board = (Board) obj;
        return Objects.equals(boardId, board.boardId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(boardId);
    }

    @Override
    public String toString() {
        return String.format("Board{boardId=%s, title='%s', isArchived=%s, ownerId=%s, createdAt=%s, updatedAt=%s}",
                boardId, title, isArchived, ownerId, getCreatedAt(), getUpdatedAt());
    }
}

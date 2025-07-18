package com.boardly.features.board.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.BaseEntity;

import java.time.Instant;

@Entity
@Table(name = "boards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardEntity extends BaseEntity {

    @Id
    @Column(name = "board_id", nullable = false)
    private String boardId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_archived", nullable = false)
    private boolean isArchived;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(name = "is_starred", nullable = false)
    private boolean isStarred;

    @Version
    @Column(name = "version")
    private Long version;

    @Builder
    private BoardEntity(String boardId, String title, String description, 
                       boolean isArchived, String ownerId,
                       boolean isStarred,
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
     * Domain Board 객체로 변환
     */
    public Board toDomainEntity() {
        return Board.builder()
                .boardId(new BoardId(this.boardId))
                .title(this.title)
                .description(this.description)
                .isArchived(this.isArchived)
                .ownerId(new UserId(this.ownerId))
                .isStarred(this.isStarred)
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .build();
    }

    /**
     * Domain Board 객체로부터 Entity 생성
     */
    public static BoardEntity fromDomainEntity(Board board) {
        return BoardEntity.builder()
                .boardId(board.getBoardId().getId())
                .title(board.getTitle())
                .description(board.getDescription())
                .isArchived(board.isArchived())
                .ownerId(board.getOwnerId().getId())
                .isStarred(board.isStarred())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }

    /**
     * 도메인 객체의 변경사항을 반영
     */
    public void updateFromDomainEntity(Board board) {
        this.title = board.getTitle();
        this.description = board.getDescription();
        this.isArchived = board.isArchived();
        this.ownerId = board.getOwnerId().getId();
        markAsUpdated(); // BaseEntity의 메서드 사용
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BoardEntity that = (BoardEntity) obj;
        return boardId != null && boardId.equals(that.boardId);
    }

    @Override
    public int hashCode() {
        return boardId != null ? boardId.hashCode() : 0;
    }
} 
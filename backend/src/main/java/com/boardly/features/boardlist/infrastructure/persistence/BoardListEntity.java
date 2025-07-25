package com.boardly.features.boardlist.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.board.domain.model.BoardId;

import java.time.Instant;

@Entity
@Table(name = "board_lists")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardListEntity {

    @Id
    @Column(name = "list_id", nullable = false)
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

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    @Builder
    private BoardListEntity(String listId, String title, String description,
            int position, String color, String boardId,
            Instant createdAt, Instant updatedAt) {
        this.listId = listId;
        this.title = title;
        this.description = description;
        this.position = position;
        this.color = color;
        this.boardId = boardId;
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
                .color(com.boardly.features.boardlist.domain.model.ListColor.of(this.color))
                .boardId(new BoardId(this.boardId))
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
                listId, title, position, color, boardId, createdAt, updatedAt);
    }
}
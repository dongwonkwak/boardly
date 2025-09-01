package com.boardly.domain.board;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 보드 컬럼 도메인 엔티티
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardColumn {

    private BoardColumnId id;
    private String name;
    private BoardId boardId;
    private int order;
    private Instant createdAt;

    @Builder
    public BoardColumn(BoardColumnId id, String name, BoardId boardId, int order, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.boardId = boardId;
        this.order = order;
        this.createdAt = createdAt;
    }

    /**
     * 보드 컬럼 생성 팩토리 메서드
     */
    public static BoardColumn create(String name, BoardId boardId, int order) {
        return BoardColumn.builder()
                .id(BoardColumnId.generate())
                .name(name)
                .boardId(boardId)
                .order(order)
                .createdAt(Instant.now())
                .build();
    }

    /**
     * 컬럼 이름 변경
     */
    public void changeName(String newName) {
        this.name = newName;
    }

    /**
     * 컬럼 순서 변경
     */
    public void changeOrder(int newOrder) {
        this.order = newOrder;
    }
}

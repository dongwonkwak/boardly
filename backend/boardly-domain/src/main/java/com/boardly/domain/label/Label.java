package com.boardly.domain.label;

import com.boardly.domain.board.BoardId;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 라벨 도메인 엔티티
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Label {

    private LabelId id;
    private String name;
    private String color;
    private BoardId boardId;
    private Instant createdAt;

    @Builder
    public Label(LabelId id, String name, String color, BoardId boardId, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.boardId = boardId;
        this.createdAt = createdAt;
    }

    /**
     * 라벨 생성 팩토리 메서드
     */
    public static Label create(String name, String color, BoardId boardId) {
        return Label.builder()
                .id(LabelId.generate())
                .name(name)
                .color(color)
                .boardId(boardId)
                .createdAt(Instant.now())
                .build();
    }

    /**
     * 라벨 정보 업데이트
     */
    public void updateInfo(String name, String color) {
        this.name = name;
        this.color = color;
    }
}

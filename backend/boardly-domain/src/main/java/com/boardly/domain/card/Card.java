package com.boardly.domain.card;

import com.boardly.domain.board.BoardColumnId;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Instant;

/**
 * 카드 도메인 엔티티
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Card {

    private CardId id;
    private String title;
    private String description;
    private BoardColumnId columnId;
    private int order;
    private LocalDate dueDate;
    private Instant createdAt;
    private Instant updatedAt;

    @Builder
    public Card(CardId id, String title, String description, BoardColumnId columnId,
            int order, LocalDate dueDate, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.columnId = columnId;
        this.order = order;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 카드 생성 팩토리 메서드
     */
    public static Card create(String title, String description, BoardColumnId columnId, int order, LocalDate dueDate) {
        return Card.builder()
                .id(CardId.generate())
                .title(title)
                .description(description)
                .columnId(columnId)
                .order(order)
                .dueDate(dueDate)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * 카드 정보 업데이트
     */
    public void updateInfo(String title, String description, LocalDate dueDate) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.updatedAt = Instant.now();
    }

    /**
     * 카드 이동
     */
    public void moveToColumn(BoardColumnId newColumnId, int newOrder) {
        this.columnId = newColumnId;
        this.order = newOrder;
        this.updatedAt = Instant.now();
    }

    /**
     * 카드 순서 변경
     */
    public void changeOrder(int newOrder) {
        this.order = newOrder;
        this.updatedAt = Instant.now();
    }

    /**
     * 마감일이 지났는지 확인
     */
    public boolean isOverdue() {
        return this.dueDate != null && this.dueDate.isBefore(LocalDate.now());
    }

    /**
     * 마감일이 오늘인지 확인
     */
    public boolean isDueToday() {
        return this.dueDate != null && this.dueDate.equals(LocalDate.now());
    }
}

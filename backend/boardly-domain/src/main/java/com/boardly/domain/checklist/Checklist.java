package com.boardly.domain.checklist;

import com.boardly.domain.card.CardId;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 체크리스트 도메인 엔티티
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Checklist {

    private ChecklistId id;
    private String title;
    private CardId cardId;
    private int order;
    private Instant createdAt;

    @Builder
    public Checklist(ChecklistId id, String title, CardId cardId, int order, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.cardId = cardId;
        this.order = order;
        this.createdAt = createdAt;
    }

    /**
     * 체크리스트 생성 팩토리 메서드
     */
    public static Checklist create(String title, CardId cardId, int order) {
        return Checklist.builder()
                .id(ChecklistId.generate())
                .title(title)
                .cardId(cardId)
                .order(order)
                .createdAt(Instant.now())
                .build();
    }

    /**
     * 체크리스트 제목 변경
     */
    public void changeTitle(String newTitle) {
        this.title = newTitle;
    }

    /**
     * 체크리스트 순서 변경
     */
    public void changeOrder(int newOrder) {
        this.order = newOrder;
    }
}

package com.boardly.domain.checklist;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 체크리스트 아이템 도메인 엔티티
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChecklistItem {

    private ChecklistItemId id;
    private String content;
    private boolean isCompleted;
    private int order;
    private Instant createdAt;

    @Builder
    public ChecklistItem(ChecklistItemId id, String content,
            boolean isCompleted, int order, Instant createdAt) {
        this.id = id;
        this.content = content;
        this.isCompleted = isCompleted;
        this.order = order;
        this.createdAt = createdAt;
    }

    /**
     * 체크리스트 아이템 생성 팩토리 메서드
     */
    public static ChecklistItem create(String content, int order) {
        return ChecklistItem.builder()
                .id(ChecklistItemId.generate())
                .content(content)
                .isCompleted(false)
                .order(order)
                .createdAt(Instant.now())
                .build();
    }

    /**
     * 완료 상태 토글
     */
    public void toggleCompletion() {
        this.isCompleted = !this.isCompleted;
    }

    /**
     * 내용 업데이트
     */
    public void updateContent(String content) {
        this.content = content;
    }

    /**
     * 순서 변경
     */
    public void changeOrder(int newOrder) {
        this.order = newOrder;
    }
}

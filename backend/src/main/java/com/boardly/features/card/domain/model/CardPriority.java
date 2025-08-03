package com.boardly.features.card.domain.model;

/**
 * 카드 우선순위
 * 
 * @since 1.0.0
 */
public enum CardPriority {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high"),
    URGENT("urgent");

    private final String value;

    CardPriority(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CardPriority fromValue(String value) {
        for (CardPriority priority : values()) {
            if (priority.value.equals(value)) {
                return priority;
            }
        }
        return MEDIUM; // 기본값
    }
}
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
        if (value == null) {
            return null;
        }

        // 공백 제거 및 소문자 변환
        String normalizedValue = value.trim().toLowerCase();

        for (CardPriority priority : values()) {
            if (priority.getValue().equals(normalizedValue)) {
                return priority;
            }
        }

        // 매칭되지 않으면 null 반환 (기본값 대신)
        return null;
    }
}
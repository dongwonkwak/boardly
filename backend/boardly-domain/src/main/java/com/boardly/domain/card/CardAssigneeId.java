package com.boardly.domain.card;

import com.boardly.shared.EntityId;

/**
 * 카드 담당자 ID 도메인 객체
 */
public class CardAssigneeId extends EntityId {

    public CardAssigneeId() {
        super();
    }

    public CardAssigneeId(String value) {
        super(value);
    }

    public static CardAssigneeId generate() {
        return new CardAssigneeId(generateWithPrefix("ca_"));
    }

    public static CardAssigneeId of(String value) {
        return new CardAssigneeId(value);
    }

    @Override
    protected String getPrefix() {
        return "ca_";
    }
}

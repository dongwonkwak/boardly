package com.boardly.domain.card;

import com.boardly.shared.EntityId;

/**
 * 카드 ID 도메인 객체
 */
public class CardId extends EntityId {

    public CardId() {
        super();
    }

    public CardId(String value) {
        super(value);
    }

    public static CardId generate() {
        return new CardId(generateWithPrefix("card_"));
    }

    public static CardId of(String value) {
        return new CardId(value);
    }

    @Override
    protected String getPrefix() {
        return "card_";
    }
}

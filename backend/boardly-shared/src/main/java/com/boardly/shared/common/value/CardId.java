package com.boardly.shared.common.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class CardId extends EntityId {

    public CardId(String cardId) {
        super(cardId);
    }

    public CardId() {
        super();
    }
}

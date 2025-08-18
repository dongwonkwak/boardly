package com.boardly.shared.common.value;

import com.github.f4b6a3.ulid.UlidCreator;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class CardId extends EntityId {

    public CardId(String cardId) {
        super(cardId);
    }

    public CardId() {
        super("card_" + UlidCreator.getUlid().toString());
    }
}

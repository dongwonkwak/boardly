package com.boardly.features.card.domain.model;

import com.boardly.shared.domain.valueobject.EntityId;

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

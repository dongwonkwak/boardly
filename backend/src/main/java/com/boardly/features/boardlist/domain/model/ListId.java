package com.boardly.features.boardlist.domain.model;

import com.boardly.shared.domain.valueobject.EntityId;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ListId extends EntityId {

  public ListId(String listId) {
    super(listId);
  }

  public ListId() {
    super();
  }
}

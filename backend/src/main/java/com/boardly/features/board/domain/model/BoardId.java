package com.boardly.features.board.domain.model;

import com.boardly.shared.domain.valueobject.EntityId;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class BoardId extends EntityId {

  public BoardId(String boardId) {
    super(boardId);
  }

  public BoardId() {
    super();
  }
}

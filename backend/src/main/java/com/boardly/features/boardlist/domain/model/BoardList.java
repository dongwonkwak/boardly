package com.boardly.features.boardlist.domain.model;

import java.time.LocalDateTime;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.shared.domain.common.BaseEntity;

import io.micrometer.common.lang.NonNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardList extends BaseEntity {

  @NonNull
  private ListId listId;
  @NonNull
  private String title;
  @NonNull
  private ListColor color;
  @NonNull
  private int position;
  @NonNull
  private BoardId boardId;

  @Builder
  private BoardList(
    ListId listId, String title, ListColor color, int position, BoardId boardId,
    LocalDateTime createdAt, LocalDateTime updatedAt
  ) {
    super(createdAt, updatedAt);
    this.listId = listId;
    this.title = title;
    this.color = color;
    this.position = position;
    this.boardId = boardId;
  }

  public static BoardList create(String title, int position, BoardId boardId) {
    return BoardList.builder()
      .listId(new ListId())
      .title(title)
      .color(ListColor.defaultColor())
      .position(position)
      .boardId(boardId)
      .createdAt(LocalDateTime.now())
      .updatedAt(LocalDateTime.now())
      .build();
  }

  public static BoardList create(String title, int position, BoardId boardId, ListColor color) {
    return BoardList.builder()
      .listId(new ListId())
      .title(title)
      .color(color)
      .position(position)
      .boardId(boardId)
      .createdAt(LocalDateTime.now())
      .updatedAt(LocalDateTime.now())
      .build();
  }

  public void updateTitle(String title) {
    if (this.title.equals(title)) {
      return;
    }

    this.title = title;
    markAsUpdated();
  }

  public void updateColor(ListColor color) {
    if (this.color.equals(color)) {
      return;
    }

    this.color = color;
    markAsUpdated();
  }

  public void updatePosition(int position) {
    if (this.position == position) {
      return;
    }

    this.position = position;
    markAsUpdated();
  }

  public boolean belongsToBoard(BoardId boardId) {
    return this.boardId.equals(boardId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BoardList boardList = (BoardList) o;
    return listId.equals(boardList.listId);
  }

  @Override
  public int hashCode() {
    return listId.hashCode();
  }

  @Override
  public String toString() {
    return "BoardList{" +
      "listId=" + listId +
      ", title='" + title + '\'' +
      ", color=" + color +
      ", position=" + position +
      ", boardId=" + boardId +
      '}';
  }
}

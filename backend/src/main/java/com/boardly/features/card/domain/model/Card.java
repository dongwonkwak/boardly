package com.boardly.features.card.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.shared.domain.common.BaseEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Card extends BaseEntity {
  private CardId cardId;
  private String title;
  private String description;
  private int position;
  private ListId listId;

  @Builder
  private Card(CardId cardId, String title, String description, int position, ListId listId, Instant createdAt,
      Instant updatedAt) {
    super(createdAt, updatedAt);
    this.cardId = cardId;
    this.title = title;
    this.description = description;
    this.position = position;
    this.listId = listId;
  }

  public static Card create(CardId cardId, String title, String description, int position, ListId listId) {
    return Card.builder()
        .cardId(cardId)
        .title(title.trim())
        .description(description != null ? description.trim() : null)
        .position(position)
        .listId(listId)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();
  }

  /**
   * 카드 제목을 수정합니다.
   */
  public void updateTitle(String title) {
    this.title = title.trim();
    markAsUpdated();
  }

  /**
   * 카드 설명을 수정합니다.
   */
  public void updateDescription(String description) {
    this.description = description != null ? description.trim() : null;
    markAsUpdated();
  }

  /**
   * 카드 위치를 수정합니다.
   */
  public void updatePosition(int position) {
    this.position = position;
    markAsUpdated();
  }

  /**
   * 카드가 속한 리스트를 변경합니다.
   */
  public void moveToList(ListId newListId) {
    this.listId = newListId;
    markAsUpdated();
  }

  /**
   * 카드를 수정합니다.
   */
  public void update(String title, String description) {
    this.title = title.trim();
    this.description = description != null ? description.trim() : null;
    markAsUpdated();
  }

  /*
   * 카드를 복제합니다.
   * 새로운 카드 ID를 생성하고, 제목과 위치를 변경합니다.
   * 생성 시간은 현재 시간으로 설정합니다.
   * 업데이트 시간은 생성 시간으로 설정합니다.
   */
  public Card clone(String newTitle, int newPosition) {
    return Card.builder()
        .cardId(new CardId())
        .title(newTitle.trim())
        .description(description != null ? description.trim() : null)
        .position(newPosition)
        .listId(listId)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();
  }

  /**
   * 카드를 새로운 리스트로 이동합니다.
   */
  public void moveToList(ListId newListId, int newPosition) {
    this.listId = newListId;
    this.position = newPosition;
    markAsUpdated();
  }

  /**
   * 카드를 다른 리스트로 복사합니다.
   */
  public Card cloneToList(String newTitle, ListId newListId, int newPosition) {
    return Card.builder()
        .cardId(new CardId())
        .title(newTitle.trim())
        .description(description != null ? description.trim() : null)
        .position(newPosition)
        .listId(newListId)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null || getClass() != obj.getClass())
      return false;
    Card card = (Card) obj;
    return Objects.equals(cardId, card.cardId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cardId);
  }

  @Override
  public String toString() {
    return String.format("Card{cardId=%s, title='%s', position=%d, listId=%s, createdAt=%s, updatedAt=%s}",
        cardId, title, position, listId, getCreatedAt(), getUpdatedAt());
  }
}

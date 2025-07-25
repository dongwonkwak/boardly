package com.boardly.features.card.infrastructure.persistence;

import java.time.Instant;

import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.boardlist.domain.model.ListId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardEntity {
  @Id
  @Column(name = "card_id", nullable = false)
  private String cardId;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "description", length = 2000)
  private String description;

  @Column(name = "position", nullable = false)
  private int position;

  @Column(name = "list_id", nullable = false)
  private String listId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Version
  @Column(name = "version")
  private Long version;

  @Builder
  private CardEntity(String cardId, String title, String description,
      int position, String listId,
      Instant createdAt, Instant updatedAt) {
    this.cardId = cardId;
    this.title = title;
    this.description = description;
    this.position = position;
    this.listId = listId;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  /**
   * Domain Card 객체로 변환
   */
  public Card toDomainEntity() {
    return Card.builder()
        .cardId(new CardId(this.cardId))
        .title(this.title)
        .description(this.description)
        .position(this.position)
        .listId(new ListId(this.listId))
        .createdAt(this.createdAt)
        .updatedAt(this.updatedAt)
        .build();
  }

  /**
   * Domain Card 객체로부터 Entity 생성
   */
  public static CardEntity fromDomainEntity(Card card) {
    return CardEntity.builder()
        .cardId(card.getCardId().getId())
        .title(card.getTitle())
        .description(card.getDescription())
        .position(card.getPosition())
        .listId(card.getListId().getId())
        .createdAt(card.getCreatedAt())
        .updatedAt(card.getUpdatedAt())
        .build();
  }

  /**
   * 도메인 객체의 변경사항을 반영
   */
  public void updateFromDomainEntity(Card card) {
    this.title = card.getTitle();
    this.description = card.getDescription();
    this.position = card.getPosition();
    this.listId = card.getListId().getId();
    this.updatedAt = Instant.now();
  }

  /**
   * 제목 업데이트
   */
  public void updateTitle(String title) {
    this.title = title;
    this.updatedAt = Instant.now();
  }

  /**
   * 설명 업데이트
   */
  public void updateDescription(String description) {
    this.description = description;
    this.updatedAt = Instant.now();
  }

  /**
   * 위치 업데이트
   */
  public void updatePosition(int position) {
    this.position = position;
    this.updatedAt = Instant.now();
  }

  /**
   * 리스트 ID 업데이트 (카드 이동)
   */
  public void updateListId(String listId) {
    this.listId = listId;
    this.updatedAt = Instant.now();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null || getClass() != obj.getClass())
      return false;
    CardEntity that = (CardEntity) obj;
    return cardId != null && cardId.equals(that.cardId);
  }

  @Override
  public int hashCode() {
    return cardId != null ? cardId.hashCode() : 0;
  }

  @Override
  public String toString() {
    return String.format("CardEntity{cardId='%s', title='%s', position=%d, listId='%s', createdAt=%s, updatedAt=%s}",
        cardId, title, position, listId, createdAt, updatedAt);
  }
}

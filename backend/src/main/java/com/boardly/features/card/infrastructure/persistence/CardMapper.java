package com.boardly.features.card.infrastructure.persistence;

import org.springframework.stereotype.Component;

import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.boardlist.domain.model.ListId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardMapper {
  /**
   * 도메인 모델을 JPA 엔티티로 변환합니다.
   */
  public CardEntity toEntity(Card card) {
    if (card == null) {
      return null;
    }

    return CardEntity.builder()
        .cardId(card.getCardId().getId())
        .listId(card.getListId().getId())
        .title(card.getTitle())
        .description(card.getDescription())
        .position(card.getPosition())
        .createdAt(card.getCreatedAt())
        .updatedAt(card.getUpdatedAt())
        .build();
  }

  /**
   * JPA 엔티티를 도메인 모델로 변환합니다.
   */
  public Card toDomain(CardEntity entity) {
    if (entity == null) {
      return null;
    }

    return Card.builder()
        .cardId(new CardId(entity.getCardId()))
        .listId(new ListId(entity.getListId()))
        .title(entity.getTitle())
        .description(entity.getDescription())
        .position(entity.getPosition())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }

  /**
   * 도메인 모델의 변경사항을 기존 JPA 엔티티에 적용합니다.
   */
  public void updateEntity(Card card, CardEntity entity) {
    if (card == null || entity == null) {
      return;
    }

    entity.updateTitle(card.getTitle());
    entity.updateDescription(card.getDescription());
    entity.updatePosition(card.getPosition());
    entity.updateListId(card.getListId().getId());
  }
}

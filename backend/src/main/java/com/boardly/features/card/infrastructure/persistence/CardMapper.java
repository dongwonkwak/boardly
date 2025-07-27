package com.boardly.features.card.infrastructure.persistence;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.card.domain.valueobject.CardMember;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardMapper {

  private final CardMemberMapper cardMemberMapper;

  /**
   * 도메인 객체를 엔티티로 변환
   */
  public CardEntity toEntity(Card card) {
    return CardEntity.from(card);
  }

  /**
   * JPA 엔티티를 도메인 모델로 변환합니다.
   */
  /**
   * 엔티티를 도메인 객체로 변환
   */
  public Card toDomain(CardEntity entity) {
    // 담당자 정보 변환
    Set<CardMember> assignedMembers = entity.getAssignedMembers().stream()
        .map(cardMemberMapper::toDomain)
        .collect(Collectors.toSet());

    return Card.restore(
        new CardId(entity.getCardId()),
        entity.getTitle(),
        entity.getDescription(),
        entity.getPosition(),
        entity.getDueDate(),
        entity.isArchived(),
        new ListId(entity.getListId()),
        assignedMembers,
        entity.getCommentsCount(),
        entity.getAttachmentsCount(),
        entity.getLabelsCount(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}

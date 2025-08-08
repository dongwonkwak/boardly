package com.boardly.features.card.infrastructure.persistence;

import org.springframework.stereotype.Component;

import com.boardly.features.card.domain.valueobject.CardMember;
import com.boardly.features.user.domain.model.UserId;

@Component
public class CardMemberMapper {
    /**
     * 도메인 값 객체를 엔티티로 변환
     */
    public CardMemberEntity toEntity(String cardId, CardMember cardMember) {
        return CardMemberEntity.from(cardId, cardMember);
    }

    /**
     * 엔티티를 도메인 값 객체로 변환
     */
    public CardMember toDomain(CardMemberEntity entity) {
        return new CardMember(new UserId(entity.getUserId()));
    }
}

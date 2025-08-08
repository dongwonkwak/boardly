package com.boardly.features.card.application.port.input;

import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.user.domain.model.UserId;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카드 상세 정보 조회 쿼리
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetCardDetailQuery {

    private CardId cardId;
    private UserId userId;

    /**
     * 카드 상세 정보 조회 쿼리 생성
     */
    public static GetCardDetailQuery of(CardId cardId, UserId userId) {
        return GetCardDetailQuery.builder()
                .cardId(cardId)
                .userId(userId)
                .build();
    }
}

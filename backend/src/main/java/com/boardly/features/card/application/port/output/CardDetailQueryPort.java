package com.boardly.features.card.application.port.output;

import java.util.Optional;

import com.boardly.features.card.domain.model.CardDetail;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.user.domain.model.UserId;

/**
 * 카드 상세 정보 조회 포트
 */
public interface CardDetailQueryPort {

    /**
     * 카드 상세 정보 조회
     */
    Optional<CardDetail> findCardDetailById(CardId cardId, UserId userId);
}

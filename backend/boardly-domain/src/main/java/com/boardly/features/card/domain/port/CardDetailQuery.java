package com.boardly.features.card.domain.port;

import com.boardly.features.card.domain.aggregate.CardDetail;
import com.boardly.shared.common.value.CardId;
import com.boardly.shared.common.value.UserId;
import java.util.Optional;

/**
 * 카드 상세 정보 조회 포트
 */
public interface CardDetailQuery {
    /**
     * 카드 상세 정보 조회
     */
    Optional<CardDetail> findCardDetailById(CardId cardId, UserId userId);
}

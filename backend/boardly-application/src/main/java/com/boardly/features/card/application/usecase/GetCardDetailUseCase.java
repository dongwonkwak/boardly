package com.boardly.features.card.application.usecase;

import com.boardly.features.card.application.query.GetCardDetailQuery;
import com.boardly.features.card.domain.aggregate.CardDetail;
import com.boardly.shared.common.value.CardId;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.common.error.Failure;

import io.vavr.control.Either;

/**
 * 카드 상세 정보 조회 유스케이스
 */
public interface GetCardDetailUseCase {

    /**
     * 카드 상세 정보 조회
     */
    Either<Failure, CardDetail> getCardDetail(GetCardDetailQuery query);

    /**
     * 카드 상세 정보 조회 (편의 메서드)
     */
    Either<Failure, CardDetail> getCardDetail(CardId cardId, UserId userId);
}

package com.boardly.features.card.application.usecase;

import com.boardly.features.card.application.port.input.GetCardDetailQuery;
import com.boardly.features.card.domain.model.CardDetail;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;

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

package com.boardly.features.card.application.service;

import org.springframework.stereotype.Service;

import com.boardly.features.card.application.port.input.GetCardDetailQuery;
import com.boardly.features.card.application.port.output.CardDetailQueryPort;
import com.boardly.features.card.application.usecase.GetCardDetailUseCase;
import com.boardly.features.card.domain.model.CardDetail;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 카드 상세 정보 조회 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CardDetailQueryService implements GetCardDetailUseCase {

    private final CardDetailQueryPort cardDetailQueryPort;

    @Override
    public Either<Failure, CardDetail> getCardDetail(GetCardDetailQuery query) {
        log.info("카드 상세 정보 조회 요청: cardId={}, userId={}",
                query.getCardId().getId(), query.getUserId().getId());

        return cardDetailQueryPort.findCardDetailById(query.getCardId(), query.getUserId())
                .map(Either::<Failure, CardDetail>right)
                .orElse(Either.left(Failure.ofNotFound("카드를 찾을 수 없습니다.")));
    }

    @Override
    public Either<Failure, CardDetail> getCardDetail(CardId cardId, UserId userId) {
        return getCardDetail(GetCardDetailQuery.of(cardId, userId));
    }
}

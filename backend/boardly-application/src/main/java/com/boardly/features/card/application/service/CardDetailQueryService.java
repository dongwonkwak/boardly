package com.boardly.features.card.application.service;

import com.boardly.features.card.application.query.GetCardDetailQuery;
import com.boardly.features.card.application.usecase.GetCardDetailUseCase;
import com.boardly.features.card.domain.aggregate.CardDetail;
import com.boardly.features.card.domain.port.CardDetailQuery;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.common.value.CardId;
import com.boardly.shared.common.value.UserId;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 카드 상세 정보 조회 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CardDetailQueryService implements GetCardDetailUseCase {

    private final CardDetailQuery cardDetailQueryPort;

    @Override
    public Either<Failure, CardDetail> getCardDetail(GetCardDetailQuery query) {
        log.info(
            "카드 상세 정보 조회 요청: cardId={}, userId={}",
            query.getCardId().getId(),
            query.getUserId().getId()
        );

        return cardDetailQueryPort
            .findCardDetailById(query.getCardId(), query.getUserId())
            .map(Either::<Failure, CardDetail>right)
            .orElse(
                Either.left(Failure.ofNotFound("카드를 찾을 수 없습니다."))
            );
    }

    @Override
    public Either<Failure, CardDetail> getCardDetail(
        CardId cardId,
        UserId userId
    ) {
        return getCardDetail(GetCardDetailQuery.of(cardId, userId));
    }
}

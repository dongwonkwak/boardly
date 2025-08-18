package com.boardly.features.card.application.policy;

import com.boardly.features.card.domain.Card;
import com.boardly.features.card.domain.port.CardRepository;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.common.value.ListId;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardClonePolicy {

    private static final int MAX_CARDS_PER_LIST = 100;
    private final CardRepository cardRepository;

    public Either<Failure, Void> canCloneWithinSameList(Card originalCard) {
        return checkCardCountLimit(originalCard.getListId());
    }

    public Either<Failure, Void> canCloneToAnotherList(Card originalCard, ListId targetListId) {
        return checkCardCountLimit(targetListId);
    }

    private Either<Failure, Void> checkCardCountLimit(ListId listId) {
        long currentCount = cardRepository.countByListId(listId);
        if (currentCount >= MAX_CARDS_PER_LIST) {
            return Either.left(Failure.ofConflict("LIST_CARD_LIMIT_EXCEEDED"));
        }
        return Either.right(null);
    }
}

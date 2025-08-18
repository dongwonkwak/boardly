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
public class CardMovePolicy {

    private static final int MAX_CARDS_PER_LIST = 100;
    private final CardRepository cardRepository;

    public Either<Failure, Void> canMoveWithinSameList(Card card, int newPosition) {
        return validatePosition(newPosition)
            .flatMap(v -> validatePositionRange(card.getListId(), newPosition));
    }

    public Either<Failure, Void> canMoveToAnotherList(Card card, ListId targetListId, int newPosition) {
        return validatePosition(newPosition)
            .flatMap(v -> checkTargetListCardLimit(targetListId))
            .flatMap(v -> validatePositionRange(targetListId, newPosition));
    }

    private Either<Failure, Void> validatePosition(int position) {
        if (position < 0) {
            return Either.left(Failure.ofConflict("POSITION_INVALID"));
        }
        return Either.right(null);
    }

    private Either<Failure, Void> checkTargetListCardLimit(ListId targetListId) {
        long currentCount = cardRepository.countByListId(targetListId);
        if (currentCount >= MAX_CARDS_PER_LIST) {
            return Either.left(Failure.ofConflict("LIST_CARD_LIMIT_EXCEEDED"));
        }
        return Either.right(null);
    }

    private Either<Failure, Void> validatePositionRange(ListId listId, int position) {
        long cardCount = cardRepository.countByListId(listId);
        if (position > cardCount) {
            return Either.left(Failure.ofConflict("POSITION_OUT_OF_RANGE"));
        }
        return Either.right(null);
    }
}

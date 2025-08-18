package com.boardly.features.card.application.policy;

import com.boardly.features.card.domain.config.CardPolicyConfig;
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
public class CardCreationPolicy {

    private final CardRepository cardRepository;
    private final CardPolicyConfig policyConfig;

    public Either<Failure, Void> canCreateCard(ListId listId) {
        long currentCount = cardRepository.countByListId(listId);
        int maxCards = policyConfig.getMaxCardsPerList();
        if (currentCount >= maxCards) {
            return Either.left(Failure.ofForbidden(
                String.format("리스트당 최대 %d개의 카드만 생성할 수 있습니다. (현재: %d개)", maxCards, currentCount)
            ));
        }
        return Either.right(null);
    }
}

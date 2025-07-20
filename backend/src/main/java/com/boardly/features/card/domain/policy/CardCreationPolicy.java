package com.boardly.features.card.domain.policy;

import org.springframework.stereotype.Component;

import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 카드 생성 정책
 * 
 * <p>
 * 카드 생성과 관련된 비즈니스 규칙을 정의하고 검증합니다.
 * 리스트당 카드 개수 제한, 생성 권한 등의 정책을 관리합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardCreationPolicy {

  private final CardRepository cardRepository;
  private final CardPolicyConfig policyConfig;

  /**
   * 카드 생성이 가능한지 검증합니다.
   */
  public Either<Failure, Void> canCreateCard(ListId listId) {
    log.debug("카드 생성 정책 검증 시작: listId={}", listId.getId());

    return checkCardCountLimit(listId)
        .peek(v -> log.debug("카드 생성 정책 검증 성공: listId={}", listId.getId()));
  }

  /**
   * 리스트당 카드 개수 제한을 확인합니다.
   */
  private Either<Failure, Void> checkCardCountLimit(ListId listId) {
    long currentCount = cardRepository.countByListId(listId);
    int maxCards = policyConfig.getMaxCardsPerList(); // ✅ 인터페이스를 통해 설정값 획득

    log.debug("현재 카드 개수 확인: listId={}, currentCount={}, maxCount={}",
        listId.getId(), currentCount, maxCards);

    if (currentCount >= maxCards) {
      log.warn("카드 개수 제한 초과: listId={}, currentCount={}, maxCount={}",
          listId.getId(), currentCount, maxCards);
      return Either.left(Failure.ofForbidden(
          String.format("리스트당 최대 %d개의 카드만 생성할 수 있습니다. (현재: %d개)",
              maxCards, currentCount)));
    }

    return Either.right(null);
  }

  /**
   * 최대 카드 개수를 반환합니다.
   */
  public int getMaxCardsPerList() {
    return policyConfig.getMaxCardsPerList();
  }

  /**
   * 추가 생성 가능한 카드 개수를 반환합니다.
   */
  public long getAvailableCardSlots(ListId listId) {
    long currentCount = cardRepository.countByListId(listId);
    int maxCards = policyConfig.getMaxCardsPerList();
    return Math.max(0, maxCards - currentCount);
  }

  /**
   * 기본 최대 카드 개수를 반환합니다.
   */
  public static int getDefaultMaxCardsPerList() {
    return CardPolicyConfig.Defaults.MAX_CARDS_PER_LIST;
  }
}

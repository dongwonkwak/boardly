package com.boardly.features.card.domain.policy;

import org.springframework.stereotype.Component;

import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 카드 복제 정책
 * 
 * <p>
 * 카드 복제와 관련된 비즈니스 규칙을 정의하고 검증합니다.
 * 복제 대상 리스트의 카드 개수 제한, 복제 권한 등의 정책을 관리합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardClonePolicy {

  private static final int MAX_CARDS_PER_LIST = 100;

  private final CardRepository cardRepository;

  /**
   * 같은 리스트에 카드 복제가 가능한지 검증합니다.
   * 
   * @param originalCard 원본 카드
   * @return 성공 시 Right(Void), 실패 시 Left(Failure)
   */
  public Either<Failure, Void> canCloneWithinSameList(Card originalCard) {
    log.debug("같은 리스트 내 카드 복제 정책 검증: cardId={}, listId={}",
        originalCard.getCardId().getId(), originalCard.getListId().getId());

    return checkCardCountLimit(originalCard.getListId())
        .peek(v -> log.debug("같은 리스트 내 카드 복제 정책 검증 성공"));
  }

  /**
   * 다른 리스트로 카드 복제가 가능한지 검증합니다.
   * 
   * @param originalCard 원본 카드
   * @param targetListId 대상 리스트 ID
   * @return 성공 시 Right(Void), 실패 시 Left(Failure)
   */
  public Either<Failure, Void> canCloneToAnotherList(Card originalCard, ListId targetListId) {
    log.debug("다른 리스트로 카드 복제 정책 검증: cardId={}, sourceListId={}, targetListId={}",
        originalCard.getCardId().getId(), originalCard.getListId().getId(), targetListId.getId());

    return checkCardCountLimit(targetListId)
        .peek(v -> log.debug("다른 리스트로 카드 복제 정책 검증 성공"));
  }

  /**
   * 리스트당 카드 개수 제한을 확인합니다.
   */
  private Either<Failure, Void> checkCardCountLimit(ListId listId) {
    long currentCount = cardRepository.countByListId(listId);

    log.debug("카드 개수 제한 확인: listId={}, currentCount={}, maxCount={}",
        listId.getId(), currentCount, MAX_CARDS_PER_LIST);

    if (currentCount >= MAX_CARDS_PER_LIST) {
      log.warn("카드 개수 제한 초과: listId={}, currentCount={}, maxCount={}",
          listId.getId(), currentCount, MAX_CARDS_PER_LIST);
      return Either.left(Failure.ofConflict("LIST_CARD_LIMIT_EXCEEDED"));
    }

    return Either.right(null);
  }

  /**
   * 최대 카드 개수를 반환합니다.
   */
  public int getMaxCardsPerList() {
    return MAX_CARDS_PER_LIST;
  }

}

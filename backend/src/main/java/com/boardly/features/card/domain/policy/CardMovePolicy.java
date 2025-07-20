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
 * 카드 이동 정책
 * 
 * <p>
 * 카드 이동과 관련된 비즈니스 규칙을 정의하고 검증합니다.
 * 대상 리스트의 카드 개수 제한, 이동 권한 등의 정책을 관리합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardMovePolicy {

  private static final int MAX_CARDS_PER_LIST = 100;

  private final CardRepository cardRepository;

  /**
   * 같은 리스트 내에서 카드 이동이 가능한지 검증합니다.
   * 
   * @param card        이동할 카드
   * @param newPosition 새로운 위치
   * @return 성공 시 Right(Void), 실패 시 Left(Failure)
   */
  public Either<Failure, Void> canMoveWithinSameList(Card card, int newPosition) {
    log.debug("같은 리스트 내 카드 이동 정책 검증: cardId={}, oldPosition={}, newPosition={}",
        card.getCardId().getId(), card.getPosition(), newPosition);

    // 위치 유효성 검증
    return validatePosition(newPosition)
        .flatMap(v -> validatePositionRange(card.getListId(), newPosition))
        .peek(v -> log.debug("같은 리스트 내 카드 이동 정책 검증 성공"));
  }

  /**
   * 다른 리스트로 카드 이동이 가능한지 검증합니다.
   * 
   * @param card         이동할 카드
   * @param targetListId 대상 리스트 ID
   * @param newPosition  새로운 위치
   * @return 성공 시 Right(Void), 실패 시 Left(Failure)
   */
  public Either<Failure, Void> canMoveToAnotherList(Card card, ListId targetListId, int newPosition) {
    log.debug("다른 리스트로 카드 이동 정책 검증: cardId={}, sourceListId={}, targetListId={}, newPosition={}",
        card.getCardId().getId(), card.getListId().getId(), targetListId.getId(), newPosition);

    // 1. 위치 유효성 검증
    return validatePosition(newPosition)
        // 2. 대상 리스트 카드 개수 제한 확인
        .flatMap(v -> checkTargetListCardLimit(targetListId))
        // 3. 대상 리스트 내 위치 범위 검증
        .flatMap(v -> validatePositionRange(targetListId, newPosition))
        .peek(v -> log.debug("다른 리스트로 카드 이동 정책 검증 성공"));
  }

  /**
   * 위치 값의 기본 유효성을 검증합니다.
   */
  private Either<Failure, Void> validatePosition(int position) {
    if (position < 0) {
      log.warn("잘못된 카드 위치: position={}", position);
      return Either.left(Failure.ofConflict("POSITION_INVALID"));
    }
    return Either.right(null);
  }

  /**
   * 대상 리스트의 카드 개수 제한을 확인합니다.
   */
  private Either<Failure, Void> checkTargetListCardLimit(ListId targetListId) {
    long currentCount = cardRepository.countByListId(targetListId);

    log.debug("대상 리스트 카드 개수 확인: targetListId={}, currentCount={}, maxCount={}",
        targetListId.getId(), currentCount, MAX_CARDS_PER_LIST);

    if (currentCount >= MAX_CARDS_PER_LIST) {
      log.warn("대상 리스트 카드 개수 제한 초과: targetListId={}, currentCount={}, maxCount={}",
          targetListId.getId(), currentCount, MAX_CARDS_PER_LIST);
      return Either.left(Failure.ofConflict("LIST_CARD_LIMIT_EXCEEDED"));
    }

    return Either.right(null);
  }

  /**
   * 리스트 내 위치 범위를 검증합니다.
   */
  private Either<Failure, Void> validatePositionRange(ListId listId, int position) {
    long cardCount = cardRepository.countByListId(listId);

    // 새로운 위치는 현재 카드 개수보다 클 수 없음 (0-based index)
    if (position > cardCount) {
      log.warn("위치 범위 초과: listId={}, position={}, cardCount={}",
          listId.getId(), position, cardCount);
      return Either.left(Failure.ofConflict("POSITION_OUT_OF_RANGE"));
    }

    return Either.right(null);
  }
}

package com.boardly.features.card.domain.repository;

import java.util.List;
import java.util.Optional;

import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

/**
 * 카드 도메인 Repository 인터페이스
 * 도메인 레이어에서 정의하여 의존성 역전 원칙을 적용
 */
public interface CardRepository {

  /**
   * 카드를 저장합니다.
   */
  Either<Failure, Card> save(Card card);

  /**
   * 카드 ID로 카드를 조회합니다.
   */
  Optional<Card> findById(CardId cardId);

  /**
   * 리스트 ID로 카드 목록을 조회합니다 (위치 순서대로).
   */
  List<Card> findByListIdOrderByPosition(ListId listId);

  /**
   * 리스트 ID로 카드 목록을 조회합니다.
   */
  List<Card> findByListId(ListId listId);

  /**
   * 리스트 ID와 위치보다 큰 카드들을 조회합니다.
   */
  List<Card> findByListIdAndPositionGreaterThan(ListId listId, int position);

  /**
   * 리스트 ID와 위치 범위로 카드들을 조회합니다.
   */
  List<Card> findByListIdAndPositionBetween(ListId listId, int startPosition, int endPosition);

  /**
   * 카드를 삭제합니다.
   */
  Either<Failure, Void> delete(CardId cardId);

  /**
   * 카드가 존재하는지 확인합니다.
   */
  boolean existsById(CardId cardId);

  /**
   * 리스트별 카드 개수를 조회합니다.
   */
  long countByListId(ListId listId);

  /**
   * 제목으로 카드를 검색합니다.
   */
  List<Card> findByListIdAndTitleContaining(ListId listId, String title);

  /**
   * 리스트 ID와 카드 ID로 카드를 조회합니다 (권한 확인용).
   */
  Optional<Card> findByIdAndListId(CardId cardId, ListId listId);

  /**
   * 카드 목록을 일괄 저장합니다.
   */
  List<Card> saveAll(List<Card> cards);

  /**
   * 리스트 ID와 위치로 카드를 조회합니다.
   */
  Optional<Card> findByListIdAndPosition(ListId listId, int position);

  /**
   * 리스트 ID의 최대 위치를 조회합니다.
   */
  Optional<Integer> findMaxPositionByListId(ListId listId);

  /**
   * 여러 리스트의 카드들을 조회합니다.
   */
  List<Card> findByListIdIn(List<ListId> listIds);

  /**
   * 리스트의 모든 카드를 삭제합니다.
   */
  Either<Failure, Void> deleteByListId(ListId listId);
}

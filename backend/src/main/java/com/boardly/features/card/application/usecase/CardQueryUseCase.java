package com.boardly.features.card.application.usecase;

import java.util.List;

import com.boardly.features.card.domain.model.Card;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

/**
 * 카드 조회 유스케이스 인터페이스
 * 
 * <p>
 * 카드 단건 조회, 리스트별 카드 목록 조회, 카드 검색 기능을 제공합니다.
 */
public interface CardQueryUseCase {
  /**
   * 카드 단건 조회
   */
  Either<Failure, Card> getCard(CardId cardId, UserId userId);

  /**
   * 리스트별 카드 목록 조회
   */
  Either<Failure, List<Card>> getCardsByListId(ListId listId, UserId userId);

  /**
   * 카드 검색
   */
  Either<Failure, List<Card>> searchCards(ListId listId, String searchTerm, UserId userId);
}

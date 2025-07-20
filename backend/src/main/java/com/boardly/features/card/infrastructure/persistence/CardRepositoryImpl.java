package com.boardly.features.card.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.shared.domain.common.Failure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.vavr.control.Either;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CardRepositoryImpl implements CardRepository {

  private final CardJpaRepository cardJpaRepository;
  private final CardMapper cardMapper;

  @Override
  public Either<Failure, Card> save(Card card) {
    log.debug("카드 저장 시작: cardId={}, title={}", card.getCardId(), card.getTitle());

    try {
      var entity = cardMapper.toEntity(card);
      var savedEntity = cardJpaRepository.save(entity);
      var savedCard = cardMapper.toDomain(savedEntity);
      log.debug("카드 저장 성공: cardId={}, title={}",
          savedCard.getCardId(), savedCard.getTitle());

      return Either.right(savedCard);
    } catch (Exception e) {
      log.error("카드 저장 실패: cardId={}, title={}, 예외={}", card.getCardId(), card.getTitle(), e.getMessage());
      return Either.left(Failure.ofInternalServerError("카드 저장 실패: " + e.getMessage()));
    }
  }

  @Override
  public Optional<Card> findById(CardId cardId) {
    log.debug("카드 조회 시작: cardId={}", cardId);

    var card = cardJpaRepository.findById(cardId.getId())
        .map(cardMapper::toDomain);

    if (card.isPresent()) {
      log.debug("카드 조회 완료: cardId={}, title={}",
          cardId.getId(), card.get().getTitle());
    }

    return card;
  }

  @Override
  public List<Card> findByListIdOrderByPosition(ListId listId) {
    log.debug("리스트 ID로 카드 조회 시작 (위치 순서): listId={}", listId.getId());
    var entities = cardJpaRepository.findByListIdOrderByPosition(listId.getId());
    var cards = entities.stream()
        .map(cardMapper::toDomain)
        .toList();
    log.debug("리스트 ID로 카드 조회 완료: listId={}, 카드 개수={}",
        listId.getId(), cards.size());
    return cards;
  }

  @Override
  public List<Card> findByListId(ListId listId) {
    log.debug("리스트 ID로 카드 조회 시작: listId={}", listId.getId());
    var entities = cardJpaRepository.findByListId(listId.getId());
    var cards = entities.stream()
        .map(cardMapper::toDomain)
        .toList();
    log.debug("리스트 ID로 카드 조회 완료: listId={}, 카드 개수={}",
        listId.getId(), cards.size());
    return cards;
  }

  @Override
  public List<Card> findByListIdAndPositionGreaterThan(ListId listId, int position) {
    log.debug("리스트 ID와 위치로 카드 조회 시작: listId={}, position={}",
        listId.getId(), position);
    var entities = cardJpaRepository.findByListIdAndPositionGreaterThan(listId.getId(), position);
    var cards = entities.stream()
        .map(cardMapper::toDomain)
        .toList();
    log.debug("리스트 ID와 위치로 카드 조회 완료: listId={}, 카드 개수={}",
        listId.getId(), cards.size());
    return cards;
  }

  @Override
  public List<Card> findByListIdAndPositionBetween(ListId listId, int startPosition, int endPosition) {
    log.debug("리스트 ID와 위치 범위로 카드 조회 시작: listId={}, startPosition={}, endPosition={}",
        listId.getId(), startPosition, endPosition);
    var entities = cardJpaRepository.findByListIdAndPositionBetween(listId.getId(), startPosition, endPosition);
    var cards = entities.stream()
        .map(cardMapper::toDomain)
        .toList();
    log.debug("리스트 ID와 위치 범위로 카드 조회 완료: listId={}, 카드 개수={}",
        listId.getId(), cards.size());
    return cards;
  }

  @Override
  public Either<Failure, Void> delete(CardId cardId) {
    try {
      log.debug("카드 삭제 시작: cardId={}", cardId.getId());

      if (!cardJpaRepository.existsById(cardId.getId())) {
        log.warn("삭제할 카드가 존재하지 않음: cardId={}", cardId.getId());
        return Either.left(Failure.ofNotFound("카드를 찾을 수 없습니다."));
      }

      cardJpaRepository.deleteById(cardId.getId());
      log.debug("카드 삭제 완료: cardId={}", cardId.getId());

      return Either.right(null);
    } catch (Exception e) {
      log.error("카드 삭제 실패: cardId={}, error={}", cardId.getId(), e.getMessage(), e);
      return Either.left(Failure.ofInternalServerError("카드 삭제에 실패했습니다: " + e.getMessage()));
    }
  }

  @Override
  public boolean existsById(CardId cardId) {
    log.debug("카드 ID 존재 확인 시작: cardId={}", cardId.getId());
    boolean exists = cardJpaRepository.existsById(cardId.getId());
    log.debug("카드 ID 존재 확인 완료: cardId={}, 존재={}", cardId.getId(), exists);
    return exists;
  }

  @Override
  public long countByListId(ListId listId) {
    log.debug("리스트별 카드 개수 조회 시작: listId={}", listId.getId());
    long count = cardJpaRepository.countByListId(listId.getId());
    log.debug("리스트별 카드 개수 조회 완료: listId={}, count={}", listId.getId(), count);
    return count;
  }

  @Override
  public List<Card> findByListIdAndTitleContaining(ListId listId, String title) {
    log.debug("리스트 ID와 제목으로 카드 조회 시작: listId={}, title={}",
        listId.getId(), title);
    var entities = cardJpaRepository.findByListIdAndTitleContaining(listId.getId(), title);
    var cards = entities.stream()
        .map(cardMapper::toDomain)
        .toList();
    log.debug("리스트 ID와 제목으로 카드 조회 완료: listId={}, 카드 개수={}",
        listId.getId(), cards.size());
    return cards;
  }

  @Override
  public Optional<Card> findByIdAndListId(CardId cardId, ListId listId) {
    log.debug("카드 ID와 리스트 ID로 카드 조회 시작: cardId={}, listId={}",
        cardId.getId(), listId.getId());
    var entity = cardJpaRepository.findByCardIdAndListId(cardId.getId(), listId.getId());
    var card = entity.map(cardMapper::toDomain);
    if (card.isPresent()) {
      log.debug("카드 ID와 리스트 ID로 카드 조회 완료: cardId={}, listId={}, title={}",
          cardId.getId(), listId.getId(), card.get().getTitle());
    }
    return card;
  }

  @Override
  public List<Card> saveAll(List<Card> cards) {
    log.debug("카드 목록 저장 시작: 카드 개수={}", cards.size());

    var entities = cards.stream()
        .map(cardMapper::toEntity)
        .toList();

    var savedEntities = cardJpaRepository.saveAll(entities);
    var savedCards = savedEntities.stream()
        .map(cardMapper::toDomain)
        .toList();

    log.debug("카드 목록 저장 완료: 저장된 카드 개수={}", savedCards.size());
    return savedCards;
  }

  @Override
  public Optional<Card> findByListIdAndPosition(ListId listId, int position) {
    log.debug("리스트 ID와 위치로 카드 조회 시작: listId={}, position={}",
        listId.getId(), position);
    var entity = cardJpaRepository.findByListIdAndPosition(listId.getId(), position);
    var card = entity.map(cardMapper::toDomain);
    if (card.isPresent()) {
      log.debug("리스트 ID와 위치로 카드 조회 완료: listId={}, position={}, title={}",
          listId.getId(), position, card.get().getTitle());
    }
    return card;
  }

  @Override
  public Optional<Integer> findMaxPositionByListId(ListId listId) {
    log.debug("리스트 ID의 최대 위치 조회 시작: listId={}", listId.getId());
    var maxPosition = cardJpaRepository.findMaxPositionByListId(listId.getId());
    log.debug("리스트 ID의 최대 위치 조회 완료: listId={}, maxPosition={}",
        listId.getId(), maxPosition.orElse(-1));
    return maxPosition;
  }

  @Override
  public List<Card> findByListIdIn(List<ListId> listIds) {
    log.debug("여러 리스트의 카드들 조회 시작: 리스트 개수={}", listIds.size());
    var listIdStrings = listIds.stream()
        .map(ListId::getId)
        .toList();
    var entities = cardJpaRepository.findByListIdIn(listIdStrings);
    var cards = entities.stream()
        .map(cardMapper::toDomain)
        .toList();
    log.debug("여러 리스트의 카드들 조회 완료: 카드 개수={}", cards.size());
    return cards;
  }

  @Override
  public Either<Failure, Void> deleteByListId(ListId listId) {
    try {
      log.debug("리스트의 모든 카드 삭제 시작: listId={}", listId.getId());
      cardJpaRepository.deleteByListId(listId.getId());
      log.debug("리스트의 모든 카드 삭제 완료: listId={}", listId.getId());
      return Either.right(null);
    } catch (Exception e) {
      log.error("리스트의 카드 삭제 실패: listId={}, error={}", listId.getId(), e.getMessage(), e);
      return Either.left(Failure.ofInternalServerError("리스트의 카드 삭제에 실패했습니다: " + e.getMessage()));
    }
  }
}

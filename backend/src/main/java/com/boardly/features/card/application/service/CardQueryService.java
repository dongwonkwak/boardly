package com.boardly.features.card.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardly.features.card.application.usecase.CardQueryUseCase;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.application.validation.Validator;
import com.boardly.shared.domain.common.Failure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.vavr.control.Either;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CardQueryService implements CardQueryUseCase {

  private final CardRepository cardRepository;
  private final BoardListRepository boardListRepository;
  private final BoardRepository boardRepository;
  private final CommonValidationRules commonValidationRules;
  private final ValidationMessageResolver validationMessageResolver;

  @Override
  public Either<Failure, Card> getCard(CardId cardId, UserId userId) {

    log.info("카드 조회 시작: cardId={}, userId={}",
        cardId != null ? cardId.getId() : "null",
        userId != null ? userId.getId() : "null");

    // 1. 입력 검증
    var validationResult = validateCardQuery(cardId, userId);
    if (validationResult.isInvalid()) {
      log.warn("카드 조회 입력 검증 실패: {}", validationResult.getErrorsAsCollection());
      return Either.left(Failure.ofInputError(
          validationMessageResolver.getMessage("validation.input.invalid"),
          "INVALID_INPUT",
          List.copyOf(validationResult.getErrorsAsCollection())));
    }

    return findCard(cardId, userId);
  }

  @Override
  public Either<Failure, List<Card>> getCardsByListId(ListId listId, UserId userId) {
    log.info("리스트별 카드 조회 시작: listId={}, userId={}",
        listId != null ? listId.getId() : "null",
        userId != null ? userId.getId() : "null");

    // 1. 입력 검증
    var validationResult = validateListQuery(listId, userId);
    if (validationResult.isInvalid()) {
      log.warn("리스트별 카드 조회 입력 검증 실패: {}", validationResult.getErrorsAsCollection());
      return Either.left(Failure.ofInputError(
          validationMessageResolver.getMessage("validation.input.invalid"),
          "INVALID_INPUT",
          List.copyOf(validationResult.getErrorsAsCollection())));
    }

    return findCardsByListId(listId, userId);
  }

  @Override
  public Either<Failure, List<Card>> searchCards(ListId listId, String searchTerm, UserId userId) {
    log.info("카드 검색 시작: listId={}, searchTerm={}, userId={}",
        listId != null ? listId.getId() : "null",
        searchTerm,
        userId != null ? userId.getId() : "null");

    // 1. 입력 검증
    var validationResult = validateListQuery(listId, userId);
    if (validationResult.isInvalid()) {
      log.warn("카드 검색 입력 검증 실패: {}", validationResult.getErrorsAsCollection());
      return Either.left(Failure.ofInputError(
          validationMessageResolver.getMessage("validation.input.invalid"),
          "INVALID_INPUT",
          List.copyOf(validationResult.getErrorsAsCollection())));
    }

    // 2. 검색어 검증
    if (searchTerm == null || searchTerm.trim().isEmpty()) {
      log.warn("검색어가 비어있음: userId={}", userId != null ? userId.getId() : "null");
      return Either.left(Failure.ofInputError(
          validationMessageResolver.getMessage("validation.search.term.required"),
          "SEARCH_TERM_EMPTY",
          null));
    }

    return findCardsBySearch(listId, searchTerm, userId);
  }

  /**
   * 카드 조회를 위한 기본 검증 (CardId, UserId)
   */
  private ValidationResult<Object> validateCardQuery(CardId cardId, UserId userId) {
    return Validator.combine(
        commonValidationRules.cardIdRequired(cmd -> cardId),
        commonValidationRules.userIdRequired(cmd -> userId)).validate(new Object());
  }

  /**
   * 리스트 조회를 위한 기본 검증 (ListId, UserId)
   */
  private ValidationResult<Object> validateListQuery(ListId listId, UserId userId) {
    return Validator.combine(
        commonValidationRules.listIdRequired(cmd -> listId),
        commonValidationRules.userIdRequired(cmd -> userId)).validate(new Object());
  }

  /**
   * 카드 조회 (단건)
   */
  private Either<Failure, Card> findCard(CardId cardId, UserId userId) {
    // 2단계: 카드 존재 확인
    Optional<Card> cardOpt = cardRepository.findById(cardId);
    if (cardOpt.isEmpty()) {
      log.warn("카드를 찾을 수 없음: cardId={}", cardId.getId());
      return Either.left(Failure.ofNotFound(
          validationMessageResolver.getMessage("error.service.card.move.not_found")));
    }

    Card card = cardOpt.get();

    // 3단계: 보드 접근 권한 확인
    return validateBoardAccess(card.getListId(), userId)
        .map(board -> card);
  }

  /**
   * 리스트별 카드 조회
   */
  private Either<Failure, List<Card>> findCardsByListId(ListId listId, UserId userId) {
    // 2단계: 보드 접근 권한 확인
    return validateBoardAccess(listId, userId)
        .flatMap(board -> {
          List<Card> cards = cardRepository.findByListIdOrderByPosition(listId);
          log.debug("카드 목록 조회 완료: listId={}, 카드 개수={}", listId.getId(), cards.size());
          return Either.right(cards);
        });
  }

  /**
   * 카드 검색
   */
  private Either<Failure, List<Card>> findCardsBySearch(ListId listId, String searchTerm, UserId userId) {
    // 2단계: 보드 접근 권한 확인
    return validateBoardAccess(listId, userId)
        .flatMap(board -> {
          List<Card> cards = cardRepository.findByListIdAndTitleContaining(listId, searchTerm.trim());
          log.debug("카드 검색 완료: listId={}, 검색 결과 개수={}", listId.getId(), cards.size());
          return Either.right(cards);
        });
  }

  /**
   * 보드 접근 권한 확인 (공통 메서드)
   */
  private Either<Failure, Object> validateBoardAccess(ListId listId, UserId userId) {
    // 리스트 존재 확인
    var boardListOpt = boardListRepository.findById(listId);
    if (boardListOpt.isEmpty()) {
      log.warn("리스트를 찾을 수 없음: listId={}", listId.getId());
      return Either.left(Failure.ofNotFound(
          validationMessageResolver.getMessage("error.service.card.move.list_not_found")));
    }

    // 보드 접근 권한 확인
    var boardList = boardListOpt.get();
    var boardOpt = boardRepository.findByIdAndOwnerId(boardList.getBoardId(), userId);

    if (boardOpt.isEmpty()) {
      log.warn("보드 접근 권한 없음: boardId={}, userId={}",
          boardList.getBoardId().getId(), userId.getId());
      return Either.left(Failure.ofPermissionDenied(
          validationMessageResolver.getMessage("error.service.card.move.access_denied")));
    }

    return Either.right(new Object());
  }
}

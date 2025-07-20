package com.boardly.features.card.application.service;

import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.application.port.input.GetCardCommand;
import com.boardly.features.card.application.usecase.GetCardUseCase;
import com.boardly.features.card.application.validation.GetCardValidator;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 카드 조회 서비스
 * 
 * <p>
 * 카드 상세 조회 관련 비즈니스 로직을 처리하는 애플리케이션 서비스입니다.
 * 입력 검증, 카드 존재 확인, 권한 검증, 카드 조회 등의 전체 카드 조회 프로세스를 관리합니다.
 * 
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetCardService implements GetCardUseCase {

  private final GetCardValidator cardValidator;
  private final CardRepository cardRepository;
  private final BoardListRepository boardListRepository;
  private final BoardRepository boardRepository;

  /**
   * 카드의 상세 정보를 조회합니다.
   * 
   * @param command 카드 조회에 필요한 정보를 담은 커맨드 객체
   * @return 조회 결과 (성공 시 카드 정보, 실패 시 실패 정보)
   */
  @Override
  public Either<Failure, Card> getCard(GetCardCommand command) {
    log.info("카드 조회 시작: cardId={}, userId={}",
        command.cardId().getId(), command.userId().getId());

    return validateInput(command)
        .flatMap(this::findCard);
  }

  /**
   * 1단계: 입력 검증
   */
  private Either<Failure, GetCardCommand> validateInput(GetCardCommand command) {
    ValidationResult<GetCardCommand> validationResult = cardValidator.validate(command);
    if (validationResult.isInvalid()) {
      log.warn("카드 조회 검증 실패: cardId={}, violations={}",
          command.cardId().getId(), validationResult.getErrorsAsCollection());
      return Either.left(Failure.ofValidation("INVALID_INPUT", validationResult.getErrorsAsCollection()));
    }
    return Either.right(command);
  }

  /**
   * 2단계: 카드 존재 확인 및 권한 검증
   */
  private Either<Failure, Card> findCard(GetCardCommand command) {
    Optional<Card> cardOpt = cardRepository.findById(command.cardId());
    if (cardOpt.isEmpty()) {
      log.warn("카드를 찾을 수 없음: cardId={}", command.cardId().getId());
      return Either.left(Failure.ofNotFound("카드를 찾을 수 없습니다."));
    }

    Card card = cardOpt.get();

    // 3단계: 보드 리스트 존재 확인
    var boardListOpt = boardListRepository.findById(card.getListId());
    if (boardListOpt.isEmpty()) {
      log.warn("카드가 속한 리스트를 찾을 수 없음: listId={}", card.getListId().getId());
      return Either.left(Failure.ofNotFound("카드가 속한 리스트를 찾을 수 없습니다."));
    }

    // 4단계: 보드 접근 권한 확인
    var boardList = boardListOpt.get();
    var boardOpt = boardRepository.findByIdAndOwnerId(boardList.getBoardId(), command.userId());

    if (boardOpt.isEmpty()) {
      log.warn("보드 접근 권한 없음: boardId={}, userId={}",
          boardList.getBoardId().getId(), command.userId().getId());
      return Either.left(Failure.ofForbidden("보드에 접근할 권한이 없습니다."));
    }

    log.info("카드 조회 완료: cardId={}, title={}", card.getCardId().getId(), card.getTitle());
    return Either.right(card);
  }

}
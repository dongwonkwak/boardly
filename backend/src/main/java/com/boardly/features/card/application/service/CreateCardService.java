package com.boardly.features.card.application.service;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.application.port.input.CreateCardCommand;
import com.boardly.features.card.application.usecase.CreateCardUseCase;
import com.boardly.features.card.application.validation.CreateCardValidator;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.policy.CardCreationPolicy;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CreateCardService implements CreateCardUseCase {

  private final CreateCardValidator cardValidator;
  private final CardCreationPolicy cardCreationPolicy;
  private final CardRepository cardRepository;
  private final BoardListRepository boardListRepository;
  private final BoardRepository boardRepository;

  @Override
  public Either<Failure, Card> createCard(CreateCardCommand command) {
    log.debug("카드 생성 시작: listId={}, title={}, userId={}",
        command.listId().getId(), command.title(), command.userId().getId());

    // 1. 입력 검증
    var validationResult = cardValidator.validate(command);
    if (validationResult.isInvalid()) {
      log.warn("카드 생성 입력 검증 실패: {}", validationResult.getErrorsAsCollection());
      return Either.left(Failure.ofValidation("INVALID_INPUT", validationResult.getErrorsAsCollection()));
    }

    // 2. 리스트 존재 확인
    var boardList = boardListRepository.findById(command.listId());
    if (boardList.isEmpty()) {
      log.warn("리스트를 찾을 수 없음: listId={}", command.listId().getId());
      return Either.left(Failure.ofNotFound("리스트를 찾을 수 없습니다."));
    }

    // 3. 보드 존재 및 권한 확인
    var board = boardRepository.findByIdAndOwnerId(boardList.get().getBoardId(), command.userId());
    if (board.isEmpty()) {
      log.warn("보드 접근 권한 없음: boardId={}, userId={}",
          boardList.get().getBoardId().getId(), command.userId().getId());
      return Either.left(Failure.ofForbidden("보드에 접근할 권한이 없습니다."));
    }

    // 4. 활성 보드인지 확인
    if (board.get().isArchived()) {
      log.warn("아카이브된 보드에 카드 생성 시도: boardId={}, userId={}",
          board.get().getBoardId().getId(), command.userId().getId());
      return Either.left(Failure.ofConflict("아카이브된 보드에는 카드를 생성할 수 없습니다."));
    }

    // 5. 비즈니스 정책 검증
    return cardCreationPolicy.canCreateCard(command.listId())
        .flatMap(v -> {
          // 6. 새 카드의 위치 계산 (맨 마지막)
          int newPosition = cardRepository.findMaxPositionByListId(command.listId())
              .map(pos -> pos + 1)
              .orElse(0);

          // 7. 카드 생성 및 저장
          Card card = Card.create(command.title(), command.description(), newPosition, command.listId());
          log.debug("카드 생성 완료: cardId={}, title={}", card.getCardId().getId(), card.getTitle());

          return cardRepository.save(card);
        });
  }
}

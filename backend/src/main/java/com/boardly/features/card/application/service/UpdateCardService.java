package com.boardly.features.card.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardly.features.card.application.port.input.UpdateCardCommand;
import com.boardly.features.card.application.port.input.MoveCardCommand;
import com.boardly.features.card.application.usecase.UpdateCardUseCase;
import com.boardly.features.card.application.usecase.MoveCardUseCase;
import com.boardly.features.card.application.validation.CardValidator;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.policy.CardMovePolicy;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * 카드 수정 서비스
 * 
 * <p>
 * 카드의 제목, 설명, 위치, 리스트를 수정하는 비즈니스 로직을 처리합니다.
 * 입력 검증, 권한 확인, 도메인 로직 실행, 저장 등의 전체 카드 수정 프로세스를 관리합니다.
 * 
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateCardService implements UpdateCardUseCase, MoveCardUseCase {

    private final CardValidator cardValidator;
    private final CardMovePolicy cardMovePolicy;
    private final CardRepository cardRepository;
    private final BoardListRepository boardListRepository;
    private final BoardRepository boardRepository;
    private final ValidationMessageResolver validationMessageResolver;

    @Override
    public Either<Failure, Card> updateCard(UpdateCardCommand command) {
        log.info("UpdateCardService.updateCard() called with command: cardId={}, title={}, userId={}",
                command.cardId().getId(), command.title(), command.userId().getId());

        return validateInput(command)
                .flatMap(this::findExistingCard)
                .flatMap(this::verifyBoardAccess)
                .flatMap(this::checkArchiveStatus)
                .flatMap(this::applyChangesToCard)
                .flatMap(this::saveUpdatedCard);
    }

    @Override
    public Either<Failure, Card> moveCard(MoveCardCommand command) {
        log.info(
                "UpdateCardService.moveCard() called with command: cardId={}, targetListId={}, newPosition={}, userId={}",
                command.cardId().getId(),
                command.targetListId() != null ? command.targetListId().getId() : "null",
                command.newPosition(),
                command.userId().getId());

        // 1. 입력 검증
        var validationResult = cardValidator.validateMove(command);
        if (validationResult.isInvalid()) {
            log.warn("카드 이동 입력 검증 실패: {}", validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }

        // 2. 카드 존재 확인
        var cardOpt = cardRepository.findById(command.cardId());
        if (cardOpt.isEmpty()) {
            log.warn("이동할 카드를 찾을 수 없음: cardId={}", command.cardId().getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("error.service.card.move.not_found")));
        }

        Card card = cardOpt.get();

        // 3. 보드 접근 권한 확인 (원본 리스트)
        var boardAccessResult = validateBoardAccess(card.getListId(), command.userId());
        if (boardAccessResult.isLeft()) {
            return Either.left(boardAccessResult.getLeft());
        }

        // 4. 이동 유형에 따른 처리
        if (command.targetListId() == null) {
            // 같은 리스트 내 이동
            return moveWithinSameList(card, command.newPosition());
        } else {
            // 다른 리스트로 이동
            return moveToAnotherList(card, command.targetListId(), command.newPosition(), command.userId());
        }
    }

    /**
     * 1단계: 입력 검증
     */
    private Either<Failure, UpdateCardCommand> validateInput(UpdateCardCommand command) {
        ValidationResult<UpdateCardCommand> validationResult = cardValidator.validateUpdate(command);
        if (validationResult.isInvalid()) {
            log.warn("카드 수정 검증 실패: cardId={}, violations={}",
                    command.cardId().getId(), validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }
        return Either.right(command);
    }

    /**
     * 2단계: 기존 카드 조회
     */
    private Either<Failure, CardUpdateContext> findExistingCard(UpdateCardCommand command) {
        Optional<Card> existingCardOpt = cardRepository.findById(command.cardId());
        if (existingCardOpt.isEmpty()) {
            log.warn("카드를 찾을 수 없음: cardId={}", command.cardId().getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("error.service.card.update.not_found")));
        }

        Card existingCard = existingCardOpt.get();
        return Either.right(new CardUpdateContext(command, existingCard));
    }

    /**
     * 3단계: 보드 접근 권한 확인
     */
    private Either<Failure, CardUpdateContext> verifyBoardAccess(CardUpdateContext context) {
        // 리스트 존재 확인
        var boardList = boardListRepository.findById(context.card().getListId());
        if (boardList.isEmpty()) {
            log.warn("리스트를 찾을 수 없음: listId={}", context.card().getListId().getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("error.service.card.update.list_not_found")));
        }

        // 보드 존재 및 권한 확인
        var board = boardRepository.findByIdAndOwnerId(boardList.get().getBoardId(), context.command().userId());
        if (board.isEmpty()) {
            log.warn("보드 접근 권한 없음: boardId={}, userId={}",
                    boardList.get().getBoardId().getId(), context.command().userId().getId());
            return Either.left(Failure.ofPermissionDenied(
                    validationMessageResolver.getMessage("error.service.card.update.access_denied")));
        }

        return Either.right(context);
    }

    /**
     * 4단계: 아카이브된 보드의 카드 수정 제한 확인
     */
    private Either<Failure, CardUpdateContext> checkArchiveStatus(CardUpdateContext context) {
        var boardList = boardListRepository.findById(context.card().getListId());
        if (boardList.isPresent()) {
            var board = boardRepository.findById(boardList.get().getBoardId());
            if (board.isPresent() && board.get().isArchived()) {
                log.warn("아카이브된 보드의 카드 수정 시도: cardId={}, boardId={}, userId={}",
                        context.command().cardId().getId(),
                        board.get().getBoardId().getId(),
                        context.command().userId().getId());
                return Either.left(Failure.ofConflict(
                        validationMessageResolver.getMessage("error.service.card.update.archived_board")));
            }
        }
        return Either.right(context);
    }

    /**
     * 5단계: 변경 사항 적용
     */
    private Either<Failure, CardUpdateContext> applyChangesToCard(CardUpdateContext context) {
        try {
            // 제목 업데이트
            context.card().updateTitle(context.command().title());
            log.debug("카드 제목 업데이트: cardId={}, newTitle={}",
                    context.command().cardId().getId(), context.command().title());

            // 설명 업데이트
            context.card().updateDescription(context.command().description());
            log.debug("카드 설명 업데이트: cardId={}, descriptionLength={}",
                    context.command().cardId().getId(),
                    context.command().description() != null ? context.command().description().length() : 0);

            return Either.right(context);
        } catch (Exception e) {
            log.error("카드 변경 중 오류 발생: cardId={}, error={}",
                    context.command().cardId().getId(), e.getMessage(), e);
            return Either.left(Failure.ofInternalServerError(
                    validationMessageResolver.getMessage("error.service.card.update.error")));
        }
    }

    /**
     * 6단계: 업데이트된 카드 저장
     */
    private Either<Failure, Card> saveUpdatedCard(CardUpdateContext context) {
        return cardRepository.save(context.card())
                .peek(card -> log.info("카드 수정 완료: cardId={}, title={}",
                        card.getCardId().getId(), card.getTitle()));
    }

    /**
     * 같은 리스트 내에서 카드를 이동합니다.
     */
    private Either<Failure, Card> moveWithinSameList(Card card, int newPosition) {
        log.debug("같은 리스트 내 카드 이동: cardId={}, oldPosition={}, newPosition={}",
                card.getCardId().getId(), card.getPosition(), newPosition);

        // 1. 이동 정책 검증
        return cardMovePolicy.canMoveWithinSameList(card, newPosition)
                .flatMap(v -> {
                    // 2. 위치 조정이 필요한지 확인
                    if (card.getPosition() == newPosition) {
                        log.debug("위치 변경 없음: cardId={}, position={}", card.getCardId().getId(), newPosition);
                        return Either.right(card);
                    }

                    // 3. 다른 카드들의 위치 조정
                    adjustCardPositions(card.getListId(), card.getPosition(), newPosition);

                    // 4. 카드 위치 업데이트
                    card.updatePosition(newPosition);
                    log.debug("카드 위치 업데이트 완료: cardId={}, newPosition={}",
                            card.getCardId().getId(), newPosition);

                    return cardRepository.save(card);
                });
    }

    /**
     * 다른 리스트로 카드를 이동합니다.
     */
    private Either<Failure, Card> moveToAnotherList(Card card,
            ListId targetListId,
            int newPosition,
            UserId userId) {

        log.debug("다른 리스트로 카드 이동: cardId={}, sourceListId={}, targetListId={}, newPosition={}",
                card.getCardId().getId(), card.getListId().getId(), targetListId.getId(), newPosition);

        // 1. 대상 리스트 존재 확인
        var targetListOpt = boardListRepository.findById(targetListId);
        if (targetListOpt.isEmpty()) {
            log.warn("대상 리스트를 찾을 수 없음: targetListId={}", targetListId.getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("error.service.card.move.target_list_not_found")));
        }

        // 2. 대상 보드 접근 권한 확인
        var targetBoardAccessResult = validateBoardAccess(targetListId, userId);
        if (targetBoardAccessResult.isLeft()) {
            return Either.left(targetBoardAccessResult.getLeft());
        }

        // 3. 이동 정책 검증
        return cardMovePolicy.canMoveToAnotherList(card, targetListId, newPosition)
                .flatMap(v -> {
                    // 4. 원본 리스트의 다른 카드들 위치 조정
                    adjustCardPositionsForRemoval(card.getListId(), card.getPosition());

                    // 5. 대상 리스트의 다른 카드들 위치 조정
                    adjustCardPositionsForInsertion(targetListId, newPosition);

                    // 6. 카드 이동
                    card.moveToList(targetListId, newPosition);
                    log.debug("카드 이동 완료: cardId={}, newListId={}, newPosition={}",
                            card.getCardId().getId(), targetListId.getId(), newPosition);

                    return cardRepository.save(card);
                });
    }

    /**
     * 보드 접근 권한을 확인합니다.
     */
    private Either<Failure, Object> validateBoardAccess(ListId listId, UserId userId) {
        // 1. 리스트 존재 확인
        var boardListOpt = boardListRepository.findById(listId);
        if (boardListOpt.isEmpty()) {
            log.warn("리스트를 찾을 수 없음: listId={}", listId.getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("error.service.card.move.list_not_found")));
        }

        var boardList = boardListOpt.get();

        // 2. 보드 존재 및 권한 확인
        var boardOpt = boardRepository.findByIdAndOwnerId(boardList.getBoardId(), userId);
        if (boardOpt.isEmpty()) {
            log.warn("보드 접근 권한 없음: boardId={}, userId={}",
                    boardList.getBoardId().getId(), userId.getId());
            return Either.left(Failure.ofPermissionDenied(
                    validationMessageResolver.getMessage("error.service.card.move.access_denied")));
        }

        var board = boardOpt.get();

        // 3. 활성 보드인지 확인
        if (board.isArchived()) {
            log.warn("아카이브된 보드에서 카드 이동 시도: boardId={}, userId={}",
                    board.getBoardId().getId(), userId.getId());
            return Either.left(Failure.ofConflict(
                    validationMessageResolver.getMessage("error.service.card.move.archived_board")));
        }

        return Either.right(new Object());
    }

    /**
     * 같은 리스트 내에서 카드들의 위치를 조정합니다.
     */
    private void adjustCardPositions(ListId listId, int oldPosition, int newPosition) {
        List<Card> cardsToUpdate;

        if (oldPosition < newPosition) {
            // 뒤로 이동: oldPosition과 newPosition 사이의 카드들을 앞으로 한 칸씩
            cardsToUpdate = cardRepository.findByListIdAndPositionBetween(listId, oldPosition + 1, newPosition);
            cardsToUpdate.forEach(card -> card.updatePosition(card.getPosition() - 1));
        } else {
            // 앞으로 이동: newPosition과 oldPosition 사이의 카드들을 뒤로 한 칸씩
            cardsToUpdate = cardRepository.findByListIdAndPositionBetween(listId, newPosition, oldPosition - 1);
            cardsToUpdate.forEach(card -> card.updatePosition(card.getPosition() + 1));
        }

        if (!cardsToUpdate.isEmpty()) {
            cardRepository.saveAll(cardsToUpdate);
            log.debug("카드 위치 조정 완료: listId={}, 조정된 카드 수={}", listId.getId(), cardsToUpdate.size());
        }
    }

    /**
     * 카드 제거로 인한 위치 조정
     */
    private void adjustCardPositionsForRemoval(ListId listId, int removedPosition) {
        List<Card> cardsToUpdate = cardRepository.findByListIdAndPositionGreaterThan(listId, removedPosition);
        cardsToUpdate.forEach(card -> card.updatePosition(card.getPosition() - 1));

        if (!cardsToUpdate.isEmpty()) {
            cardRepository.saveAll(cardsToUpdate);
            log.debug("카드 제거로 인한 위치 조정 완료: listId={}, 조정된 카드 수={}",
                    listId.getId(), cardsToUpdate.size());
        }
    }

    /**
     * 카드 삽입으로 인한 위치 조정
     */
    private void adjustCardPositionsForInsertion(ListId listId, int insertPosition) {
        List<Card> cardsToUpdate = cardRepository.findByListIdAndPositionGreaterThan(listId, insertPosition - 1);
        cardsToUpdate.forEach(card -> card.updatePosition(card.getPosition() + 1));

        if (!cardsToUpdate.isEmpty()) {
            cardRepository.saveAll(cardsToUpdate);
            log.debug("카드 삽입으로 인한 위치 조정 완료: listId={}, 조정된 카드 수={}",
                    listId.getId(), cardsToUpdate.size());
        }
    }

    /**
     * 카드 업데이트 과정에서 사용되는 컨텍스트 객체
     */
    private record CardUpdateContext(
            UpdateCardCommand command,
            Card card) {
    }
}
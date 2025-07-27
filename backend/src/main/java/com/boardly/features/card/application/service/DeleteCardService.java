package com.boardly.features.card.application.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.activity.domain.model.ActivityType;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.application.port.input.DeleteCardCommand;
import com.boardly.features.card.application.usecase.DeleteCardUseCase;
import com.boardly.features.card.application.validation.CardValidator;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;
import com.boardly.features.user.domain.model.UserId;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 카드 삭제 서비스
 * 
 * <p>
 * 카드를 영구적으로 삭제하는 비즈니스 로직을 처리합니다.
 * 
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DeleteCardService implements DeleteCardUseCase {

    private final CardValidator cardValidator;
    private final CardRepository cardRepository;
    private final BoardListRepository boardListRepository;
    private final BoardRepository boardRepository;
    private final ValidationMessageResolver validationMessageResolver;
    private final ActivityHelper activityHelper;

    @Override
    public Either<Failure, Void> deleteCard(DeleteCardCommand command) {
        log.info("DeleteCardService.deleteCard() called with command: cardId={}, userId={}",
                command.cardId().getId(), command.userId().getId());

        // 1. 입력 검증
        var validationResult = validateInput(command);
        if (validationResult.isLeft()) {
            return validationResult;
        }

        // 2. 카드 조회 및 검증
        var cardResult = findAndValidateCard(command);
        if (cardResult.isLeft()) {
            return Either.left(cardResult.getLeft());
        }

        Card cardToDelete = cardResult.get();

        // 3. 권한 검증
        var permissionResult = validatePermissions(cardToDelete, command.userId());
        if (permissionResult.isLeft()) {
            return permissionResult;
        }

        // 4. 카드 삭제 실행
        var deleteResult = executeCardDeletion(command.cardId());
        if (deleteResult.isLeft()) {
            return deleteResult;
        }

        // 5. 후처리 작업
        performPostDeletionTasks(cardToDelete, command.userId());

        log.info("카드 삭제 완료: cardId={}, title={}, listId={}",
                cardToDelete.getCardId().getId(), cardToDelete.getTitle(), cardToDelete.getListId().getId());

        return Either.right(null);
    }

    /**
     * 입력 검증을 수행합니다.
     */
    private Either<Failure, Void> validateInput(DeleteCardCommand command) {
        var validationResult = cardValidator.validateDelete(command);
        if (validationResult.isInvalid()) {
            log.warn("카드 삭제 입력 검증 실패: {}", validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }
        return Either.right(null);
    }

    /**
     * 카드를 조회하고 기본 검증을 수행합니다.
     */
    private Either<Failure, Card> findAndValidateCard(DeleteCardCommand command) {
        var cardOpt = cardRepository.findById(command.cardId());
        if (cardOpt.isEmpty()) {
            log.warn("삭제할 카드를 찾을 수 없음: cardId={}", command.cardId().getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("error.service.card.delete.not_found")));
        }
        return Either.right(cardOpt.get());
    }

    /**
     * 보드 접근 권한을 확인합니다.
     */
    private Either<Failure, Void> validatePermissions(Card card, UserId userId) {
        return validateBoardAccess(card.getListId(), userId);
    }

    /**
     * 카드 삭제를 실행합니다.
     */
    private Either<Failure, Void> executeCardDeletion(CardId cardId) {
        var deleteResult = cardRepository.delete(cardId);
        if (deleteResult.isLeft()) {
            log.error("카드 삭제 실패: cardId={}, error={}",
                    cardId.getId(), deleteResult.getLeft().getMessage());
            return Either.left(deleteResult.getLeft());
        }
        return Either.right(null);
    }

    /**
     * 카드 삭제 후 필요한 후처리 작업을 수행합니다.
     */
    private void performPostDeletionTasks(Card card, UserId userId) {
        reorderRemainingCards(card.getListId(), card.getPosition());
        logCardDeleteActivity(userId, card);
    }

    /**
     * 보드 접근 권한을 확인합니다.
     */
    private Either<Failure, Void> validateBoardAccess(ListId listId,
            UserId userId) {

        // 1. 리스트 존재 확인
        var boardListResult = findBoardList(listId);
        if (boardListResult.isLeft()) {
            return Either.left(boardListResult.getLeft());
        }

        var boardList = boardListResult.get();

        // 2. 보드 권한 확인
        var boardResult = validateBoardPermissions(boardList, userId);
        if (boardResult.isLeft()) {
            return boardResult;
        }

        return Either.right(null);
    }

    /**
     * 리스트를 조회합니다.
     */
    private Either<Failure, BoardList> findBoardList(ListId listId) {
        var boardListOpt = boardListRepository.findById(listId);
        if (boardListOpt.isEmpty()) {
            log.warn("리스트를 찾을 수 없음: listId={}", listId.getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("error.service.card.delete.list_not_found")));
        }
        return Either.right(boardListOpt.get());
    }

    /**
     * 보드 권한을 확인합니다.
     */
    private Either<Failure, Void> validateBoardPermissions(
            BoardList boardList,
            UserId userId) {

        // 보드 존재 및 소유권 확인
        var boardOpt = boardRepository.findByIdAndOwnerId(boardList.getBoardId(), userId);
        if (boardOpt.isEmpty()) {
            log.warn("보드 접근 권한 없음: boardId={}, userId={}",
                    boardList.getBoardId().getId(), userId.getId());
            return Either.left(Failure.ofPermissionDenied(
                    validationMessageResolver.getMessage("error.service.card.delete.access_denied")));
        }

        var board = boardOpt.get();

        // 활성 보드인지 확인
        if (board.isArchived()) {
            log.warn("아카이브된 보드에서 카드 삭제 시도: boardId={}, userId={}",
                    board.getBoardId().getId(), userId.getId());
            return Either.left(Failure.ofBusinessRuleViolation(
                    validationMessageResolver.getMessage("error.service.card.delete.archived_board")));
        }

        return Either.right(null);
    }

    /**
     * 카드 삭제 활동 로그를 기록합니다.
     */
    private void logCardDeleteActivity(UserId userId, Card card) {
        try {
            var boardListResult = findBoardListForActivity(card.getListId());
            if (boardListResult.isEmpty()) {
                return;
            }

            var boardList = boardListResult.get();
            var payload = createActivityPayload(card, boardList);

            recordActivityLog(userId, card, boardList, payload);

        } catch (Exception e) {
            log.error("카드 삭제 활동 로그 기록 중 오류 발생: cardId={}, error={}",
                    card.getCardId().getId(), e.getMessage(), e);
        }
    }

    /**
     * 활동 로그용 리스트 정보를 조회합니다.
     */
    private Optional<BoardList> findBoardListForActivity(
            ListId listId) {
        var boardListOpt = boardListRepository.findById(listId);
        if (boardListOpt.isEmpty()) {
            log.warn("활동 로그 기록을 위한 리스트 정보를 찾을 수 없음: listId={}", listId.getId());
        }
        return boardListOpt;
    }

    /**
     * 활동 로그 payload를 생성합니다.
     */
    private Map<String, Object> createActivityPayload(Card card,
            BoardList boardList) {
        return Map.<String, Object>of(
                "cardTitle", card.getTitle(),
                "listName", boardList.getTitle(),
                "cardId", card.getCardId().getId(),
                "listId", card.getListId().getId());
    }

    /**
     * 활동 로그를 기록합니다.
     */
    private void recordActivityLog(UserId userId, Card card,
            BoardList boardList, Map<String, Object> payload) {

        // 보드 정보 조회
        var boardOpt = boardRepository.findById(boardList.getBoardId());
        String boardName = boardOpt.map(board -> board.getTitle()).orElse("알 수 없는 보드");

        activityHelper.logCardActivity(
                ActivityType.CARD_DELETE,
                userId,
                payload,
                boardName,
                boardList.getBoardId(),
                card.getListId(),
                card.getCardId());

        log.debug("카드 삭제 활동 로그 기록 완료: cardId={}, title={}",
                card.getCardId().getId(), card.getTitle());
    }

    /**
     * 삭제된 카드 이후의 모든 카드들의 position을 재정렬합니다.
     */
    private void reorderRemainingCards(ListId listId, int deletedPosition) {
        log.debug("카드 위치 재정렬 시작: listId={}, deletedPosition={}", listId.getId(), deletedPosition);

        try {
            var cardsToReorder = findCardsToReorder(listId, deletedPosition);
            if (cardsToReorder.isEmpty()) {
                return;
            }

            updateCardPositions(cardsToReorder);
            saveUpdatedCards(cardsToReorder);

            log.debug("카드 위치 재정렬 완료: listId={}, 재정렬된 카드 개수={}",
                    listId.getId(), cardsToReorder.size());

        } catch (Exception e) {
            log.error("카드 위치 재정렬 중 오류 발생: listId={}, deletedPosition={}, error={}",
                    listId.getId(), deletedPosition, e.getMessage(), e);
        }
    }

    /**
     * 재정렬이 필요한 카드들을 조회합니다.
     */
    private List<Card> findCardsToReorder(ListId listId, int deletedPosition) {
        List<Card> cardsToReorder = cardRepository.findByListIdAndPositionGreaterThan(listId, deletedPosition);

        if (cardsToReorder.isEmpty()) {
            log.debug("재정렬할 카드가 없음: listId={}, deletedPosition={}", listId.getId(), deletedPosition);
        }

        return cardsToReorder;
    }

    /**
     * 카드들의 position을 업데이트합니다.
     */
    private void updateCardPositions(List<Card> cardsToReorder) {
        for (Card card : cardsToReorder) {
            card.updatePosition(card.getPosition() - 1);
        }
    }

    /**
     * 업데이트된 카드들을 저장합니다.
     */
    private void saveUpdatedCards(List<Card> cardsToReorder) {
        cardRepository.saveAll(cardsToReorder);
    }
}
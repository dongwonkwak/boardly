package com.boardly.features.card.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.application.port.input.DeleteCardCommand;
import com.boardly.features.card.application.usecase.DeleteCardUseCase;
import com.boardly.features.card.application.validation.CardValidator;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;

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

    @Override
    public Either<Failure, Void> deleteCard(DeleteCardCommand command) {
        log.info("DeleteCardService.deleteCard() called with command: cardId={}, userId={}",
                command.cardId().getId(), command.userId().getId());

        // 1. 입력 검증
        var validationResult = cardValidator.validateDelete(command);
        if (validationResult.isInvalid()) {
            log.warn("카드 삭제 입력 검증 실패: {}", validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }

        // 2. 카드 존재 확인
        var cardOpt = cardRepository.findById(command.cardId());
        if (cardOpt.isEmpty()) {
            log.warn("삭제할 카드를 찾을 수 없음: cardId={}", command.cardId().getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("error.service.card.delete.not_found")));
        }

        Card cardToDelete = cardOpt.get();

        // 3. 보드 접근 권한 확인
        var boardAccessResult = validateBoardAccess(cardToDelete.getListId(), command.userId());
        if (boardAccessResult.isLeft()) {
            return Either.left(boardAccessResult.getLeft());
        }

        // 4. 카드 삭제
        var deleteResult = cardRepository.delete(command.cardId());
        if (deleteResult.isLeft()) {
            log.error("카드 삭제 실패: cardId={}, error={}",
                    command.cardId().getId(), deleteResult.getLeft().getMessage());
            return Either.left(deleteResult.getLeft());
        }

        // 5. 남은 카드들의 위치 조정
        reorderRemainingCards(cardToDelete.getListId(), cardToDelete.getPosition());

        log.info("카드 삭제 완료: cardId={}, title={}, listId={}",
                cardToDelete.getCardId().getId(), cardToDelete.getTitle(), cardToDelete.getListId().getId());

        return Either.right(null);
    }

    /**
     * 보드 접근 권한 확인 (공통 메서드)
     */
    private Either<Failure, Void> validateBoardAccess(ListId listId,
            com.boardly.features.user.domain.model.UserId userId) {
        // 1. 리스트 존재 확인
        var boardListOpt = boardListRepository.findById(listId);
        if (boardListOpt.isEmpty()) {
            log.warn("리스트를 찾을 수 없음: listId={}", listId.getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("error.service.card.delete.list_not_found")));
        }

        var boardList = boardListOpt.get();

        // 2. 보드 존재 및 권한 확인
        var boardOpt = boardRepository.findByIdAndOwnerId(boardList.getBoardId(), userId);
        if (boardOpt.isEmpty()) {
            log.warn("보드 접근 권한 없음: boardId={}, userId={}",
                    boardList.getBoardId().getId(), userId.getId());
            return Either.left(Failure.ofPermissionDenied(
                    validationMessageResolver.getMessage("error.service.card.delete.access_denied")));
        }

        var board = boardOpt.get();

        // 3. 활성 보드인지 확인
        if (board.isArchived()) {
            log.warn("아카이브된 보드에서 카드 삭제 시도: boardId={}, userId={}",
                    board.getBoardId().getId(), userId.getId());
            return Either.left(Failure.ofBusinessRuleViolation(
                    validationMessageResolver.getMessage("error.service.card.delete.archived_board")));
        }

        return Either.right(null);
    }

    /**
     * 삭제된 카드 이후의 모든 카드들의 position을 재정렬합니다.
     * 
     * @param listId          삭제된 카드가 속한 리스트 ID
     * @param deletedPosition 삭제된 카드의 position
     */
    private void reorderRemainingCards(ListId listId, int deletedPosition) {
        log.debug("카드 위치 재정렬 시작: listId={}, deletedPosition={}", listId.getId(), deletedPosition);

        try {
            // 삭제된 위치보다 큰 position을 가진 카드들을 조회
            List<Card> cardsToReorder = cardRepository.findByListIdAndPositionGreaterThan(listId, deletedPosition);

            if (cardsToReorder.isEmpty()) {
                log.debug("재정렬할 카드가 없음: listId={}, deletedPosition={}", listId.getId(), deletedPosition);
                return;
            }

            // 각 카드의 position을 1씩 감소시켜 재정렬
            for (Card card : cardsToReorder) {
                card.updatePosition(card.getPosition() - 1);
            }

            // 변경된 카드들을 일괄 저장
            cardRepository.saveAll(cardsToReorder);

            log.debug("카드 위치 재정렬 완료: listId={}, 재정렬된 카드 개수={}",
                    listId.getId(), cardsToReorder.size());

        } catch (Exception e) {
            log.error("카드 위치 재정렬 중 오류 발생: listId={}, deletedPosition={}, error={}",
                    listId.getId(), deletedPosition, e.getMessage(), e);
            // 위치 재정렬 실패는 카드 삭제 자체의 실패로 간주하지 않음
            // 로그만 남기고 계속 진행
        }
    }
}
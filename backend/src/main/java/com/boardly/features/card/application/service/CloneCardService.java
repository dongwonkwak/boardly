package com.boardly.features.card.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.application.port.input.CloneCardCommand;
import com.boardly.features.card.application.usecase.CloneCardUseCase;
import com.boardly.features.card.application.validation.CloneCardValidator;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.policy.CardClonePolicy;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 카드 복제 서비스
 * 
 * <p>
 * 기존 카드의 내용을 복사하여 새로운 카드를 생성하는 비즈니스 로직을 처리합니다.
 * 
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CloneCardService implements CloneCardUseCase {

    private final CloneCardValidator cloneCardValidator;
    private final CardClonePolicy cardClonePolicy;
    private final CardRepository cardRepository;
    private final BoardListRepository boardListRepository;
    private final BoardRepository boardRepository;
    private final ValidationMessageResolver validationMessageResolver;

    @Override
    public Either<Failure, Card> cloneCard(CloneCardCommand command) {
        log.debug("카드 복제 시작: cardId={}, newTitle={}, targetListId={}, userId={}",
                command.cardId().getId(), command.newTitle(),
                command.targetListId() != null ? command.targetListId().getId() : "null",
                command.userId().getId());

        // 1. 입력 검증
        var validationResult = cloneCardValidator.validate(command);
        if (validationResult.isInvalid()) {
            log.warn("카드 복제 입력 검증 실패: {}", validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }

        // 2. 원본 카드 존재 확인
        var originalCardOpt = cardRepository.findById(command.cardId());
        if (originalCardOpt.isEmpty()) {
            log.warn("복제할 원본 카드를 찾을 수 없음: cardId={}", command.cardId().getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("error.service.card.clone.not_found")));
        }

        Card originalCard = originalCardOpt.get();

        // 3. 원본 카드 접근 권한 확인
        var sourceBoardAccessResult = validateBoardAccess(originalCard.getListId(), command.userId());
        if (sourceBoardAccessResult.isLeft()) {
            return Either.left(sourceBoardAccessResult.getLeft());
        }

        // 4. 복제 대상 리스트 결정 및 검증
        ListId targetListId = command.targetListId() != null ? command.targetListId() : originalCard.getListId();

        // 다른 리스트로 복제하는 경우 대상 리스트 접근 권한 확인
        if (!targetListId.equals(originalCard.getListId())) {
            var targetBoardAccessResult = validateBoardAccess(targetListId, command.userId());
            if (targetBoardAccessResult.isLeft()) {
                return Either.left(targetBoardAccessResult.getLeft());
            }
        }

        // 5. 비즈니스 정책 검증
        var policyResult = targetListId.equals(originalCard.getListId())
                ? cardClonePolicy.canCloneWithinSameList(originalCard)
                : cardClonePolicy.canCloneToAnotherList(originalCard, targetListId);

        if (policyResult.isLeft()) {
            return Either.left(policyResult.getLeft());
        }

        // 6. 새 카드의 위치 계산 (대상 리스트의 맨 마지막)
        int newPosition = cardRepository.findMaxPositionByListId(targetListId)
                .map(pos -> pos + 1)
                .orElse(0);

        // 7. 카드 복제 및 저장
        Card clonedCard = targetListId.equals(originalCard.getListId())
                ? originalCard.clone(command.newTitle(), newPosition)
                : originalCard.cloneToList(command.newTitle(), targetListId, newPosition);

        log.debug("카드 복제 완료: originalCardId={}, clonedCardId={}, title={}, targetListId={}",
                originalCard.getCardId().getId(), clonedCard.getCardId().getId(),
                clonedCard.getTitle(), clonedCard.getListId().getId());

        return cardRepository.save(clonedCard);
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
                    validationMessageResolver.getMessage("error.service.card.clone.list_not_found")));
        }

        var boardList = boardListOpt.get();

        // 2. 보드 존재 및 권한 확인
        var boardOpt = boardRepository.findByIdAndOwnerId(boardList.getBoardId(), userId);
        if (boardOpt.isEmpty()) {
            log.warn("보드 접근 권한 없음: boardId={}, userId={}",
                    boardList.getBoardId().getId(), userId.getId());
            return Either.left(Failure.ofPermissionDenied(
                    validationMessageResolver.getMessage("error.service.card.clone.access_denied")));
        }

        var board = boardOpt.get();

        // 3. 활성 보드인지 확인
        if (board.isArchived()) {
            log.warn("아카이브된 보드에서 카드 복제 시도: boardId={}, userId={}",
                    board.getBoardId().getId(), userId.getId());
            return Either.left(Failure.ofBusinessRuleViolation(
                    validationMessageResolver.getMessage("error.service.card.clone.archived_board")));
        }

        return Either.right(null);
    }
}
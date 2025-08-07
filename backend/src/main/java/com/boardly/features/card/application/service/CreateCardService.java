package com.boardly.features.card.application.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.activity.domain.model.ActivityType;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.application.port.input.CloneCardCommand;
import com.boardly.features.card.application.port.input.CreateCardCommand;
import com.boardly.features.card.application.usecase.CloneCardUseCase;
import com.boardly.features.card.application.usecase.CreateCardUseCase;
import com.boardly.features.card.application.validation.CardValidator;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.policy.CardClonePolicy;
import com.boardly.features.card.domain.policy.CardCreationPolicy;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 카드 생성 서비스
 * 
 * <p>
 * 카드 생성 및 복제 관련 비즈니스 로직을 처리합니다.
 * 입력 검증, 권한 확인, 비즈니스 정책 검증, 카드 생성 및 저장을 담당합니다.
 * 
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CreateCardService implements CreateCardUseCase, CloneCardUseCase {

    private final CardValidator cardValidator;
    private final CardCreationPolicy cardCreationPolicy;
    private final CardClonePolicy cardClonePolicy;
    private final CardRepository cardRepository;
    private final BoardListRepository boardListRepository;
    private final BoardRepository boardRepository;
    private final ValidationMessageResolver validationMessageResolver;
    private final ActivityHelper activityHelper;

    @Override
    public Either<Failure, Card> createCard(CreateCardCommand command) {
        log.info("CreateCardService.createCard() called with command: listId={}, title={}, userId={}",
                command.listId().getId(), command.title(), command.userId().getId());

        // 1. 입력 검증
        return validateCreateInput(command)
                .flatMap(v -> validateBoardAccess(command.listId(), command.userId()))
                .flatMap(boardList -> createAndSaveCard(command, boardList));
    }

    @Override
    public Either<Failure, Card> cloneCard(CloneCardCommand command) {
        log.info(
                "CreateCardService.cloneCard() called with command: cardId={}, newTitle={}, targetListId={}, userId={}",
                command.cardId().getId(), command.newTitle(),
                command.targetListId() != null ? command.targetListId().getId() : "null",
                command.userId().getId());

        // 1. 입력 검증
        return validateCloneInput(command)
                .flatMap(v -> findOriginalCard(command.cardId()))
                .flatMap(originalCard -> validateCloneAccess(originalCard, command))
                .flatMap(originalCard -> cloneAndSaveCard(command, originalCard));
    }

    // =================================================================
    // 🎯 카드 생성 관련 메서드들
    // =================================================================

    /**
     * 카드 생성 입력 검증
     */
    private Either<Failure, Void> validateCreateInput(CreateCardCommand command) {
        var validationResult = cardValidator.validateCreate(command);
        if (validationResult.isInvalid()) {
            log.warn("카드 생성 입력 검증 실패: {}", validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }
        return Either.right(null);
    }

    /**
     * 카드 생성 시 보드 접근 권한 확인
     */
    private Either<Failure, BoardList> validateBoardAccess(ListId listId,
            com.boardly.features.user.domain.model.UserId userId) {
        // 리스트 존재 확인
        var boardList = boardListRepository.findById(listId);
        if (boardList.isEmpty()) {
            log.warn("리스트를 찾을 수 없음: listId={}", listId.getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("error.service.card.create.list_not_found")));
        }

        // 보드 존재 및 권한 확인
        var board = boardRepository.findByIdAndOwnerId(boardList.get().getBoardId(), userId);
        if (board.isEmpty()) {
            log.warn("보드 접근 권한 없음: boardId={}, userId={}",
                    boardList.get().getBoardId().getId(), userId.getId());
            return Either.left(Failure.ofPermissionDenied(
                    validationMessageResolver.getMessage("error.service.card.create.access_denied")));
        }

        // 활성 보드인지 확인
        if (board.get().isArchived()) {
            log.warn("아카이브된 보드에 카드 생성 시도: boardId={}, userId={}",
                    board.get().getBoardId().getId(), userId.getId());
            return Either.left(Failure.ofBusinessRuleViolation(
                    validationMessageResolver.getMessage("error.service.card.create.archived_board")));
        }

        return Either.right(boardList.get());
    }

    /**
     * 카드 생성 및 저장
     */
    private Either<Failure, Card> createAndSaveCard(CreateCardCommand command, BoardList boardList) {
        return cardCreationPolicy.canCreateCard(command.listId())
                .flatMap(v -> {
                    // 새 카드의 위치 계산 (맨 마지막)
                    int newPosition = calculateNewCardPosition(command.listId());

                    // 카드 생성 및 저장
                    Card card = Card.create(command.title(), command.description(), newPosition, command.listId(),
                            command.userId());
                    log.info("카드 생성 완료: cardId={}, title={}, createdBy={}", card.getCardId().getId(), card.getTitle(),
                            card.getCreatedBy().getId());

                    return cardRepository.save(card)
                            .peek(savedCard -> logCardCreateActivity(command, boardList, savedCard));
                });
    }

    /**
     * 새 카드 위치 계산
     */
    private int calculateNewCardPosition(ListId listId) {
        return cardRepository.findMaxPositionByListId(listId)
                .map(pos -> pos + 1)
                .orElse(0);
    }

    /**
     * 카드 생성 활동 로그 기록
     */
    private void logCardCreateActivity(CreateCardCommand command, BoardList boardList, Card savedCard) {
        // 보드 정보 조회
        var boardOpt = boardRepository.findById(boardList.getBoardId());
        String boardName = boardOpt.map(board -> board.getTitle()).orElse("알 수 없는 보드");

        activityHelper.logCardCreate(
                command.userId(),
                boardList.getTitle(),
                savedCard.getTitle(),
                boardName,
                boardList.getBoardId(),
                savedCard.getListId(),
                savedCard.getCardId());
    }

    // =================================================================
    // 🎯 카드 복제 관련 메서드들
    // =================================================================

    /**
     * 카드 복제 입력 검증
     */
    private Either<Failure, Void> validateCloneInput(CloneCardCommand command) {
        var validationResult = cardValidator.validateClone(command);
        if (validationResult.isInvalid()) {
            log.warn("카드 복제 입력 검증 실패: {}", validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }
        return Either.right(null);
    }

    /**
     * 원본 카드 조회
     */
    private Either<Failure, Card> findOriginalCard(CardId cardId) {
        var originalCardOpt = cardRepository.findById(cardId);
        if (originalCardOpt.isEmpty()) {
            log.warn("복제할 원본 카드를 찾을 수 없음: cardId={}", cardId.getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("error.service.card.clone.not_found")));
        }
        return Either.right(originalCardOpt.get());
    }

    /**
     * 카드 복제 접근 권한 확인
     */
    private Either<Failure, Card> validateCloneAccess(Card originalCard, CloneCardCommand command) {
        // 원본 카드 접근 권한 확인
        var sourceBoardAccessResult = validateBoardAccessForClone(originalCard.getListId(), command.userId());
        if (sourceBoardAccessResult.isLeft()) {
            return Either.left(sourceBoardAccessResult.getLeft());
        }

        // 복제 대상 리스트 결정 및 검증
        ListId targetListId = command.targetListId() != null ? command.targetListId() : originalCard.getListId();

        // 다른 리스트로 복제하는 경우 대상 리스트 접근 권한 확인
        if (!targetListId.equals(originalCard.getListId())) {
            var targetBoardAccessResult = validateBoardAccessForClone(targetListId, command.userId());
            if (targetBoardAccessResult.isLeft()) {
                return Either.left(targetBoardAccessResult.getLeft());
            }
        }

        return Either.right(originalCard);
    }

    /**
     * 카드 복제 및 저장
     */
    private Either<Failure, Card> cloneAndSaveCard(CloneCardCommand command, Card originalCard) {
        // 복제 대상 리스트 결정
        ListId targetListId = command.targetListId() != null ? command.targetListId() : originalCard.getListId();

        // 비즈니스 정책 검증
        var policyResult = targetListId.equals(originalCard.getListId())
                ? cardClonePolicy.canCloneWithinSameList(originalCard)
                : cardClonePolicy.canCloneToAnotherList(originalCard, targetListId);

        if (policyResult.isLeft()) {
            return Either.left(policyResult.getLeft());
        }

        // 새 카드의 위치 계산 (대상 리스트의 맨 마지막)
        int newPosition = calculateNewCardPosition(targetListId);

        // 카드 복제 및 저장
        Card clonedCard = targetListId.equals(originalCard.getListId())
                ? originalCard.clone(command.newTitle(), newPosition, command.userId())
                : originalCard.cloneToList(command.newTitle(), targetListId, newPosition, command.userId());

        log.info("카드 복제 완료: originalCardId={}, clonedCardId={}, title={}, targetListId={}, createdBy={}",
                originalCard.getCardId().getId(), clonedCard.getCardId().getId(),
                clonedCard.getTitle(), clonedCard.getListId().getId(), clonedCard.getCreatedBy().getId());

        return cardRepository.save(clonedCard)
                .peek(savedClonedCard -> logCardDuplicateActivity(command, originalCard, savedClonedCard));
    }

    /**
     * 카드 복제 활동 로그 기록
     */
    private void logCardDuplicateActivity(CloneCardCommand command, Card originalCard, Card savedClonedCard) {
        BoardList targetList = boardListRepository.findById(savedClonedCard.getListId()).get();

        // 보드 정보 조회
        var boardOpt = boardRepository.findById(targetList.getBoardId());
        String boardName = boardOpt.map(board -> board.getTitle()).orElse("알 수 없는 보드");

        Map<String, Object> payload = Map.of(
                "originalCardTitle", originalCard.getTitle(),
                "newCardTitle", savedClonedCard.getTitle(),
                "originalCardId", originalCard.getCardId().getId(),
                "newCardId", savedClonedCard.getCardId().getId(),
                "listName", targetList.getTitle(),
                "boardName", boardName);

        activityHelper.logCardActivity(
                ActivityType.CARD_DUPLICATE,
                command.userId(),
                payload,
                boardName,
                targetList.getBoardId(),
                savedClonedCard.getListId(),
                savedClonedCard.getCardId());
    }

    // =================================================================
    // 🛡️ 공통 유틸리티 메서드들
    // =================================================================

    /**
     * 카드 복제용 보드 접근 권한 확인
     */
    private Either<Failure, Void> validateBoardAccessForClone(ListId listId,
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

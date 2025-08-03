package com.boardly.features.card.application.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.activity.domain.model.ActivityType;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.application.port.input.MoveCardCommand;
import com.boardly.features.card.application.port.input.UpdateCardCommand;
import com.boardly.features.card.application.usecase.MoveCardUseCase;
import com.boardly.features.card.application.usecase.UpdateCardUseCase;
import com.boardly.features.card.application.validation.CardValidator;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.model.CardPriority;
import com.boardly.features.card.domain.policy.CardMovePolicy;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    private final ActivityHelper activityHelper;

    @Override
    public Either<Failure, Card> updateCard(UpdateCardCommand command) {
        log.info("UpdateCardService.updateCard() called with command: cardId={}, title={}, userId={}",
                command.cardId().getId(), command.title(), command.userId().getId());

        return validateInput(command)
                .flatMap(this::findExistingCard)
                .flatMap(context -> findBoardList(context.card().getListId())
                        .flatMap(boardList -> findBoardByList(boardList)
                                .flatMap(board -> verifyBoardAccessForList(boardList, context.command().userId())
                                        .flatMap(v -> checkBoardArchiveStatus(board))
                                        .map(v -> new CardUpdateContext(context.command(), context.card(),
                                                context.oldTitle(), boardList, board)))))
                .flatMap(this::applyChangesToCard)
                .flatMap(this::saveUpdatedCard);
    }

    @Override
    public Either<Failure, Card> updateCardCompleted(String cardId, boolean isCompleted) {
        log.info("UpdateCardService.updateCardCompleted() called with cardId={}, isCompleted={}", cardId, isCompleted);

        return validateCardId(cardId)
                .flatMap(this::findCardById)
                .flatMap(context -> findBoardList(context.card().getListId())
                        .flatMap(boardList -> findBoardByList(boardList)
                                .flatMap(board -> verifyBoardAccessForList(boardList, null)
                                        .flatMap(v -> checkBoardArchiveStatus(board))
                                        .map(v -> context)))
                        .map(v -> context))
                .flatMap(context -> applyCompletedChange(context, isCompleted))
                .flatMap(this::saveUpdatedCardForCompleted);
    }

    @Override
    public Either<Failure, Card> updateCardDueDate(String cardId, Instant dueDate) {
        log.info("UpdateCardService.updateCardDueDate() called with cardId={}, dueDate={}", cardId, dueDate);

        return validateCardId(cardId)
                .flatMap(this::findCardById)
                .flatMap(context -> findBoardList(context.card().getListId())
                        .flatMap(boardList -> findBoardByList(boardList)
                                .flatMap(board -> verifyBoardAccessForList(boardList, null)
                                        .flatMap(v -> checkBoardArchiveStatus(board))
                                        .map(v -> context)))
                        .map(v -> context))
                .flatMap(context -> applyDueDateChange(context, dueDate))
                .flatMap(this::saveUpdatedCardForDueDate);
    }

    @Override
    public Either<Failure, Card> updateCardPriority(String cardId, String priority) {
        log.info("UpdateCardService.updateCardPriority() called with cardId={}, priority={}", cardId, priority);

        return validateCardId(cardId)
                .flatMap(this::findCardById)
                .flatMap(context -> findBoardList(context.card().getListId())
                        .flatMap(boardList -> findBoardByList(boardList)
                                .flatMap(board -> verifyBoardAccessForList(boardList, null)
                                        .flatMap(v -> checkBoardArchiveStatus(board))
                                        .map(v -> context)))
                        .map(v -> context))
                .flatMap(context -> applyPriorityChange(context, priority))
                .flatMap(this::saveUpdatedCardForPriority);
    }

    @Override
    public Either<Failure, Card> updateCardStartDate(String cardId, Instant startDate) {
        log.info("UpdateCardService.updateCardStartDate() called with cardId={}, startDate={}", cardId, startDate);

        return validateCardId(cardId)
                .flatMap(this::findCardById)
                .flatMap(context -> findBoardList(context.card().getListId())
                        .flatMap(boardList -> findBoardByList(boardList)
                                .flatMap(board -> verifyBoardAccessForList(boardList, null)
                                        .flatMap(v -> checkBoardArchiveStatus(board))
                                        .map(v -> context)))
                        .map(v -> context))
                .flatMap(context -> applyStartDateChange(context, startDate))
                .flatMap(this::saveUpdatedCardForStartDate);
    }

    @Override
    public Either<Failure, Card> moveCard(MoveCardCommand command) {
        log.info(
                "UpdateCardService.moveCard() called with command: cardId={}, targetListId={}, newPosition={}, userId={}",
                command.cardId().getId(),
                command.targetListId() != null ? command.targetListId().getId() : "null",
                command.newPosition(),
                command.userId().getId());

        return validateMoveInput(command)
                .flatMap(this::findCardForMove)
                .flatMap(this::verifyBoardAccessForMove)
                .flatMap(this::executeCardMove);
    }

    // ==================== 카드 기본 수정 메서드들 ====================

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
        return Either.right(new CardUpdateContext(command, existingCard, null, null, null));
    }

    /**
     * 5단계: 변경 사항 적용
     */
    private Either<Failure, CardUpdateContext> applyChangesToCard(CardUpdateContext context) {
        try {
            String oldTitle = context.card().getTitle();
            context.card().updateTitle(context.command().title());
            context.card().updateDescription(context.command().description());

            log.debug("카드 변경사항 적용 완료: cardId={}, oldTitle={}, newTitle={}",
                    context.command().cardId().getId(), oldTitle, context.command().title());

            return Either.right(new CardUpdateContext(context.command(), context.card(), oldTitle, context.boardList(),
                    context.board()));
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
                .peek(card -> {
                    log.info("카드 수정 완료: cardId={}, title={}",
                            card.getCardId().getId(), card.getTitle());
                    logCardUpdateActivity(context, card);
                });
    }

    // ==================== 카드 완료 상태 수정 메서드들 ====================

    /**
     * 카드 ID 검증
     */
    private Either<Failure, String> validateCardId(String cardId) {
        if (cardId == null || cardId.trim().isEmpty()) {
            log.warn("카드 ID가 유효하지 않음: cardId={}", cardId);
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.card.id.invalid")));
        }
        return Either.right(cardId);
    }

    /**
     * 카드 ID로 카드 조회
     */
    private Either<Failure, CardUpdateContext> findCardById(String cardId) {
        Optional<Card> cardOpt = cardRepository.findById(new CardId(cardId));
        if (cardOpt.isEmpty()) {
            log.warn("카드를 찾을 수 없음: cardId={}", cardId);
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("error.service.card.update.not_found")));
        }
        return Either.right(new CardUpdateContext(null, cardOpt.get(), null, null, null));
    }

    /**
     * 완료 상태 변경 적용
     */
    private Either<Failure, CardUpdateContext> applyCompletedChange(CardUpdateContext context, boolean isCompleted) {
        try {
            if (isCompleted) {
                context.card().complete();
            } else {
                context.card().uncomplete();
            }

            log.debug("카드 완료 상태 변경: cardId={}, isCompleted={}",
                    context.card().getCardId().getId(), isCompleted);

            return Either.right(context);
        } catch (Exception e) {
            log.error("카드 완료 상태 변경 중 오류: cardId={}, error={}",
                    context.card().getCardId().getId(), e.getMessage(), e);
            return Either.left(Failure.ofInternalServerError(
                    validationMessageResolver.getMessage("error.service.card.update.error")));
        }
    }

    /**
     * 완료 상태 변경된 카드 저장
     */
    private Either<Failure, Card> saveUpdatedCardForCompleted(CardUpdateContext context) {
        return cardRepository.save(context.card())
                .peek(card -> {
                    log.info("카드 완료 상태 변경 완료: cardId={}, isCompleted={}",
                            card.getCardId().getId(), card.isCompleted());
                    logCardCompletedActivity(context, card);
                });
    }

    // ==================== 카드 마감일 수정 메서드들 ====================

    /**
     * 마감일 변경 적용
     */
    private Either<Failure, CardUpdateContext> applyDueDateChange(CardUpdateContext context, Instant dueDate) {
        try {
            if (dueDate == null) {
                context.card().removeDueDate();
            } else {
                context.card().setDueDate(dueDate);
            }

            log.debug("카드 마감일 변경: cardId={}, dueDate={}",
                    context.card().getCardId().getId(), dueDate);

            return Either.right(context);
        } catch (Exception e) {
            log.error("카드 마감일 변경 중 오류: cardId={}, error={}",
                    context.card().getCardId().getId(), e.getMessage(), e);
            return Either.left(Failure.ofInternalServerError(
                    validationMessageResolver.getMessage("error.service.card.update.error")));
        }
    }

    /**
     * 마감일 변경된 카드 저장
     */
    private Either<Failure, Card> saveUpdatedCardForDueDate(CardUpdateContext context) {
        return cardRepository.save(context.card())
                .peek(card -> {
                    log.info("카드 마감일 변경 완료: cardId={}, dueDate={}",
                            card.getCardId().getId(), card.getDueDate());
                    logCardDueDateActivity(context, card);
                });
    }

    // ==================== 카드 우선순위 수정 메서드들 ====================

    /**
     * 우선순위 변경 적용
     */
    private Either<Failure, CardUpdateContext> applyPriorityChange(CardUpdateContext context, String priority) {
        try {
            CardPriority cardPriority = CardPriority.fromValue(priority);
            context.card().setPriority(cardPriority);

            log.debug("카드 우선순위 변경: cardId={}, priority={}",
                    context.card().getCardId().getId(), priority);

            return Either.right(context);
        } catch (Exception e) {
            log.error("카드 우선순위 변경 중 오류: cardId={}, error={}",
                    context.card().getCardId().getId(), e.getMessage(), e);
            return Either.left(Failure.ofInternalServerError(
                    validationMessageResolver.getMessage("error.service.card.update.error")));
        }
    }

    /**
     * 우선순위 변경된 카드 저장
     */
    private Either<Failure, Card> saveUpdatedCardForPriority(CardUpdateContext context) {
        return cardRepository.save(context.card())
                .peek(card -> {
                    String priorityValue = card.getPriority() != null ? card.getPriority().getValue() : "null";
                    log.info("카드 우선순위 변경 완료: cardId={}, priority={}",
                            card.getCardId().getId(), priorityValue);
                    logCardPriorityActivity(context, card);
                });
    }

    // ==================== 카드 시작일 수정 메서드들 ====================

    /**
     * 시작일 변경 적용
     */
    private Either<Failure, CardUpdateContext> applyStartDateChange(CardUpdateContext context, Instant startDate) {
        try {
            if (startDate == null) {
                context.card().removeStartDate();
            } else {
                context.card().setStartDate(startDate);
            }

            log.debug("카드 시작일 변경: cardId={}, startDate={}",
                    context.card().getCardId().getId(), startDate);

            return Either.right(context);
        } catch (Exception e) {
            log.error("카드 시작일 변경 중 오류: cardId={}, error={}",
                    context.card().getCardId().getId(), e.getMessage(), e);
            return Either.left(Failure.ofInternalServerError(
                    validationMessageResolver.getMessage("error.service.card.update.error")));
        }
    }

    /**
     * 시작일 변경된 카드 저장
     */
    private Either<Failure, Card> saveUpdatedCardForStartDate(CardUpdateContext context) {
        return cardRepository.save(context.card())
                .peek(card -> {
                    log.info("카드 시작일 변경 완료: cardId={}, startDate={}",
                            card.getCardId().getId(), card.getStartDate());
                    logCardStartDateActivity(context, card);
                });
    }

    // ==================== 카드 이동 메서드들 ====================

    /**
     * 이동 입력 검증
     */
    private Either<Failure, MoveCardCommand> validateMoveInput(MoveCardCommand command) {
        ValidationResult<MoveCardCommand> validationResult = cardValidator.validateMove(command);
        if (validationResult.isInvalid()) {
            log.warn("카드 이동 입력 검증 실패: {}", validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }
        return Either.right(command);
    }

    /**
     * 이동할 카드 조회
     */
    private Either<Failure, CardMoveContext> findCardForMove(MoveCardCommand command) {
        Optional<Card> cardOpt = cardRepository.findById(command.cardId());
        if (cardOpt.isEmpty()) {
            log.warn("이동할 카드를 찾을 수 없음: cardId={}", command.cardId().getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("error.service.card.move.not_found")));
        }
        return Either.right(new CardMoveContext(command, cardOpt.get()));
    }

    /**
     * 이동에 대한 보드 접근 권한 확인
     */
    private Either<Failure, CardMoveContext> verifyBoardAccessForMove(CardMoveContext context) {
        return validateBoardAccess(context.card().getListId(), context.command().userId())
                .map(v -> context);
    }

    /**
     * 카드 이동 실행
     */
    private Either<Failure, Card> executeCardMove(CardMoveContext context) {
        if (context.command().targetListId() == null) {
            return moveWithinSameList(context.card(), context.command().newPosition(), context.command().userId());
        } else {
            return moveToAnotherList(context.card(), context.command().targetListId(), context.command().newPosition(),
                    context.command().userId());
        }
    }

    // ==================== 공통 헬퍼 메서드들 ====================

    /**
     * 리스트 조회
     */
    private Either<Failure, BoardList> findBoardList(ListId listId) {
        Optional<BoardList> boardList = boardListRepository
                .findById(listId);
        if (boardList.isEmpty()) {
            log.warn("리스트를 찾을 수 없음: listId={}", listId.getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("error.service.card.update.list_not_found")));
        }
        return Either.right(boardList.get());
    }

    /**
     * 리스트로부터 보드 조회
     */
    private Either<Failure, Board> findBoardByList(BoardList boardList) {
        Optional<Board> board = boardRepository
                .findById(boardList.getBoardId());
        if (board.isEmpty()) {
            log.warn("보드를 찾을 수 없음: boardId={}", boardList.getBoardId().getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("error.service.card.update.board_not_found")));
        }
        return Either.right(board.get());
    }

    /**
     * 보드 접근 권한 확인 (리스트 기반)
     */
    private Either<Failure, Object> verifyBoardAccessForList(
            BoardList boardList, UserId userId) {
        if (userId == null) {
            // userId가 null인 경우 (단순 조회용)는 성공으로 처리
            return Either.right(new Object());
        }

        Optional<Board> board = boardRepository
                .findByIdAndOwnerId(boardList.getBoardId(), userId);
        if (board.isEmpty()) {
            log.warn("보드 접근 권한 없음: boardId={}, userId={}",
                    boardList.getBoardId().getId(), userId.getId());
            return Either.left(Failure.ofPermissionDenied(
                    validationMessageResolver.getMessage("error.service.card.update.access_denied")));
        }
        return Either.right(new Object());
    }

    /**
     * 보드 아카이브 상태 확인
     */
    private Either<Failure, Object> checkBoardArchiveStatus(Board board) {
        if (board.isArchived()) {
            log.warn("아카이브된 보드의 카드 수정 시도: boardId={}",
                    board.getBoardId().getId());
            return Either.left(Failure.ofConflict(
                    validationMessageResolver.getMessage("error.service.card.update.archived_board")));
        }
        return Either.right(new Object());
    }

    /**
     * 보드 접근 권한을 확인합니다.
     */
    private Either<Failure, Object> validateBoardAccess(ListId listId, UserId userId) {
        return findBoardList(listId)
                .flatMap(boardList -> {
                    Optional<Board> board = boardRepository
                            .findByIdAndOwnerId(boardList.getBoardId(), userId);
                    if (board.isEmpty()) {
                        log.warn("보드 접근 권한 없음: boardId={}, userId={}",
                                boardList.getBoardId().getId(), userId.getId());
                        return Either.left(Failure.ofPermissionDenied(
                                validationMessageResolver.getMessage("error.service.card.move.access_denied")));
                    }

                    if (board.get().isArchived()) {
                        log.warn("아카이브된 보드에서 카드 이동 시도: boardId={}, userId={}",
                                board.get().getBoardId().getId(), userId.getId());
                        return Either.left(Failure.ofConflict(
                                validationMessageResolver.getMessage("error.service.card.move.archived_board")));
                    }

                    return Either.right(new Object());
                });
    }

    // ==================== 활동 로그 메서드들 ====================

    /**
     * 카드 수정 활동 로그
     */
    private void logCardUpdateActivity(CardUpdateContext context, Card card) {
        String boardName = context.board() != null ? context.board().getTitle()
                : validationMessageResolver.getMessage("error.service.board.unknown");
        BoardId boardId = context.board() != null ? context.board().getBoardId()
                : getBoardIdFromListId(card.getListId());

        var payload = Map.<String, Object>of(
                "oldTitle", context.oldTitle() != null ? context.oldTitle() : context.command().title(),
                "newTitle", context.command().title(),
                "cardId", card.getCardId().getId(),
                "boardName", boardName);

        activityHelper.logCardActivity(
                ActivityType.CARD_RENAME,
                context.command().userId(),
                payload,
                boardName,
                boardId,
                card.getListId(),
                card.getCardId());
    }

    /**
     * 카드 완료 상태 변경 활동 로그
     */
    private void logCardCompletedActivity(CardUpdateContext context, Card card) {
        // 활동 로그는 필요에 따라 구현
        log.debug("카드 완료 상태 변경 활동 로그: cardId={}, isCompleted={}",
                card.getCardId().getId(), card.isCompleted());
    }

    /**
     * 카드 마감일 변경 활동 로그
     */
    private void logCardDueDateActivity(CardUpdateContext context, Card card) {
        // 활동 로그는 필요에 따라 구현
        log.debug("카드 마감일 변경 활동 로그: cardId={}, dueDate={}",
                card.getCardId().getId(), card.getDueDate());
    }

    /**
     * 카드 우선순위 변경 활동 로그
     */
    private void logCardPriorityActivity(CardUpdateContext context, Card card) {
        // 활동 로그는 필요에 따라 구현
        String priorityValue = card.getPriority() != null ? card.getPriority().getValue() : "null";
        log.debug("카드 우선순위 변경 활동 로그: cardId={}, priority={}",
                card.getCardId().getId(), priorityValue);
    }

    /**
     * 카드 시작일 변경 활동 로그
     */
    private void logCardStartDateActivity(CardUpdateContext context, Card card) {
        // 활동 로그는 필요에 따라 구현
        log.debug("카드 시작일 변경 활동 로그: cardId={}, startDate={}",
                card.getCardId().getId(), card.getStartDate());
    }

    // ==================== 기존 메서드들 (이동 관련) ====================

    /**
     * 같은 리스트 내에서 카드를 이동합니다.
     */
    private Either<Failure, Card> moveWithinSameList(Card card, int newPosition, UserId userId) {
        log.debug("같은 리스트 내 카드 이동: cardId={}, oldPosition={}, newPosition={}",
                card.getCardId().getId(), card.getPosition(), newPosition);

        return cardMovePolicy.canMoveWithinSameList(card, newPosition)
                .flatMap(v -> {
                    if (card.getPosition() == newPosition) {
                        log.debug("위치 변경 없음: cardId={}, position={}", card.getCardId().getId(), newPosition);
                        return Either.right(card);
                    }

                    adjustCardPositions(card.getListId(), card.getPosition(), newPosition);
                    card.updatePosition(newPosition);
                    log.debug("카드 위치 업데이트 완료: cardId={}, newPosition={}",
                            card.getCardId().getId(), newPosition);

                    return cardRepository.save(card)
                            .peek(savedCard -> {
                                log.debug("같은 리스트 내 카드 이동 완료: cardId={}, newPosition={}",
                                        savedCard.getCardId().getId(), newPosition);
                            });
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

        return findTargetList(targetListId)
                .flatMap(v -> validateTargetBoardAccess(targetListId, userId))
                .flatMap(v -> cardMovePolicy.canMoveToAnotherList(card, targetListId, newPosition))
                .flatMap(v -> executeMoveToAnotherList(card, targetListId, newPosition, userId));
    }

    /**
     * 대상 리스트 조회
     */
    private Either<Failure, Object> findTargetList(ListId targetListId) {
        Optional<BoardList> targetListOpt = boardListRepository
                .findById(targetListId);
        if (targetListOpt.isEmpty()) {
            log.warn("대상 리스트를 찾을 수 없음: targetListId={}", targetListId.getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("error.service.card.move.target_list_not_found")));
        }
        return Either.right(new Object());
    }

    /**
     * 대상 보드 접근 권한 확인
     */
    private Either<Failure, Object> validateTargetBoardAccess(ListId targetListId, UserId userId) {
        return validateBoardAccess(targetListId, userId);
    }

    /**
     * 다른 리스트로 이동 실행
     */
    private Either<Failure, Card> executeMoveToAnotherList(Card card, ListId targetListId, int newPosition,
            UserId userId) {
        ListId sourceListId = card.getListId();

        adjustCardPositionsForRemoval(card.getListId(), card.getPosition());
        adjustCardPositionsForInsertion(targetListId, newPosition);

        card.moveToList(targetListId, newPosition);
        log.debug("카드 이동 완료: cardId={}, newListId={}, newPosition={}",
                card.getCardId().getId(), targetListId.getId(), newPosition);

        return cardRepository.save(card)
                .peek(savedCard -> {
                    logCardMoveActivity(userId, savedCard, sourceListId, targetListId);
                });
    }

    /**
     * 카드 이동 활동 로그
     */
    private void logCardMoveActivity(UserId userId, Card savedCard, ListId sourceListId, ListId targetListId) {
        var sourceList = boardListRepository.findById(sourceListId).orElse(null);
        var targetList = boardListRepository.findById(targetListId).orElse(null);

        if (sourceList != null && targetList != null) {
            var boardOpt = boardRepository.findById(getBoardIdFromListId(targetListId));
            String boardName = boardOpt.map(board -> board.getTitle()).orElse(
                    validationMessageResolver.getMessage("error.service.board.unknown"));

            activityHelper.logCardMove(
                    userId,
                    savedCard.getTitle(),
                    sourceList.getTitle(),
                    targetList.getTitle(),
                    boardName,
                    getBoardIdFromListId(targetListId),
                    sourceList.getListId(),
                    targetList.getListId(),
                    savedCard.getCardId());
        }
    }

    /**
     * 같은 리스트 내에서 카드들의 위치를 조정합니다.
     */
    private void adjustCardPositions(ListId listId, int oldPosition, int newPosition) {
        List<Card> cardsToUpdate;

        if (oldPosition < newPosition) {
            cardsToUpdate = cardRepository.findByListIdAndPositionBetween(listId, oldPosition + 1, newPosition);
            cardsToUpdate.forEach(card -> card.updatePosition(card.getPosition() - 1));
        } else {
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
     * 리스트 ID로부터 보드 ID를 조회하는 헬퍼 메서드
     */
    private BoardId getBoardIdFromListId(ListId listId) {
        return boardListRepository.findById(listId)
                .map(list -> list.getBoardId())
                .orElse(null);
    }

    // ==================== 컨텍스트 클래스들 ====================

    /**
     * 카드 업데이트 과정에서 사용되는 컨텍스트 객체
     */
    private record CardUpdateContext(
            UpdateCardCommand command,
            Card card,
            String oldTitle,
            BoardList boardList,
            Board board) {

        /**
         * BoardList와 Board 정보를 포함한 생성자
         */
        CardUpdateContext(UpdateCardCommand command, Card card, String oldTitle,
                BoardList boardList,
                Board board) {
            this.command = command;
            this.card = card;
            this.oldTitle = oldTitle;
            this.boardList = boardList;
            this.board = board;
        }
    }

    /**
     * 카드 이동 과정에서 사용되는 컨텍스트 객체
     */
    private record CardMoveContext(
            MoveCardCommand command,
            Card card) {
    }
}
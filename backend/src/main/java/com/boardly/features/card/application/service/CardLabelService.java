package com.boardly.features.card.application.service;

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
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.application.port.input.AddCardLabelCommand;
import com.boardly.features.card.application.port.input.RemoveCardLabelCommand;
import com.boardly.features.card.application.usecase.ManageCardLabelUseCase;
import com.boardly.features.card.application.validation.CardLabelValidator;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.repository.CardLabelRepository;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.label.domain.model.Label;
import com.boardly.features.label.domain.model.LabelId;
import com.boardly.features.label.domain.repository.LabelRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CardLabelService implements ManageCardLabelUseCase {

    private final CardLabelValidator validator;
    private final ValidationMessageResolver messageResolver;
    private final CardRepository cardRepository;
    private final LabelRepository labelRepository;
    private final CardLabelRepository cardLabelRepository;
    private final ActivityHelper activityHelper;
    private final BoardRepository boardRepository;
    private final BoardListRepository boardListRepository;

    @Override
    public Either<Failure, Void> addLabel(AddCardLabelCommand command) {
        log.info("CardLabelService.addLabel() called with command: cardId={}, labelId={}, requesterId={}",
                command.cardId().getId(), command.labelId().getId(), command.requesterId().getId());

        return validateAddCommand(command)
                .flatMap(this::findCardForAdd)
                .flatMap(card -> findLabelForAdd(command)
                        .flatMap(label -> validateCardAndLabelBelongToSameBoard(command, card, label)
                                .flatMap(v -> validateCardAccessForAdd(command, card, label)
                                        .flatMap(v2 -> addLabelToCard(command, card, label)
                                                .peek(result -> logAddLabelActivity(command, card, label))))));
    }

    @Override
    public Either<Failure, Void> removeLabel(RemoveCardLabelCommand command) {
        log.info("CardLabelService.removeLabel() called with command: cardId={}, labelId={}, requesterId={}",
                command.cardId().getId(), command.labelId().getId(), command.requesterId().getId());

        return validateRemoveCommand(command)
                .flatMap(this::findCardForRemove)
                .flatMap(card -> findLabelForRemove(command)
                        .flatMap(label -> validateCardAccessForRemove(command, card, label)
                                .flatMap(v -> removeLabelFromCard(command, card, label)
                                        .peek(result -> logRemoveLabelActivity(command, card, label)))));
    }

    @Override
    public List<LabelId> getCardLabels(CardId cardId, UserId requesterId) {
        log.debug("CardLabelService.getCardLabels() called with cardId={}, requesterId={}",
                cardId.getId(), requesterId.getId());

        // 카드 존재 확인
        Optional<Card> cardOpt = cardRepository.findById(cardId);
        if (cardOpt.isEmpty()) {
            log.warn("카드를 찾을 수 없음: cardId={}", cardId.getId());
            return List.of();
        }

        Card card = cardOpt.get();

        // 카드 접근 권한 확인 (간단한 검증)
        if (!hasCardAccess(card, requesterId)) {
            log.warn("카드 접근 권한 없음: cardId={}, requesterId={}", cardId.getId(), requesterId.getId());
            return List.of();
        }

        List<LabelId> labelIds = cardLabelRepository.findLabelIdsByCardId(cardId);
        log.debug("카드 라벨 조회 완료: cardId={}, 라벨 수={}", cardId.getId(), labelIds.size());
        return labelIds;
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * 명령어 검증
     */
    private Either<Failure, AddCardLabelCommand> validateAddCommand(AddCardLabelCommand command) {
        var validationResult = validator.validateAdd(command);
        if (validationResult.isInvalid()) {
            log.warn("카드 라벨 추가 명령어 검증 실패: {}", validationResult.getErrors());
            return Either.left(Failure.ofInputError(
                    messageResolver.getMessage("validation.card.label.add.validation"),
                    "VALIDATION_ERROR",
                    validationResult.getErrorsAsCollection().stream().toList()));
        }
        return Either.right(command);
    }

    private Either<Failure, RemoveCardLabelCommand> validateRemoveCommand(RemoveCardLabelCommand command) {
        var validationResult = validator.validateRemove(command);
        if (validationResult.isInvalid()) {
            log.warn("카드 라벨 제거 명령어 검증 실패: {}", validationResult.getErrors());
            return Either.left(Failure.ofInputError(
                    messageResolver.getMessage("validation.card.label.remove.validation"),
                    "VALIDATION_ERROR",
                    validationResult.getErrorsAsCollection().stream().toList()));
        }
        return Either.right(command);
    }

    /**
     * 카드 조회
     */
    private Either<Failure, Card> findCardForAdd(AddCardLabelCommand command) {
        Optional<Card> cardOpt = cardRepository.findById(command.cardId());
        if (cardOpt.isEmpty()) {
            log.warn("카드를 찾을 수 없음: cardId={}", command.cardId().getId());
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("validation.card.not.found")));
        }
        return Either.right(cardOpt.get());
    }

    private Either<Failure, Card> findCardForRemove(RemoveCardLabelCommand command) {
        Optional<Card> cardOpt = cardRepository.findById(command.cardId());
        if (cardOpt.isEmpty()) {
            log.warn("카드를 찾을 수 없음: cardId={}", command.cardId().getId());
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("validation.card.not.found")));
        }
        return Either.right(cardOpt.get());
    }

    /**
     * 라벨 조회
     */
    private Either<Failure, Label> findLabelForAdd(AddCardLabelCommand command) {
        Optional<Label> labelOpt = labelRepository.findById(command.labelId());
        if (labelOpt.isEmpty()) {
            log.warn("라벨을 찾을 수 없음: labelId={}", command.labelId().getId());
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("validation.label.not.found")));
        }
        return Either.right(labelOpt.get());
    }

    private Either<Failure, Label> findLabelForRemove(RemoveCardLabelCommand command) {
        Optional<Label> labelOpt = labelRepository.findById(command.labelId());
        if (labelOpt.isEmpty()) {
            log.warn("라벨을 찾을 수 없음: labelId={}", command.labelId().getId());
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("validation.label.not.found")));
        }
        return Either.right(labelOpt.get());
    }

    /**
     * 카드와 라벨이 같은 보드에 속하는지 확인
     */
    private Either<Failure, Void> validateCardAndLabelBelongToSameBoard(AddCardLabelCommand command, Card card,
            Label label) {
        // 카드의 리스트를 통해 보드 ID 조회
        Optional<BoardList> listOpt = boardListRepository
                .findById(card.getListId());
        if (listOpt.isEmpty()) {
            log.warn("카드의 리스트를 찾을 수 없음: listId={}", card.getListId().getId());
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("validation.boardlist.not.found")));
        }

        BoardList list = listOpt.get();
        BoardId cardBoardId = list.getBoardId();
        BoardId labelBoardId = label.getBoardId();

        if (!cardBoardId.equals(labelBoardId)) {
            log.warn("카드와 라벨이 다른 보드에 속함: cardBoardId={}, labelBoardId={}",
                    cardBoardId.getId(), labelBoardId.getId());
            return Either.left(Failure.ofConflict(
                    messageResolver.getMessage("validation.card.label.different.board")));
        }

        return Either.right(null);
    }

    /**
     * 카드 접근 권한 확인
     */
    private Either<Failure, Void> validateCardAccessForAdd(AddCardLabelCommand command, Card card, Label label) {
        if (!hasCardAccess(card, command.requesterId())) {
            log.warn("카드 접근 권한 없음: cardId={}, requesterId={}",
                    command.cardId().getId(), command.requesterId().getId());
            return Either.left(Failure.ofForbidden(
                    messageResolver.getMessage("error.auth.access.denied")));
        }
        return Either.right(null);
    }

    private Either<Failure, Void> validateCardAccessForRemove(RemoveCardLabelCommand command, Card card, Label label) {
        if (!hasCardAccess(card, command.requesterId())) {
            log.warn("카드 접근 권한 없음: cardId={}, requesterId={}",
                    command.cardId().getId(), command.requesterId().getId());
            return Either.left(Failure.ofForbidden(
                    messageResolver.getMessage("error.auth.access.denied")));
        }
        return Either.right(null);
    }

    /**
     * 카드에 라벨 추가
     */
    private Either<Failure, Void> addLabelToCard(AddCardLabelCommand command, Card card, Label label) {
        return cardLabelRepository.addLabelToCard(command.cardId(), command.labelId());
    }

    /**
     * 카드에서 라벨 제거
     */
    private Either<Failure, Void> removeLabelFromCard(RemoveCardLabelCommand command, Card card, Label label) {
        return cardLabelRepository.removeLabelFromCard(command.cardId(), command.labelId());
    }

    /**
     * 카드 접근 권한 확인 (간단한 검증)
     */
    private boolean hasCardAccess(Card card, UserId requesterId) {
        // 카드의 리스트를 통해 보드 ID 조회
        Optional<BoardList> listOpt = boardListRepository
                .findById(card.getListId());
        if (listOpt.isEmpty()) {
            return false;
        }

        BoardList list = listOpt.get();
        BoardId boardId = list.getBoardId();

        // 보드 소유자 확인
        Optional<com.boardly.features.board.domain.model.Board> boardOpt = boardRepository.findById(boardId);
        if (boardOpt.isEmpty()) {
            return false;
        }

        com.boardly.features.board.domain.model.Board board = boardOpt.get();
        return board.getOwnerId().equals(requesterId);
    }

    /**
     * 라벨 추가 활동 로그 기록
     */
    private void logAddLabelActivity(AddCardLabelCommand command, Card card, Label label) {
        try {
            // 리스트 정보 조회
            Optional<BoardList> listOpt = boardListRepository.findById(card.getListId());

            if (listOpt.isPresent()) {
                BoardList list = listOpt.get();

                // 보드 정보 조회
                Optional<Board> boardOpt = boardRepository.findById(list.getBoardId());

                if (boardOpt.isPresent()) {
                    Board board = boardOpt.get();

                    var payload = Map.<String, Object>of(
                            "cardTitle", card.getTitle(),
                            "cardId", card.getCardId().getId(),
                            "labelName", label.getName(),
                            "labelId", label.getLabelId().getId(),
                            "boardName", board.getTitle(),
                            "boardId", board.getBoardId().getId());

                    activityHelper.logCardActivity(
                            ActivityType.CARD_ADD_LABEL,
                            command.requesterId(),
                            payload,
                            board.getTitle(),
                            board.getBoardId(),
                            list.getListId(),
                            card.getCardId());
                }
            }
        } catch (Exception e) {
            log.error("라벨 추가 활동 로그 기록 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 라벨 제거 활동 로그 기록
     */
    private void logRemoveLabelActivity(RemoveCardLabelCommand command, Card card, Label label) {
        try {
            // 리스트 정보 조회
            Optional<BoardList> listOpt = boardListRepository.findById(card.getListId());

            if (listOpt.isPresent()) {
                BoardList list = listOpt.get();

                // 보드 정보 조회
                Optional<Board> boardOpt = boardRepository.findById(list.getBoardId());

                if (boardOpt.isPresent()) {
                    Board board = boardOpt.get();

                    var payload = Map.<String, Object>of(
                            "cardTitle", card.getTitle(),
                            "cardId", card.getCardId().getId(),
                            "labelName", label.getName(),
                            "labelId", label.getLabelId().getId(),
                            "boardName", board.getTitle(),
                            "boardId", board.getBoardId().getId());

                    activityHelper.logCardActivity(
                            ActivityType.CARD_REMOVE_LABEL,
                            command.requesterId(),
                            payload,
                            board.getTitle(),
                            board.getBoardId(),
                            list.getListId(),
                            card.getCardId());
                }
            }
        } catch (Exception e) {
            log.error("라벨 제거 활동 로그 기록 실패: {}", e.getMessage(), e);
        }
    }
}

package com.boardly.features.comment.application.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.activity.domain.model.ActivityType;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.comment.application.port.input.CreateCommentCommand;
import com.boardly.features.comment.application.usecase.CreateCommentUseCase;
import com.boardly.features.comment.application.validation.CommentValidator;
import com.boardly.features.comment.domain.model.Comment;
import com.boardly.features.comment.domain.repository.CommentRepository;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.boardlist.domain.model.ListId;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommentCreateService implements CreateCommentUseCase {

    private final CommentValidator commentValidator;
    private final ValidationMessageResolver validationMessageResolver;
    private final CommentRepository commentRepository;
    private final CardRepository cardRepository;
    private final BoardListRepository boardListRepository;
    private final ActivityHelper activityHelper;

    @Override
    public Either<Failure, Comment> createComment(CreateCommentCommand command) {
        // 1. 입력 검증
        var validationResult = validateInput(command);
        if (validationResult.isLeft()) {
            return Either.left(validationResult.getLeft());
        }

        // 2. 카드 조회
        var cardResult = findCard(command.cardId());
        if (cardResult.isLeft()) {
            return Either.left(cardResult.getLeft());
        }
        var card = cardResult.get();

        // 3. 보드 리스트 조회 (BoardId 획득용)
        var boardListResult = findBoardList(card.getListId());
        if (boardListResult.isLeft()) {
            return Either.left(boardListResult.getLeft());
        }
        var boardList = boardListResult.get();

        // 4. 댓글 생성 및 저장
        var saveResult = createAndSaveComment(command, card);
        if (saveResult.isLeft()) {
            return saveResult;
        }
        var savedComment = saveResult.get();

        // 5. 활동 로그 기록
        logActivity(command, card, boardList, savedComment);

        log.debug("Comment created successfully: {}", savedComment.getCommentId());
        return Either.right(savedComment);
    }

    /**
     * 입력 검증을 수행합니다.
     */
    private Either<Failure, CreateCommentCommand> validateInput(CreateCommentCommand command) {
        var validationResult = commentValidator.validateCreate(command);
        if (validationResult.isInvalid()) {
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("error.service.comment.create.validation"),
                    "VALIDATION_ERROR",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }
        return Either.right(command);
    }

    /**
     * 카드를 조회합니다.
     */
    private Either<Failure, Card> findCard(CardId cardId) {
        try {
            var cardOptional = cardRepository.findById(cardId);
            if (cardOptional.isEmpty()) {
                return Either.left(Failure.ofNotFound(
                        validationMessageResolver.getMessage("error.service.comment.create.card.not.found")));
            }
            return Either.right(cardOptional.get());
        } catch (Exception e) {
            log.error("Failed to fetch card: {}", e.getMessage(), e);
            return Either.left(Failure.ofInternalServerError(
                    validationMessageResolver.getMessage("error.service.comment.create.card.fetch.failed")));
        }
    }

    /**
     * 보드 리스트를 조회합니다.
     */
    private Either<Failure, BoardList> findBoardList(ListId listId) {
        try {
            var boardListOptional = boardListRepository.findById(listId);
            if (boardListOptional.isEmpty()) {
                return Either.left(Failure.ofNotFound(
                        validationMessageResolver.getMessage("error.service.comment.create.boardlist.not.found")));
            }
            return Either.right(boardListOptional.get());
        } catch (Exception e) {
            log.error("Failed to fetch board list: {}", e.getMessage(), e);
            return Either.left(Failure.ofInternalServerError(
                    validationMessageResolver.getMessage("error.service.comment.create.boardlist.fetch.failed")));
        }
    }

    /**
     * 댓글을 생성하고 저장합니다.
     */
    private Either<Failure, Comment> createAndSaveComment(CreateCommentCommand command, Card card) {
        var comment = Comment.create(command.cardId(), command.authorId(), command.content());

        var saveResult = commentRepository.save(comment);
        if (saveResult.isLeft()) {
            log.error("Failed to save comment: {}", saveResult.getLeft().getMessage());
            return Either.left(Failure.ofInternalServerError(
                    validationMessageResolver.getMessage("error.service.comment.create.save.failed")));
        }
        return saveResult;
    }

    /**
     * 활동 로그를 기록합니다.
     */
    private void logActivity(CreateCommentCommand command, Card card, BoardList boardList, Comment savedComment) {
        var payload = Map.<String, Object>of(
                "commentId", savedComment.getCommentId().getId(),
                "content", command.content(),
                "cardTitle", card.getTitle(),
                "cardId", command.cardId().getId());

        activityHelper.logCardActivity(
                ActivityType.CARD_ADD_COMMENT,
                command.authorId(),
                payload,
                boardList.getBoardId(),
                card.getListId(),
                command.cardId());
    }
}

package com.boardly.features.comment.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardly.features.comment.application.port.input.UpdateCommentCommand;
import com.boardly.features.comment.application.usecase.UpdateCommentUseCase;
import com.boardly.features.comment.application.validation.CommentValidator;
import com.boardly.features.comment.domain.model.Comment;
import com.boardly.features.comment.domain.repository.CommentRepository;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommentUpdateService implements UpdateCommentUseCase {

    private final CommentValidator commentValidator;
    private final ValidationMessageResolver validationMessageResolver;
    private final CommentRepository commentRepository;

    @Override
    public Either<Failure, Comment> updateComment(UpdateCommentCommand command) {
        // 1. 입력 검증
        return validateInput(command)
                .flatMap(this::findComment)
                .flatMap(comment -> checkPermission(comment, command))
                .flatMap(comment -> updateCommentContent(comment, command))
                .flatMap(this::saveComment);
    }

    /**
     * 입력 검증
     */
    private Either<Failure, UpdateCommentCommand> validateInput(UpdateCommentCommand command) {
        var validationResult = commentValidator.validateUpdate(command);
        if (validationResult.isInvalid()) {
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.comment.update.error"),
                    "VALIDATION_ERROR",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }
        return Either.right(command);
    }

    /**
     * 댓글 존재 여부 확인
     */
    private Either<Failure, Comment> findComment(UpdateCommentCommand command) {
        var commentOpt = commentRepository.findById(command.commentId());
        if (commentOpt.isEmpty()) {
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("validation.comment.not.found"),
                    "COMMENT_NOT_FOUND",
                    command.commentId()));
        }
        return Either.right(commentOpt.get());
    }

    /**
     * 권한 확인 (댓글 작성자인지)
     */
    private Either<Failure, Comment> checkPermission(Comment comment, UpdateCommentCommand command) {
        if (!comment.isAuthor(command.requesterId())) {
            return Either.left(Failure.ofPermissionDenied(
                    validationMessageResolver.getMessage("validation.comment.update.access.denied"),
                    "COMMENT_UPDATE_ACCESS_DENIED",
                    comment.getCommentId()));
        }
        return Either.right(comment);
    }

    /**
     * 댓글 내용 업데이트
     */
    private Either<Failure, Comment> updateCommentContent(Comment comment, UpdateCommentCommand command) {
        comment.updateContent(command.content());
        return Either.right(comment);
    }

    /**
     * 댓글 저장
     */
    private Either<Failure, Comment> saveComment(Comment comment) {
        return commentRepository.save(comment);
    }
}

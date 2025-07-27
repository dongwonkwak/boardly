package com.boardly.features.comment.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardly.features.comment.application.port.input.DeleteCommentCommand;
import com.boardly.features.comment.application.usecase.DeleteCommentUseCase;
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
public class CommentDeleteService implements DeleteCommentUseCase {

    private final CommentValidator commentValidator;
    private final ValidationMessageResolver validationMessageResolver;
    private final CommentRepository commentRepository;

    @Override
    public Either<Failure, Void> deleteComment(DeleteCommentCommand command) {
        return validateCommand(command)
                .flatMap(this::findComment)
                .flatMap(comment -> validateAuthorization(comment, command))
                .flatMap(this::deleteComment);
    }

    /**
     * 1단계: 입력 검증
     */
    private Either<Failure, DeleteCommentCommand> validateCommand(DeleteCommentCommand command) {
        log.debug("댓글 삭제 입력 검증 시작: commentId={}, requesterId={}",
                command.commentId(), command.requesterId());

        return commentValidator.validateDelete(command)
                .fold(
                        violations -> Either.left(Failure.ofInputError(
                                validationMessageResolver.getMessage("error.service.comment.delete.validation"))),
                        Either::right);
    }

    /**
     * 2단계: 댓글 조회
     */
    private Either<Failure, Comment> findComment(DeleteCommentCommand command) {
        log.debug("댓글 조회 시작: commentId={}", command.commentId());

        return commentRepository.findById(command.commentId())
                .map(Either::<Failure, Comment>right)
                .orElse(Either.left(Failure.ofNotFound(
                        validationMessageResolver.getMessage("error.service.comment.delete.not.found"))));
    }

    /**
     * 3단계: 권한 검증
     */
    private Either<Failure, Comment> validateAuthorization(Comment comment, DeleteCommentCommand command) {
        log.debug("댓글 삭제 권한 검증 시작: commentId={}, authorId={}, requesterId={}",
                comment.getCommentId(), comment.getAuthorId(), command.requesterId());

        if (!comment.isAuthor(command.requesterId())) {
            return Either.left(Failure.ofPermissionDenied(
                    validationMessageResolver.getMessage("error.service.comment.delete.unauthorized")));
        }

        return Either.right(comment);
    }

    /**
     * 4단계: 댓글 삭제
     */
    private Either<Failure, Void> deleteComment(Comment comment) {
        log.debug("댓글 삭제 실행: commentId={}", comment.getCommentId());

        return commentRepository.delete(comment.getCommentId())
                .peekLeft(failure -> log.error("댓글 삭제 실패: commentId={}, failure={}",
                        comment.getCommentId(), failure))
                .peek(voidResult -> log.info("댓글 삭제 성공: commentId={}", comment.getCommentId()));
    }
}

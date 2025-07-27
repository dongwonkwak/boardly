package com.boardly.features.comment.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.comment.application.usecase.GetCommentUseCase;
import com.boardly.features.comment.domain.model.Comment;
import com.boardly.features.comment.domain.model.CommentId;
import com.boardly.features.comment.domain.repository.CommentRepository;
import com.boardly.features.user.domain.model.UserId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentReadService implements GetCommentUseCase {

    private final CommentRepository commentRepository;

    @Override
    public Optional<Comment> getComment(CommentId commentId, UserId requesterId) {
        log.debug("댓글 조회 요청: commentId={}, requesterId={}", commentId, requesterId);

        try {
            Optional<Comment> comment = commentRepository.findById(commentId);

            if (comment.isPresent()) {
                log.debug("댓글 조회 성공: commentId={}", commentId);
            } else {
                log.debug("댓글을 찾을 수 없음: commentId={}", commentId);
            }

            return comment;
        } catch (Exception e) {
            log.error("댓글 조회 중 오류 발생: commentId={}, error={}", commentId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<Comment> getCardComments(CardId cardId, UserId requesterId) {
        log.debug("카드 댓글 목록 조회 요청: cardId={}, requesterId={}", cardId, requesterId);

        try {
            List<Comment> comments = commentRepository.findByCardIdOrderByCreatedAt(cardId);

            log.debug("카드 댓글 목록 조회 완료: cardId={}, 댓글 수={}", cardId, comments.size());
            return comments;
        } catch (Exception e) {
            log.error("카드 댓글 목록 조회 중 오류 발생: cardId={}, error={}", cardId, e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public List<Comment> getUserComments(UserId userId, UserId requesterId) {
        log.debug("사용자 댓글 목록 조회 요청: userId={}, requesterId={}", userId, requesterId);

        try {
            List<Comment> comments = commentRepository.findByAuthorIdOrderByCreatedAtDesc(userId);

            log.debug("사용자 댓글 목록 조회 완료: userId={}, 댓글 수={}", userId, comments.size());
            return comments;
        } catch (Exception e) {
            log.error("사용자 댓글 목록 조회 중 오류 발생: userId={}, error={}", userId, e.getMessage(), e);
            return List.of();
        }
    }
}

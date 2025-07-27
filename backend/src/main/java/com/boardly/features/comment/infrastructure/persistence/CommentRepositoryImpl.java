package com.boardly.features.comment.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.boardly.features.comment.domain.model.Comment;
import com.boardly.features.comment.domain.model.CommentId;
import com.boardly.features.comment.domain.repository.CommentRepository;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepository {

    private final CommentJpaRepository commentJpaRepository;
    private final CommentMapper commentMapper;

    @Override
    public Either<Failure, Comment> save(Comment comment) {
        log.debug("댓글 저장 시작: commentId={}, cardId={}", comment.getCommentId(), comment.getCardId());

        try {
            var entity = commentMapper.toEntity(comment);
            var savedEntity = commentJpaRepository.save(entity);
            var savedComment = commentMapper.toDomain(savedEntity);
            log.debug("댓글 저장 성공: commentId={}", savedComment.getCommentId());

            return Either.right(savedComment);
        } catch (Exception e) {
            log.error("댓글 저장 실패: commentId={}, 예외={}", comment.getCommentId(), e.getMessage());
            return Either.left(Failure.ofInternalServerError("댓글 저장 실패: " + e.getMessage()));
        }
    }

    @Override
    public Optional<Comment> findById(CommentId commentId) {
        log.debug("댓글 조회 시작: commentId={}", commentId);

        var comment = commentJpaRepository.findById(commentId.getId())
                .map(commentMapper::toDomain);

        if (comment.isPresent()) {
            log.debug("댓글 조회 완료: commentId={}", commentId);
        }

        return comment;
    }

    @Override
    public List<Comment> findByCardIdOrderByCreatedAt(CardId cardId) {
        log.debug("카드별 댓글 조회 시작: cardId={}", cardId.getId());
        var entities = commentJpaRepository.findByCardIdOrderByCreatedAt(cardId.getId());
        var comments = entities.stream()
                .map(commentMapper::toDomain)
                .toList();
        log.debug("카드별 댓글 조회 완료: cardId={}, 댓글 개수={}", cardId.getId(), comments.size());
        return comments;
    }

    @Override
    public List<Comment> findByAuthorIdOrderByCreatedAtDesc(UserId authorId) {
        log.debug("작성자별 댓글 조회 시작: authorId={}", authorId.getId());
        var entities = commentJpaRepository.findByAuthorIdOrderByCreatedAtDesc(authorId.getId());
        var comments = entities.stream()
                .map(commentMapper::toDomain)
                .toList();
        log.debug("작성자별 댓글 조회 완료: authorId={}, 댓글 개수={}", authorId.getId(), comments.size());
        return comments;
    }

    @Override
    public Either<Failure, Void> delete(CommentId commentId) {
        log.debug("댓글 삭제 시작: commentId={}", commentId);

        try {
            commentJpaRepository.deleteById(commentId.getId());
            log.debug("댓글 삭제 완료: commentId={}", commentId);
            return Either.right(null);
        } catch (Exception e) {
            log.error("댓글 삭제 실패: commentId={}, 예외={}", commentId, e.getMessage());
            return Either.left(Failure.ofInternalServerError("댓글 삭제 실패: " + e.getMessage()));
        }
    }

    @Override
    public boolean existsById(CommentId commentId) {
        return commentJpaRepository.existsById(commentId.getId());
    }

    @Override
    public int countByCardId(CardId cardId) {
        return commentJpaRepository.countByCardId(cardId.getId());
    }

    @Override
    public Either<Failure, Void> deleteByCardId(CardId cardId) {
        log.debug("카드별 댓글 삭제 시작: cardId={}", cardId.getId());

        try {
            commentJpaRepository.deleteByCardId(cardId.getId());
            log.debug("카드별 댓글 삭제 완료: cardId={}", cardId.getId());
            return Either.right(null);
        } catch (Exception e) {
            log.error("카드별 댓글 삭제 실패: cardId={}, 예외={}", cardId.getId(), e.getMessage());
            return Either.left(Failure.ofInternalServerError("카드별 댓글 삭제 실패: " + e.getMessage()));
        }
    }
}

package com.boardly.features.comment.domain.repository;

import java.util.List;
import java.util.Optional;

import com.boardly.features.comment.domain.model.Comment;
import com.boardly.features.comment.domain.model.CommentId;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;

public interface CommentRepository {

    /**
     * 댓글을 저장합니다.
     */
    Either<Failure, Comment> save(Comment comment);

    /**
     * 댓글 ID로 댓글을 조회합니다.
     */
    Optional<Comment> findById(CommentId commentId);

    /**
     * 카드별 댓글 조회 (생성일시 순)
     */
    List<Comment> findByCardIdOrderByCreatedAt(CardId cardId);

    /**
     * 작성자별 댓글 조회
     */
    List<Comment> findByAuthorIdOrderByCreatedAtDesc(UserId authorId);

    /**
     * 댓글을 삭제합니다.
     */
    Either<Failure, Void> delete(CommentId commentId);

    /**
     * 댓글이 존재하는지 확인합니다.
     */
    boolean existsById(CommentId commentId);

    /**
     * 카드별 댓글 수 조회
     */
    int countByCardId(CardId cardId);

    /**
     * 카드 삭제 시 관련 댓글 모두 삭제
     */
    Either<Failure, Void> deleteByCardId(CardId cardId);
}

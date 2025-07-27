package com.boardly.features.comment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentJpaRepository extends JpaRepository<CommentEntity, String> {
    /**
     * 카드별 댓글 조회 (생성일시 순)
     */
    @Query("SELECT c FROM CommentEntity c WHERE c.cardId = :cardId ORDER BY c.createdAt ASC")
    List<CommentEntity> findByCardIdOrderByCreatedAt(@Param("cardId") String cardId);

    /**
     * 카드별 댓글 수 조회
     */
    @Query("SELECT COUNT(c) FROM CommentEntity c WHERE c.cardId = :cardId")
    int countByCardId(@Param("cardId") String cardId);

    /**
     * 작성자별 댓글 조회
     */
    @Query("SELECT c FROM CommentEntity c WHERE c.authorId = :authorId ORDER BY c.createdAt DESC")
    List<CommentEntity> findByAuthorIdOrderByCreatedAtDesc(@Param("authorId") String authorId);

    /**
     * 카드 삭제 시 관련 댓글 모두 삭제
     */
    void deleteByCardId(String cardId);
}
package com.boardly.features.card.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;

@Repository
public interface CardLabelJpaRepository extends JpaRepository<CardLabelEntity, Long> {

    /**
     * 카드별 라벨 ID 조회
     */
    @Query("SELECT cl.labelId FROM CardLabelEntity cl WHERE cl.cardId = :cardId ORDER BY cl.appliedAt ASC")
    List<String> findLabelIdsByCardId(@Param("cardId") String cardId);

    /**
     * 라벨별 카드 ID 조회
     */
    @Query("SELECT cl.cardId FROM CardLabelEntity cl WHERE cl.labelId = :labelId ORDER BY cl.appliedAt ASC")
    List<String> findCardIdsByLabelId(@Param("labelId") String labelId);

    /**
     * 카드별 라벨 수 조회
     */
    @Query("SELECT COUNT(cl) FROM CardLabelEntity cl WHERE cl.cardId = :cardId")
    int countByCardId(@Param("cardId") String cardId);

    /**
     * 라벨별 카드 수 조회
     */
    @Query("SELECT COUNT(cl) FROM CardLabelEntity cl WHERE cl.labelId = :labelId")
    int countByLabelId(@Param("labelId") String labelId);

    /**
     * 특정 카드-라벨 연결 존재 확인
     */
    boolean existsByCardIdAndLabelId(String cardId, String labelId);

    /**
     * 카드-라벨 연결 삭제
     */
    @Modifying
    @Query("DELETE FROM CardLabelEntity cl WHERE cl.cardId = :cardId AND cl.labelId = :labelId")
    void deleteByCardIdAndLabelId(@Param("cardId") String cardId, @Param("labelId") String labelId);

    /**
     * 카드 삭제 시 관련 라벨 연결 모두 삭제
     */
    @Modifying
    @Query("DELETE FROM CardLabelEntity cl WHERE cl.cardId = :cardId")
    void deleteByCardId(@Param("cardId") String cardId);

    /**
     * 라벨 삭제 시 관련 카드 연결 모두 삭제
     */
    @Modifying
    @Query("DELETE FROM CardLabelEntity cl WHERE cl.labelId = :labelId")
    void deleteByLabelId(@Param("labelId") String labelId);

    /**
     * 보드의 모든 카드-라벨 연결 삭제 (보드 삭제 시)
     */
    @Modifying
    @Query("DELETE FROM CardLabelEntity cl WHERE cl.labelId IN " +
            "(SELECT l.labelId FROM LabelEntity l WHERE l.boardId = :boardId)")
    void deleteByBoardId(@Param("boardId") String boardId);
}
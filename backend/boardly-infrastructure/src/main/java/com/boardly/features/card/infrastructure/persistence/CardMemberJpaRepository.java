package com.boardly.features.card.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CardMemberJpaRepository extends JpaRepository<CardMemberEntity, Long> {

    /**
     * 카드별 담당자 조회
     */
    @Query("SELECT cm FROM CardMemberEntity cm WHERE cm.cardId = :cardId ORDER BY cm.assignedAt ASC")
    List<CardMemberEntity> findByCardIdOrderByAssignedAt(@Param("cardId") String cardId);

    /**
     * 사용자별 담당 카드 조회
     */
    @Query("SELECT cm FROM CardMemberEntity cm WHERE cm.userId = :userId ORDER BY cm.assignedAt DESC")
    List<CardMemberEntity> findByUserIdOrderByAssignedAtDesc(@Param("userId") String userId);

    /**
     * 특정 카드-사용자 담당 관계 존재 확인
     */
    boolean existsByCardIdAndUserId(String cardId, String userId);

    /**
     * 카드-사용자 담당 관계 삭제
     */
    void deleteByCardIdAndUserId(String cardId, String userId);

    /**
     * 카드 삭제 시 관련 담당자 관계 모두 삭제
     */
    void deleteByCardId(String cardId);

    /**
     * 사용자 삭제 시 관련 담당자 관계 모두 삭제
     */
    void deleteByUserId(String userId);

}

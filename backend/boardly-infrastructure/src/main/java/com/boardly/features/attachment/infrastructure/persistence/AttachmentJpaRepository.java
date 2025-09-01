package com.boardly.features.attachment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentJpaRepository extends JpaRepository<AttachmentEntity, String> {

    /**
     * 카드별 첨부파일 조회 (업로드일시 순)
     */
    @Query("SELECT a FROM AttachmentEntity a WHERE a.cardId = :cardId ORDER BY a.createdAt ASC")
    List<AttachmentEntity> findByCardIdOrderByCreatedAt(@Param("cardId") String cardId);

    /**
     * 카드별 첨부파일 수 조회
     */
    @Query("SELECT COUNT(a) FROM AttachmentEntity a WHERE a.cardId = :cardId")
    int countByCardId(@Param("cardId") String cardId);

    /**
     * 업로더별 첨부파일 조회
     */
    @Query("SELECT a FROM AttachmentEntity a WHERE a.uploaderId = :uploaderId ORDER BY a.createdAt DESC")
    List<AttachmentEntity> findByUploaderIdOrderByCreatedAtDesc(@Param("uploaderId") String uploaderId);

    /**
     * 카드별 총 파일 크기 조회
     */
    @Query("SELECT COALESCE(SUM(a.fileSize), 0) FROM AttachmentEntity a WHERE a.cardId = :cardId")
    long sumFileSizeByCardId(@Param("cardId") String cardId);

    /**
     * 카드 삭제 시 관련 첨부파일 모두 삭제
     */
    void deleteByCardId(String cardId);
}

package com.boardly.features.board.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardJpaRepository extends JpaRepository<BoardEntity, String> {

    /**
     * 소유자 ID로 보드 목록을 조회합니다.
     */
    List<BoardEntity> findByOwnerId(String ownerId);

    /**
     * 소유자 ID로 활성 보드 목록을 조회합니다.
     */
    List<BoardEntity> findByOwnerIdAndIsArchivedFalse(String ownerId);

    /**
     * 소유자 ID로 아카이브된 보드 목록을 조회합니다.
     */
    List<BoardEntity> findByOwnerIdAndIsArchivedTrue(String ownerId);

    /**
     * 소유자별 보드 개수를 조회합니다.
     */
    long countByOwnerId(String ownerId);

    /**
     * 소유자별 활성 보드 개수를 조회합니다.
     */
    long countByOwnerIdAndIsArchivedFalse(String ownerId);

    /**
     * 소유자 ID와 보드 ID로 보드를 조회합니다.
     */
    Optional<BoardEntity> findByBoardIdAndOwnerId(String boardId, String ownerId);

    /**
     * 제목으로 보드를 검색합니다.
     */
    @Query("SELECT b FROM BoardEntity b WHERE b.ownerId = :ownerId AND b.title LIKE %:title%")
    List<BoardEntity> findByOwnerIdAndTitleContaining(@Param("ownerId") String ownerId, @Param("title") String title);

    /**
     * 보드 ID가 존재하는지 확인합니다.
     */
    boolean existsByBoardId(String boardId);
}
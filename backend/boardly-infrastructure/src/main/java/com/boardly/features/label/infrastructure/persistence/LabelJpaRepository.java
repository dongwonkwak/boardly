package com.boardly.features.label.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabelJpaRepository extends JpaRepository<LabelEntity, String> {

    /**
     * 보드별 라벨 조회 (이름순)
     */
    @Query("SELECT l FROM LabelEntity l WHERE l.boardId = :boardId ORDER BY l.name ASC")
    List<LabelEntity> findByBoardIdOrderByName(@Param("boardId") String boardId);

    /**
     * 보드 내 라벨명 중복 확인
     */
    @Query("SELECT l FROM LabelEntity l WHERE l.boardId = :boardId AND l.name = :name")
    Optional<LabelEntity> findByBoardIdAndName(@Param("boardId") String boardId, @Param("name") String name);

    /**
     * 보드별 라벨 수 조회
     */
    @Query("SELECT COUNT(l) FROM LabelEntity l WHERE l.boardId = :boardId")
    int countByBoardId(@Param("boardId") String boardId);

    /**
     * 보드 삭제 시 관련 라벨 모두 삭제
     */
    void deleteByBoardId(String boardId);
}

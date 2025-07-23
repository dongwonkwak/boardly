package com.boardly.features.activity.infrastructure.persistence;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Activity 엔티티에 대한 JPA Repository
 * 보드 활동, 사용자 활동, 시간 기반 조회 등의 기능을 제공합니다.
 */
@Repository
public interface ActivityJpaRepository extends JpaRepository<ActivityEntity, String> {

    /**
     * 특정 보드의 모든 활동을 생성일시 역순으로 조회합니다.
     * 
     * @param boardId 조회할 보드 ID
     * @return 해당 보드의 활동 목록 (최신순)
     */
    @Query("SELECT a FROM ActivityEntity a WHERE a.boardId = :boardId ORDER BY a.createdAt DESC")
    List<ActivityEntity> findByBoardIdOrderByCreatedAtDesc(@Param("boardId") String boardId);

    /**
     * 특정 보드의 활동을 생성일시 역순으로 페이징하여 조회합니다.
     * 
     * @param boardId  조회할 보드 ID
     * @param pageable 페이징 정보 (페이지 번호, 크기 등)
     * @return 해당 보드의 활동 목록 (최신순, 페이징 적용)
     */
    @Query("SELECT a FROM ActivityEntity a WHERE a.boardId = :boardId ORDER BY a.createdAt DESC")
    List<ActivityEntity> findByBoardIdOrderByCreatedAtDesc(@Param("boardId") String boardId, Pageable pageable);

    /**
     * 특정 사용자의 모든 활동을 생성일시 역순으로 조회합니다.
     * 
     * @param actorId 조회할 사용자 ID
     * @return 해당 사용자의 활동 목록 (최신순)
     */
    @Query("SELECT a FROM ActivityEntity a WHERE a.actorId = :actorId ORDER BY a.createdAt DESC")
    List<ActivityEntity> findByActorIdOrderByCreatedAtDesc(@Param("actorId") String actorId);

    /**
     * 특정 사용자의 활동을 생성일시 역순으로 페이징하여 조회합니다.
     * 
     * @param actorId  조회할 사용자 ID
     * @param pageable 페이징 정보 (페이지 번호, 크기 등)
     * @return 해당 사용자의 활동 목록 (최신순, 페이징 적용)
     */
    @Query("SELECT a FROM ActivityEntity a WHERE a.actorId = :actorId ORDER BY a.createdAt DESC")
    List<ActivityEntity> findByActorIdOrderByCreatedAtDesc(@Param("actorId") String actorId, Pageable pageable);

    /**
     * 특정 보드의 활동 중 지정된 시간 범위 내의 활동을 생성일시 역순으로 조회합니다.
     * 
     * @param boardId   조회할 보드 ID
     * @param startTime 조회 시작 시간 (포함)
     * @param endTime   조회 종료 시간 (포함)
     * @return 해당 보드의 시간 범위 내 활동 목록 (최신순)
     */
    @Query("SELECT a FROM ActivityEntity a WHERE a.boardId = :boardId AND a.createdAt BETWEEN :startTime AND :endTime ORDER BY a.createdAt DESC")
    List<ActivityEntity> findByBoardIdAndCreatedAtBetween(
            @Param("boardId") String boardId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);

    /**
     * 특정 보드의 활동 개수를 조회합니다.
     * 
     * @param boardId 조회할 보드 ID
     * @return 해당 보드의 활동 총 개수
     */
    @Query("SELECT COUNT(a) FROM ActivityEntity a WHERE a.boardId = :boardId")
    long countByBoardId(@Param("boardId") String boardId);

    /**
     * 특정 보드의 활동 중 지정된 시간 이후의 활동 개수를 조회합니다.
     * 
     * @param boardId 조회할 보드 ID
     * @param after   조회 시작 시간 (이후)
     * @return 해당 보드의 지정 시간 이후 활동 총 개수
     */
    @Query("SELECT COUNT(a) FROM ActivityEntity a WHERE a.boardId = :boardId AND a.createdAt > :after")
    long countByBoardIdAndCreatedAtAfter(@Param("boardId") String boardId, @Param("after") Instant after);

    /**
     * 특정 사용자의 활동 개수를 조회합니다.
     * 
     * @param actorId 조회할 사용자 ID
     * @return 해당 사용자의 활동 총 개수
     */
    @Query("SELECT COUNT(a) FROM ActivityEntity a WHERE a.actorId = :actorId")
    long countByActorId(@Param("actorId") String actorId);

    /**
     * 특정 보드의 활동 중 지정된 시간 이후의 활동을 생성일시 역순으로 조회합니다.
     * 
     * @param boardId 조회할 보드 ID
     * @param after   조회 시작 시간 (이후)
     * @return 해당 보드의 지정 시간 이후 활동 목록 (최신순)
     */
    @Query("SELECT a FROM ActivityEntity a WHERE a.boardId = :boardId AND a.createdAt > :after ORDER BY a.createdAt DESC")
    List<ActivityEntity> findByBoardIdAndCreatedAtAfter(@Param("boardId") String boardId,
            @Param("after") Instant after);

    /**
     * 특정 보드의 활동 중 지정된 시간 이후의 활동을 생성일시 역순으로 페이징하여 조회합니다.
     * 
     * @param boardId  조회할 보드 ID
     * @param after    조회 시작 시간 (이후)
     * @param pageable 페이징 정보 (페이지 번호, 크기 등)
     * @return 해당 보드의 지정 시간 이후 활동 목록 (최신순, 페이징 적용)
     */
    @Query("SELECT a FROM ActivityEntity a WHERE a.boardId = :boardId AND a.createdAt > :after ORDER BY a.createdAt DESC")
    List<ActivityEntity> findByBoardIdAndCreatedAtAfter(@Param("boardId") String boardId,
            @Param("after") Instant after, Pageable pageable);

    /**
     * 지정된 시간 이전의 모든 활동을 삭제합니다.
     * 주로 오래된 활동 데이터 정리를 위해 사용됩니다.
     * 
     * @param before 삭제 기준 시간 (이전)
     */
    @Modifying
    @Query("DELETE FROM ActivityEntity a WHERE a.createdAt < :before")
    void deleteByCreatedAtBefore(@Param("before") Instant before);
}

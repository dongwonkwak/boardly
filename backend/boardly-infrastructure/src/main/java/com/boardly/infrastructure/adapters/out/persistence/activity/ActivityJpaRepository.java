package com.boardly.infrastructure.adapters.out.persistence.activity;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityJpaRepository extends JpaRepository<ActivityEntity, String> {
  @Query("SELECT a FROM ActivityEntity a WHERE a.boardId = :boardId ORDER BY a.createdAt DESC")
  List<ActivityEntity> findByBoardIdOrderByCreatedAtDesc(@Param("boardId") String boardId);

  @Query("SELECT a FROM ActivityEntity a WHERE a.boardId = :boardId ORDER BY a.createdAt DESC")
  List<ActivityEntity> findByBoardIdOrderByCreatedAtDesc(
      @Param("boardId") String boardId, Pageable pageable);

  @Query("SELECT a FROM ActivityEntity a WHERE a.actorId = :actorId ORDER BY a.createdAt DESC")
  List<ActivityEntity> findByActorIdOrderByCreatedAtDesc(@Param("actorId") String actorId);

  @Query("SELECT a FROM ActivityEntity a WHERE a.actorId = :actorId ORDER BY a.createdAt DESC")
  List<ActivityEntity> findByActorIdOrderByCreatedAtDesc(
      @Param("actorId") String actorId, Pageable pageable);

  @Query(
      "SELECT a FROM ActivityEntity a WHERE a.boardId = :boardId AND a.createdAt BETWEEN :startTime AND :endTime ORDER BY a.createdAt DESC")
  List<ActivityEntity> findByBoardIdAndCreatedAtBetween(
      @Param("boardId") String boardId,
      @Param("startTime") Instant startTime,
      @Param("endTime") Instant endTime);

  @Query("SELECT COUNT(a) FROM ActivityEntity a WHERE a.boardId = :boardId")
  long countByBoardId(@Param("boardId") String boardId);

  @Query("SELECT COUNT(a) FROM ActivityEntity a WHERE a.actorId = :actorId")
  long countByActorId(@Param("actorId") String actorId);

  @Query(
      "SELECT a FROM ActivityEntity a WHERE a.boardId = :boardId AND a.createdAt > :after ORDER BY a.createdAt DESC")
  List<ActivityEntity> findByBoardIdAndCreatedAtAfter(
      @Param("boardId") String boardId, @Param("after") Instant after);

  @Modifying
  @Query("DELETE FROM ActivityEntity a WHERE a.createdAt < :before")
  void deleteByCreatedAtBefore(@Param("before") Instant before);
}

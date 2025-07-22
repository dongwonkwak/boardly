package com.boardly.features.card.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CardJpaRepository extends JpaRepository<CardEntity, String> {

  /**
   * 리스트 ID로 카드 목록을 위치 순서대로 조회
   */

  List<CardEntity> findByListIdOrderByPosition(String listId);

  /**
   * 리스트 ID로 카드 목록을 조회
   */
  List<CardEntity> findByListId(String listId);

  /**
   * 리스트 ID와 위치보다 큰 카드들을 조회
   */
  List<CardEntity> findByListIdAndPositionGreaterThan(String listId, int position);

  /**
   * 리스트 ID와 위치 범위로 카드들을 조회
   */
  List<CardEntity> findByListIdAndPositionBetween(String listId, int startPosition, int endPosition);

  /**
   * 리스트별 카드 개수를 조회
   */
  long countByListId(String listId);

  /**
   * 제목으로 카드를 검색
   */
  List<CardEntity> findByListIdAndTitleContaining(String listId, String title);

  /**
   * 리스트 ID와 카드 ID로 카드를 조회
   */
  Optional<CardEntity> findByCardIdAndListId(String cardId, String listId);

  /**
   * 리스트 ID와 위치로 카드를 조회
   */
  Optional<CardEntity> findByListIdAndPosition(String listId, int position);

  /**
   * 리스트 ID의 최대 위치를 조회
   */
  @Query("SELECT MAX(c.position) FROM CardEntity c WHERE c.listId = :listId")
  Optional<Integer> findMaxPositionByListId(@Param("listId") String listId);

  /**
   * 여러 리스트의 카드들을 조회
   */
  List<CardEntity> findByListIdIn(List<String> listIds);

  /**
   * 리스트의 모든 카드를 삭제
   */
  void deleteByListId(String listId);

  /**
   * 보드의 모든 카드를 삭제 (리스트 ID 목록으로)
   */
  @Query("DELETE FROM CardEntity c WHERE c.listId IN :listIds")
  void deleteByListIds(@Param("listIds") List<String> listIds);
}

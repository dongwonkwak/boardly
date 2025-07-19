package com.boardly.features.boardlist.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardListJpaRepository extends JpaRepository<BoardListEntity, String> {

  /**
   * 보드 ID로 해당 보드의 모든 리스트를 position 순서대로 조회합니다.
   * 
   * @param boardId 보드 ID
   * @return 보드 리스트 목록 (위치 순으로 정렬)
   */
  List<BoardListEntity> findByBoardIdOrderByPosition(String boardId);

  /**
   * 보드 ID로 해당 보드의 모든 리스트를 조회합니다.
   */
  List<BoardListEntity> findByBoardId(String boardId);

  /**
   * 보드 내 특정 위치 이후의 모든 리스트를 조회합니다.
   */
  List<BoardListEntity> findByBoardIdAndPositionGreaterThan(String boardId, int position);

  /**
   * 보드 내 특정 위치 범위의 리스트를 조회합니다.
   */
  List<BoardListEntity> findByBoardIdAndPositionBetween(String boardId, int startPosition, int endPosition);

  /**
   * 보드별 리스트 개수를 조회합니다.
   */
  long countByBoardId(String boardId);

  /**
   * 보드 내에서 가장 높은 position 값을 조회합니다.
   */
  @Query("SELECT MAX(bl.position) FROM BoardListEntity bl WHERE bl.boardId = :boardId")
  Optional<Integer> findMaxPositionByBoardId(@Param("boardId") String boardId);

  /**
   * 보드의 모든 리스트를 삭제합니다.
   */
  void deleteByBoardId(String boardId);

  /**
   * 보드 ID와 리스트 ID로 리스트를 조회합니다.
   */
  Optional<BoardListEntity> findByListIdAndBoardId(String listId, String boardId);

  /**
   * 보드 내에서 제목으로 리스트를 검색합니다.
   */
  @Query("SELECT bl FROM BoardListEntity bl WHERE bl.boardId = :boardId AND bl.title LIKE %:title%")
  List<BoardListEntity> findByBoardIdAndTitleContaining(@Param("boardId") String boardId, @Param("title") String title);

  /**
   * 리스트 ID가 존재하는지 확인합니다.
   */
  boolean existsByListId(String listId);

  /**
   * 보드 내 특정 position에 있는 리스트를 조회합니다.
   */
  Optional<BoardListEntity> findByBoardIdAndPosition(String boardId, int position);

  /**
   * 보드 내 특정 위치 이전의 모든 리스트를 조회합니다.
   */
  List<BoardListEntity> findByBoardIdAndPositionLessThan(String boardId, int position);

  /**
   * 보드 내 특정 위치 이상의 모든 리스트를 조회합니다.
   */
  List<BoardListEntity> findByBoardIdAndPositionGreaterThanEqual(String boardId, int position);

  /**
   * 보드와 색상으로 리스트를 조회합니다.
   */
  List<BoardListEntity> findByBoardIdAndColor(String boardId, String color);
}

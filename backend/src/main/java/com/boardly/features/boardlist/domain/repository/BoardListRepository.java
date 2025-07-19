package com.boardly.features.boardlist.domain.repository;

import com.boardly.shared.domain.common.Failure;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListId;

import java.util.List;
import java.util.Optional;

import io.vavr.control.Either;

public interface BoardListRepository {
  
  /**
   * 새로운 보드 리스트를 저장합니다.
   * 
   * @param boardList 저장할 보드 리스트
   * @return 저장된 보드 리스트
   */
  BoardList save(BoardList boardList);
  
  /**
   * 리스트 ID로 보드 리스트를 조회합니다.
   * 
   * @param listId 조회할 리스트 ID
   * @return 보드 리스트 (존재하지 않으면 empty)
   */
  Optional<BoardList> findById(ListId listId);
  
  /**
   * 보드 ID로 해당 보드의 모든 리스트를 조회합니다.
   * 
   * @param boardId 보드 ID
   * @return 보드 리스트 목록 (위치 순으로 정렬)
   */
  List<BoardList> findByBoardIdOrderByPosition(BoardId boardId);

  /**
   * 보드 ID로 해당 보드의 모든 리스트를 조회합니다.
   * 
   * @param boardId 보드의 고유 식별자
   * @return 리스트 목록
   */
  List<BoardList> findByBoardId(BoardId boardId);

  /**
   * 보드 내 특정 위치 이후의 모든 리스트를 조회합니다.
   * position 업데이트 시 사용됩니다.
   * 
   * @param boardId 보드의 고유 식별자
   * @param position 기준 위치
   * @return 기준 위치 이후의 리스트 목록
   */
  List<BoardList> findByBoardIdAndPositionGreaterThan(BoardId boardId, int position);

  /**
   * 보드 내 특정 위치 범위의 리스트를 조회합니다.
   * 
   * @param boardId 보드의 고유 식별자
   * @param startPosition 시작 위치 (inclusive)
   * @param endPosition 끝 위치 (inclusive)
   * @return 범위 내의 리스트 목록
   */
  List<BoardList> findByBoardIdAndPositionBetween(BoardId boardId, int startPosition, int endPosition);

  
  /**
   * 보드 ID로 해당 보드의 리스트 개수를 조회합니다.
   * 
   * @param boardId 보드 ID
   * @return 리스트 개수
   */
  Long countByBoardId(BoardId boardId);

  /**
   * 보드 내에서 가장 높은 position 값을 조회합니다.
   * 새 리스트 추가 시 position 계산에 사용됩니다.
   * 
   * @param boardId 보드의 고유 식별자
   * @return 가장 높은 position 값 (리스트가 없으면 Optional.empty())
   */
  Optional<Integer> findMaxPositionByBoardId(BoardId boardId);
  
  /**
   * 보드 리스트를 삭제합니다.
   * 
   * @param listId 삭제할 리스트 ID
   */
  void deleteById(ListId listId);

  /**
   * 리스트를 삭제합니다.
   * 
   * @param boardList 삭제할 리스트 엔티티
   */
  void delete(BoardList boardList);

  /**
   * 보드의 모든 리스트를 삭제합니다.
   * 보드 삭제 시 관련된 모든 리스트를 함께 삭제할 때 사용됩니다.
   * 
   * @param boardId 보드의 고유 식별자
   */
  void deleteByBoardId(BoardId boardId);
  

  /**
   * 리스트 ID가 존재하는지 확인합니다.
   * 
   * @param listId 확인할 리스트의 고유 식별자
   * @return 존재하면 true, 그렇지 않으면 false
   */
  boolean existsById(ListId listId);

  /**
   * 보드 ID와 리스트 ID로 리스트를 조회합니다.
   * 권한 확인이 필요한 경우 사용됩니다.
   * 
   * @param listId 리스트의 고유 식별자
   * @param boardId 보드의 고유 식별자
   * @return 리스트 엔티티 (존재하지 않으면 Optional.empty())
   */
  Optional<BoardList> findByIdAndBoardId(ListId listId, BoardId boardId);

  /**
   * 보드 내에서 제목으로 리스트를 검색합니다.
   * 
   * @param boardId 보드의 고유 식별자
   * @param title 검색할 제목 (부분 일치)
   * @return 제목에 해당하는 리스트 목록
   */
  List<BoardList> findByBoardIdAndTitleContaining(BoardId boardId, String title);

  /**
   * 여러 리스트를 배치로 저장합니다.
   * position 업데이트 시 성능 최적화를 위해 사용됩니다.
   * 
   * @param boardLists 저장할 리스트 목록
   * @return 저장된 리스트 목록
   */
  List<BoardList> saveAll(List<BoardList> boardLists);
}

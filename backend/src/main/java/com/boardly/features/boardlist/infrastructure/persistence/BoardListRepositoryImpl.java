package com.boardly.features.boardlist.infrastructure.persistence;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class BoardListRepositoryImpl implements BoardListRepository {

  private final BoardListJpaRepository boardListJpaRepository;
  private final BoardListMapper boardListMapper;

  @Override
  public BoardList save(BoardList boardList) {
    log.debug("리스트 저장 시작: listId={}, title={}",
        boardList.getListId().getId(), boardList.getTitle());

    var entity = boardListMapper.toEntity(boardList);
    var savedEntity = boardListJpaRepository.save(entity);

    return boardListMapper.toDomain(savedEntity);
  }

  @Override
  public List<BoardList> findByBoardIdAndPositionGreaterThan(BoardId boardId, int position) {
    log.debug("보드 ID와 위치로 리스트 조회 시작: boardId={}, position={}", boardId.getId(), position);
    var entities = boardListJpaRepository.findByBoardIdAndPositionGreaterThan(boardId.getId(), position);
    List<BoardList> boardLists = entities.stream()
        .map(boardListMapper::toDomain)
        .toList();
    log.debug("보드 ID와 위치로 리스트 조회 완료: boardId={}, 리스트 개수={}", boardId.getId(), boardLists.size());
    return boardLists;
  }

  @Override
  public List<BoardList> findByBoardIdAndPositionBetween(BoardId boardId, int startPosition, int endPosition) {
    log.debug("보드 ID와 위치 범위로 리스트 조회 시작: boardId={}, startPosition={}, endPosition={}", boardId.getId(), startPosition,
        endPosition);
    var entities = boardListJpaRepository.findByBoardIdAndPositionBetween(boardId.getId(), startPosition, endPosition);
    var boardLists = entities.stream()
        .map(boardListMapper::toDomain)
        .toList();
    log.debug("보드 ID와 위치 범위로 리스트 조회 완료: boardId={}, 리스트 개수={}", boardId.getId(), boardLists.size());
    return boardLists;
  }

  @Override
  public Optional<BoardList> findById(ListId listId) {
    log.debug("리스트 조회 시작: listId={}", listId.getId());
    var boardList = boardListJpaRepository.findById(listId.getId())
        .map(boardListMapper::toDomain);

    if (boardList.isPresent()) {
      log.debug("리스트 조회 완료: listId={}, title={}", listId.getId(), boardList.get().getTitle());
    }

    return boardList;
  }

  @Override
  public List<BoardList> findByBoardIdOrderByPosition(BoardId boardId) {
    log.debug("보드 ID로 리스트 조회 시작: boardId={}", boardId.getId());
    var entities = boardListJpaRepository.findByBoardIdOrderByPosition(boardId.getId());
    var boardLists = entities.stream()
        .map(boardListMapper::toDomain)
        .toList();
    log.debug("보드 ID로 리스트 조회 완료: boardId={}, 리스트 개수={}", boardId.getId(), boardLists.size());
    return boardLists;
  }

  @Override
  public List<BoardList> findByBoardId(BoardId boardId) {
    log.debug("보드 ID로 리스트 조회 시작: boardId={}", boardId.getId());
    var entities = boardListJpaRepository.findByBoardId(boardId.getId());
    var boardLists = entities.stream()
        .map(boardListMapper::toDomain)
        .toList();
    log.debug("보드 ID로 리스트 조회 완료: boardId={}, 리스트 개수={}", boardId.getId(), boardLists.size());
    return boardLists;
  }

  @Override
  public boolean existsById(ListId listId) {
    log.debug("리스트 ID 존재 확인 시작: listId={}", listId.getId());
    boolean exists = boardListJpaRepository.existsById(listId.getId());
    log.debug("리스트 ID 존재 확인 완료: listId={}, 존재={}", listId.getId(), exists);
    return exists;
  }

  @Override
  public Optional<BoardList> findByIdAndBoardId(ListId listId, BoardId boardId) {
    log.debug("리스트 ID와 보드 ID로 리스트 조회 시작: listId={}, boardId={}", listId.getId(), boardId.getId());
    var entity = boardListJpaRepository.findByListIdAndBoardId(listId.getId(), boardId.getId());
    var boardList = entity.map(boardListMapper::toDomain);
    if (boardList.isPresent()) {
      log.debug("리스트 ID와 보드 ID로 리스트 조회 완료: listId={}, boardId={}, title={}", listId.getId(), boardId.getId(),
          boardList.get().getTitle());
    }
    return boardList;
  }

  @Override
  public List<BoardList> findByBoardIdAndTitleContaining(BoardId boardId, String title) {
    log.debug("보드 ID와 제목으로 리스트 조회 시작: boardId={}, title={}", boardId.getId(), title);
    var entities = boardListJpaRepository.findByBoardIdAndTitleContaining(boardId.getId(), title);
    var boardLists = entities.stream()
        .map(boardListMapper::toDomain)
        .toList();
    log.debug("보드 ID와 제목으로 리스트 조회 완료: boardId={}, 리스트 개수={}", boardId.getId(), boardLists.size());
    return boardLists;
  }

  @Override
  public List<BoardList> saveAll(List<BoardList> boardLists) {
    log.debug("리스트 목록 저장 시작: 리스트 개수={}", boardLists.size());

    var entities = boardLists.stream()
        .map(boardListMapper::toEntity)
        .toList();
    var savedEntities = boardListJpaRepository.saveAll(entities);
    var savedBoardLists = savedEntities.stream()
        .map(boardListMapper::toDomain)
        .toList();
    log.debug("리스트 목록 저장 완료: 리스트 개수={}", savedBoardLists.size());
    return savedBoardLists;
  }

  @Override
  public Long countByBoardId(BoardId boardId) {
    log.debug("보드 ID로 리스트 개수 조회 시작: boardId={}", boardId.getId());
    var count = boardListJpaRepository.countByBoardId(boardId.getId());
    log.debug("보드 ID로 리스트 개수 조회 완료: boardId={}, 개수={}", boardId.getId(), count);
    return count;
  }

  @Override
  public Optional<Integer> findMaxPositionByBoardId(BoardId boardId) {
    log.debug("보드 ID로 가장 높은 위치 조회 시작: boardId={}", boardId.getId());
    var maxPosition = boardListJpaRepository.findMaxPositionByBoardId(boardId.getId());
    log.debug("보드 ID로 가장 높은 위치 조회 완료: boardId={}, 위치={}", boardId.getId(), maxPosition.orElse(0));
    return maxPosition;
  }

  @Override
  public void deleteById(ListId listId) {
    log.debug("리스트 삭제 시작: listId={}", listId.getId());
    boardListJpaRepository.deleteById(listId.getId());
    log.debug("리스트 삭제 완료: listId={}", listId.getId());
  }

  @Override
  public void delete(BoardList boardList) {
    log.debug("리스트 삭제 시작: listId={}", boardList.getListId().getId());
    boardListJpaRepository.delete(boardListMapper.toEntity(boardList));
    log.debug("리스트 삭제 완료: listId={}", boardList.getListId().getId());
  }

  @Override
  public void deleteByBoardId(BoardId boardId) {
    log.debug("보드 ID로 리스트 삭제 시작: boardId={}", boardId.getId());
    boardListJpaRepository.deleteByBoardId(boardId.getId());
    log.debug("보드 ID로 리스트 삭제 완료: boardId={}", boardId.getId());
  }
}

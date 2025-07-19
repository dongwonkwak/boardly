package com.boardly.features.boardlist.application.service;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.application.port.input.UpdateBoardListPositionCommand;
import com.boardly.features.boardlist.application.usecase.UpdateBoardListPositionUseCase;
import com.boardly.features.boardlist.application.validation.UpdateBoardListPositionValidator;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.application.validation.ValidationResult;

import io.vavr.control.Either;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * 보드 리스트 위치 변경 서비스
 * 
 * @since 1.0.0
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UpdateBoardListPositionService implements UpdateBoardListPositionUseCase {

  private final UpdateBoardListPositionValidator updateBoardListPositionValidator;
  private final BoardRepository boardRepository;
  private final BoardListRepository boardListRepository;

  @Override
  public Either<Failure, List<BoardList>> updateBoardListPosition(UpdateBoardListPositionCommand command) {
    log.info("UpdateBoardListPositionService.updateBoardListPosition() called with command: {}", command);

    // 1. 입력 데이터 검증
    ValidationResult<UpdateBoardListPositionCommand> validationResult = updateBoardListPositionValidator.validate(command);
    if (validationResult.isInvalid()) {
      log.warn("보드 리스트 위치 변경 검증 실패: listId={}, newPosition={}, violations={}", 
                    command.listId(), command.newPosition(), validationResult.getErrorsAsCollection());
      return Either.left(Failure.ofValidation(
        "INVALID_INPUT", validationResult.getErrorsAsCollection()));
    }

    // 2. 리스트 존재 확인
    Optional<BoardList> listResult = boardListRepository.findById(command.listId());
    if (listResult.isEmpty()) {
      log.warn("리스트를 찾을 수 없음: listId={}", command.listId().getId());
      return Either.left(Failure.ofNotFound("LIST_NOT_FOUND"));
    }

    BoardList targetList = listResult.get();
    int currentPosition = targetList.getPosition();

    // 3. 보드 존재 확인
    Optional<Board> boardResult = boardRepository.findById(targetList.getBoardId());
    if (boardResult.isEmpty()) {
      log.warn("보드를 찾을 수 없음: boardId={}", targetList.getBoardId().getId());
      return Either.left(Failure.ofNotFound("BOARD_NOT_FOUND"));
    }

    Board board = boardResult.get();

    // 4. 권한 확인 (보드 소유자만 수정 가능)
    if (!board.getOwnerId().equals(command.userId())) {
      log.warn("리스트 위치 변경 권한 없음: listId={}, userId={}, boardOwnerId={}", 
               command.listId().getId(), command.userId().getId(), board.getOwnerId().getId());
      return Either.left(Failure.ofForbidden("UNAUTHORIZED_ACCESS"));
    }

    // 5. 보드의 모든 리스트 조회
    List<BoardList> allLists = boardListRepository.findByBoardIdOrderByPosition(targetList.getBoardId());
    
    // 6. 새로운 위치의 유효성 확인
    if (command.newPosition() >= allLists.size()) {
      log.warn("새로운 위치가 리스트 개수를 초과함: newPosition={}, totalLists={}", 
               command.newPosition(), allLists.size());
      return Either.left(Failure.ofConflict("INVALID_POSITION"));
    }

    // 7. 위치가 실제로 변경되는지 확인
    if (currentPosition == command.newPosition()) {
      log.info("위치가 변경되지 않음: listId={}, currentPosition={}, newPosition={}", 
               command.listId().getId(), currentPosition, command.newPosition());
      return Either.right(allLists);
    }

    // 8. 위치 변경 및 다른 리스트들의 position 조정
    try {
      List<BoardList> updatedLists = reorderLists(allLists, currentPosition, command.newPosition());
      
      // 9. 변경된 리스트들을 배치로 저장
      List<BoardList> savedLists = boardListRepository.saveAll(updatedLists);
      
      log.info("보드 리스트 위치 변경 완료: listId={}, oldPosition={}, newPosition={}, updatedLists={}", 
               command.listId().getId(), currentPosition, command.newPosition(), savedLists.size());
      return Either.right(savedLists);

    } catch (Exception e) {
      log.error("보드 리스트 위치 변경 중 예외 발생: listId={}, error={}", 
                command.listId().getId(), e.getMessage(), e);
      return Either.left(Failure.ofInternalServerError(e.getMessage()));
    }
  }

  /**
   * 리스트들의 위치를 재정렬합니다.
   * 
   * @param lists 현재 보드의 모든 리스트 (position 순서대로 정렬됨)
   * @param fromPosition 이동할 리스트의 현재 위치
   * @param toPosition 이동할 리스트의 새로운 위치
   * @return 위치가 조정된 리스트들의 목록
   */
  private List<BoardList> reorderLists(List<BoardList> lists, int fromPosition, int toPosition) {
    // 새로운 ArrayList로 복사하여 수정 가능하게 만듦
    List<BoardList> mutableLists = new java.util.ArrayList<>(lists);
    
    // 이동할 리스트를 제거
    BoardList movedList = mutableLists.remove(fromPosition);
    
    // 새로운 위치에 삽입
    mutableLists.add(toPosition, movedList);
    
    // 모든 리스트의 position을 0부터 순차적으로 재설정
    for (int i = 0; i < mutableLists.size(); i++) {
      mutableLists.get(i).updatePosition(i);
    }
    
    return mutableLists;
  }
} 
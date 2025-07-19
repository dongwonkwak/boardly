package com.boardly.features.boardlist.application.service;

import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.application.port.input.CreateBoardListCommand;
import com.boardly.features.boardlist.application.usecase.CreateBoardListUseCase;
import com.boardly.features.boardlist.application.validation.CreateBoardListValidator;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.policy.ListLimitPolicy;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.application.validation.ValidationResult;

import io.vavr.control.Either;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CreateBoardListService implements CreateBoardListUseCase {

  private final CreateBoardListValidator createBoardListValidator;
  private final BoardRepository boardRepository;
  private final BoardListRepository boardListRepository;
  private final ListLimitPolicy listLimitPolicy;

    
  @Override
  public Either<Failure, BoardList> createBoardList(CreateBoardListCommand command) {
    log.info("CreateBoardListService.createBoardList() called with command: {}", command);

    // 1. 입력 데이터 검증
    ValidationResult<CreateBoardListCommand> validationResult = createBoardListValidator.validate(command);
    if (validationResult.isInvalid()) {
      log.warn("보드 리스트 생성 검증 실패: boardId={}, violations={}", 
                    command.boardId(), validationResult.getErrorsAsCollection());
      return Either.left(Failure.ofValidation(
        "INVALID_INPUT", validationResult.getErrorsAsCollection()));
    }
    // 2. 보드 존재 및 권한 확인
    var boardResult = boardRepository.findById(command.boardId());
    if (boardResult.isEmpty()) {
      log.warn("보드를 찾을 수 없음: boardId={}", command.boardId().getId());
      return Either.left(Failure.ofNotFound("BOARD_NOT_FOUND"));
    }
    
    var currentBoard = boardResult.get();
    if (!currentBoard.getOwnerId().equals(command.userId())) {
      log.warn("보드 접근 권한 없음: boardId={}, userId={}", command.boardId().getId(), command.userId().getId());
      return Either.left(Failure.ofForbidden("UNAUTHORIZED_ACCESS"));
    }

    // 3. 리스트 생성 정책 확인
    var currentListCount = boardListRepository.countByBoardId(command.boardId());
    if (!listLimitPolicy.canCreateList(currentListCount)) {
      log.warn("리스트 생성 한도 초과: boardId={}, currentCount={}", command.boardId().getId(), currentListCount);
      return Either.left(Failure.ofForbidden("LIST_LIMIT_EXCEEDED"));
    }

    // 4. 리스트 생성
    try {
      // 다음 위치 계산
      var maxPositionResult = boardListRepository.findMaxPositionByBoardId(command.boardId());
      int nextPosition = maxPositionResult.map(pos -> pos + 1).orElse(0);
      
      var newList = BoardList.create(command.title(), command.description(), nextPosition, command.color(), command.boardId());
      var savedList = boardListRepository.save(newList);
      
      log.info("리스트 생성 완료: boardId={}, listId={}, title={}", 
               command.boardId().getId(), savedList.getListId().getId(), savedList.getTitle());
      return Either.right(savedList);
      
    } catch (Exception e) {
      log.error("리스트 생성 중 예외 발생: boardId={}, error={}", command.boardId().getId(), e.getMessage(), e);
      return Either.left(Failure.ofInternalServerError(e.getMessage()));
    }
  }
}

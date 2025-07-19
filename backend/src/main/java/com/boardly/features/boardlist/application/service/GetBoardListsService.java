package com.boardly.features.boardlist.application.service;

import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.application.port.input.GetBoardListsCommand;
import com.boardly.features.boardlist.application.usecase.GetBoardListsUseCase;
import com.boardly.features.boardlist.application.validation.GetBoardListsValidator;
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

/**
 * 보드 리스트 조회 서비스
 * 
 * @since 1.0.0
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetBoardListsService implements GetBoardListsUseCase {

  private final GetBoardListsValidator getBoardListsValidator;
  private final BoardRepository boardRepository;
  private final BoardListRepository boardListRepository;

  @Override
  public Either<Failure, List<BoardList>> getBoardLists(GetBoardListsCommand command) {
    log.info("GetBoardListsService.getBoardLists() called with command: {}", command);

    // 1. 입력 데이터 검증
    ValidationResult<GetBoardListsCommand> validationResult = getBoardListsValidator.validate(command);
    if (validationResult.isInvalid()) {
      log.warn("보드 리스트 조회 검증 실패: listId={}, violations={}", 
                    command.listId(), validationResult.getErrorsAsCollection());
      return Either.left(Failure.ofValidation(
        "INVALID_INPUT", validationResult.getErrorsAsCollection()));
    }

    // 2. 리스트 존재 확인
    var listResult = boardListRepository.findById(command.listId());
    if (listResult.isEmpty()) {
      log.warn("리스트를 찾을 수 없음: listId={}", command.listId().getId());
      return Either.left(Failure.ofNotFound("LIST_NOT_FOUND"));
    }

    var list = listResult.get();

    // 3. 보드 존재 확인
    var boardResult = boardRepository.findById(list.getBoardId());
    if (boardResult.isEmpty()) {
      log.warn("보드를 찾을 수 없음: boardId={}", list.getBoardId().getId());
      return Either.left(Failure.ofNotFound("BOARD_NOT_FOUND"));
    }

    var board = boardResult.get();

    // 4. 권한 확인 (보드 소유자만 조회 가능)
    if (!board.getOwnerId().equals(command.userId())) {
      log.warn("리스트 조회 권한 없음: listId={}, userId={}, boardOwnerId={}", 
               command.listId().getId(), command.userId().getId(), board.getOwnerId().getId());
      return Either.left(Failure.ofForbidden("UNAUTHORIZED_ACCESS"));
    }

    // 5. 보드의 모든 리스트 조회
    try {
      List<BoardList> boardLists = boardListRepository.findByBoardIdOrderByPosition(list.getBoardId());
      
      log.info("보드 리스트 조회 완료: boardId={}, 리스트 개수={}", 
               list.getBoardId().getId(), boardLists.size());
      return Either.right(boardLists);

    } catch (Exception e) {
      log.error("보드 리스트 조회 중 예외 발생: listId={}, error={}", 
                command.listId().getId(), e.getMessage(), e);
      return Either.left(Failure.ofInternalServerError(e.getMessage()));
    }
  }
} 
package com.boardly.features.boardlist.application.service;

import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.application.port.input.UpdateBoardListCommand;
import com.boardly.features.boardlist.application.usecase.UpdateBoardListUseCase;
import com.boardly.features.boardlist.application.validation.UpdateBoardListValidator;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.application.validation.ValidationResult;

import io.vavr.control.Either;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 보드 리스트 수정 서비스
 * 
 * @since 1.0.0
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UpdateBoardListService implements UpdateBoardListUseCase {

  private final UpdateBoardListValidator updateBoardListValidator;
  private final BoardRepository boardRepository;
  private final BoardListRepository boardListRepository;

  @Override
  public Either<Failure, BoardList> updateBoardList(UpdateBoardListCommand command) {
    log.info("UpdateBoardListService.updateBoardList() called with command: {}", command);

    // 1. 입력 데이터 검증
    ValidationResult<UpdateBoardListCommand> validationResult = updateBoardListValidator.validate(command);
    if (validationResult.isInvalid()) {
      log.warn("보드 리스트 수정 검증 실패: listId={}, violations={}", 
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

    var currentList = listResult.get();

    // 3. 보드 존재 확인
    var boardResult = boardRepository.findById(currentList.getBoardId());
    if (boardResult.isEmpty()) {
      log.warn("보드를 찾을 수 없음: boardId={}", currentList.getBoardId().getId());
      return Either.left(Failure.ofNotFound("BOARD_NOT_FOUND"));
    }

    var board = boardResult.get();

    // 4. 권한 확인 (보드 소유자만 수정 가능)
    if (!board.getOwnerId().equals(command.userId())) {
      log.warn("리스트 수정 권한 없음: listId={}, userId={}, boardOwnerId={}", 
               command.listId().getId(), command.userId().getId(), board.getOwnerId().getId());
      return Either.left(Failure.ofForbidden("UNAUTHORIZED_ACCESS"));
    }

    // 5. 리스트 정보 업데이트
    try {
      // 제목 업데이트
      if (command.title() != null) {
        currentList.updateTitle(command.title());
      }
      
      // 설명 업데이트 (null이 아닌 경우에만)
      if (command.description() != null) {
        currentList.updateDescription(command.description());
      }
      
      // 색상 업데이트 (null이 아닌 경우에만)
      if (command.color() != null) {
        currentList.updateColor(command.color());
      }

      var savedList = boardListRepository.save(currentList);

      log.info("리스트 수정 완료: listId={}, title={}", 
               savedList.getListId().getId(), savedList.getTitle());
      return Either.right(savedList);

    } catch (Exception e) {
      log.error("리스트 수정 중 예외 발생: listId={}, error={}", 
                command.listId().getId(), e.getMessage(), e);
      return Either.left(Failure.ofInternalServerError(e.getMessage()));
    }
  }
} 
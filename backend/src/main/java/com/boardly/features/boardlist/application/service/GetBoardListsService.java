package com.boardly.features.boardlist.application.service;

import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.application.port.input.GetBoardListsCommand;
import com.boardly.features.boardlist.application.usecase.GetBoardListsUseCase;
import com.boardly.features.boardlist.application.validation.GetBoardListsValidator;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.policy.BoardListCreationPolicy;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.application.validation.ValidationResult;

import io.vavr.control.Either;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

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
  private final BoardListCreationPolicy boardListCreationPolicy;
  private final ValidationMessageResolver validationMessageResolver;

  @Override
  public Either<Failure, List<BoardList>> getBoardLists(GetBoardListsCommand command) {
    log.info("GetBoardListsService.getBoardLists() called with command: {}", command);

    // 1. 입력 데이터 검증
    ValidationResult<GetBoardListsCommand> validationResult = getBoardListsValidator.validate(command);
    if (validationResult.isInvalid()) {
      log.warn("보드 리스트 조회 검증 실패: boardId={}, violations={}",
          command.boardId(), validationResult.getErrorsAsCollection());
      return Either.left(Failure.ofInputError(
          validationMessageResolver.getMessage("validation.input.invalid"),
          "INVALID_INPUT",
          List.copyOf(validationResult.getErrorsAsCollection())));
    }

    // 2. 보드 존재 확인
    var boardResult = boardRepository.findById(command.boardId());
    if (boardResult.isEmpty()) {
      log.warn("보드를 찾을 수 없음: boardId={}", command.boardId().getId());
      return Either.left(Failure.ofNotFound(
          validationMessageResolver.getMessage("validation.board.not.found"),
          "BOARD_NOT_FOUND",
          Map.of("boardId", command.boardId().getId())));
    }

    var board = boardResult.get();

    // 3. 권한 확인 (보드 소유자만 조회 가능)
    if (!board.getOwnerId().equals(command.userId())) {
      log.warn("보드 리스트 조회 권한 없음: boardId={}, userId={}, boardOwnerId={}",
          command.boardId().getId(), command.userId().getId(), board.getOwnerId().getId());
      return Either.left(Failure.ofPermissionDenied(
          validationMessageResolver.getMessage("validation.board.modification.access.denied"),
          "UNAUTHORIZED_ACCESS",
          Map.of("boardId", command.boardId().getId(), "userId", command.userId().getId())));
    }

    // 4. 보드의 모든 리스트 조회
    try {
      List<BoardList> boardLists = boardListRepository.findByBoardIdOrderByPosition(command.boardId());

      // 5. 리스트 개수 상태 정보 로깅
      var listCountStatus = boardListCreationPolicy.getStatus(command.boardId());
      if (listCountStatus.requiresNotification()) {
        log.warn("보드 리스트 개수 상태: boardId={}, status={}, message={}",
            command.boardId().getId(), listCountStatus.getDisplayName(), listCountStatus.getMessage());
      }

      log.info("보드 리스트 조회 완료: boardId={}, 리스트 개수={}, 상태={}",
          command.boardId().getId(), boardLists.size(), listCountStatus.getDisplayName());
      return Either.right(boardLists);

    } catch (Exception e) {
      log.error("보드 리스트 조회 중 예외 발생: boardId={}, error={}",
          command.boardId().getId(), e.getMessage(), e);
      return Either.left(Failure.ofInternalError(
          e.getMessage(),
          "BOARD_LIST_QUERY_ERROR",
          null));
    }
  }
}
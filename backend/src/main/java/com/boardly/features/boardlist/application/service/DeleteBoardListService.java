package com.boardly.features.boardlist.application.service;

import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.application.port.input.DeleteBoardListCommand;
import com.boardly.features.boardlist.application.usecase.DeleteBoardListUseCase;
import com.boardly.features.boardlist.application.validation.DeleteBoardListValidator;
import com.boardly.features.boardlist.domain.model.BoardList;
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

/**
 * 보드 리스트 삭제 서비스
 * 
 * @since 1.0.0
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DeleteBoardListService implements DeleteBoardListUseCase {

  private final DeleteBoardListValidator deleteBoardListValidator;
  private final BoardRepository boardRepository;
  private final BoardListRepository boardListRepository;
  private final ValidationMessageResolver validationMessageResolver;

  @Override
  public Either<Failure, Void> deleteBoardList(DeleteBoardListCommand command) {
    log.info("DeleteBoardListService.deleteBoardList() called with command: {}", command);

    // 1. 입력 데이터 검증
    ValidationResult<DeleteBoardListCommand> validationResult = deleteBoardListValidator.validate(command);
    if (validationResult.isInvalid()) {
      log.warn("보드 리스트 삭제 검증 실패: listId={}, violations={}",
          command.listId(), validationResult.getErrorsAsCollection());
      return Either.left(Failure.ofInputError(
          validationMessageResolver.getMessage("validation.input.invalid"),
          "INVALID_INPUT",
          List.copyOf(validationResult.getErrorsAsCollection())));
    }

    // 2. 리스트 존재 확인
    var listResult = boardListRepository.findById(command.listId());
    if (listResult.isEmpty()) {
      log.warn("리스트를 찾을 수 없음: listId={}", command.listId().getId());
      return Either.left(Failure.ofNotFound("LIST_NOT_FOUND"));
    }

    var listToDelete = listResult.get();

    // 3. 보드 존재 확인
    var boardResult = boardRepository.findById(listToDelete.getBoardId());
    if (boardResult.isEmpty()) {
      log.warn("보드를 찾을 수 없음: boardId={}", listToDelete.getBoardId().getId());
      return Either.left(Failure.ofNotFound("BOARD_NOT_FOUND"));
    }

    var board = boardResult.get();

    // 4. 권한 확인 (보드 소유자만 삭제 가능)
    if (!board.getOwnerId().equals(command.userId())) {
      log.warn("리스트 삭제 권한 없음: listId={}, userId={}, boardOwnerId={}",
          command.listId().getId(), command.userId().getId(), board.getOwnerId().getId());
      return Either.left(Failure.ofForbidden("UNAUTHORIZED_ACCESS"));
    }

    // 5. 리스트 삭제 및 position 재정렬
    try {
      // 리스트 삭제
      boardListRepository.deleteById(command.listId());
      log.info("리스트 삭제 완료: listId={}, title={}",
          command.listId().getId(), listToDelete.getTitle());

      // 이후 리스트들의 position 재정렬
      reorderRemainingLists(listToDelete.getBoardId(), listToDelete.getPosition());

      return Either.right(null);

    } catch (Exception e) {
      log.error("리스트 삭제 중 예외 발생: listId={}, error={}",
          command.listId().getId(), e.getMessage(), e);
      return Either.left(Failure.ofInternalServerError(e.getMessage()));
    }
  }

  /**
   * 삭제된 리스트 이후의 모든 리스트들의 position을 재정렬합니다.
   * 
   * @param boardId         보드 ID
   * @param deletedPosition 삭제된 리스트의 position
   */
  private void reorderRemainingLists(com.boardly.features.board.domain.model.BoardId boardId, int deletedPosition) {
    try {
      // 삭제된 position 이후의 모든 리스트 조회
      List<BoardList> remainingLists = boardListRepository.findByBoardIdAndPositionGreaterThan(boardId,
          deletedPosition);

      if (!remainingLists.isEmpty()) {
        log.debug("리스트 position 재정렬 시작: boardId={}, 삭제된 position={}, 재정렬할 리스트 수={}",
            boardId.getId(), deletedPosition, remainingLists.size());
        // position을 1씩 감소시켜 재정렬
        remainingLists.forEach(list -> list.updatePosition(list.getPosition() - 1));
        // 변경된 리스트들을 배치로 저장
        boardListRepository.saveAll(remainingLists);

        log.debug("리스트 position 재정렬 완료: boardId={}, 재정렬된 리스트 수={}",
            boardId.getId(), remainingLists.size());
      }
    } catch (Exception e) {
      log.error("리스트 position 재정렬 중 오류 발생: boardId={}, deletedPosition={}, error={}",
          boardId.getId(), deletedPosition, e.getMessage(), e);
      // position 재정렬 실패는 전체 삭제 작업을 실패시키지 않도록 로그만 남김
    }
  }
}
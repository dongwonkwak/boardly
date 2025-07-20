package com.boardly.features.board.application.service;

import com.boardly.features.board.application.port.input.ToggleStarBoardCommand;
import com.boardly.features.board.application.usecase.ToggleStarBoardUseCase;
import com.boardly.features.board.application.validation.ToggleStarBoardValidator;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;
import io.vavr.control.Try;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 보드 즐겨찾기 토글 서비스
 * 
 * <p>
 * 보드의 즐겨찾기 상태 토글 관련 비즈니스 로직을 처리하는 애플리케이션 서비스입니다.
 * 입력 검증, 권한 확인, 도메인 로직 실행, 저장 등의 전체 즐겨찾기 토글 프로세스를 관리합니다.
 * 
 * @since 1.0.0
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ToggleStarBoardService implements ToggleStarBoardUseCase {

  private final ToggleStarBoardValidator boardValidator;
  private final BoardRepository boardRepository;
  private final ValidationMessageResolver messageResolver;

  @Override
  public Either<Failure, Board> starringBoard(ToggleStarBoardCommand command) {
    log.info("보드 즐겨찾기 추가 시작: boardId={}, requestedBy={}",
        command.boardId().getId(), command.requestedBy().getId());
    return processBoardStarStatus(command, true);
  }

  @Override
  public Either<Failure, Board> unstarringBoard(ToggleStarBoardCommand command) {
    log.info("보드 즐겨찾기 제거 시작: boardId={}, requestedBy={}",
        command.boardId().getId(), command.requestedBy().getId());
    return processBoardStarStatus(command, false);
  }

  private Either<Failure, Board> processBoardStarStatus(ToggleStarBoardCommand command, boolean starStatus) {
    return validateInput(command)
        .flatMap(this::findExistingBoard)
        .flatMap(this::verifyOwnership)
        .flatMap(context -> checkForChanges(context, starStatus))
        .flatMap(this::applyStarChangeToBoard)
        .flatMap(this::saveUpdatedBoard);
  }

  /**
   * 1단계: 입력 검증
   */
  private Either<Failure, ToggleStarBoardCommand> validateInput(ToggleStarBoardCommand command) {
    ValidationResult<ToggleStarBoardCommand> validationResult = boardValidator.validate(command);
    if (validationResult.isInvalid()) {
      log.warn("보드 즐겨찾기 토글 검증 실패: boardId={}, violations={}",
          command.boardId().getId(), validationResult.getErrorsAsCollection());
      return Either.left(Failure.ofInputError(
          messageResolver.getMessage("validation.input.invalid"),
          "INVALID_INPUT",
          List.copyOf(validationResult.getErrorsAsCollection())));
    }
    return Either.right(command);
  }

  /**
   * 2단계: 기존 보드 조회
   */
  private Either<Failure, ToggleStarContext> findExistingBoard(ToggleStarBoardCommand command) {
    Optional<Board> existingBoardOpt = boardRepository.findById(command.boardId());
    if (existingBoardOpt.isEmpty()) {
      log.warn("보드를 찾을 수 없음: boardId={}", command.boardId().getId());
      return Either.left(Failure.ofNotFound(messageResolver.getMessage("validation.board.not.found")));
    }

    Board existingBoard = existingBoardOpt.get();
    return Either.right(new ToggleStarContext(command, existingBoard));
  }

  /**
   * 3단계: 권한 확인 (요청자가 보드 소유자인지 확인)
   */
  private Either<Failure, ToggleStarContext> verifyOwnership(ToggleStarContext context) {
    if (!context.board().getOwnerId().equals(context.command().requestedBy())) {
      log.warn("보드 즐겨찾기 토글 권한 없음: boardId={}, ownerId={}, requestedBy={}",
          context.command().boardId().getId(),
          context.board().getOwnerId().getId(),
          context.command().requestedBy().getId());
      return Either
          .left(Failure.ofForbidden(messageResolver.getMessage("validation.board.modification.access.denied")));
    }
    return Either.right(context);
  }

  /**
   * 4단계: 현재 즐겨찾기 상태 확인 및 변경 여부 결정
   */
  private Either<Failure, ToggleStarContext> checkForChanges(ToggleStarContext context, boolean targetStarStatus) {
    if (targetStarStatus == context.board().isStarred()) {
      log.info("보드 즐겨찾기 상태 변경 없음: boardId={}, 현재상태={}, 요청상태={}",
          context.command().boardId().getId(), context.board().isStarred(), targetStarStatus);
      return Either.right(new ToggleStarContext(context.command(), context.board(), targetStarStatus, true));
    }

    log.debug("보드 즐겨찾기 상태 변경 필요: boardId={}, 현재상태={}, 요청상태={}",
        context.command().boardId().getId(), context.board().isStarred(), targetStarStatus);
    return Either.right(new ToggleStarContext(context.command(), context.board(), targetStarStatus, false));
  }

  /**
   * 5단계: 즐겨찾기 상태 변경 적용
   */
  private Either<Failure, ToggleStarContext> applyStarChangeToBoard(ToggleStarContext context) {
    // 변경 사항이 없으면 바로 반환
    if (context.hasNoChange()) {
      return Either.right(context);
    }

    try {
      boolean oldStatus = context.board().isStarred();
      context.board().updateStarred(context.targetStarStatus());

      log.info("보드 즐겨찾기 상태 변경 적용: boardId={}, 이전상태={}, 새상태={}",
          context.command().boardId().getId(), oldStatus, context.targetStarStatus());
      return Either.right(context);
    } catch (Exception e) {
      log.error("보드 즐겨찾기 상태 변경 중 오류 발생: boardId={}, error={}",
          context.command().boardId().getId(), e.getMessage(), e);
      return Either
          .left(Failure.ofInternalServerError(messageResolver.getMessage("validation.board.star.toggle.error")));
    }
  }

  /**
   * 6단계: 업데이트된 보드 저장
   */
  private Either<Failure, Board> saveUpdatedBoard(ToggleStarContext context) {
    // 변경 사항이 없으면 기존 보드 반환
    if (context.hasNoChange()) {
      return Either.right(context.board());
    }

    return Try.of(() -> boardRepository.save(context.board()))
        .fold(
            throwable -> {
              log.error("보드 즐겨찾기 변경 중 예외 발생: boardId={}, error={}",
                  context.command().boardId().getId(), throwable.getMessage(), throwable);
              return Either
                  .left(Failure.ofInternalServerError(messageResolver.getMessage("validation.board.star.save.error")));
            },
            saveResult -> {
              if (saveResult.isRight()) {
                Board savedBoard = saveResult.get();
                log.info("보드 즐겨찾기 변경 완료: boardId={}, 최종상태={}",
                    savedBoard.getBoardId().getId(), savedBoard.isStarred());
                return Either.right(savedBoard);
              } else {
                log.error("보드 저장 실패: boardId={}, error={}",
                    context.command().boardId().getId(), saveResult.getLeft().getMessage());
                return saveResult;
              }
            });
  }

  /**
   * 보드 즐겨찾기 변경 과정에서 사용되는 컨텍스트 객체
   */
  private record ToggleStarContext(
      ToggleStarBoardCommand command,
      Board board,
      boolean targetStarStatus,
      boolean hasNoChange) {
    ToggleStarContext(ToggleStarBoardCommand command, Board board) {
      this(command, board, false, false);
    }
  }
}
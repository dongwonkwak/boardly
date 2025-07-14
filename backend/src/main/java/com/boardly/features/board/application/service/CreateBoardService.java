package com.boardly.features.board.application.service;

import com.boardly.features.board.application.port.input.CreateBoardCommand;
import com.boardly.features.board.application.usecase.CreateBoardUseCase;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;
import jakarta.transaction.Transactional;
import io.vavr.control.Try;
import org.springframework.stereotype.Service;

import com.boardly.features.board.application.validation.CreateBoardValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CreateBoardService implements CreateBoardUseCase {

  private final CreateBoardValidator boardValidator;
  private final BoardRepository boardRepository;

  @Override
  public Either<Failure, Board> createBoard(CreateBoardCommand command) {
    log.info("보드 생성 시작: title={}, description={}, ownerId={}", command.title(), command.description(), command.ownerId());

    // 1. 입력 검증
    ValidationResult<CreateBoardCommand> validationResult = boardValidator.validate(command);
    if (validationResult.isInvalid()) {
      log.warn("보드 생성 검증 실패: title={}, violations={}", command.title(), validationResult.getErrorsAsCollection());
      return Either.left(Failure.ofValidation("INVALID_INPUT", validationResult.getErrorsAsCollection()));
    }

    // 2. 보드 생성
    Board board = Board.create(command.title(), command.description(), command.ownerId());

    // 3. 보드 저장
    return Try.of(() -> boardRepository.save(board))
      .fold(
        throwable -> {
          log.error("보드 생성 중 예외 발생: title={}, error={}", command.title(), throwable.getMessage(), throwable);
          return Either.left(Failure.ofInternalServerError("보드 생성 중 오류가 발생했습니다."));
        },
        saveResult -> {
          if (saveResult.isRight()) {
            log.info("보드 생성 완료: boardId={}, title={}", saveResult.get().getBoardId(), command.title());
          } else {
            log.error("보드 저장 실패: title={}, error={}", command.title(), saveResult.getLeft().message());
          }
          return saveResult;
        }
      );
  }
}

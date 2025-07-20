package com.boardly.features.board.application.service;

import com.boardly.features.board.application.port.input.GetUserBoardsCommand;
import com.boardly.features.board.application.usecase.GetUserBoardsUseCase;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetUserBoardsService implements GetUserBoardsUseCase {

    private final BoardRepository boardRepository;
    private final ValidationMessageResolver validationMessageResolver;

    @Override
    public Either<Failure, List<Board>> getUserBoards(GetUserBoardsCommand command) {
        if (command == null) {
            var violation = Failure.FieldViolation.builder()
                    .field("command")
                    .message("GetUserBoardsCommand is null")
                    .rejectedValue(null)
                    .build();
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.input.invalid"),
                    "INVALID_COMMAND",
                    List.of(violation)));
        }

        log.info("사용자 보드 목록 조회 시작: ownerId={}, includeArchived={}",
                command.ownerId(), command.includeArchived());

        // 1. 입력 검증
        if (command.ownerId() == null) {
            var violation = Failure.FieldViolation.builder()
                    .field("userId")
                    .message(validationMessageResolver.getMessage("validation.user.id.required"))
                    .rejectedValue(null)
                    .build();
            log.warn("사용자 보드 목록 조회 검증 실패: ownerId=null");
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.of(violation)));
        }

        // 2. 보드 목록 조회
        return Try.of(() -> {
            List<Board> boards;

            if (command.includeArchived()) {
                // 모든 보드 조회 (활성 + 아카이브)
                boards = boardRepository.findByOwnerId(command.ownerId());
                log.debug("전체 보드 조회 완료: ownerId={}, count={}", command.ownerId(), boards.size());
            } else {
                // 활성 보드만 조회
                boards = boardRepository.findActiveByOwnerId(command.ownerId());
                log.debug("활성 보드 조회 완료: ownerId={}, count={}", command.ownerId(), boards.size());
            }

            // 3. 최신 수정 시간 순으로 정렬
            List<Board> sortedBoards = boards.stream()
                    .sorted(Comparator.comparing(Board::getUpdatedAt).reversed())
                    .toList();

            log.info("사용자 보드 목록 조회 완료: ownerId={}, totalCount={}, includeArchived={}",
                    command.ownerId(), sortedBoards.size(), command.includeArchived());

            return sortedBoards;
        })
                .fold(
                        throwable -> {
                            log.error("사용자 보드 목록 조회 중 예외 발생: ownerId={}, error={}",
                                    command.ownerId(), throwable.getMessage(), throwable);
                            return Either
                                    .left(Failure.ofInternalError(throwable.getMessage(), "BOARD_QUERY_ERROR", null));
                        },
                        Either::right);
    }
}
package com.boardly.features.board.application.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardly.features.board.application.dto.BoardDetailDto;
import com.boardly.features.board.application.port.input.GetBoardDetailCommand;
import com.boardly.features.board.application.port.input.GetUserBoardsCommand;
import com.boardly.features.board.application.port.output.GetBoardDetailPort;
import com.boardly.features.board.application.port.output.GetBoardDetailPort.BoardDetailData;
import com.boardly.features.board.application.usecase.GetBoardDetailUseCase;
import com.boardly.features.board.application.usecase.GetUserBoardsUseCase;
import com.boardly.features.board.application.validation.BoardValidator;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.user.application.service.UserFinder;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 보드 조회 서비스
 * 
 * <p>
 * 보드 조회 관련 작업을 담당하는 통합 서비스입니다.
 * 사용자 보드 목록 조회와 보드 상세 조회 기능을 제공합니다.
 * </p>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BoardQueryService implements GetUserBoardsUseCase, GetBoardDetailUseCase {

    private final BoardRepository boardRepository;
    private final UserFinder userFinder;
    private final GetBoardDetailPort getBoardDetailPort;
    private final ValidationMessageResolver validationMessageResolver;
    private final BoardValidator boardValidator;

    @Override
    public Either<Failure, List<Board>> getUserBoards(GetUserBoardsCommand command) {
        return validateCommand(command)
                .flatMap(cmd -> validateOwnerId(cmd))
                .flatMap(cmd -> checkUserExists(cmd))
                .flatMap(cmd -> fetchUserBoards(cmd));
    }

    private Either<Failure, GetUserBoardsCommand> validateCommand(GetUserBoardsCommand command) {
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

        return Either.right(command);
    }

    private Either<Failure, GetUserBoardsCommand> validateOwnerId(GetUserBoardsCommand command) {
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
        return Either.right(command);
    }

    private Either<Failure, GetUserBoardsCommand> checkUserExists(GetUserBoardsCommand command) {
        if (!userFinder.checkUserExists(command.ownerId())) {
            return Either.left(Failure
                    .ofNotFound(validationMessageResolver.getMessage("validation.user.not.found")));
        }
        return Either.right(command);
    }

    private Either<Failure, List<Board>> fetchUserBoards(GetUserBoardsCommand command) {
        return Try.of(() -> {
            List<Board> boards = retrieveBoardsByType(command);
            List<Board> sortedBoards = sortBoardsByUpdatedAt(boards);

            log.info("사용자 보드 목록 조회 완료: ownerId={}, totalCount={}, includeArchived={}",
                    command.ownerId(), sortedBoards.size(), command.includeArchived());

            return sortedBoards;
        })
                .fold(
                        throwable -> handleBoardQueryError(command, throwable),
                        Either::right);
    }

    private List<Board> retrieveBoardsByType(GetUserBoardsCommand command) {
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

        return boards;
    }

    private List<Board> sortBoardsByUpdatedAt(List<Board> boards) {
        return boards.stream()
                .sorted(Comparator.comparing(Board::getUpdatedAt).reversed())
                .toList();
    }

    private Either<Failure, List<Board>> handleBoardQueryError(GetUserBoardsCommand command, Throwable throwable) {
        log.error("사용자 보드 목록 조회 중 예외 발생: ownerId={}, error={}",
                command.ownerId(), throwable.getMessage(), throwable);
        return Either.left(Failure.ofInternalError(
                throwable.getMessage(),
                "BOARD_QUERY_ERROR", null));
    }

    @Override
    public Either<Failure, BoardDetailDto> getBoardDetail(GetBoardDetailCommand command) {
        log.info("보드 상세 조회 시작: boardId={}, userId={}",
                command.boardId().getId(), command.userId().getId());

        return validateBoardDetailInput(command)
                .flatMap(cmd -> fetchBoardDetailData(cmd))
                .flatMap(data -> createBoardDetailDto(data, command));
    }

    private Either<Failure, GetBoardDetailCommand> validateBoardDetailInput(GetBoardDetailCommand command) {
        var validationResult = boardValidator.validateGetDetail(command);
        if (validationResult.isInvalid()) {
            log.warn("보드 상세 조회 검증 실패: boardId={}, userId={}, violations={}",
                    command.boardId().getId(), command.userId().getId(), validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    new ArrayList<>(validationResult.getErrorsAsCollection())));
        }
        return Either.right(command);
    }

    private Either<Failure, BoardDetailData> fetchBoardDetailData(GetBoardDetailCommand command) {
        try {
            Either<Failure, BoardDetailData> dataResult = getBoardDetailPort
                    .getBoardDetail(command.boardId(), command.userId());

            if (dataResult.isLeft()) {
                log.warn("보드 상세 데이터 조회 실패: boardId={}, error={}",
                        command.boardId().getId(), dataResult.getLeft().getMessage());
                return Either.left(dataResult.getLeft());
            }

            return Either.right(dataResult.get());
        } catch (Exception e) {
            log.error("보드 상세 조회 중 예외 발생: boardId={}, error={}",
                    command.boardId().getId(), e.getMessage(), e);

            String message = validationMessageResolver.getMessage("board.detail.get.error", "보드 상세 조회 중 오류가 발생했습니다.");
            return Either.left(Failure.ofInternalServerError(message));
        }
    }

    private Either<Failure, BoardDetailDto> createBoardDetailDto(BoardDetailData data, GetBoardDetailCommand command) {
        try {
            BoardDetailDto boardDetailDto = BoardDetailDto.of(
                    data.board(),
                    data.boardLists(),
                    data.boardMembers(),
                    data.labels(),
                    data.cards().values().stream().flatMap(List::stream).collect(Collectors.toList()),
                    data.cardMembers(),
                    data.users().values().stream().collect(Collectors.toList()));

            log.info("보드 상세 조회 완료: boardId={}", command.boardId().getId());
            return Either.right(boardDetailDto);
        } catch (Exception e) {
            log.error("보드 상세 DTO 생성 중 예외 발생: boardId={}, error={}",
                    command.boardId().getId(), e.getMessage(), e);
            return Either.left(Failure.ofInternalServerError("보드 상세 정보 생성 중 오류가 발생했습니다."));
        }
    }
}
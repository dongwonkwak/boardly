package com.boardly.features.boardlist.application.service;

import com.boardly.features.board.domain.port.BoardRepository;
import com.boardly.features.boardlist.application.query.GetBoardListsQuery;
import com.boardly.features.boardlist.application.usecase.GetBoardListsUseCase;
import com.boardly.features.boardlist.application.validation.BoardListValidator;
import com.boardly.features.boardlist.domain.BoardList;
import com.boardly.features.boardlist.domain.port.BoardListRepository;
import com.boardly.features.boardlist.infrastructure.policy.BoardListCreationPolicy;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.validation.MessageResolver;
import com.boardly.shared.validation.ValidationResult;
import io.vavr.control.Either;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BoardListReadService implements GetBoardListsUseCase {

    private final BoardListValidator boardListValidator;
    private final BoardRepository boardRepository;
    private final BoardListRepository boardListRepository;
    private final BoardListCreationPolicy boardListCreationPolicy;
    private final MessageResolver messageResolver;

    @Override
    public Either<Failure, List<BoardList>> getBoardLists(GetBoardListsQuery command) {
        log.info("BoardListReadService.getBoardLists() called with command: {}", command);

        ValidationResult<GetBoardListsQuery> validationResult = boardListValidator
                .validateGetBoardLists(command);
        if (validationResult.isInvalid()) {
            log.warn("보드 리스트 조회 검증 실패: boardId={}, violations={}",
                    command.boardId(), validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    messageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }

        var boardResult = boardRepository.findById(command.boardId());
        if (boardResult.isEmpty()) {
            log.warn("보드를 찾을 수 없음: boardId={}", command.boardId().getId());
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("validation.board.not.found"),
                    "BOARD_NOT_FOUND",
                    Map.of("boardId", command.boardId().getId())));
        }

        var board = boardResult.get();

        if (!board.getOwnerId().equals(command.userId())) {
            log.warn("보드 리스트 조회 권한 없음: boardId={}, userId={}, boardOwnerId={}",
                    command.boardId().getId(), command.userId().getId(),
                    board.getOwnerId().getId());
            return Either.left(Failure.ofPermissionDenied(
                    messageResolver.getMessage("validation.board.modification.access.denied"),
                    "UNAUTHORIZED_ACCESS",
                    Map.of("boardId", command.boardId().getId(), "userId",
                            command.userId().getId())));
        }

        try {
            List<BoardList> boardLists = boardListRepository
                    .findByBoardIdOrderByPosition(command.boardId());

            var listCountStatus = boardListCreationPolicy.getStatus(command.boardId());
            if (listCountStatus.requiresNotification()) {
                log.warn("보드 리스트 개수 상태: boardId={}, status={}, message={}",
                        command.boardId().getId(), listCountStatus.getDisplayName(),
                        listCountStatus.getMessage());
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

package com.boardly.features.boardlist.application.service;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.activity.domain.ActivityType;
import com.boardly.features.board.application.service.BoardPermissionService;
import com.boardly.features.board.domain.Board;
import com.boardly.features.board.domain.port.BoardRepository;
import com.boardly.features.boardlist.application.command.DeleteBoardListCommand;
import com.boardly.features.boardlist.application.usecase.DeleteBoardListUseCase;
import com.boardly.features.boardlist.application.validation.BoardListValidator;
import com.boardly.features.boardlist.domain.BoardList;
import com.boardly.features.boardlist.domain.port.BoardListRepository;
import com.boardly.features.card.domain.port.CardRepository;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.common.value.BoardId;
import com.boardly.shared.common.value.ListId;
import com.boardly.shared.common.value.UserId;
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
@Transactional
@RequiredArgsConstructor
public class BoardListDeleteService implements DeleteBoardListUseCase {

    private final BoardListValidator boardListValidator;
    private final BoardRepository boardRepository;
    private final BoardListRepository boardListRepository;
    private final CardRepository cardRepository;
    private final BoardPermissionService boardPermissionService;
    private final MessageResolver messageResolver;
    private final ActivityHelper activityHelper;

    @Override
    public Either<Failure, Void> deleteBoardList(DeleteBoardListCommand command) {
        log.info("BoardListDeleteService.deleteBoardList() called with command: {}", command);

        var validationResult = validateCommand(command);
        if (validationResult.isLeft()) {
            return validationResult;
        }

        var listAndBoardResult = findListAndBoard(command);
        if (listAndBoardResult.isLeft()) {
            return Either.left(listAndBoardResult.getLeft());
        }
        var tuple = listAndBoardResult.get();
        var listToDelete = tuple._1;
        var board = tuple._2;

        var permissionResult = checkDeletePermission(listToDelete, command.userId());
        if (permissionResult.isLeft()) {
            return permissionResult;
        }

        var deleteResult = executeListDeletion(command, listToDelete);
        if (deleteResult.isLeft()) {
            return deleteResult;
        }

        logActivity(command, listToDelete, board);

        return Either.right(null);
    }

    private Either<Failure, Void> validateCommand(DeleteBoardListCommand command) {
        ValidationResult<DeleteBoardListCommand> validationResult = boardListValidator.validateDeleteBoardList(command);
        if (validationResult.isInvalid()) {
            log.warn("보드 리스트 삭제 검증 실패: listId={}, violations={}",
                    command.listId(), validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    messageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }
        return Either.right(null);
    }

    private Either<Failure, io.vavr.Tuple2<BoardList, Board>> findListAndBoard(
            DeleteBoardListCommand command) {
        var listResult = boardListRepository.findById(command.listId());
        if (listResult.isEmpty()) {
            log.warn("리스트를 찾을 수 없음: listId={}", command.listId().getId());
            return Either.left(Failure.ofNotFound("LIST_NOT_FOUND"));
        }

        var listToDelete = listResult.get();

        var boardResult = boardRepository.findById(listToDelete.getBoardId());
        if (boardResult.isEmpty()) {
            log.warn("보드를 찾을 수 없음: boardId={}", listToDelete.getBoardId().getId());
            return Either.left(Failure.ofNotFound("BOARD_NOT_FOUND"));
        }

        return Either.right(io.vavr.Tuple.of(listToDelete, boardResult.get()));
    }

    private Either<Failure, Void> checkDeletePermission(BoardList listToDelete, UserId userId) {
        var permissionResult = boardPermissionService.canWriteBoard(listToDelete.getBoardId(), userId);
        if (permissionResult.isLeft()) {
            log.warn("리스트 삭제 권한 확인 실패: listId={}, userId={}, error={}",
                    listToDelete.getListId().getId(), userId.getId(), permissionResult.getLeft().getMessage());
            return Either.left(permissionResult.getLeft());
        }

        if (!permissionResult.get()) {
            log.warn("리스트 삭제 권한 없음: listId={}, userId={}, boardId={}",
                    listToDelete.getListId().getId(), userId.getId(), listToDelete.getBoardId().getId());
            return Either.left(Failure.ofForbidden("validation.boardlist.delete.access.denied"));
        }

        return Either.right(null);
    }

    private Either<Failure, Void> executeListDeletion(DeleteBoardListCommand command, BoardList listToDelete) {
        try {
            var cardDeleteResult = deleteCardsInList(command.listId());
            if (cardDeleteResult.isLeft()) {
                return cardDeleteResult;
            }

            boardListRepository.deleteById(command.listId());
            log.info("리스트 삭제 완료: listId={}, title={}",
                    command.listId().getId(), listToDelete.getTitle());

            reorderRemainingLists(listToDelete.getBoardId(), listToDelete.getPosition());

            return Either.right(null);

        } catch (Exception e) {
            log.error("리스트 삭제 중 예외 발생: listId={}, error={}",
                    command.listId().getId(), e.getMessage(), e);
            return Either.left(Failure.ofInternalServerError(e.getMessage()));
        }
    }

    private Either<Failure, Void> deleteCardsInList(ListId listId) {
        log.debug("리스트의 카드들 삭제 시작: listId={}", listId.getId());

        var cardDeleteResult = cardRepository.deleteByListId(listId);
        if (cardDeleteResult.isLeft()) {
            log.error("리스트의 카드 삭제 실패: listId={}, error={}",
                    listId.getId(), cardDeleteResult.getLeft().getMessage());
            return Either.left(cardDeleteResult.getLeft());
        }

        log.debug("리스트의 카드들 삭제 완료: listId={}", listId.getId());
        return Either.right(null);
    }

    private void logActivity(
            DeleteBoardListCommand command,
            BoardList listToDelete,
            Board board) {
        long cardCount = cardRepository.countByListId(command.listId());
        var payload = Map.<String, Object>of(
                "listName", listToDelete.getTitle(),
                "listId", listToDelete.getListId().getId(),
                "boardName", board.getTitle(),
                "cardCount", cardCount);

        activityHelper.logListActivity(
                ActivityType.LIST_DELETE,
                command.userId(),
                payload,
                board.getTitle(),
                listToDelete.getBoardId(),
                listToDelete.getListId());
    }

    private void reorderRemainingLists(BoardId boardId, int deletedPosition) {
        try {
            java.util.List<BoardList> remainingLists = boardListRepository.findByBoardIdAndPositionGreaterThan(boardId,
                    deletedPosition);

            if (!remainingLists.isEmpty()) {
                log.debug("리스트 position 재정렬 시작: boardId={}, 삭제된 position={}, 재정렬할 리스트 수={}",
                        boardId.getId(), deletedPosition, remainingLists.size());
                remainingLists.forEach(list -> list.updatePosition(list.getPosition() - 1));
                boardListRepository.saveAll(remainingLists);

                log.debug("리스트 position 재정렬 완료: boardId={}, 재정렬된 리스트 수={}",
                        boardId.getId(), remainingLists.size());
            }
        } catch (Exception e) {
            log.error("리스트 position 재정렬 중 오류 발생: boardId={}, deletedPosition={}, error={}",
                    boardId.getId(), deletedPosition, e.getMessage(), e);
        }
    }
}

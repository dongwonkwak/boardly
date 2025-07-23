package com.boardly.features.boardlist.application.service;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.activity.domain.model.ActivityType;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.board.application.service.BoardPermissionService;
import com.boardly.features.boardlist.application.port.input.DeleteBoardListCommand;
import com.boardly.features.boardlist.application.usecase.DeleteBoardListUseCase;
import com.boardly.features.boardlist.application.validation.BoardListValidator;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.user.domain.model.UserId;

import java.util.Map;

import io.vavr.control.Either;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 보드 리스트 삭제 서비스 (Delete)
 * 
 * @since 1.0.0
 */
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
    private final ValidationMessageResolver validationMessageResolver;
    private final ActivityHelper activityHelper;

    @Override
    public Either<Failure, Void> deleteBoardList(DeleteBoardListCommand command) {
        log.info("BoardListDeleteService.deleteBoardList() called with command: {}", command);

        // 1. 입력 데이터 검증
        var validationResult = validateCommand(command);
        if (validationResult.isLeft()) {
            return validationResult;
        }

        // 2. 리스트 및 보드 조회
        var listAndBoardResult = findListAndBoard(command);
        if (listAndBoardResult.isLeft()) {
            return Either.left(listAndBoardResult.getLeft());
        }
        var tuple = listAndBoardResult.get();
        var listToDelete = tuple._1;
        var board = tuple._2;

        // 3. 권한 확인
        var permissionResult = checkDeletePermission(listToDelete, command.userId());
        if (permissionResult.isLeft()) {
            return permissionResult;
        }

        // 4. 리스트 삭제 실행
        var deleteResult = executeListDeletion(command, listToDelete);
        if (deleteResult.isLeft()) {
            return deleteResult;
        }

        // 5. 활동 로그 기록
        logActivity(command, listToDelete, board);

        return Either.right(null);
    }

    /**
     * 입력 명령어 검증
     */
    private Either<Failure, Void> validateCommand(DeleteBoardListCommand command) {
        ValidationResult<DeleteBoardListCommand> validationResult = boardListValidator.validateDeleteBoardList(command);
        if (validationResult.isInvalid()) {
            log.warn("보드 리스트 삭제 검증 실패: listId={}, violations={}",
                    command.listId(), validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }
        return Either.right(null);
    }

    /**
     * 리스트와 보드 조회
     */
    private Either<Failure, io.vavr.Tuple2<BoardList, Board>> findListAndBoard(
            DeleteBoardListCommand command) {
        // 리스트 존재 확인
        var listResult = boardListRepository.findById(command.listId());
        if (listResult.isEmpty()) {
            log.warn("리스트를 찾을 수 없음: listId={}", command.listId().getId());
            return Either.left(Failure.ofNotFound("LIST_NOT_FOUND"));
        }

        var listToDelete = listResult.get();

        // 보드 존재 확인
        var boardResult = boardRepository.findById(listToDelete.getBoardId());
        if (boardResult.isEmpty()) {
            log.warn("보드를 찾을 수 없음: boardId={}", listToDelete.getBoardId().getId());
            return Either.left(Failure.ofNotFound("BOARD_NOT_FOUND"));
        }

        return Either.right(io.vavr.Tuple.of(listToDelete, boardResult.get()));
    }

    /**
     * 삭제 권한 확인
     */
    private Either<Failure, Void> checkDeletePermission(BoardList listToDelete,
            UserId userId) {
        var permissionResult = boardPermissionService.canWriteBoard(listToDelete.getBoardId(), userId);
        if (permissionResult.isLeft()) {
            log.warn("리스트 삭제 권한 확인 실패: listId={}, userId={}, error={}",
                    listToDelete.getListId().getId(), userId.getId(), permissionResult.getLeft().getMessage());
            return Either.left(permissionResult.getLeft());
        }

        if (!permissionResult.get()) {
            log.warn("리스트 삭제 권한 없음: listId={}, userId={}, boardId={}",
                    listToDelete.getListId().getId(), userId.getId(), listToDelete.getBoardId().getId());
            return Either.left(Failure.ofForbidden(
                    validationMessageResolver.getMessage("validation.boardlist.delete.access.denied")));
        }

        return Either.right(null);
    }

    /**
     * 리스트 삭제 실행
     */
    private Either<Failure, Void> executeListDeletion(DeleteBoardListCommand command, BoardList listToDelete) {
        try {
            // 1. 리스트의 모든 카드 삭제
            var cardDeleteResult = deleteCardsInList(command.listId());
            if (cardDeleteResult.isLeft()) {
                return cardDeleteResult;
            }

            // 2. 리스트 삭제
            boardListRepository.deleteById(command.listId());
            log.info("리스트 삭제 완료: listId={}, title={}",
                    command.listId().getId(), listToDelete.getTitle());

            // 3. 이후 리스트들의 position 재정렬
            reorderRemainingLists(listToDelete.getBoardId(), listToDelete.getPosition());

            return Either.right(null);

        } catch (Exception e) {
            log.error("리스트 삭제 중 예외 발생: listId={}, error={}",
                    command.listId().getId(), e.getMessage(), e);
            return Either.left(Failure.ofInternalServerError(e.getMessage()));
        }
    }

    /**
     * 리스트 내 카드들 삭제
     */
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

    /**
     * 활동 로그 기록
     */
    private void logActivity(
            DeleteBoardListCommand command,
            BoardList listToDelete,
            Board board) {
        // 카드 개수 조회
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
                listToDelete.getBoardId(),
                listToDelete.getListId());
    }

    /**
     * 삭제된 리스트 이후의 모든 리스트들의 position을 재정렬합니다.
     * 
     * @param boardId         보드 ID
     * @param deletedPosition 삭제된 리스트의 position
     */
    private void reorderRemainingLists(BoardId boardId, int deletedPosition) {
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
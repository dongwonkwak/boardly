package com.boardly.features.boardlist.application.service;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.activity.domain.ActivityType;
import com.boardly.features.board.domain.port.BoardRepository;
import com.boardly.features.boardlist.application.command.UpdateBoardListCommand;
import com.boardly.features.boardlist.application.command.UpdateBoardListPositionCommand;
import com.boardly.features.boardlist.application.usecase.UpdateBoardListPositionUseCase;
import com.boardly.features.boardlist.application.usecase.UpdateBoardListUseCase;
import com.boardly.features.boardlist.application.validation.BoardListValidator;
import com.boardly.features.boardlist.domain.BoardList;
import com.boardly.features.boardlist.domain.config.BoardListPolicyConfig;
import com.boardly.features.boardlist.domain.port.BoardListRepository;
import com.boardly.features.boardlist.application.policy.BoardListMovePolicy;
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
@Transactional
@RequiredArgsConstructor
public class BoardListUpdateService implements UpdateBoardListUseCase, UpdateBoardListPositionUseCase {

    private final BoardListValidator boardListValidator;
    private final BoardRepository boardRepository;
    private final BoardListRepository boardListRepository;
    private final BoardListPolicyConfig boardListPolicyConfig;
    private final BoardListMovePolicy boardListMovePolicy;
    private final MessageResolver messageResolver;
    private final ActivityHelper activityHelper;

    @Override
    public Either<Failure, BoardList> updateBoardList(UpdateBoardListCommand command) {
        log.info("BoardListUpdateService.updateBoardList() called with command: {}", command);

        ValidationResult<UpdateBoardListCommand> validationResult = boardListValidator.validateUpdateBoardList(command);
        if (validationResult.isInvalid()) {
            log.warn("보드 리스트 수정 검증 실패: listId={}, violations={}",
                    command.listId(), validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    messageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }

        var listResult = boardListRepository.findById(command.listId());
        if (listResult.isEmpty()) {
            log.warn("리스트를 찾을 수 없음: listId={}", command.listId().getId());
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("validation.boardlist.not.found"),
                    "LIST_NOT_FOUND",
                    Map.of("listId", command.listId().getId())));
        }

        var currentList = listResult.get();

        var boardResult = boardRepository.findById(currentList.getBoardId());
        if (boardResult.isEmpty()) {
            log.warn("보드를 찾을 수 없음: boardId={}", currentList.getBoardId().getId());
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("validation.board.not.found"),
                    "BOARD_NOT_FOUND",
                    Map.of("boardId", currentList.getBoardId().getId())));
        }

        var board = boardResult.get();

        if (!board.getOwnerId().equals(command.userId())) {
            log.warn("리스트 수정 권한 없음: listId={}, userId={}, boardOwnerId={}",
                    command.listId().getId(), command.userId().getId(), board.getOwnerId().getId());
            return Either.left(Failure.ofPermissionDenied(
                    messageResolver.getMessage("validation.boardlist.update.access.denied"),
                    "UNAUTHORIZED_ACCESS",
                    Map.of("listId", command.listId().getId(), "userId", command.userId().getId())));
        }

        try {
            if (command.title() != null) {
                if (command.title().length() > boardListPolicyConfig.getMaxTitleLength()) {
                    log.warn("리스트 제목 길이 제한 초과: listId={}, titleLength={}, maxLength={}",
                            command.listId().getId(), command.title().length(),
                            boardListPolicyConfig.getMaxTitleLength());
                    return Either.left(Failure.ofBusinessRuleViolation(
                            messageResolver.getMessage("validation.boardlist.title.length.exceeded",
                                    boardListPolicyConfig.getMaxTitleLength()),
                            "TITLE_LENGTH_EXCEEDED",
                            Map.of("listId", command.listId().getId(), "titleLength", command.title().length())));
                }
                currentList.updateTitle(command.title());
            }

            if (command.description() != null) {
                currentList.updateDescription(command.description());
            }

            if (command.color() != null) {
                currentList.updateColor(command.color());
            }

            var savedList = boardListRepository.save(currentList);

            if (command.title() != null) {
                var payload = Map.<String, Object>of(
                        "oldName", currentList.getTitle(),
                        "newName", command.title(),
                        "listId", savedList.getListId().getId(),
                        "boardName", board.getTitle());
                activityHelper.logListActivity(
                        ActivityType.LIST_RENAME,
                        command.userId(),
                        payload,
                        board.getTitle(),
                        savedList.getBoardId(),
                        savedList.getListId());
            }

            if (command.color() != null) {
                var payload = Map.<String, Object>of(
                        "listName", savedList.getTitle(),
                        "listId", savedList.getListId().getId(),
                        "oldColor", currentList.getColor(),
                        "newColor", command.color());
                activityHelper.logListActivity(
                        ActivityType.LIST_CHANGE_COLOR,
                        command.userId(),
                        payload,
                        board.getTitle(),
                        savedList.getBoardId(),
                        savedList.getListId());
            }

            log.info("리스트 수정 완료: listId={}, title={}",
                    savedList.getListId().getId(), savedList.getTitle());
            return Either.right(savedList);

        } catch (Exception e) {
            log.error("리스트 수정 중 예외 발생: listId={}, error={}",
                    command.listId().getId(), e.getMessage(), e);
            return Either.left(Failure.ofInternalError(
                    messageResolver.getMessage("validation.boardlist.update.error"),
                    "BOARD_LIST_UPDATE_ERROR",
                    Map.of("listId", command.listId().getId(), "error", e.getMessage())));
        }
    }

    @Override
    public Either<Failure, List<BoardList>> updateBoardListPosition(UpdateBoardListPositionCommand command) {
        log.info("BoardListUpdateService.updateBoardListPosition() called with command: {}", command);

        ValidationResult<UpdateBoardListPositionCommand> validationResult = boardListValidator
                .validateUpdateBoardListPosition(command);
        if (validationResult.isInvalid()) {
            log.warn("보드 리스트 위치 변경 검증 실패: listId={}, newPosition={}, violations={}",
                    command.listId(), command.newPosition(), validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    messageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }

        var listResult = boardListRepository.findById(command.listId());
        if (listResult.isEmpty()) {
            log.warn("리스트를 찾을 수 없음: listId={}", command.listId().getId());
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("validation.boardlist.not.found"),
                    "LIST_NOT_FOUND",
                    Map.of("listId", command.listId().getId())));
        }

        var targetList = listResult.get();
        int currentPosition = targetList.getPosition();

        var boardResult = boardRepository.findById(targetList.getBoardId());
        if (boardResult.isEmpty()) {
            log.warn("보드를 찾을 수 없음: boardId={}", targetList.getBoardId().getId());
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("validation.board.not.found"),
                    "BOARD_NOT_FOUND",
                    Map.of("boardId", targetList.getBoardId().getId())));
        }

        var board = boardResult.get();

        if (!board.getOwnerId().equals(command.userId())) {
            log.warn("리스트 위치 변경 권한 없음: listId={}, userId={}, boardOwnerId={}",
                    command.listId().getId(), command.userId().getId(), board.getOwnerId().getId());
            return Either.left(Failure.ofPermissionDenied(
                    messageResolver.getMessage("validation.boardlist.update.access.denied"),
                    "UNAUTHORIZED_ACCESS",
                    Map.of("listId", command.listId().getId(), "userId", command.userId().getId())));
        }

        List<BoardList> allLists = boardListRepository.findByBoardIdOrderByPosition(targetList.getBoardId());

        var movePolicyResult = boardListMovePolicy.canMoveWithinSameBoard(targetList, command.newPosition());
        if (movePolicyResult.isLeft()) {
            log.warn("리스트 이동 정책 위반: listId={}, newPosition={}, error={}",
                    command.listId().getId(), command.newPosition(), movePolicyResult.getLeft().getMessage());
            return Either.left(Failure.ofBusinessRuleViolation(
                    messageResolver.getMessage("validation.boardlist.move.policy.violation"),
                    "LIST_MOVE_POLICY_VIOLATION",
                    Map.of("listId", command.listId().getId(), "newPosition", command.newPosition())));
        }

        if (!boardListMovePolicy.hasPositionChanged(targetList, command.newPosition())) {
            log.info("위치가 변경되지 않음: listId={}, currentPosition={}, newPosition={}",
                    command.listId().getId(), currentPosition, command.newPosition());
            return Either.right(allLists);
        }

        try {
            List<BoardList> updatedLists = reorderLists(allLists, currentPosition, command.newPosition());
            List<BoardList> savedLists = boardListRepository.saveAll(updatedLists);

            var payload = Map.<String, Object>of(
                    "listName", targetList.getTitle(),
                    "listId", targetList.getListId().getId(),
                    "boardName", board.getTitle(),
                    "oldPosition", currentPosition,
                    "newPosition", command.newPosition());
            activityHelper.logListActivity(
                    ActivityType.LIST_MOVE,
                    command.userId(),
                    payload,
                    board.getTitle(),
                    targetList.getBoardId(),
                    targetList.getListId());

            log.info("보드 리스트 위치 변경 완료: listId={}, oldPosition={}, newPosition={}, updatedLists={}",
                    command.listId().getId(), currentPosition, command.newPosition(), savedLists.size());
            return Either.right(savedLists);

        } catch (Exception e) {
            log.error("보드 리스트 위치 변경 중 예외 발생: listId={}, error={}",
                    command.listId().getId(), e.getMessage(), e);
            return Either.left(Failure.ofInternalError(
                    messageResolver.getMessage("validation.boardlist.position.update.error"),
                    "BOARD_LIST_POSITION_UPDATE_ERROR",
                    Map.of("listId", command.listId().getId(), "error", e.getMessage())));
        }
    }

    private List<BoardList> reorderLists(List<BoardList> lists, int fromPosition, int toPosition) {
        List<BoardList> mutableLists = new java.util.ArrayList<>(lists);
        BoardList movedList = mutableLists.remove(fromPosition);
        mutableLists.add(toPosition, movedList);
        for (int i = 0; i < mutableLists.size(); i++) {
            mutableLists.get(i).updatePosition(i);
        }
        return mutableLists;
    }
}

package com.boardly.features.boardlist.application.service;

import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.application.port.input.UpdateBoardListCommand;
import com.boardly.features.boardlist.application.port.input.UpdateBoardListPositionCommand;
import com.boardly.features.boardlist.application.usecase.UpdateBoardListUseCase;
import com.boardly.features.boardlist.application.usecase.UpdateBoardListPositionUseCase;
import com.boardly.features.boardlist.application.validation.BoardListValidator;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.policy.BoardListMovePolicy;
import com.boardly.features.boardlist.domain.policy.BoardListPolicyConfig;
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
 * 보드 리스트 수정 서비스 (Update)
 * 
 * @since 1.0.0
 */
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
    private final ValidationMessageResolver validationMessageResolver;

    @Override
    public Either<Failure, BoardList> updateBoardList(UpdateBoardListCommand command) {
        log.info("BoardListUpdateService.updateBoardList() called with command: {}", command);

        // 1. 입력 데이터 검증
        ValidationResult<UpdateBoardListCommand> validationResult = boardListValidator.validateUpdateBoardList(command);
        if (validationResult.isInvalid()) {
            log.warn("보드 리스트 수정 검증 실패: listId={}, violations={}",
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
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("error.service.card.move.list_not_found"),
                    "LIST_NOT_FOUND",
                    Map.of("listId", command.listId().getId())));
        }

        var currentList = listResult.get();

        // 3. 보드 존재 확인
        var boardResult = boardRepository.findById(currentList.getBoardId());
        if (boardResult.isEmpty()) {
            log.warn("보드를 찾을 수 없음: boardId={}", currentList.getBoardId().getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("validation.board.not.found"),
                    "BOARD_NOT_FOUND",
                    Map.of("boardId", currentList.getBoardId().getId())));
        }

        var board = boardResult.get();

        // 4. 권한 확인 (보드 소유자만 수정 가능)
        if (!board.getOwnerId().equals(command.userId())) {
            log.warn("리스트 수정 권한 없음: listId={}, userId={}, boardOwnerId={}",
                    command.listId().getId(), command.userId().getId(), board.getOwnerId().getId());
            return Either.left(Failure.ofPermissionDenied(
                    validationMessageResolver.getMessage("validation.board.modification.access.denied"),
                    "UNAUTHORIZED_ACCESS",
                    Map.of("listId", command.listId().getId(), "userId", command.userId().getId())));
        }

        // 5. 리스트 정보 업데이트
        try {
            // 제목 업데이트 (길이 제한 확인)
            if (command.title() != null) {
                if (command.title().length() > boardListPolicyConfig.getMaxTitleLength()) {
                    log.warn("리스트 제목 길이 제한 초과: listId={}, titleLength={}, maxLength={}",
                            command.listId().getId(), command.title().length(),
                            boardListPolicyConfig.getMaxTitleLength());
                    return Either.left(Failure.ofBusinessRuleViolation(
                            String.format("리스트 제목은 최대 %d자까지 입력할 수 있습니다.", boardListPolicyConfig.getMaxTitleLength()),
                            "TITLE_LENGTH_EXCEEDED",
                            Map.of("listId", command.listId().getId(), "titleLength", command.title().length())));
                }
                currentList.updateTitle(command.title());
            }

            // 설명 업데이트 (null이 아닌 경우에만)
            if (command.description() != null) {
                currentList.updateDescription(command.description());
            }

            // 색상 업데이트 (null이 아닌 경우에만)
            if (command.color() != null) {
                currentList.updateColor(command.color());
            }

            var savedList = boardListRepository.save(currentList);

            log.info("리스트 수정 완료: listId={}, title={}",
                    savedList.getListId().getId(), savedList.getTitle());
            return Either.right(savedList);

        } catch (Exception e) {
            log.error("리스트 수정 중 예외 발생: listId={}, error={}",
                    command.listId().getId(), e.getMessage(), e);
            return Either.left(Failure.ofInternalError(
                    e.getMessage(),
                    "BOARD_LIST_UPDATE_ERROR",
                    null));
        }
    }

    // ==================== Position Update Methods ====================

    @Override
    public Either<Failure, List<BoardList>> updateBoardListPosition(UpdateBoardListPositionCommand command) {
        log.info("BoardListUpdateService.updateBoardListPosition() called with command: {}", command);

        // 1. 입력 데이터 검증
        ValidationResult<UpdateBoardListPositionCommand> validationResult = boardListValidator
                .validateUpdateBoardListPosition(command);
        if (validationResult.isInvalid()) {
            log.warn("보드 리스트 위치 변경 검증 실패: listId={}, newPosition={}, violations={}",
                    command.listId(), command.newPosition(), validationResult.getErrorsAsCollection());
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

        var targetList = listResult.get();
        int currentPosition = targetList.getPosition();

        // 3. 보드 존재 확인
        var boardResult = boardRepository.findById(targetList.getBoardId());
        if (boardResult.isEmpty()) {
            log.warn("보드를 찾을 수 없음: boardId={}", targetList.getBoardId().getId());
            return Either.left(Failure.ofNotFound("BOARD_NOT_FOUND"));
        }

        var board = boardResult.get();

        // 4. 권한 확인 (보드 소유자만 수정 가능)
        if (!board.getOwnerId().equals(command.userId())) {
            log.warn("리스트 위치 변경 권한 없음: listId={}, userId={}, boardOwnerId={}",
                    command.listId().getId(), command.userId().getId(), board.getOwnerId().getId());
            return Either.left(Failure.ofForbidden("UNAUTHORIZED_ACCESS"));
        }

        // 5. 보드의 모든 리스트 조회
        List<BoardList> allLists = boardListRepository.findByBoardIdOrderByPosition(targetList.getBoardId());

        // 6. 리스트 이동 정책 확인
        var movePolicyResult = boardListMovePolicy.canMoveWithinSameBoard(targetList, command.newPosition());
        if (movePolicyResult.isLeft()) {
            log.warn("리스트 이동 정책 위반: listId={}, newPosition={}, error={}",
                    command.listId().getId(), command.newPosition(), movePolicyResult.getLeft().getMessage());
            return Either.left(Failure.ofBusinessRuleViolation(
                    movePolicyResult.getLeft().getMessage(),
                    "LIST_MOVE_POLICY_VIOLATION",
                    null));
        }

        // 7. 위치가 실제로 변경되는지 확인
        if (!boardListMovePolicy.hasPositionChanged(targetList, command.newPosition())) {
            log.info("위치가 변경되지 않음: listId={}, currentPosition={}, newPosition={}",
                    command.listId().getId(), currentPosition, command.newPosition());
            return Either.right(allLists);
        }

        // 8. 위치 변경 및 다른 리스트들의 position 조정
        try {
            List<BoardList> updatedLists = reorderLists(allLists, currentPosition, command.newPosition());

            // 9. 변경된 리스트들을 배치로 저장
            List<BoardList> savedLists = boardListRepository.saveAll(updatedLists);

            log.info("보드 리스트 위치 변경 완료: listId={}, oldPosition={}, newPosition={}, updatedLists={}",
                    command.listId().getId(), currentPosition, command.newPosition(), savedLists.size());
            return Either.right(savedLists);

        } catch (Exception e) {
            log.error("보드 리스트 위치 변경 중 예외 발생: listId={}, error={}",
                    command.listId().getId(), e.getMessage(), e);
            return Either.left(Failure.ofInternalServerError(e.getMessage()));
        }
    }

    /**
     * 리스트들의 위치를 재정렬합니다.
     * 
     * @param lists        현재 보드의 모든 리스트 (position 순서대로 정렬됨)
     * @param fromPosition 이동할 리스트의 현재 위치
     * @param toPosition   이동할 리스트의 새로운 위치
     * @return 위치가 조정된 리스트들의 목록
     */
    private List<BoardList> reorderLists(List<BoardList> lists, int fromPosition, int toPosition) {
        // 새로운 ArrayList로 복사하여 수정 가능하게 만듦
        List<BoardList> mutableLists = new java.util.ArrayList<>(lists);

        // 이동할 리스트를 제거
        BoardList movedList = mutableLists.remove(fromPosition);

        // 새로운 위치에 삽입
        mutableLists.add(toPosition, movedList);

        // 모든 리스트의 position을 0부터 순차적으로 재설정
        for (int i = 0; i < mutableLists.size(); i++) {
            mutableLists.get(i).updatePosition(i);
        }

        return mutableLists;
    }
}
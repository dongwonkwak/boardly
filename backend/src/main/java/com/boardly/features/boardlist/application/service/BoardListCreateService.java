package com.boardly.features.boardlist.application.service;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.application.port.input.CreateBoardListCommand;
import com.boardly.features.boardlist.application.usecase.CreateBoardListUseCase;
import com.boardly.features.boardlist.application.validation.BoardListValidator;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.policy.BoardListCreationPolicy;
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
 * 보드 리스트 생성 서비스 (Create)
 * 
 * @since 1.0.0
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BoardListCreateService implements CreateBoardListUseCase {

    private final BoardListValidator boardListValidator;
    private final BoardRepository boardRepository;
    private final BoardListRepository boardListRepository;
    private final BoardListCreationPolicy boardListCreationPolicy;
    private final BoardListPolicyConfig boardListPolicyConfig;
    private final ValidationMessageResolver validationMessageResolver;
    private final ActivityHelper activityHelper;

    @Override
    public Either<Failure, BoardList> createBoardList(CreateBoardListCommand command) {
        log.info("BoardListCreateService.createBoardList() called with command: {}", command);

        // 1. 입력 데이터 검증
        var validationResult = validateCommand(command);
        if (validationResult.isLeft()) {
            return Either.left(validationResult.getLeft());
        }

        // 2. 보드 존재 및 권한 확인
        var boardResult = validateBoardAccess(command);
        if (boardResult.isLeft()) {
            return Either.left(boardResult.getLeft());
        }
        var currentBoard = boardResult.get();

        // 3. 비즈니스 규칙 검증
        var businessRuleResult = validateBusinessRules(command);
        if (businessRuleResult.isLeft()) {
            return Either.left(businessRuleResult.getLeft());
        }

        // 4. 리스트 생성 및 저장
        return createAndSaveBoardList(command, currentBoard);
    }

    /**
     * 입력 데이터 검증
     */
    private Either<Failure, Void> validateCommand(CreateBoardListCommand command) {
        ValidationResult<CreateBoardListCommand> validationResult = boardListValidator
                .validateCreateBoardList(command);
        if (validationResult.isInvalid()) {
            log.warn("보드 리스트 생성 검증 실패: boardId={}, violations={}",
                    command.boardId(), validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }
        return Either.right(null);
    }

    /**
     * 보드 존재 및 권한 확인
     */
    private Either<Failure, Board> validateBoardAccess(CreateBoardListCommand command) {
        var boardResult = boardRepository.findById(command.boardId());
        if (boardResult.isEmpty()) {
            log.warn("보드를 찾을 수 없음: boardId={}", command.boardId().getId());
            Map<String, Object> context = Map.of("boardId", command.boardId().getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("validation.board.not.found"),
                    "BOARD_NOT_FOUND",
                    context));
        }

        var currentBoard = boardResult.get();
        if (!currentBoard.getOwnerId().equals(command.userId())) {
            log.warn("보드 접근 권한 없음: boardId={}, userId={}", command.boardId().getId(),
                    command.userId().getId());
            Map<String, Object> context = Map.of(
                    "boardId", command.boardId().getId(),
                    "userId", command.userId().getId());
            return Either.left(Failure.ofPermissionDenied(
                    validationMessageResolver
                            .getMessage("validation.board.modification.access.denied"),
                    "UNAUTHORIZED_ACCESS",
                    context));
        }

        return Either.right(currentBoard);
    }

    /**
     * 비즈니스 규칙 검증
     */
    private Either<Failure, Void> validateBusinessRules(CreateBoardListCommand command) {
        // 리스트 생성 정책 확인
        var creationPolicyResult = boardListCreationPolicy.canCreateBoardList(command.boardId());
        if (creationPolicyResult.isLeft()) {
            log.warn("리스트 생성 정책 위반: boardId={}, error={}", command.boardId().getId(),
                    creationPolicyResult.getLeft().getMessage());
            Map<String, Object> context = Map.of("boardId", command.boardId().getId());
            return Either.left(Failure.ofBusinessRuleViolation(
                    creationPolicyResult.getLeft().getMessage(),
                    "LIST_CREATION_POLICY_VIOLATION",
                    context));
        }

        // 제목 길이 제한 확인
        if (command.title().length() > boardListPolicyConfig.getMaxTitleLength()) {
            log.warn("리스트 제목 길이 제한 초과: boardId={}, titleLength={}, maxLength={}",
                    command.boardId().getId(), command.title().length(),
                    boardListPolicyConfig.getMaxTitleLength());
            return Either.left(Failure.ofBusinessRuleViolation(
                    String.format("리스트 제목은 최대 %d자까지 입력할 수 있습니다.",
                            boardListPolicyConfig.getMaxTitleLength()),
                    "TITLE_LENGTH_EXCEEDED",
                    Map.of("boardId", command.boardId().getId(), "titleLength",
                            command.title().length())));
        }

        return Either.right(null);
    }

    /**
     * 리스트 생성 및 저장
     */
    private Either<Failure, BoardList> createAndSaveBoardList(CreateBoardListCommand command, Board currentBoard) {
        try {
            // 다음 위치 계산
            var maxPositionResult = boardListRepository.findMaxPositionByBoardId(command.boardId());
            int nextPosition = maxPositionResult.map(pos -> pos + 1).orElse(0);

            var newList = BoardList.create(command.title(), command.description(), nextPosition,
                    command.color(),
                    command.boardId());
            var savedList = boardListRepository.save(newList);

            // 활동 로그 기록
            activityHelper.logListCreate(
                    command.userId(),
                    savedList.getTitle(),
                    currentBoard.getTitle(),
                    command.boardId(),
                    savedList.getListId());

            log.info("리스트 생성 완료: boardId={}, listId={}, title={}",
                    command.boardId().getId(), savedList.getListId().getId(), savedList.getTitle());
            return Either.right(savedList);

        } catch (Exception e) {
            log.error("리스트 생성 중 예외 발생: boardId={}, error={}", command.boardId().getId(), e.getMessage(), e);
            return Either.left(Failure.ofInternalError(
                    e.getMessage(),
                    "LIST_CREATION_ERROR",
                    null));
        }
    }
}
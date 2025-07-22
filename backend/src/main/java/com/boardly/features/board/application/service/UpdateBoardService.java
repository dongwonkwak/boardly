package com.boardly.features.board.application.service;

import com.boardly.features.board.application.port.input.UpdateBoardCommand;
import com.boardly.features.board.application.usecase.UpdateBoardUseCase;
import com.boardly.features.board.application.validation.UpdateBoardValidator;
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
 * 보드 업데이트 서비스
 * 
 * <p>
 * 보드의 제목과 설명 수정 관련 비즈니스 로직을 처리하는 애플리케이션 서비스입니다.
 * 입력 검증, 권한 확인, 도메인 로직 실행, 저장 등의 전체 보드 업데이트 프로세스를 관리합니다.
 * 
 *
 * @since 1.0.0
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UpdateBoardService implements UpdateBoardUseCase {

    private final UpdateBoardValidator boardValidator;
    private final BoardRepository boardRepository;
    private final ValidationMessageResolver messageResolver;
    private final BoardPermissionService boardPermissionService;

    /**
     * 보드를 업데이트합니다.
     * 
     * @param command 보드 업데이트 명령
     * @return 업데이트 결과 (성공 시 업데이트된 보드, 실패 시 실패 정보)
     */
    @Override
    public Either<Failure, Board> updateBoard(UpdateBoardCommand command) {
        log.info("보드 업데이트 시작: boardId={}, title={}, description={}, requestedBy={}",
                command.boardId().getId(), command.title(), command.description(), command.requestedBy().getId());

        return validateInput(command)
                .flatMap(this::findExistingBoard)
                .flatMap(this::verifyOwnership)
                .flatMap(this::checkArchiveStatus)
                .flatMap(this::checkForChanges)
                .flatMap(this::applyChangesToBoard)
                .flatMap(this::saveUpdatedBoard);
    }

    /**
     * 1단계: 입력 검증
     */
    private Either<Failure, UpdateBoardCommand> validateInput(UpdateBoardCommand command) {
        ValidationResult<UpdateBoardCommand> validationResult = boardValidator.validate(command);
        if (validationResult.isInvalid()) {
            log.warn("보드 업데이트 검증 실패: boardId={}, violations={}",
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
    private Either<Failure, BoardUpdateContext> findExistingBoard(UpdateBoardCommand command) {
        Optional<Board> existingBoardOpt = boardRepository.findById(command.boardId());
        if (existingBoardOpt.isEmpty()) {
            log.warn("보드를 찾을 수 없음: boardId={}", command.boardId().getId());
            return Either.left(Failure.ofNotFound(messageResolver.getMessage("validation.board.not.found")));
        }

        Board existingBoard = existingBoardOpt.get();
        return Either.right(new BoardUpdateContext(command, existingBoard));
    }

    /**
     * 3단계: 권한 확인 (멤버 권한 시스템 사용)
     */
    private Either<Failure, BoardUpdateContext> verifyOwnership(BoardUpdateContext context) {
        return boardPermissionService.canWriteBoard(context.command().boardId(), context.command().requestedBy())
                .flatMap(canWrite -> {
                    if (!canWrite) {
                        log.warn("보드 수정 권한 없음: boardId={}, requestedBy={}",
                                context.command().boardId().getId(),
                                context.command().requestedBy().getId());
                        return Either.left(
                                Failure.ofPermissionDenied(
                                        messageResolver.getMessage("validation.board.modification.access.denied")));
                    }
                    return Either.right(context);
                });
    }

    /**
     * 4단계: 아카이브된 보드 수정 제한 확인
     */
    private Either<Failure, BoardUpdateContext> checkArchiveStatus(BoardUpdateContext context) {
        if (context.board().isArchived()) {
            log.warn("아카이브된 보드의 수정 시도: boardId={}, requestedBy={}",
                    context.command().boardId().getId(), context.command().requestedBy().getId());
            return Either.left(
                    Failure.ofConflict(messageResolver.getMessage("validation.board.archived.modification.denied")));
        }
        return Either.right(context);
    }

    /**
     * 5단계: 변경 사항이 있는지 확인
     */
    private Either<Failure, BoardUpdateContext> checkForChanges(BoardUpdateContext context) {
        if (isNoChangesApplied(context.board(), context.command())) {
            log.info("보드 변경 사항 없음: boardId={}", context.command().boardId().getId());
            return Either.right(new BoardUpdateContext(context.command(), context.board(), true));
        }
        return Either.right(context);
    }

    /**
     * 6단계: 변경 사항 적용
     */
    private Either<Failure, BoardUpdateContext> applyChangesToBoard(BoardUpdateContext context) {
        // 변경 사항이 없으면 바로 반환
        if (context.hasNoChanges()) {
            return Either.right(context);
        }

        try {
            applyChanges(context.board(), context.command());
            log.debug("보드 변경 사항 적용 완료: boardId={}", context.command().boardId().getId());
            return Either.right(context);
        } catch (Exception e) {
            log.error("보드 변경 중 오류 발생: boardId={}, error={}",
                    context.command().boardId().getId(), e.getMessage(), e);
            return Either.left(
                    Failure.ofInternalServerError(messageResolver.getMessage("validation.board.modification.error")));
        }
    }

    /**
     * 7단계: 업데이트된 보드 저장
     */
    private Either<Failure, Board> saveUpdatedBoard(BoardUpdateContext context) {
        // 변경 사항이 없으면 기존 보드 반환
        if (context.hasNoChanges()) {
            return Either.right(context.board());
        }

        return Try.of(() -> boardRepository.save(context.board()))
                .fold(
                        throwable -> {
                            log.error("보드 업데이트 중 예외 발생: boardId={}, error={}",
                                    context.command().boardId().getId(), throwable.getMessage(), throwable);
                            return Either.left(Failure.ofInternalServerError(
                                    messageResolver.getMessage("validation.board.update.error")));
                        },
                        saveResult -> {
                            if (saveResult.isRight()) {
                                log.info("보드 업데이트 완료: boardId={}, title={}",
                                        saveResult.get().getBoardId().getId(), saveResult.get().getTitle());
                            } else {
                                log.error("보드 저장 실패: boardId={}, error={}",
                                        context.command().boardId().getId(), saveResult.getLeft().getMessage());
                            }
                            return saveResult;
                        });
    }

    /**
     * 보드에 변경 사항을 적용합니다.
     * 
     * @param board   기존 보드 객체
     * @param command 업데이트 명령
     */
    private void applyChanges(Board board, UpdateBoardCommand command) {
        // 제목 업데이트 (null이 아닌 경우에만)
        if (command.title() != null) {
            board.updateTitle(command.title());
            log.debug("보드 제목 업데이트: boardId={}, newTitle={}",
                    board.getBoardId().getId(), command.title());
        }

        // 설명 업데이트 (null이 아닌 경우에만)
        if (command.description() != null) {
            board.updateDescription(command.description());
            log.debug("보드 설명 업데이트: boardId={}, descriptionLength={}",
                    board.getBoardId().getId(), command.description().length());
        }
    }

    /**
     * 변경 사항이 실제로 적용되었는지 확인합니다.
     * 
     * @param board   기존 보드 객체
     * @param command 업데이트 명령
     * @return 변경 사항이 없으면 true, 있으면 false
     */
    private boolean isNoChangesApplied(Board board, UpdateBoardCommand command) {
        // 제목 변경 확인
        if (command.title() != null && !command.title().equals(board.getTitle())) {
            return false;
        }

        // 설명 변경 확인
        if (command.description() != null && !command.description().equals(board.getDescription())) {
            return false;
        }
        return true;
    }

    /**
     * 보드 업데이트 과정에서 사용되는 컨텍스트 객체
     */
    private record BoardUpdateContext(
            UpdateBoardCommand command,
            Board board,
            boolean hasNoChanges) {
        BoardUpdateContext(UpdateBoardCommand command, Board board) {
            this(command, board, false);
        }
    }
}
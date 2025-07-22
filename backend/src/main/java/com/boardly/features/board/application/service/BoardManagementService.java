package com.boardly.features.board.application.service;

import com.boardly.features.board.application.port.input.*;
import com.boardly.features.board.application.usecase.*;
import com.boardly.features.board.application.validation.*;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.board.domain.repository.BoardMemberRepository;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.user.application.service.UserFinder;
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
 * 보드 관리 서비스
 * 
 * <p>
 * 보드의 생성, 수정, 삭제, 아카이브 등 CRUD 작업을 담당하는 통합 서비스입니다.
 * </p>
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BoardManagementService implements
        CreateBoardUseCase,
        UpdateBoardUseCase,
        DeleteBoardUseCase,
        ArchiveBoardUseCase {

    // Validator
    private final BoardValidator boardValidator;

    // Repositories
    private final BoardRepository boardRepository;
    private final BoardListRepository boardListRepository;
    private final CardRepository cardRepository;
    private final BoardMemberRepository boardMemberRepository;

    // Services
    private final BoardPermissionService boardPermissionService;
    private final UserFinder userFinder;
    private final ValidationMessageResolver validationMessageResolver;

    // ==================== CREATE BOARD ====================

    @Override
    public Either<Failure, Board> createBoard(CreateBoardCommand command) {
        log.info("보드 생성 시작: title={}, description={}, ownerId={}",
                command.title(), command.description(), command.ownerId());

        // 1. 입력 검증
        ValidationResult<CreateBoardCommand> validationResult = boardValidator.validateCreate(command);
        if (validationResult.isInvalid()) {
            log.warn("보드 생성 검증 실패: title={}, violations={}",
                    command.title(), validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }

        // 2. 사용자 존재 확인
        if (!userFinder.checkUserExists(command.ownerId())) {
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("validation.user.not.found")));
        }

        // 3. 보드 생성
        Board board = Board.create(command.title(), command.description(), command.ownerId());

        // 4. 보드 저장
        return Try.of(() -> boardRepository.save(board))
                .fold(
                        throwable -> {
                            log.error("보드 생성 중 예외 발생: title={}, error={}",
                                    command.title(), throwable.getMessage(), throwable);
                            return Either.left(Failure.ofInternalError(
                                    throwable.getMessage(), "BOARD_CREATION_ERROR", null));
                        },
                        saveResult -> {
                            if (saveResult.isRight()) {
                                log.info("보드 생성 완료: boardId={}, title={}",
                                        saveResult.get().getBoardId(), command.title());
                            } else {
                                log.error("보드 저장 실패: title={}, error={}",
                                        command.title(), saveResult.getLeft().getMessage());
                            }
                            return saveResult;
                        });
    }

    // ==================== UPDATE BOARD ====================

    @Override
    public Either<Failure, Board> updateBoard(UpdateBoardCommand command) {
        log.info("보드 업데이트 시작: boardId={}, title={}, description={}, requestedBy={}",
                command.boardId().getId(), command.title(), command.description(),
                command.requestedBy().getId());

        // 1. 사용자 존재 확인
        if (!userFinder.checkUserExists(command.requestedBy())) {
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("validation.user.not.found")));
        }

        return validateUpdateInput(command)
                .flatMap(this::findExistingBoard)
                .flatMap(this::verifyUpdatePermission)
                .flatMap(this::checkArchiveStatus)
                .flatMap(this::checkForChanges)
                .flatMap(this::applyChangesToBoard)
                .flatMap(this::saveUpdatedBoard);
    }

    private Either<Failure, UpdateBoardCommand> validateUpdateInput(UpdateBoardCommand command) {
        ValidationResult<UpdateBoardCommand> validationResult = boardValidator.validateUpdate(command);
        if (validationResult.isInvalid()) {
            log.warn("보드 업데이트 검증 실패: boardId={}, violations={}",
                    command.boardId().getId(), validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }
        return Either.right(command);
    }

    private Either<Failure, BoardUpdateContext> findExistingBoard(UpdateBoardCommand command) {
        Optional<Board> existingBoardOpt = boardRepository.findById(command.boardId());
        if (existingBoardOpt.isEmpty()) {
            log.warn("보드를 찾을 수 없음: boardId={}", command.boardId().getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("validation.board.not.found")));
        }

        Board existingBoard = existingBoardOpt.get();
        return Either.right(new BoardUpdateContext(command, existingBoard));
    }

    private Either<Failure, BoardUpdateContext> verifyUpdatePermission(BoardUpdateContext context) {
        return boardPermissionService.canWriteBoard(context.command().boardId(), context.command().requestedBy())
                .flatMap(canWrite -> {
                    if (!canWrite) {
                        log.warn("보드 수정 권한 없음: boardId={}, requestedBy={}",
                                context.command().boardId().getId(), context.command().requestedBy().getId());
                        return Either.left(Failure.ofPermissionDenied(
                                validationMessageResolver.getMessage("validation.board.modification.access.denied")));
                    }
                    return Either.right(context);
                });
    }

    private Either<Failure, BoardUpdateContext> checkArchiveStatus(BoardUpdateContext context) {
        if (context.board().isArchived()) {
            log.warn("아카이브된 보드의 수정 시도: boardId={}, requestedBy={}",
                    context.command().boardId().getId(), context.command().requestedBy().getId());
            return Either.left(Failure.ofConflict(
                    validationMessageResolver.getMessage("validation.board.archived.modification.denied")));
        }
        return Either.right(context);
    }

    private Either<Failure, BoardUpdateContext> checkForChanges(BoardUpdateContext context) {
        if (isNoChangesApplied(context.board(), context.command())) {
            log.info("보드 변경 사항 없음: boardId={}", context.command().boardId().getId());
            return Either.right(new BoardUpdateContext(context.command(), context.board(), true));
        }
        return Either.right(context);
    }

    private Either<Failure, BoardUpdateContext> applyChangesToBoard(BoardUpdateContext context) {
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
            return Either.left(Failure.ofInternalServerError(
                    validationMessageResolver.getMessage("validation.board.modification.error")));
        }
    }

    private Either<Failure, Board> saveUpdatedBoard(BoardUpdateContext context) {
        if (context.hasNoChanges()) {
            return Either.right(context.board());
        }

        return Try.of(() -> boardRepository.save(context.board()))
                .fold(
                        throwable -> {
                            log.error("보드 업데이트 중 예외 발생: boardId={}, error={}",
                                    context.command().boardId().getId(), throwable.getMessage(), throwable);
                            return Either.left(Failure.ofInternalServerError(
                                    validationMessageResolver.getMessage("validation.board.update.error")));
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

    private void applyChanges(Board board, UpdateBoardCommand command) {
        if (command.title() != null) {
            board.updateTitle(command.title());
            log.debug("보드 제목 업데이트: boardId={}, newTitle={}",
                    board.getBoardId().getId(), command.title());
        }

        if (command.description() != null) {
            board.updateDescription(command.description());
            log.debug("보드 설명 업데이트: boardId={}, descriptionLength={}",
                    board.getBoardId().getId(), command.description().length());
        }
    }

    private boolean isNoChangesApplied(Board board, UpdateBoardCommand command) {
        if (command.title() != null && !command.title().equals(board.getTitle())) {
            return false;
        }

        if (command.description() != null && !command.description().equals(board.getDescription())) {
            return false;
        }
        return true;
    }

    // ==================== DELETE BOARD ====================

    @Override
    public Either<Failure, Void> deleteBoard(DeleteBoardCommand command) {
        log.info("보드 삭제 시작: boardId={}, requestedBy={}",
                command.boardId().getId(), command.requestedBy().getId());

        // 1. 입력 검증
        ValidationResult<DeleteBoardCommand> validationResult = boardValidator.validateDelete(command);
        if (validationResult.isInvalid()) {
            log.warn("보드 삭제 검증 실패: boardId={}, violations={}",
                    command.boardId().getId(), validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }

        // 2. 사용자 존재 확인
        if (!userFinder.checkUserExists(command.requestedBy())) {
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("validation.user.not.found")));
        }

        // 3. 보드 존재 확인
        Optional<Board> boardOpt = boardRepository.findById(command.boardId());
        if (boardOpt.isEmpty()) {
            log.warn("보드를 찾을 수 없음: boardId={}", command.boardId().getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("validation.board.not.found")));
        }

        Board board = boardOpt.get();

        // 4. 권한 확인
        return boardPermissionService.canDeleteBoard(command.boardId(), command.requestedBy())
                .flatMap(canDelete -> {
                    if (!canDelete) {
                        log.warn("보드 삭제 권한 없음: boardId={}, requestedBy={}",
                                command.boardId().getId(), command.requestedBy().getId());
                        return Either.left(Failure.ofPermissionDenied(
                                validationMessageResolver.getMessage("validation.board.delete.access.denied")));
                    }
                    return Either.right(board);
                })
                .flatMap(this::deleteBoardAndRelatedData);
    }

    private Either<Failure, Void> deleteBoardAndRelatedData(Board board) {
        try {
            log.debug("보드 삭제 프로세스 시작: boardId={}, title={}",
                    board.getBoardId().getId(), board.getTitle());

            // 카드 삭제
            var cardDeleteResult = deleteAllCards(board.getBoardId());
            if (cardDeleteResult.isLeft()) {
                return Either.left(cardDeleteResult.getLeft());
            }

            // 리스트 삭제
            var listDeleteResult = deleteAllLists(board.getBoardId());
            if (listDeleteResult.isLeft()) {
                return Either.left(listDeleteResult.getLeft());
            }

            // 멤버 삭제
            var memberDeleteResult = deleteAllMembers(board.getBoardId());
            if (memberDeleteResult.isLeft()) {
                return Either.left(memberDeleteResult.getLeft());
            }

            // 보드 삭제
            var boardDeleteResult = deleteBoardEntity(board.getBoardId());
            if (boardDeleteResult.isLeft()) {
                return Either.left(boardDeleteResult.getLeft());
            }

            log.info("보드 삭제 완료: boardId={}, title={}",
                    board.getBoardId().getId(), board.getTitle());

            return Either.right(null);

        } catch (Exception e) {
            log.error("보드 삭제 중 예외 발생: boardId={}, error={}",
                    board.getBoardId().getId(), e.getMessage(), e);
            return Either.left(Failure.ofInternalServerError(
                    validationMessageResolver.getMessage("validation.board.delete.error")));
        }
    }

    private Either<Failure, Void> deleteAllCards(BoardId boardId) {
        log.debug("보드의 모든 카드 삭제 시작: boardId={}", boardId.getId());
        var result = cardRepository.deleteByBoardId(boardId);
        if (result.isLeft()) {
            log.error("보드의 카드 삭제 실패: boardId={}, error={}",
                    boardId.getId(), result.getLeft().getMessage());
            return Either.left(result.getLeft());
        }
        log.debug("보드의 모든 카드 삭제 완료: boardId={}", boardId.getId());
        return Either.right(null);
    }

    private Either<Failure, Void> deleteAllLists(BoardId boardId) {
        try {
            log.debug("보드의 모든 리스트 삭제 시작: boardId={}", boardId.getId());
            boardListRepository.deleteByBoardId(boardId);
            log.debug("보드의 모든 리스트 삭제 완료: boardId={}", boardId.getId());
            return Either.right(null);
        } catch (Exception e) {
            log.error("보드의 리스트 삭제 실패: boardId={}, error={}",
                    boardId.getId(), e.getMessage(), e);
            return Either.left(Failure.ofInternalServerError(
                    validationMessageResolver.getMessage("validation.board.list.delete.error")));
        }
    }

    private Either<Failure, Void> deleteAllMembers(BoardId boardId) {
        log.debug("보드의 모든 멤버 삭제 시작: boardId={}", boardId.getId());
        var result = boardMemberRepository.deleteByBoardId(boardId);
        if (result.isLeft()) {
            log.error("보드의 멤버 삭제 실패: boardId={}, error={}",
                    boardId.getId(), result.getLeft().getMessage());
            return Either.left(result.getLeft());
        }
        log.debug("보드의 모든 멤버 삭제 완료: boardId={}", boardId.getId());
        return Either.right(null);
    }

    private Either<Failure, Void> deleteBoardEntity(BoardId boardId) {
        log.debug("보드 엔티티 삭제 시작: boardId={}", boardId.getId());
        var result = boardRepository.delete(boardId);
        if (result.isLeft()) {
            log.error("보드 삭제 실패: boardId={}, error={}",
                    boardId.getId(), result.getLeft().getMessage());
            return Either.left(result.getLeft());
        }
        log.debug("보드 엔티티 삭제 완료: boardId={}", boardId.getId());
        return Either.right(null);
    }

    // ==================== ARCHIVE BOARD ====================

    @Override
    public Either<Failure, Board> archiveBoard(ArchiveBoardCommand command) {
        log.info("보드 아카이브 시작: boardId={}, requestedBy={}",
                command.boardId().getId(), command.requestedBy().getId());

        return validateArchiveCommand(command)
                .flatMap(this::findBoardForArchive)
                .flatMap(board -> verifyArchivePermission(command, board))
                .flatMap(this::performArchive);
    }

    @Override
    public Either<Failure, Board> unarchiveBoard(ArchiveBoardCommand command) {
        log.info("보드 언아카이브 시작: boardId={}, requestedBy={}",
                command.boardId().getId(), command.requestedBy().getId());

        return validateArchiveCommand(command)
                .flatMap(this::findBoardForArchive)
                .flatMap(board -> verifyArchivePermission(command, board))
                .flatMap(this::performUnarchive);
    }

    private Either<Failure, ArchiveBoardCommand> validateArchiveCommand(ArchiveBoardCommand command) {
        ValidationResult<ArchiveBoardCommand> validationResult = boardValidator.validateArchive(command);
        if (validationResult.isInvalid()) {
            log.warn("보드 아카이브 검증 실패: boardId={}, violations={}",
                    command.boardId().getId(), validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }

        if (!userFinder.checkUserExists(command.requestedBy())) {
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("validation.user.not.found")));
        }

        return Either.right(command);
    }

    private Either<Failure, Board> findBoardForArchive(ArchiveBoardCommand command) {
        Optional<Board> boardOpt = boardRepository.findById(command.boardId());
        if (boardOpt.isEmpty()) {
            log.warn("보드를 찾을 수 없음: boardId={}", command.boardId().getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("validation.board.not.found")));
        }

        return Either.right(boardOpt.get());
    }

    private Either<Failure, Board> verifyArchivePermission(ArchiveBoardCommand command, Board board) {
        return boardPermissionService.canArchiveBoard(command.boardId(), command.requestedBy())
                .flatMap(canArchive -> {
                    if (!canArchive) {
                        log.warn("보드 아카이브 권한 없음: boardId={}, requestedBy={}",
                                command.boardId().getId(), command.requestedBy().getId());
                        return Either.left(Failure.ofPermissionDenied(
                                validationMessageResolver.getMessage("validation.board.archive.access.denied")));
                    }
                    return Either.right(board);
                });
    }

    private Either<Failure, Board> performArchive(Board board) {
        try {
            if (board.isArchived()) {
                log.info("보드가 이미 아카이브됨: boardId={}", board.getBoardId().getId());
                return Either.right(board);
            }

            board.archive();
            log.debug("보드 아카이브 처리 완료: boardId={}", board.getBoardId().getId());

            return boardRepository.save(board)
                    .peek(savedBoard -> {
                        log.info("보드 아카이브 완료: boardId={}, title={}",
                                savedBoard.getBoardId().getId(), savedBoard.getTitle());
                    });

        } catch (Exception e) {
            log.error("보드 아카이브 중 오류 발생: boardId={}, error={}",
                    board.getBoardId().getId(), e.getMessage(), e);
            return Either.left(Failure.ofInternalServerError(
                    validationMessageResolver.getMessage("validation.board.archive.error")));
        }
    }

    private Either<Failure, Board> performUnarchive(Board board) {
        try {
            if (!board.isArchived()) {
                log.info("보드가 이미 활성 상태임: boardId={}", board.getBoardId().getId());
                return Either.right(board);
            }

            board.unarchive();
            log.debug("보드 언아카이브 처리 완료: boardId={}", board.getBoardId().getId());

            return boardRepository.save(board)
                    .peek(savedBoard -> {
                        log.info("보드 언아카이브 완료: boardId={}, title={}",
                                savedBoard.getBoardId().getId(), savedBoard.getTitle());
                    });

        } catch (Exception e) {
            log.error("보드 언아카이브 중 오류 발생: boardId={}, error={}",
                    board.getBoardId().getId(), e.getMessage(), e);
            return Either.left(Failure.ofInternalServerError(
                    validationMessageResolver.getMessage("validation.board.unarchive.error")));
        }
    }

    // ==================== HELPER CLASSES ====================

    private record BoardUpdateContext(
            UpdateBoardCommand command,
            Board board,
            boolean hasNoChanges) {
        BoardUpdateContext(UpdateBoardCommand command, Board board) {
            this(command, board, false);
        }
    }
}
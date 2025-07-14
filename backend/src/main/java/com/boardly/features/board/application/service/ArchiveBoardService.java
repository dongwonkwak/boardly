package com.boardly.features.board.application.service;

import com.boardly.features.board.application.port.input.ArchiveBoardCommand;
import com.boardly.features.board.application.usecase.ArchiveBoardUseCase;
import com.boardly.features.board.application.validation.ArchiveBoardValidator;
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

import java.util.Optional;

/**
 * 보드 아카이브 상태 변경 서비스
 * 
 * <p>보드의 아카이브 상태 변경 관련 비즈니스 로직을 처리하는 애플리케이션 서비스입니다.
 * 권한 확인, 상태 변경, 저장 등의 전체 보드 아카이브 상태 변경 프로세스를 관리합니다.
 * 
 *
 * @since 1.0.0
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ArchiveBoardService implements ArchiveBoardUseCase {

    private final BoardRepository boardRepository;
    private final ArchiveBoardValidator boardValidator;
    private final ValidationMessageResolver messageResolver;

    /**
     * 보드를 아카이브합니다.
     * 
     * @param command 보드 아카이브 명령
     * @return 아카이브 결과 (성공 시 아카이브된 보드, 실패 시 실패 정보)
     */
    @Override
    public Either<Failure, Board> archiveBoard(ArchiveBoardCommand command) {
        log.info("보드 아카이브 시작: boardId={}, requestedBy={}", 
                command.boardId().getId(), command.requestedBy().getId());

        return processBoardArchiveStatus(command, true);
    }

    /**
     * 보드를 언아카이브합니다.
     * 
     * @param command 보드 언아카이브 명령
     * @return 언아카이브 결과 (성공 시 활성화된 보드, 실패 시 실패 정보)
     */
    @Override
    public Either<Failure, Board> unarchiveBoard(ArchiveBoardCommand command) {
        log.info("보드 언아카이브 시작: boardId={}, requestedBy={}", 
                command.boardId().getId(), command.requestedBy().getId());

        return processBoardArchiveStatus(command, false);
    }

    /**
     * 보드 아카이브 상태 변경 처리
     * 
     * @param command 아카이브 상태 변경 명령
     * @param archiveStatus 설정할 아카이브 상태 (true: 아카이브, false: 언아카이브)
     * @return 처리 결과
     */
    private Either<Failure, Board> processBoardArchiveStatus(ArchiveBoardCommand command, boolean archiveStatus) {
        // 1. 입력 검증
        ValidationResult<ArchiveBoardCommand> validationResult = boardValidator.validate(command);
        if (validationResult.isInvalid()) {
            log.warn("보드 아카이브 검증 실패: boardId={}, violations={}", 
                    command.boardId().getId(), validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofValidation("INVALID_INPUT", validationResult.getErrorsAsCollection()));
        }

        // 2. 기존 보드 조회
        Optional<Board> existingBoardOpt = boardRepository.findById(command.boardId());
        if (existingBoardOpt.isEmpty()) {
            log.warn("보드를 찾을 수 없음: boardId={}", command.boardId().getId());
            return Either.left(Failure.ofNotFound(messageResolver.getMessage("validation.board.not.found")));
        }

        Board existingBoard = existingBoardOpt.get();

        // 3. 권한 확인 (요청자가 보드 소유자인지 확인)
        if (!existingBoard.getOwnerId().equals(command.requestedBy())) {
            log.warn("보드 아카이브 권한 없음: boardId={}, ownerId={}, requestedBy={}", 
                    command.boardId().getId(), existingBoard.getOwnerId().getId(), command.requestedBy().getId());
            return Either.left(Failure.ofForbidden(messageResolver.getMessage("validation.board.archive.access.denied")));
        }

        // 4. 상태 변경 필요 여부 확인
        if (existingBoard.isArchived() == archiveStatus) {
            String statusMessage = archiveStatus ? "이미 아카이브된" : "이미 활성화된";
            log.info("보드 상태 변경 불필요: boardId={}, status={}", 
                    command.boardId().getId(), statusMessage);
            return Either.right(existingBoard);
        }

        // 5. 아카이브 상태 변경
        try {
            if (archiveStatus) {
                existingBoard.archive();
                log.debug("보드 아카이브 완료: boardId={}", command.boardId().getId());
            } else {
                existingBoard.unarchive();
                log.debug("보드 언아카이브 완료: boardId={}", command.boardId().getId());
            }
        } catch (Exception e) {
            log.error("보드 아카이브 상태 변경 중 오류 발생: boardId={}, error={}", 
                    command.boardId().getId(), e.getMessage(), e);
            return Either.left(Failure.ofInternalServerError(messageResolver.getMessage("validation.board.archive.status.change.error")));
        }

        // 6. 변경된 보드 저장
        String actionName = archiveStatus ? "아카이브" : "언아카이브";
        String errorMessageKey = archiveStatus ? "validation.board.archive.error" : "validation.board.unarchive.error";
        
        return Try.of(() -> boardRepository.save(existingBoard))
            .fold(
                throwable -> {
                    log.error("보드 {} 중 예외 발생: boardId={}, error={}", 
                            actionName, command.boardId().getId(), throwable.getMessage(), throwable);
                    return Either.left(Failure.ofInternalServerError(messageResolver.getMessage(errorMessageKey)));
                },
                saveResult -> {
                    if (saveResult.isRight()) {
                        log.info("보드 {} 완료: boardId={}, isArchived={}", 
                                actionName, saveResult.get().getBoardId().getId(), saveResult.get().isArchived());
                    } else {
                        log.error("보드 저장 실패: boardId={}, error={}", 
                                command.boardId().getId(), saveResult.getLeft().message());
                    }
                    return saveResult;
                }
            );
    }
} 
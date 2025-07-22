package com.boardly.features.board.application.service;

import com.boardly.features.board.application.port.input.DeleteBoardCommand;
import com.boardly.features.board.application.usecase.DeleteBoardUseCase;
import com.boardly.features.board.application.validation.DeleteBoardValidator;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.board.domain.repository.BoardMemberRepository;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 보드 삭제 서비스
 * 
 * <p>
 * 보드를 영구적으로 삭제하는 비즈니스 로직을 처리하는 애플리케이션 서비스입니다.
 * 보드 삭제 시 연관된 모든 데이터(BoardList, Card, BoardMember)를 함께 삭제합니다.
 * 
 * @since 1.0.0
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DeleteBoardService implements DeleteBoardUseCase {

    private final DeleteBoardValidator deleteBoardValidator;
    private final BoardRepository boardRepository;
    private final BoardListRepository boardListRepository;
    private final CardRepository cardRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final ValidationMessageResolver validationMessageResolver;

    /**
     * 보드를 영구적으로 삭제합니다.
     * 
     * @param command 보드 삭제 명령
     * @return 삭제 결과 (성공 시 null, 실패 시 실패 정보)
     */
    @Override
    public Either<Failure, Void> deleteBoard(DeleteBoardCommand command) {
        log.info("보드 삭제 시작: boardId={}, requestedBy={}",
                command.boardId().getId(), command.requestedBy().getId());

        // 1. 입력 검증
        ValidationResult<DeleteBoardCommand> validationResult = deleteBoardValidator.validate(command);
        if (validationResult.isInvalid()) {
            log.warn("보드 삭제 검증 실패: boardId={}, violations={}",
                    command.boardId().getId(), validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }

        // 2. 보드 존재 확인
        Optional<Board> boardOpt = boardRepository.findById(command.boardId());
        if (boardOpt.isEmpty()) {
            log.warn("보드를 찾을 수 없음: boardId={}", command.boardId().getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("validation.board.not.found")));
        }

        Board board = boardOpt.get();

        // 3. 권한 확인 (요청자가 보드 소유자인지 확인)
        if (!board.getOwnerId().equals(command.requestedBy())) {
            log.warn("보드 삭제 권한 없음: boardId={}, ownerId={}, requestedBy={}",
                    command.boardId().getId(), board.getOwnerId().getId(),
                    command.requestedBy().getId());
            return Either.left(Failure.ofPermissionDenied(
                    validationMessageResolver.getMessage("validation.board.delete.access.denied")));
        }

        // 4. 보드 삭제 및 연관 데이터 정리
        try {
            log.debug("보드 삭제 프로세스 시작: boardId={}, title={}",
                    command.boardId().getId(), board.getTitle());

            // 4-1. 보드의 모든 카드 삭제
            var cardDeleteResult = deleteAllCards(command.boardId());
            if (cardDeleteResult.isLeft()) {
                return Either.left(cardDeleteResult.getLeft());
            }

            // 4-2. 보드의 모든 리스트 삭제
            var listDeleteResult = deleteAllLists(command.boardId());
            if (listDeleteResult.isLeft()) {
                return Either.left(listDeleteResult.getLeft());
            }

            // 4-3. 보드의 모든 멤버 삭제
            var memberDeleteResult = deleteAllMembers(command.boardId());
            if (memberDeleteResult.isLeft()) {
                return Either.left(memberDeleteResult.getLeft());
            }

            // 4-4. 보드 삭제
            var boardDeleteResult = deleteBoardEntity(command.boardId());
            if (boardDeleteResult.isLeft()) {
                return Either.left(boardDeleteResult.getLeft());
            }

            log.info("보드 삭제 완료: boardId={}, title={}",
                    command.boardId().getId(), board.getTitle());

            return Either.right(null);

        } catch (Exception e) {
            log.error("보드 삭제 중 예외 발생: boardId={}, error={}",
                    command.boardId().getId(), e.getMessage(), e);
            return Either.left(Failure.ofInternalServerError(
                    validationMessageResolver.getMessage("validation.board.delete.error")));
        }
    }

    /**
     * 보드의 모든 카드를 삭제합니다.
     * 
     * @param boardId 보드 ID
     * @return 삭제 결과
     */
    private Either<Failure, Void> deleteAllCards(com.boardly.features.board.domain.model.BoardId boardId) {
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

    /**
     * 보드의 모든 리스트를 삭제합니다.
     * 
     * @param boardId 보드 ID
     * @return 삭제 결과
     */
    private Either<Failure, Void> deleteAllLists(com.boardly.features.board.domain.model.BoardId boardId) {
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

    /**
     * 보드의 모든 멤버를 삭제합니다.
     * 
     * @param boardId 보드 ID
     * @return 삭제 결과
     */
    private Either<Failure, Void> deleteAllMembers(com.boardly.features.board.domain.model.BoardId boardId) {
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

    /**
     * 보드 엔티티를 삭제합니다.
     * 
     * @param boardId 보드 ID
     * @return 삭제 결과
     */
    private Either<Failure, Void> deleteBoardEntity(com.boardly.features.board.domain.model.BoardId boardId) {
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
}
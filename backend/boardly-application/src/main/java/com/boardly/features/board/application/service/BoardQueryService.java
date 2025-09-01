package com.boardly.features.board.application.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardly.features.attachment.domain.port.AttachmentRepository;
import com.boardly.features.board.application.dto.BoardDetailDto;
import com.boardly.features.board.application.query.GetBoardDetailQuery;
import com.boardly.features.board.application.query.GetUserBoardsQuery;
import com.boardly.features.board.application.usecase.GetBoardDetailUseCase;
import com.boardly.features.board.application.usecase.GetUserBoardsUseCase;
import com.boardly.features.board.application.validation.BoardValidator;
import com.boardly.features.board.domain.Board;
import com.boardly.features.board.domain.port.BoardRepository;
import com.boardly.features.board.domain.port.GetBoardDetailPort;
import com.boardly.features.board.domain.port.GetBoardDetailPort.BoardDetailData;
import com.boardly.features.comment.domain.port.CommentRepository;
import com.boardly.features.user.domain.port.UserFinder;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.common.value.CardId;
import com.boardly.shared.validation.MessageResolver;

import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 보드 조회 서비스
 * 
 * <p>
 * 보드 조회 관련 작업을 담당하는 통합 서비스입니다.
 * 사용자 보드 목록 조회와 보드 상세 조회 기능을 제공합니다.
 * </p>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BoardQueryService implements GetUserBoardsUseCase, GetBoardDetailUseCase {

    private final BoardRepository boardRepository;
    private final UserFinder userFinder;
    private final GetBoardDetailPort getBoardDetailPort;
    private final MessageResolver validationMessageResolver;
    private final BoardValidator boardValidator;
    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;

    @Override
    public Either<Failure, List<Board>> getUserBoards(GetUserBoardsQuery command) {
        return validateCommand(command)
                .flatMap(cmd -> validateOwnerId(cmd))
                .flatMap(cmd -> checkUserExists(cmd))
                .flatMap(cmd -> fetchUserBoards(cmd));
    }

    private Either<Failure, GetUserBoardsQuery> validateCommand(GetUserBoardsQuery command) {
        if (command == null) {
            var violation = Failure.FieldViolation.builder()
                    .field("command")
                    .message("GetUserBoardsQuery is null")
                    .rejectedValue(null)
                    .build();
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.input.invalid"),
                    "INVALID_COMMAND",
                    List.of(violation)));
        }

        log.info("사용자 보드 목록 조회 시작: ownerId={}, includeArchived={}",
                command.ownerId(), command.includeArchived());

        return Either.right(command);
    }

    private Either<Failure, GetUserBoardsQuery> validateOwnerId(GetUserBoardsQuery command) {
        if (command.ownerId() == null) {
            var violation = Failure.FieldViolation.builder()
                    .field("userId")
                    .message(validationMessageResolver.getMessage("validation.user.id.required"))
                    .rejectedValue(null)
                    .build();
            log.warn("사용자 보드 목록 조회 검증 실패: ownerId=null");
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.of(violation)));
        }
        return Either.right(command);
    }

    private Either<Failure, GetUserBoardsQuery> checkUserExists(GetUserBoardsQuery command) {
        if (!userFinder.userExists(command.ownerId())) {
            return Either.left(Failure
                    .ofNotFound(validationMessageResolver.getMessage("validation.user.not.found")));
        }
        return Either.right(command);
    }

    private Either<Failure, List<Board>> fetchUserBoards(GetUserBoardsQuery command) {
        return Try.of(() -> {
            List<Board> boards = retrieveBoardsByType(command);
            List<Board> sortedBoards = sortBoardsByUpdatedAt(boards);

            log.info("사용자 보드 목록 조회 완료: ownerId={}, totalCount={}, includeArchived={}",
                    command.ownerId(), sortedBoards.size(), command.includeArchived());

            return sortedBoards;
        })
                .fold(
                        throwable -> handleBoardQueryError(command, throwable),
                        Either::right);
    }

    private List<Board> retrieveBoardsByType(GetUserBoardsQuery command) {
        List<Board> boards;

        if (command.includeArchived()) {
            // 모든 보드 조회 (활성 + 아카이브)
            boards = boardRepository.findByOwnerId(command.ownerId());
            log.debug("전체 보드 조회 완료: ownerId={}, count={}", command.ownerId(), boards.size());
        } else {
            // 활성 보드만 조회
            boards = boardRepository.findActiveByOwnerId(command.ownerId());
            log.debug("활성 보드 조회 완료: ownerId={}, count={}", command.ownerId(), boards.size());
        }

        return boards;
    }

    private List<Board> sortBoardsByUpdatedAt(List<Board> boards) {
        return boards.stream()
                .sorted(Comparator.comparing(Board::getUpdatedAt).reversed())
                .toList();
    }

    private Either<Failure, List<Board>> handleBoardQueryError(GetUserBoardsQuery command, Throwable throwable) {
        log.error("사용자 보드 목록 조회 중 예외 발생: ownerId={}, error={}",
                command.ownerId(), throwable.getMessage(), throwable);
        return Either.left(Failure.ofInternalError(
                throwable.getMessage(),
                "BOARD_QUERY_ERROR", null));
    }

    @Override
    public Either<Failure, BoardDetailDto> getBoardDetail(GetBoardDetailQuery command) {
        log.info("보드 상세 조회 시작: boardId={}, userId={}",
                command.boardId().getId(), command.userId().getId());

        return validateBoardDetailInput(command)
                .flatMap(cmd -> fetchBoardDetailData(cmd))
                .flatMap(data -> createBoardDetailDto(data, command));
    }

    private Either<Failure, GetBoardDetailQuery> validateBoardDetailInput(GetBoardDetailQuery command) {
        var validationResult = boardValidator.validateGetDetail(command);
        if (validationResult.isInvalid()) {
            log.warn("보드 상세 조회 검증 실패: boardId={}, userId={}, violations={}",
                    command.boardId().getId(), command.userId().getId(), validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    new ArrayList<>(validationResult.getErrorsAsCollection())));
        }
        return Either.right(command);
    }

    private Either<Failure, BoardDetailData> fetchBoardDetailData(GetBoardDetailQuery command) {
        try {
            Either<Failure, BoardDetailData> dataResult = getBoardDetailPort
                    .getBoardDetail(command.boardId(), command.userId());

            if (dataResult.isLeft()) {
                log.warn("보드 상세 데이터 조회 실패: boardId={}, error={}",
                        command.boardId().getId(), dataResult.getLeft().getMessage());
                return Either.left(dataResult.getLeft());
            }

            return Either.right(dataResult.get());
        } catch (Exception e) {
            log.error("보드 상세 조회 중 예외 발생: boardId={}, error={}",
                    command.boardId().getId(), e.getMessage(), e);

            String message = validationMessageResolver.getMessage("board.detail.get.error", "보드 상세 조회 중 오류가 발생했습니다.");
            return Either.left(Failure.ofInternalServerError(message));
        }
    }

    private Either<Failure, BoardDetailDto> createBoardDetailDto(BoardDetailData data, GetBoardDetailQuery command) {
        try {
            // 카드별 댓글 수와 첨부파일 수 조회
            List<CardId> cardIds = data.cards().values().stream()
                    .flatMap(List::stream)
                    .map(card -> card.getCardId())
                    .collect(Collectors.toList());

            Map<CardId, Integer> cardCommentCounts = cardIds.stream()
                    .collect(Collectors.toMap(
                            cardId -> cardId,
                            cardId -> commentRepository.countByCardId(cardId)));

            Map<CardId, Integer> cardAttachmentCounts = cardIds.stream()
                    .collect(Collectors.toMap(
                            cardId -> cardId,
                            cardId -> attachmentRepository.countByCardId(cardId)));

            BoardDetailDto boardDetailDto = BoardDetailDto.of(
                    data.board(),
                    data.boardLists(),
                    data.boardMembers(),
                    data.labels(),
                    data.cards().values().stream().flatMap(List::stream).collect(Collectors.toList()),
                    data.cardMembers(),
                    data.cardLabels(),
                    cardCommentCounts,
                    cardAttachmentCounts,
                    data.users().values().stream().collect(Collectors.toList()));

            log.info("보드 상세 조회 완료: boardId={}", command.boardId().getId());
            return Either.right(boardDetailDto);
        } catch (Exception e) {
            log.error("보드 상세 DTO 생성 중 예외 발생: boardId={}, error={}",
                    command.boardId().getId(), e.getMessage(), e);
            return Either.left(Failure.ofInternalServerError("보드 상세 정보 생성 중 오류가 발생했습니다."));
        }
    }
}
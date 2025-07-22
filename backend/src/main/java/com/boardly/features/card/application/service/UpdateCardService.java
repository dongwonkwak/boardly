package com.boardly.features.card.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardly.features.card.application.port.input.UpdateCardCommand;
import com.boardly.features.card.application.usecase.UpdateCardUseCase;
import com.boardly.features.card.application.validation.UpdateCardValidator;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * 카드 수정 서비스
 * 
 * <p>
 * 카드의 제목과 설명을 수정하는 비즈니스 로직을 처리합니다.
 * 입력 검증, 권한 확인, 도메인 로직 실행, 저장 등의 전체 카드 수정 프로세스를 관리합니다.
 * 
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateCardService implements UpdateCardUseCase {

    private final UpdateCardValidator cardValidator;
    private final CardRepository cardRepository;
    private final BoardListRepository boardListRepository;
    private final BoardRepository boardRepository;
    private final ValidationMessageResolver validationMessageResolver;

    @Override
    public Either<Failure, Card> updateCard(UpdateCardCommand command) {
        log.info("카드 수정 시작: cardId={}, title={}, userId={}",
                command.cardId().getId(), command.title(), command.userId().getId());

        return validateInput(command)
                .flatMap(this::findExistingCard)
                .flatMap(this::verifyBoardAccess)
                .flatMap(this::checkArchiveStatus)
                .flatMap(this::applyChangesToCard)
                .flatMap(this::saveUpdatedCard);
    }

    /**
     * 1단계: 입력 검증
     */
    private Either<Failure, UpdateCardCommand> validateInput(UpdateCardCommand command) {
        ValidationResult<UpdateCardCommand> validationResult = cardValidator.validate(command);
        if (validationResult.isInvalid()) {
            log.warn("카드 수정 검증 실패: cardId={}, violations={}",
                    command.cardId().getId(), validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    validationMessageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }
        return Either.right(command);
    }

    /**
     * 2단계: 기존 카드 조회
     */
    private Either<Failure, CardUpdateContext> findExistingCard(UpdateCardCommand command) {
        Optional<Card> existingCardOpt = cardRepository.findById(command.cardId());
        if (existingCardOpt.isEmpty()) {
            log.warn("카드를 찾을 수 없음: cardId={}", command.cardId().getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("error.service.card.move.not_found")));
        }

        Card existingCard = existingCardOpt.get();
        return Either.right(new CardUpdateContext(command, existingCard));
    }

    /**
     * 3단계: 보드 접근 권한 확인
     */
    private Either<Failure, CardUpdateContext> verifyBoardAccess(CardUpdateContext context) {
        // 리스트 존재 확인
        var boardList = boardListRepository.findById(context.card().getListId());
        if (boardList.isEmpty()) {
            log.warn("리스트를 찾을 수 없음: listId={}", context.card().getListId().getId());
            return Either.left(Failure.ofNotFound(
                    validationMessageResolver.getMessage("error.service.card.move.list_not_found")));
        }

        // 보드 존재 및 권한 확인
        var board = boardRepository.findByIdAndOwnerId(boardList.get().getBoardId(), context.command().userId());
        if (board.isEmpty()) {
            log.warn("보드 접근 권한 없음: boardId={}, userId={}",
                    boardList.get().getBoardId().getId(), context.command().userId().getId());
            return Either.left(Failure.ofPermissionDenied(
                    validationMessageResolver.getMessage("error.service.card.move.access_denied")));
        }

        return Either.right(context);
    }

    /**
     * 4단계: 아카이브된 보드의 카드 수정 제한 확인
     */
    private Either<Failure, CardUpdateContext> checkArchiveStatus(CardUpdateContext context) {
        var boardList = boardListRepository.findById(context.card().getListId());
        if (boardList.isPresent()) {
            var board = boardRepository.findById(boardList.get().getBoardId());
            if (board.isPresent() && board.get().isArchived()) {
                log.warn("아카이브된 보드의 카드 수정 시도: cardId={}, boardId={}, userId={}",
                        context.command().cardId().getId(),
                        board.get().getBoardId().getId(),
                        context.command().userId().getId());
                return Either.left(Failure.ofConflict(
                        validationMessageResolver.getMessage("error.service.card.move.archived_board")));
            }
        }
        return Either.right(context);
    }

    /**
     * 5단계: 변경 사항 적용
     */
    private Either<Failure, CardUpdateContext> applyChangesToCard(CardUpdateContext context) {
        try {
            // 제목 업데이트
            context.card().updateTitle(context.command().title());
            log.debug("카드 제목 업데이트: cardId={}, newTitle={}",
                    context.command().cardId().getId(), context.command().title());

            // 설명 업데이트
            context.card().updateDescription(context.command().description());
            log.debug("카드 설명 업데이트: cardId={}, descriptionLength={}",
                    context.command().cardId().getId(),
                    context.command().description() != null ? context.command().description().length() : 0);

            return Either.right(context);
        } catch (Exception e) {
            log.error("카드 변경 중 오류 발생: cardId={}, error={}",
                    context.command().cardId().getId(), e.getMessage(), e);
            return Either.left(Failure.ofInternalServerError(
                    validationMessageResolver.getMessage("validation.card.modification.error")));
        }
    }

    /**
     * 6단계: 업데이트된 카드 저장
     */
    private Either<Failure, Card> saveUpdatedCard(CardUpdateContext context) {
        return cardRepository.save(context.card())
                .peek(card -> log.info("카드 수정 완료: cardId={}, title={}",
                        card.getCardId().getId(), card.getTitle()));
    }

    /**
     * 카드 업데이트 과정에서 사용되는 컨텍스트 객체
     */
    private record CardUpdateContext(
            UpdateCardCommand command,
            Card card) {
    }
}
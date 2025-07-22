package com.boardly.features.card.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListColor;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.application.port.input.UpdateCardCommand;
import com.boardly.features.card.application.port.input.MoveCardCommand;
import com.boardly.features.card.application.validation.CardValidator;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.policy.CardMovePolicy;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateCardService 테스트")
class UpdateCardServiceTest {

    @Mock
    private CardValidator cardValidator;

    @Mock
    private CardMovePolicy cardMovePolicy;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private BoardListRepository boardListRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private ValidationMessageResolver validationMessageResolver;

    private UpdateCardService updateCardService;

    @BeforeEach
    void setUp() {
        updateCardService = new UpdateCardService(
                cardValidator,
                cardMovePolicy,
                cardRepository,
                boardListRepository,
                boardRepository,
                validationMessageResolver);

        // 공통으로 사용되는 메시지 설정
        lenient().when(validationMessageResolver.getMessage("validation.input.invalid"))
                .thenReturn("입력값이 유효하지 않습니다.");
        lenient().when(validationMessageResolver.getMessage("error.service.card.update.not_found"))
                .thenReturn("카드를 찾을 수 없습니다.");
        lenient().when(validationMessageResolver.getMessage("error.service.card.update.list_not_found"))
                .thenReturn("리스트를 찾을 수 없습니다.");
        lenient().when(validationMessageResolver.getMessage("error.service.card.update.access_denied"))
                .thenReturn("보드 접근 권한이 없습니다.");
        lenient().when(validationMessageResolver.getMessage("error.service.card.update.archived_board"))
                .thenReturn("아카이브된 보드의 카드는 수정할 수 없습니다.");
        lenient().when(validationMessageResolver.getMessage("error.service.card.update.error"))
                .thenReturn("카드 수정 중 오류가 발생했습니다.");
        lenient().when(validationMessageResolver.getMessage("error.service.card.move.not_found"))
                .thenReturn("이동할 카드를 찾을 수 없습니다.");
        lenient().when(validationMessageResolver.getMessage("error.service.card.move.list_not_found"))
                .thenReturn("리스트를 찾을 수 없습니다.");
        lenient().when(validationMessageResolver.getMessage("error.service.card.move.access_denied"))
                .thenReturn("보드 접근 권한이 없습니다.");
        lenient().when(validationMessageResolver.getMessage("error.service.card.move.archived_board"))
                .thenReturn("아카이브된 보드의 카드는 이동할 수 없습니다.");
        lenient().when(validationMessageResolver.getMessage("error.service.card.move.target_list_not_found"))
                .thenReturn("대상 리스트를 찾을 수 없습니다.");
    }

    @Nested
    @DisplayName("updateCard 메서드 테스트")
    class UpdateCardTest {

        private UpdateCardCommand validCommand;
        private Card existingCard;
        private BoardList boardList;
        private Board board;

        @BeforeEach
        void setUp() {
            CardId cardId = new CardId("card-1");
            UserId userId = new UserId("user-1");
            ListId listId = new ListId("list-1");
            BoardId boardId = new BoardId("board-1");

            validCommand = UpdateCardCommand.of(cardId, "새로운 제목", "새로운 설명", userId);

            existingCard = Card.builder()
                    .cardId(cardId)
                    .title("기존 제목")
                    .description("기존 설명")
                    .position(0)
                    .listId(listId)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            boardList = BoardList.builder()
                    .listId(listId)
                    .title("테스트 리스트")
                    .boardId(boardId)
                    .position(0)
                    .color(ListColor.of("#0079BF"))
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            board = Board.builder()
                    .boardId(boardId)
                    .title("테스트 보드")
                    .description("테스트 보드 설명")
                    .ownerId(userId)
                    .isArchived(false)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
        }

        @Test
        @DisplayName("유효한 커맨드로 카드 수정 시 성공한다")
        void shouldUpdateCardSuccessfully() {
            // given
            ValidationResult<UpdateCardCommand> validResult = ValidationResult.valid(validCommand);
            when(cardValidator.validateUpdate(validCommand)).thenReturn(validResult);
            when(cardRepository.findById(validCommand.cardId())).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(existingCard.getListId())).thenReturn(Optional.of(boardList));
            when(boardRepository.findByIdAndOwnerId(boardList.getBoardId(), validCommand.userId()))
                    .thenReturn(Optional.of(board));
            when(boardRepository.findById(boardList.getBoardId())).thenReturn(Optional.of(board));
            when(cardRepository.save(any(Card.class))).thenReturn(Either.right(existingCard));

            // when
            Either<Failure, Card> result = updateCardService.updateCard(validCommand);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(existingCard);
            verify(cardValidator).validateUpdate(validCommand);
            verify(cardRepository).findById(validCommand.cardId());
            verify(boardListRepository, org.mockito.Mockito.times(2)).findById(existingCard.getListId());
            verify(boardRepository).findByIdAndOwnerId(boardList.getBoardId(), validCommand.userId());
            verify(boardRepository).findById(boardList.getBoardId());
            verify(cardRepository).save(existingCard);
        }

        @Test
        @DisplayName("입력 검증 실패 시 실패를 반환한다")
        void shouldReturnFailureWhenValidationFails() {
            // given
            List<Failure.FieldViolation> validationErrors = List.of(
                    Failure.FieldViolation.builder()
                            .field("title")
                            .message("제목은 필수입니다.")
                            .rejectedValue(null)
                            .build(),
                    Failure.FieldViolation.builder()
                            .field("description")
                            .message("설명이 너무 깁니다.")
                            .rejectedValue("너무 긴 설명...")
                            .build());
            when(cardValidator.validateUpdate(validCommand))
                    .thenReturn(ValidationResult
                            .invalid(io.vavr.collection.List.ofAll(validationErrors)));

            // when
            Either<Failure, Card> result = updateCardService.updateCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
            Failure.InputError inputError = (Failure.InputError) result.getLeft();
            assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
            assertThat(inputError.getViolations()).containsExactlyElementsOf(validationErrors);
        }

        @Test
        @DisplayName("카드가 존재하지 않을 때 실패를 반환한다")
        void shouldReturnFailureWhenCardNotFound() {
            // given
            ValidationResult<UpdateCardCommand> validResult = ValidationResult.valid(validCommand);
            when(cardValidator.validateUpdate(validCommand)).thenReturn(validResult);
            when(cardRepository.findById(validCommand.cardId())).thenReturn(Optional.empty());

            // when
            Either<Failure, Card> result = updateCardService.updateCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
            assertThat(((Failure.NotFound) result.getLeft()).getErrorCode()).isEqualTo("NOT_FOUND");
        }

        @Test
        @DisplayName("리스트가 존재하지 않을 때 실패를 반환한다")
        void shouldReturnFailureWhenListNotFound() {
            // given
            ValidationResult<UpdateCardCommand> validResult = ValidationResult.valid(validCommand);
            when(cardValidator.validateUpdate(validCommand)).thenReturn(validResult);
            when(cardRepository.findById(validCommand.cardId())).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(existingCard.getListId())).thenReturn(Optional.empty());

            // when
            Either<Failure, Card> result = updateCardService.updateCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
            assertThat(((Failure.NotFound) result.getLeft()).getErrorCode()).isEqualTo("NOT_FOUND");
        }

        @Test
        @DisplayName("보드 접근 권한이 없을 때 실패를 반환한다")
        void shouldReturnFailureWhenBoardAccessDenied() {
            // given
            ValidationResult<UpdateCardCommand> validResult = ValidationResult.valid(validCommand);
            when(cardValidator.validateUpdate(validCommand)).thenReturn(validResult);
            when(cardRepository.findById(validCommand.cardId())).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(existingCard.getListId())).thenReturn(Optional.of(boardList));
            when(boardRepository.findByIdAndOwnerId(boardList.getBoardId(), validCommand.userId()))
                    .thenReturn(Optional.empty());

            // when
            Either<Failure, Card> result = updateCardService.updateCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
            assertThat(((Failure.PermissionDenied) result.getLeft()).getErrorCode())
                    .isEqualTo("PERMISSION_DENIED");
        }

        @Test
        @DisplayName("아카이브된 보드의 카드 수정 시도 시 실패를 반환한다")
        void shouldReturnFailureWhenBoardIsArchived() {
            // given
            Board archivedBoard = Board.builder()
                    .boardId(board.getBoardId())
                    .title("아카이브된 보드")
                    .description("아카이브된 보드 설명")
                    .ownerId(validCommand.userId())
                    .isArchived(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            ValidationResult<UpdateCardCommand> validResult = ValidationResult.valid(validCommand);
            when(cardValidator.validateUpdate(validCommand)).thenReturn(validResult);
            when(cardRepository.findById(validCommand.cardId())).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(existingCard.getListId())).thenReturn(Optional.of(boardList));
            when(boardRepository.findByIdAndOwnerId(boardList.getBoardId(), validCommand.userId()))
                    .thenReturn(Optional.of(archivedBoard));
            when(boardRepository.findById(boardList.getBoardId())).thenReturn(Optional.of(archivedBoard));

            // when
            Either<Failure, Card> result = updateCardService.updateCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.ResourceConflict.class);
            assertThat(((Failure.ResourceConflict) result.getLeft()).getErrorCode())
                    .isEqualTo("RESOURCE_CONFLICT");
        }

        @Test
        @DisplayName("카드 저장 실패 시 실패를 반환한다")
        void shouldReturnFailureWhenCardSaveFails() {
            // given
            ValidationResult<UpdateCardCommand> validResult = ValidationResult.valid(validCommand);
            when(cardValidator.validateUpdate(validCommand)).thenReturn(validResult);
            when(cardRepository.findById(validCommand.cardId())).thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(existingCard.getListId())).thenReturn(Optional.of(boardList));
            when(boardRepository.findByIdAndOwnerId(boardList.getBoardId(), validCommand.userId()))
                    .thenReturn(Optional.of(board));
            when(cardRepository.save(any(Card.class)))
                    .thenReturn(Either.left(Failure.ofInternalServerError("저장 실패")));

            // when
            Either<Failure, Card> result = updateCardService.updateCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
            assertThat(((Failure.InternalError) result.getLeft()).getErrorCode())
                    .isEqualTo("INTERNAL_ERROR");
        }
    }

    @Nested
    @DisplayName("moveCard 메서드 테스트")
    class MoveCardTest {

        private MoveCardCommand sameListMoveCommand;
        private MoveCardCommand differentListMoveCommand;
        private Card existingCard;
        private BoardList sourceBoardList;
        private BoardList targetBoardList;
        private Board board;

        @BeforeEach
        void setUp() {
            CardId cardId = new CardId("card-1");
            UserId userId = new UserId("user-1");
            ListId sourceListId = new ListId("list-1");
            ListId targetListId = new ListId("list-2");
            BoardId boardId = new BoardId("board-1");

            sameListMoveCommand = MoveCardCommand.of(cardId, null, 2, userId);
            differentListMoveCommand = MoveCardCommand.of(cardId, targetListId, 1, userId);

            existingCard = Card.builder()
                    .cardId(cardId)
                    .title("테스트 카드")
                    .description("테스트 카드 설명")
                    .position(0)
                    .listId(sourceListId)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            sourceBoardList = BoardList.builder()
                    .listId(sourceListId)
                    .title("소스 리스트")
                    .boardId(boardId)
                    .position(0)
                    .color(ListColor.of("#0079BF"))
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            targetBoardList = BoardList.builder()
                    .listId(targetListId)
                    .title("타겟 리스트")
                    .boardId(boardId)
                    .position(1)
                    .color(ListColor.of("#519839"))
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            board = Board.builder()
                    .boardId(boardId)
                    .title("테스트 보드")
                    .description("테스트 보드 설명")
                    .ownerId(userId)
                    .isArchived(false)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
        }

        @Test
        @DisplayName("같은 리스트 내에서 카드 이동 시 성공한다")
        void shouldMoveCardWithinSameListSuccessfully() {
            // given
            ValidationResult<MoveCardCommand> validResult = ValidationResult.valid(sameListMoveCommand);
            when(cardValidator.validateMove(sameListMoveCommand)).thenReturn(validResult);
            when(cardRepository.findById(sameListMoveCommand.cardId()))
                    .thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(existingCard.getListId()))
                    .thenReturn(Optional.of(sourceBoardList));
            when(boardRepository.findByIdAndOwnerId(sourceBoardList.getBoardId(),
                    sameListMoveCommand.userId()))
                    .thenReturn(Optional.of(board));
            when(cardMovePolicy.canMoveWithinSameList(existingCard, sameListMoveCommand.newPosition()))
                    .thenReturn(Either.right(null));
            when(cardRepository.save(existingCard)).thenReturn(Either.right(existingCard));

            // when
            Either<Failure, Card> result = updateCardService.moveCard(sameListMoveCommand);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(existingCard);
            verify(cardValidator).validateMove(sameListMoveCommand);
            verify(cardRepository).findById(sameListMoveCommand.cardId());
            verify(boardListRepository).findById(existingCard.getListId());
            verify(boardRepository).findByIdAndOwnerId(sourceBoardList.getBoardId(),
                    sameListMoveCommand.userId());
            verify(cardMovePolicy).canMoveWithinSameList(existingCard, sameListMoveCommand.newPosition());
            verify(cardRepository).save(existingCard);
        }

        @Test
        @DisplayName("다른 리스트로 카드 이동 시 성공한다")
        void shouldMoveCardToAnotherListSuccessfully() {
            // given
            ValidationResult<MoveCardCommand> validResult = ValidationResult
                    .valid(differentListMoveCommand);
            when(cardValidator.validateMove(differentListMoveCommand)).thenReturn(validResult);
            when(cardRepository.findById(differentListMoveCommand.cardId()))
                    .thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(existingCard.getListId()))
                    .thenReturn(Optional.of(sourceBoardList));
            when(boardRepository.findByIdAndOwnerId(sourceBoardList.getBoardId(),
                    differentListMoveCommand.userId()))
                    .thenReturn(Optional.of(board));
            when(boardListRepository.findById(differentListMoveCommand.targetListId()))
                    .thenReturn(Optional.of(targetBoardList));
            when(boardRepository.findByIdAndOwnerId(targetBoardList.getBoardId(),
                    differentListMoveCommand.userId()))
                    .thenReturn(Optional.of(board));
            when(cardMovePolicy.canMoveToAnotherList(existingCard, differentListMoveCommand.targetListId(),
                    differentListMoveCommand.newPosition()))
                    .thenReturn(Either.right(null));
            when(cardRepository.save(existingCard)).thenReturn(Either.right(existingCard));

            // when
            Either<Failure, Card> result = updateCardService.moveCard(differentListMoveCommand);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(existingCard);
            verify(cardValidator).validateMove(differentListMoveCommand);
            verify(cardRepository).findById(differentListMoveCommand.cardId());
            verify(boardListRepository, org.mockito.Mockito.atLeastOnce()).findById(any());
            verify(boardRepository, org.mockito.Mockito.atLeastOnce()).findByIdAndOwnerId(any(), any());
            verify(cardMovePolicy).canMoveToAnotherList(existingCard,
                    differentListMoveCommand.targetListId(),
                    differentListMoveCommand.newPosition());
            verify(cardRepository).save(existingCard);
        }

        @Test
        @DisplayName("입력 검증 실패 시 실패를 반환한다")
        void shouldReturnFailureWhenValidationFails() {
            // given
            List<Failure.FieldViolation> validationErrors = List.of(
                    Failure.FieldViolation.builder()
                            .field("newPosition")
                            .message("새로운 위치는 0 이상이어야 합니다.")
                            .rejectedValue(-1)
                            .build());
            when(cardValidator.validateMove(sameListMoveCommand))
                    .thenReturn(ValidationResult
                            .invalid(io.vavr.collection.List.ofAll(validationErrors)));

            // when
            Either<Failure, Card> result = updateCardService.moveCard(sameListMoveCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
            Failure.InputError inputError = (Failure.InputError) result.getLeft();
            assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
            assertThat(inputError.getViolations()).containsExactlyElementsOf(validationErrors);
        }

        @Test
        @DisplayName("이동할 카드가 존재하지 않을 때 실패를 반환한다")
        void shouldReturnFailureWhenCardNotFound() {
            // given
            ValidationResult<MoveCardCommand> validResult = ValidationResult.valid(sameListMoveCommand);
            when(cardValidator.validateMove(sameListMoveCommand)).thenReturn(validResult);
            when(cardRepository.findById(sameListMoveCommand.cardId())).thenReturn(Optional.empty());

            // when
            Either<Failure, Card> result = updateCardService.moveCard(sameListMoveCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
            assertThat(((Failure.NotFound) result.getLeft()).getErrorCode()).isEqualTo("NOT_FOUND");
        }

        @Test
        @DisplayName("원본 리스트가 존재하지 않을 때 실패를 반환한다")
        void shouldReturnFailureWhenSourceListNotFound() {
            // given
            ValidationResult<MoveCardCommand> validResult = ValidationResult.valid(sameListMoveCommand);
            when(cardValidator.validateMove(sameListMoveCommand)).thenReturn(validResult);
            when(cardRepository.findById(sameListMoveCommand.cardId()))
                    .thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(existingCard.getListId())).thenReturn(Optional.empty());

            // when
            Either<Failure, Card> result = updateCardService.moveCard(sameListMoveCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
            assertThat(((Failure.NotFound) result.getLeft()).getErrorCode()).isEqualTo("NOT_FOUND");
        }

        @Test
        @DisplayName("원본 보드 접근 권한이 없을 때 실패를 반환한다")
        void shouldReturnFailureWhenSourceBoardAccessDenied() {
            // given
            ValidationResult<MoveCardCommand> validResult = ValidationResult.valid(sameListMoveCommand);
            when(cardValidator.validateMove(sameListMoveCommand)).thenReturn(validResult);
            when(cardRepository.findById(sameListMoveCommand.cardId()))
                    .thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(existingCard.getListId()))
                    .thenReturn(Optional.of(sourceBoardList));
            when(boardRepository.findByIdAndOwnerId(sourceBoardList.getBoardId(),
                    sameListMoveCommand.userId()))
                    .thenReturn(Optional.empty());

            // when
            Either<Failure, Card> result = updateCardService.moveCard(sameListMoveCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
            assertThat(((Failure.PermissionDenied) result.getLeft()).getErrorCode())
                    .isEqualTo("PERMISSION_DENIED");
        }

        @Test
        @DisplayName("아카이브된 보드에서 카드 이동 시도 시 실패를 반환한다")
        void shouldReturnFailureWhenSourceBoardIsArchived() {
            // given
            Board archivedBoard = Board.builder()
                    .boardId(board.getBoardId())
                    .title("아카이브된 보드")
                    .description("아카이브된 보드 설명")
                    .ownerId(sameListMoveCommand.userId())
                    .isArchived(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            ValidationResult<MoveCardCommand> validResult = ValidationResult.valid(sameListMoveCommand);
            when(cardValidator.validateMove(sameListMoveCommand)).thenReturn(validResult);
            when(cardRepository.findById(sameListMoveCommand.cardId()))
                    .thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(existingCard.getListId()))
                    .thenReturn(Optional.of(sourceBoardList));
            when(boardRepository.findByIdAndOwnerId(sourceBoardList.getBoardId(),
                    sameListMoveCommand.userId()))
                    .thenReturn(Optional.of(archivedBoard));

            // when
            Either<Failure, Card> result = updateCardService.moveCard(sameListMoveCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.ResourceConflict.class);
            assertThat(((Failure.ResourceConflict) result.getLeft()).getErrorCode())
                    .isEqualTo("RESOURCE_CONFLICT");
        }

        @Test
        @DisplayName("대상 리스트가 존재하지 않을 때 실패를 반환한다")
        void shouldReturnFailureWhenTargetListNotFound() {
            // given
            ValidationResult<MoveCardCommand> validResult = ValidationResult
                    .valid(differentListMoveCommand);
            when(cardValidator.validateMove(differentListMoveCommand)).thenReturn(validResult);
            when(cardRepository.findById(differentListMoveCommand.cardId()))
                    .thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(existingCard.getListId()))
                    .thenReturn(Optional.of(sourceBoardList));
            when(boardRepository.findByIdAndOwnerId(sourceBoardList.getBoardId(),
                    differentListMoveCommand.userId()))
                    .thenReturn(Optional.of(board));
            when(boardListRepository.findById(differentListMoveCommand.targetListId()))
                    .thenReturn(Optional.empty());

            // when
            Either<Failure, Card> result = updateCardService.moveCard(differentListMoveCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);
            assertThat(((Failure.NotFound) result.getLeft()).getErrorCode()).isEqualTo("NOT_FOUND");
        }

        @Test
        @DisplayName("대상 보드 접근 권한이 없을 때 실패를 반환한다")
        void shouldReturnFailureWhenTargetBoardAccessDenied() {
            // given
            ValidationResult<MoveCardCommand> validResult = ValidationResult
                    .valid(differentListMoveCommand);
            when(cardValidator.validateMove(differentListMoveCommand)).thenReturn(validResult);
            when(cardRepository.findById(differentListMoveCommand.cardId()))
                    .thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(existingCard.getListId()))
                    .thenReturn(Optional.of(sourceBoardList));
            when(boardRepository.findByIdAndOwnerId(sourceBoardList.getBoardId(),
                    differentListMoveCommand.userId()))
                    .thenReturn(Optional.of(board));
            when(boardRepository.findByIdAndOwnerId(targetBoardList.getBoardId(),
                    differentListMoveCommand.userId()))
                    .thenReturn(Optional.empty());

            // when
            Either<Failure, Card> result = updateCardService.moveCard(differentListMoveCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.PermissionDenied.class);
            assertThat(((Failure.PermissionDenied) result.getLeft()).getErrorCode())
                    .isEqualTo("PERMISSION_DENIED");
            verify(cardValidator).validateMove(differentListMoveCommand);
            verify(cardRepository).findById(differentListMoveCommand.cardId());
            verify(boardListRepository, org.mockito.Mockito.atLeastOnce()).findById(any());
            verify(boardRepository, org.mockito.Mockito.atLeastOnce()).findByIdAndOwnerId(any(), any());
        }

        @Test
        @DisplayName("같은 리스트 내 이동 정책 검증 실패 시 실패를 반환한다")
        void shouldReturnFailureWhenSameListMovePolicyFails() {
            // given
            ValidationResult<MoveCardCommand> validResult = ValidationResult.valid(sameListMoveCommand);
            when(cardValidator.validateMove(sameListMoveCommand)).thenReturn(validResult);
            when(cardRepository.findById(sameListMoveCommand.cardId()))
                    .thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(existingCard.getListId()))
                    .thenReturn(Optional.of(sourceBoardList));
            when(boardRepository.findByIdAndOwnerId(sourceBoardList.getBoardId(),
                    sameListMoveCommand.userId()))
                    .thenReturn(Optional.of(board));
            when(cardMovePolicy.canMoveWithinSameList(existingCard, sameListMoveCommand.newPosition()))
                    .thenReturn(Either.left(Failure.ofConflict("이동할 수 없는 위치입니다.")));

            // when
            Either<Failure, Card> result = updateCardService.moveCard(sameListMoveCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.ResourceConflict.class);
            assertThat(((Failure.ResourceConflict) result.getLeft()).getErrorCode())
                    .isEqualTo("RESOURCE_CONFLICT");
        }

        @Test
        @DisplayName("다른 리스트로 이동 정책 검증 실패 시 실패를 반환한다")
        void shouldReturnFailureWhenDifferentListMovePolicyFails() {
            // given
            ValidationResult<MoveCardCommand> validResult = ValidationResult
                    .valid(differentListMoveCommand);
            when(cardValidator.validateMove(differentListMoveCommand)).thenReturn(validResult);
            when(cardRepository.findById(differentListMoveCommand.cardId()))
                    .thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(existingCard.getListId()))
                    .thenReturn(Optional.of(sourceBoardList));
            when(boardRepository.findByIdAndOwnerId(sourceBoardList.getBoardId(),
                    differentListMoveCommand.userId()))
                    .thenReturn(Optional.of(board));
            when(boardListRepository.findById(differentListMoveCommand.targetListId()))
                    .thenReturn(Optional.of(targetBoardList));
            when(boardRepository.findByIdAndOwnerId(targetBoardList.getBoardId(),
                    differentListMoveCommand.userId()))
                    .thenReturn(Optional.of(board));
            when(cardMovePolicy.canMoveToAnotherList(existingCard, differentListMoveCommand.targetListId(),
                    differentListMoveCommand.newPosition()))
                    .thenReturn(Either.left(Failure.ofConflict("대상 리스트의 카드 개수 제한을 초과했습니다.")));

            // when
            Either<Failure, Card> result = updateCardService.moveCard(differentListMoveCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.ResourceConflict.class);
            assertThat(failure.getMessage()).isEqualTo("대상 리스트의 카드 개수 제한을 초과했습니다.");
            verify(cardValidator).validateMove(differentListMoveCommand);
            verify(cardRepository).findById(differentListMoveCommand.cardId());
            verify(boardListRepository, org.mockito.Mockito.atLeastOnce()).findById(any());
            verify(boardRepository, org.mockito.Mockito.atLeastOnce()).findByIdAndOwnerId(any(), any());
            verify(cardMovePolicy).canMoveToAnotherList(existingCard,
                    differentListMoveCommand.targetListId(),
                    differentListMoveCommand.newPosition());
        }

        @Test
        @DisplayName("같은 위치로 이동 시 위치 변경 없이 성공한다")
        void shouldSucceedWhenMovingToSamePosition() {
            // given
            MoveCardCommand samePositionCommand = MoveCardCommand.of(existingCard.getCardId(), null,
                    existingCard.getPosition(), sameListMoveCommand.userId());
            ValidationResult<MoveCardCommand> validResult = ValidationResult.valid(samePositionCommand);
            when(cardValidator.validateMove(samePositionCommand)).thenReturn(validResult);
            when(cardRepository.findById(samePositionCommand.cardId()))
                    .thenReturn(Optional.of(existingCard));
            when(boardListRepository.findById(existingCard.getListId()))
                    .thenReturn(Optional.of(sourceBoardList));
            when(boardRepository.findByIdAndOwnerId(sourceBoardList.getBoardId(),
                    samePositionCommand.userId()))
                    .thenReturn(Optional.of(board));
            when(cardMovePolicy.canMoveWithinSameList(existingCard, samePositionCommand.newPosition()))
                    .thenReturn(Either.right(null));

            // when
            Either<Failure, Card> result = updateCardService.moveCard(samePositionCommand);

            // then
            assertThat(result.isRight()).isTrue();
            assertThat(result.get()).isEqualTo(existingCard);
            verify(cardValidator).validateMove(samePositionCommand);
            verify(cardRepository).findById(samePositionCommand.cardId());
            verify(boardListRepository).findById(existingCard.getListId());
            verify(boardRepository).findByIdAndOwnerId(sourceBoardList.getBoardId(),
                    samePositionCommand.userId());
            verify(cardMovePolicy).canMoveWithinSameList(existingCard, samePositionCommand.newPosition());
            // 같은 위치로 이동할 때는 저장하지 않음
            verify(cardRepository, org.mockito.Mockito.never()).save(any(Card.class));
        }
    }
}
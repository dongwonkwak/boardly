package com.boardly.features.card.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.activity.domain.model.ActivityType;
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

        @Mock
        private ActivityHelper activityHelper;

        private UpdateCardService updateCardService;

        @BeforeEach
        void setUp() {
                updateCardService = new UpdateCardService(
                                cardValidator,
                                cardMovePolicy,
                                cardRepository,
                                boardListRepository,
                                boardRepository,
                                validationMessageResolver,
                                activityHelper);

                // 공통으로 사용되는 메시지 설정
                lenient().when(validationMessageResolver.getMessage("validation.input.invalid"))
                                .thenReturn("validation.input.invalid");
                lenient().when(validationMessageResolver.getMessage("error.service.card.update.not_found"))
                                .thenReturn("error.service.card.update.not_found");
                lenient().when(validationMessageResolver.getMessage("error.service.card.update.list_not_found"))
                                .thenReturn("error.service.card.update.list_not_found");
                lenient().when(validationMessageResolver.getMessage("error.service.card.update.access_denied"))
                                .thenReturn("error.service.card.update.access_denied");
                lenient().when(validationMessageResolver.getMessage("error.service.card.update.archived_board"))
                                .thenReturn("error.service.card.update.archived_board");
                lenient().when(validationMessageResolver.getMessage("error.service.card.update.error"))
                                .thenReturn("error.service.card.update.error");
                lenient().when(validationMessageResolver.getMessage("error.service.card.move.not_found"))
                                .thenReturn("error.service.card.move.not_found");
                lenient().when(validationMessageResolver.getMessage("error.service.card.move.list_not_found"))
                                .thenReturn("error.service.card.move.list_not_found");
                lenient().when(validationMessageResolver.getMessage("error.service.card.move.access_denied"))
                                .thenReturn("error.service.card.move.access_denied");
                lenient().when(validationMessageResolver.getMessage("error.service.card.move.archived_board"))
                                .thenReturn("error.service.card.move.archived_board");
                lenient().when(validationMessageResolver.getMessage("error.service.card.move.target_list_not_found"))
                                .thenReturn("error.service.card.move.target_list_not_found");
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
                        verify(boardListRepository, times(4)).findById(existingCard.getListId());
                        verify(boardRepository).findByIdAndOwnerId(boardList.getBoardId(), validCommand.userId());
                        verify(boardRepository, times(2)).findById(boardList.getBoardId());
                        verify(cardRepository).save(existingCard);
                        verify(activityHelper).logCardActivity(
                                        eq(ActivityType.CARD_RENAME),
                                        eq(validCommand.userId()),
                                        any(Map.class),
                                        any(String.class), // boardName
                                        eq(board.getBoardId()),
                                        eq(existingCard.getListId()),
                                        eq(existingCard.getCardId()));

                }

                @Test
                @DisplayName("입력 검증 실패 시 실패를 반환한다")
                void shouldReturnFailureWhenValidationFails() {
                        // given
                        List<Failure.FieldViolation> validationErrors = List.of(
                                        Failure.FieldViolation.builder()
                                                        .field("title")
                                                        .message("validation.card.title.required")
                                                        .rejectedValue(null)
                                                        .build(),
                                        Failure.FieldViolation.builder()
                                                        .field("description")
                                                        .message("validation.card.description.too_long")
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
                        verify(activityHelper, never()).logCardActivity(any(), any(), any(), any(), any(), any(),
                                        any());
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
                        verify(activityHelper, never()).logCardActivity(any(), any(), any(), any(), any(), any(),
                                        any());
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
                        verify(activityHelper, never()).logCardActivity(any(), any(), any(), any(), any(), any(),
                                        any());
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
                        verify(activityHelper, never()).logCardActivity(any(), any(), any(), any(), any(), any(),
                                        any());
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
                        verify(activityHelper, never()).logCardActivity(any(), any(), any(), any(), any(), any(),
                                        any());
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
                        when(boardRepository.findById(boardList.getBoardId())).thenReturn(Optional.of(board));
                        when(cardRepository.save(any(Card.class)))
                                        .thenReturn(Either.left(Failure
                                                        .ofInternalServerError("error.service.card.save.failed")));

                        // when
                        Either<Failure, Card> result = updateCardService.updateCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
                        assertThat(((Failure.InternalError) result.getLeft()).getErrorCode())
                                        .isEqualTo("INTERNAL_ERROR");
                        verify(activityHelper, never()).logCardActivity(any(), any(), any(), any(), any(), any(),
                                        any());
                }

                @Test
                @DisplayName("설명이 null인 경우에도 성공한다")
                void shouldUpdateCardSuccessfullyWithNullDescription() {
                        // given
                        UpdateCardCommand commandWithNullDescription = UpdateCardCommand.of(
                                        validCommand.cardId(), "새로운 제목", null, validCommand.userId());
                        ValidationResult<UpdateCardCommand> validResult = ValidationResult
                                        .valid(commandWithNullDescription);
                        when(cardValidator.validateUpdate(commandWithNullDescription)).thenReturn(validResult);
                        when(cardRepository.findById(commandWithNullDescription.cardId()))
                                        .thenReturn(Optional.of(existingCard));
                        when(boardListRepository.findById(existingCard.getListId())).thenReturn(Optional.of(boardList));
                        when(boardRepository.findByIdAndOwnerId(boardList.getBoardId(),
                                        commandWithNullDescription.userId()))
                                        .thenReturn(Optional.of(board));
                        when(boardRepository.findById(boardList.getBoardId())).thenReturn(Optional.of(board));
                        when(cardRepository.save(any(Card.class))).thenReturn(Either.right(existingCard));

                        // when
                        Either<Failure, Card> result = updateCardService.updateCard(commandWithNullDescription);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(result.get()).isEqualTo(existingCard);
                        verify(activityHelper).logCardActivity(
                                        eq(ActivityType.CARD_RENAME),
                                        eq(commandWithNullDescription.userId()),
                                        any(Map.class),
                                        any(String.class), // boardName
                                        eq(board.getBoardId()),
                                        eq(existingCard.getListId()),
                                        eq(existingCard.getCardId()));
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
                        when(cardRepository.findByListIdAndPositionBetween(any(), anyInt(), anyInt()))
                                        .thenReturn(List.of());
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
                        // 같은 리스트 내 이동은 활동 로그에 기록하지 않음
                        verify(activityHelper, never()).logCardMove(any(), any(), any(), any(), any(), any(), any(),
                                        any(), any());
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
                        when(cardRepository.findByListIdAndPositionGreaterThan(any(), anyInt()))
                                        .thenReturn(List.of());
                        when(cardRepository.save(existingCard)).thenReturn(Either.right(existingCard));
                        // 활동 로그에서 사용하는 보드 조회를 위한 모킹
                        when(boardRepository.findById(targetBoardList.getBoardId()))
                                        .thenReturn(Optional.of(board));

                        // when
                        Either<Failure, Card> result = updateCardService.moveCard(differentListMoveCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(result.get()).isEqualTo(existingCard);
                        verify(cardValidator).validateMove(differentListMoveCommand);
                        verify(cardRepository).findById(differentListMoveCommand.cardId());
                        verify(boardListRepository, times(7)).findById(any());
                        verify(boardRepository, times(2)).findByIdAndOwnerId(any(), any());
                        verify(boardRepository, times(1)).findById(any());
                        verify(cardMovePolicy).canMoveToAnotherList(existingCard,
                                        differentListMoveCommand.targetListId(),
                                        differentListMoveCommand.newPosition());
                        verify(cardRepository).save(existingCard);
                        verify(activityHelper).logCardMove(
                                        eq(differentListMoveCommand.userId()),
                                        eq(existingCard.getTitle()),
                                        eq(sourceBoardList.getTitle()),
                                        eq(targetBoardList.getTitle()),
                                        any(String.class), // boardName
                                        eq(targetBoardList.getBoardId()),
                                        eq(sourceBoardList.getListId()),
                                        eq(targetBoardList.getListId()),
                                        eq(existingCard.getCardId()));
                }

                @Test
                @DisplayName("입력 검증 실패 시 실패를 반환한다")
                void shouldReturnFailureWhenValidationFails() {
                        // given
                        List<Failure.FieldViolation> validationErrors = List.of(
                                        Failure.FieldViolation.builder()
                                                        .field("newPosition")
                                                        .message("validation.card.position.invalid")
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
                        verify(activityHelper, never()).logCardMove(any(), any(), any(), any(), any(), any(), any(),
                                        any(), any());
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
                        verify(activityHelper, never()).logCardMove(any(), any(), any(), any(), any(), any(), any(),
                                        any(), any());
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
                        verify(activityHelper, never()).logCardMove(any(), any(), any(), any(), any(), any(), any(),
                                        any(), any());
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
                        verify(activityHelper, never()).logCardMove(any(), any(), any(), any(), any(), any(), any(),
                                        any(), any());
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
                        verify(activityHelper, never()).logCardMove(any(), any(), any(), any(), any(), any(), any(),
                                        any(), any());
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
                        verify(activityHelper, never()).logCardMove(any(), any(), any(), any(), any(), any(), any(),
                                        any(), any());
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
                        verify(activityHelper, never()).logCardMove(any(), any(), any(), any(), any(), any(), any(),
                                        any(), any());
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
                                        .thenReturn(Either.left(Failure
                                                        .ofConflict("error.service.card.move.invalid_position")));

                        // when
                        Either<Failure, Card> result = updateCardService.moveCard(sameListMoveCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isInstanceOf(Failure.ResourceConflict.class);
                        assertThat(((Failure.ResourceConflict) result.getLeft()).getErrorCode())
                                        .isEqualTo("RESOURCE_CONFLICT");
                        verify(activityHelper, never()).logCardMove(any(), any(), any(), any(), any(), any(), any(),
                                        any(), any());
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
                                        .thenReturn(Either.left(Failure
                                                        .ofConflict("error.service.card.move.list_limit_exceeded")));

                        // when
                        Either<Failure, Card> result = updateCardService.moveCard(differentListMoveCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.ResourceConflict.class);
                        assertThat(failure.getMessage()).isEqualTo("error.service.card.move.list_limit_exceeded");
                        verify(activityHelper, never()).logCardMove(any(), any(), any(), any(), any(), any(), any(),
                                        any(), any());
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
                        verify(cardRepository, never()).save(any(Card.class));
                        verify(activityHelper, never()).logCardMove(any(), any(), any(), any(), any(), any(), any(),
                                        any(), any());
                }

                @Test
                @DisplayName("같은 리스트 내에서 다른 카드들의 위치 조정이 성공한다")
                void shouldAdjustOtherCardsPositionsWithinSameList() {
                        // given
                        Card otherCard1 = Card.builder()
                                        .cardId(new CardId("card-2"))
                                        .title("다른 카드 1")
                                        .position(1)
                                        .listId(existingCard.getListId())
                                        .build();
                        Card otherCard2 = Card.builder()
                                        .cardId(new CardId("card-3"))
                                        .title("다른 카드 2")
                                        .position(2)
                                        .listId(existingCard.getListId())
                                        .build();

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
                        when(cardRepository.findByListIdAndPositionBetween(existingCard.getListId(), 1, 2))
                                        .thenReturn(List.of(otherCard1, otherCard2));
                        when(cardRepository.saveAll(anyList())).thenReturn(List.of(otherCard1, otherCard2));
                        when(cardRepository.save(existingCard)).thenReturn(Either.right(existingCard));

                        // when
                        Either<Failure, Card> result = updateCardService.moveCard(sameListMoveCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        verify(cardRepository).findByListIdAndPositionBetween(existingCard.getListId(), 1, 2);
                        verify(cardRepository).saveAll(List.of(otherCard1, otherCard2));
                        verify(cardRepository).save(existingCard);
                }

                @Test
                @DisplayName("다른 리스트로 이동 시 위치 조정이 성공한다")
                void shouldAdjustPositionsWhenMovingToAnotherList() {
                        // given
                        Card otherCard1 = Card.builder()
                                        .cardId(new CardId("card-2"))
                                        .title("다른 카드 1")
                                        .position(1)
                                        .listId(existingCard.getListId())
                                        .build();
                        Card otherCard2 = Card.builder()
                                        .cardId(new CardId("card-3"))
                                        .title("다른 카드 2")
                                        .position(1)
                                        .listId(targetBoardList.getListId())
                                        .build();

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
                        when(cardRepository.findByListIdAndPositionGreaterThan(existingCard.getListId(), 0))
                                        .thenReturn(List.of(otherCard1));
                        when(cardRepository.findByListIdAndPositionGreaterThan(targetBoardList.getListId(), 0))
                                        .thenReturn(List.of(otherCard2));
                        when(cardRepository.saveAll(anyList())).thenReturn(List.of(otherCard1, otherCard2));
                        when(cardRepository.save(existingCard)).thenReturn(Either.right(existingCard));

                        // when
                        Either<Failure, Card> result = updateCardService.moveCard(differentListMoveCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        verify(cardRepository).findByListIdAndPositionGreaterThan(existingCard.getListId(), 0);
                        verify(cardRepository).findByListIdAndPositionGreaterThan(targetBoardList.getListId(), 0);
                        verify(cardRepository, times(2)).saveAll(anyList());
                        verify(cardRepository).save(existingCard);
                }
        }
}
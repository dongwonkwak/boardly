package com.boardly.features.card.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.application.port.input.DeleteCardCommand;
import com.boardly.features.card.application.validation.CardValidator;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteCardService 테스트")
class DeleteCardServiceTest {

        @Mock
        private CardValidator cardValidator;

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

        private DeleteCardService deleteCardService;

        @BeforeEach
        void setUp() {
                deleteCardService = new DeleteCardService(
                                cardValidator,
                                cardRepository,
                                boardListRepository,
                                boardRepository,
                                validationMessageResolver,
                                activityHelper);

                // 공통으로 사용되는 메시지 설정
                lenient().when(validationMessageResolver.getMessage("validation.input.invalid"))
                                .thenReturn("입력값이 유효하지 않습니다.");
                lenient().when(validationMessageResolver.getMessage("error.service.card.delete.not_found"))
                                .thenReturn("삭제할 카드를 찾을 수 없습니다.");
                lenient().when(validationMessageResolver.getMessage("error.service.card.delete.list_not_found"))
                                .thenReturn("리스트를 찾을 수 없습니다.");
                lenient().when(validationMessageResolver.getMessage("error.service.card.delete.access_denied"))
                                .thenReturn("보드 접근 권한이 없습니다.");
                lenient().when(validationMessageResolver.getMessage("error.service.card.delete.archived_board"))
                                .thenReturn("아카이브된 보드에서는 카드를 삭제할 수 없습니다.");
        }

        @Nested
        @DisplayName("deleteCard 메서드 테스트")
        class DeleteCardTest {

                private DeleteCardCommand validCommand;
                private Card cardToDelete;
                private BoardList boardList;
                private Board board;
                private UserId userId;
                private CardId cardId;
                private ListId listId;
                private BoardId boardId;

                @BeforeEach
                void setUp() {
                        userId = new UserId("user-123");
                        cardId = new CardId("card-123");
                        listId = new ListId("list-123");
                        boardId = new BoardId("board-123");

                        validCommand = new DeleteCardCommand(cardId, userId);

                        cardToDelete = Card.builder()
                                        .cardId(cardId)
                                        .title("테스트 카드")
                                        .description("테스트 설명")
                                        .listId(listId)
                                        .position(2)
                                        .build();

                        boardList = BoardList.builder()
                                        .listId(listId)
                                        .title("테스트 리스트")
                                        .boardId(boardId)
                                        .position(1)
                                        .build();

                        board = Board.builder()
                                        .boardId(boardId)
                                        .title("테스트 보드")
                                        .ownerId(userId)
                                        .isArchived(false)
                                        .build();
                }

                @Test
                @DisplayName("유효한 커맨드로 카드 삭제 시 성공한다")
                void shouldDeleteCardSuccessfully() {
                        // given
                        when(cardValidator.validateDelete(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId))
                                        .thenReturn(Optional.of(cardToDelete));
                        when(boardListRepository.findById(listId))
                                        .thenReturn(Optional.of(boardList));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                        .thenReturn(Optional.of(board));
                        when(cardRepository.delete(cardId))
                                        .thenReturn(Either.right(null));
                        when(cardRepository.findByListIdAndPositionGreaterThan(listId, 2))
                                        .thenReturn(List.of());
                        when(boardRepository.findById(boardId))
                                        .thenReturn(Optional.of(board));

                        // when
                        Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(result.get()).isNull();

                        verify(cardValidator).validateDelete(validCommand);
                        verify(cardRepository).findById(cardId);
                        verify(boardListRepository, times(2)).findById(listId); // 권한 검증과 활동 로그 기록에서 각각 호출
                        verify(boardRepository).findByIdAndOwnerId(boardId, userId);
                        verify(cardRepository).delete(cardId);
                        verify(cardRepository).findByListIdAndPositionGreaterThan(listId, 2);
                        verify(boardRepository).findById(boardId); // 활동 로그 기록에서 호출
                        verify(activityHelper).logCardActivity(
                                        eq(ActivityType.CARD_DELETE),
                                        eq(userId),
                                        any(Map.class),
                                        eq("테스트 보드"),
                                        eq(boardId),
                                        eq(listId),
                                        eq(cardId));
                }

                @Test
                @DisplayName("입력 검증 실패 시 실패를 반환한다")
                void shouldReturnFailureWhenValidationFails() {
                        // given
                        var validationErrors = List.of(
                                        Failure.FieldViolation.builder()
                                                        .field("cardId")
                                                        .message("카드 ID가 유효하지 않습니다.")
                                                        .rejectedValue("invalid-id")
                                                        .build());
                        ValidationResult<DeleteCardCommand> invalidResult = ValidationResult.invalid(
                                        io.vavr.collection.List.ofAll(validationErrors));
                        when(cardValidator.validateDelete(validCommand))
                                        .thenReturn(invalidResult);

                        // when
                        Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.InputError.class);
                        Failure.InputError inputError = (Failure.InputError) failure;
                        assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
                        assertThat(inputError.getMessage()).isEqualTo("입력값이 유효하지 않습니다.");
                        assertThat(inputError.getViolations()).containsExactlyElementsOf(validationErrors);

                        verify(cardValidator).validateDelete(validCommand);
                        verifyNoInteractions(cardRepository, boardListRepository, boardRepository, activityHelper);
                }

                @Test
                @DisplayName("카드가 존재하지 않을 때 실패를 반환한다")
                void shouldReturnFailureWhenCardNotFound() {
                        // given
                        when(cardValidator.validateDelete(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId))
                                        .thenReturn(Optional.empty());

                        // when
                        Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.NotFound.class);
                        Failure.NotFound notFound = (Failure.NotFound) failure;
                        assertThat(notFound.getErrorCode()).isEqualTo("NOT_FOUND");
                        assertThat(notFound.getMessage()).isEqualTo("삭제할 카드를 찾을 수 없습니다.");

                        verify(cardValidator).validateDelete(validCommand);
                        verify(cardRepository).findById(cardId);
                        verifyNoInteractions(boardListRepository, boardRepository, activityHelper);
                }

                @Test
                @DisplayName("리스트가 존재하지 않을 때 실패를 반환한다")
                void shouldReturnFailureWhenListNotFound() {
                        // given
                        when(cardValidator.validateDelete(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId))
                                        .thenReturn(Optional.of(cardToDelete));
                        when(boardListRepository.findById(listId))
                                        .thenReturn(Optional.empty());

                        // when
                        Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.NotFound.class);
                        Failure.NotFound notFound = (Failure.NotFound) failure;
                        assertThat(notFound.getErrorCode()).isEqualTo("NOT_FOUND");
                        assertThat(notFound.getMessage()).isEqualTo("리스트를 찾을 수 없습니다.");

                        verify(cardValidator).validateDelete(validCommand);
                        verify(cardRepository).findById(cardId);
                        verify(boardListRepository).findById(listId);
                        verifyNoInteractions(boardRepository, activityHelper);
                }

                @Test
                @DisplayName("보드 접근 권한이 없을 때 실패를 반환한다")
                void shouldReturnFailureWhenBoardAccessDenied() {
                        // given
                        when(cardValidator.validateDelete(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId))
                                        .thenReturn(Optional.of(cardToDelete));
                        when(boardListRepository.findById(listId))
                                        .thenReturn(Optional.of(boardList));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                        .thenReturn(Optional.empty());

                        // when
                        Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.PermissionDenied.class);
                        Failure.PermissionDenied permissionDenied = (Failure.PermissionDenied) failure;
                        assertThat(permissionDenied.getErrorCode()).isEqualTo("PERMISSION_DENIED");
                        assertThat(permissionDenied.getMessage()).isEqualTo("보드 접근 권한이 없습니다.");

                        verify(cardValidator).validateDelete(validCommand);
                        verify(cardRepository).findById(cardId);
                        verify(boardListRepository).findById(listId);
                        verify(boardRepository).findByIdAndOwnerId(boardId, userId);
                        verifyNoInteractions(activityHelper);
                }

                @Test
                @DisplayName("아카이브된 보드에서 카드 삭제 시도 시 실패를 반환한다")
                void shouldReturnFailureWhenBoardIsArchived() {
                        // given
                        Board archivedBoard = Board.builder()
                                        .boardId(boardId)
                                        .title("아카이브된 보드")
                                        .ownerId(userId)
                                        .isArchived(true)
                                        .build();

                        when(cardValidator.validateDelete(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId))
                                        .thenReturn(Optional.of(cardToDelete));
                        when(boardListRepository.findById(listId))
                                        .thenReturn(Optional.of(boardList));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                        .thenReturn(Optional.of(archivedBoard));

                        // when
                        Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.BusinessRuleViolation.class);
                        Failure.BusinessRuleViolation businessRuleViolation = (Failure.BusinessRuleViolation) failure;
                        assertThat(businessRuleViolation.getErrorCode()).isEqualTo("BUSINESS_RULE_VIOLATION");
                        assertThat(businessRuleViolation.getMessage()).isEqualTo("아카이브된 보드에서는 카드를 삭제할 수 없습니다.");

                        verify(cardValidator).validateDelete(validCommand);
                        verify(cardRepository).findById(cardId);
                        verify(boardListRepository).findById(listId);
                        verify(boardRepository).findByIdAndOwnerId(boardId, userId);
                        verifyNoInteractions(activityHelper);
                }

                @Test
                @DisplayName("카드 삭제 실패 시 실패를 반환한다")
                void shouldReturnFailureWhenCardDeletionFails() {
                        // given
                        Failure deletionFailure = Failure.ofInternalError("카드 삭제 중 오류가 발생했습니다.", "INTERNAL_ERROR",
                                        null);

                        when(cardValidator.validateDelete(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId))
                                        .thenReturn(Optional.of(cardToDelete));
                        when(boardListRepository.findById(listId))
                                        .thenReturn(Optional.of(boardList));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                        .thenReturn(Optional.of(board));
                        when(cardRepository.delete(cardId))
                                        .thenReturn(Either.left(deletionFailure));

                        // when
                        Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        assertThat(result.getLeft()).isEqualTo(deletionFailure);

                        verify(cardValidator).validateDelete(validCommand);
                        verify(cardRepository).findById(cardId);
                        verify(boardListRepository).findById(listId);
                        verify(boardRepository).findByIdAndOwnerId(boardId, userId);
                        verify(cardRepository).delete(cardId);
                        verifyNoInteractions(activityHelper);
                }

                @Test
                @DisplayName("카드 삭제 후 나머지 카드들의 위치를 재정렬한다")
                void shouldReorderRemainingCardsAfterDeletion() {
                        // given
                        Card remainingCard1 = Card.builder()
                                        .cardId(new CardId("card-456"))
                                        .title("남은 카드 1")
                                        .listId(listId)
                                        .position(3)
                                        .build();

                        Card remainingCard2 = Card.builder()
                                        .cardId(new CardId("card-789"))
                                        .title("남은 카드 2")
                                        .listId(listId)
                                        .position(4)
                                        .build();

                        List<Card> cardsToReorder = List.of(remainingCard1, remainingCard2);

                        when(cardValidator.validateDelete(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId))
                                        .thenReturn(Optional.of(cardToDelete));
                        when(boardListRepository.findById(listId))
                                        .thenReturn(Optional.of(boardList));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                        .thenReturn(Optional.of(board));
                        when(cardRepository.delete(cardId))
                                        .thenReturn(Either.right(null));
                        when(cardRepository.findByListIdAndPositionGreaterThan(listId, 2))
                                        .thenReturn(cardsToReorder);
                        when(boardRepository.findById(boardId))
                                        .thenReturn(Optional.of(board));

                        // when
                        Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();

                        verify(cardRepository).findByListIdAndPositionGreaterThan(listId, 2);
                        verify(cardRepository).saveAll(cardsToReorder);

                        // 위치가 재정렬되었는지 확인
                        assertThat(remainingCard1.getPosition()).isEqualTo(2);
                        assertThat(remainingCard2.getPosition()).isEqualTo(3);
                }

                @Test
                @DisplayName("활동 로그 기록 시 보드 정보를 찾을 수 없어도 삭제는 성공한다")
                void shouldSucceedEvenWhenBoardInfoNotFoundForActivityLog() {
                        // given
                        when(cardValidator.validateDelete(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId))
                                        .thenReturn(Optional.of(cardToDelete));
                        when(boardListRepository.findById(listId))
                                        .thenReturn(Optional.of(boardList));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                        .thenReturn(Optional.of(board));
                        when(cardRepository.delete(cardId))
                                        .thenReturn(Either.right(null));
                        when(cardRepository.findByListIdAndPositionGreaterThan(listId, 2))
                                        .thenReturn(List.of());
                        when(boardRepository.findById(boardId))
                                        .thenReturn(Optional.empty());

                        // when
                        Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();

                        verify(activityHelper).logCardActivity(
                                        eq(ActivityType.CARD_DELETE),
                                        eq(userId),
                                        any(Map.class),
                                        eq("알 수 없는 보드"),
                                        eq(boardId),
                                        eq(listId),
                                        eq(cardId));
                }

                @Test
                @DisplayName("활동 로그 기록 중 예외가 발생해도 삭제는 성공한다")
                void shouldSucceedEvenWhenActivityLogThrowsException() {
                        // given
                        when(cardValidator.validateDelete(validCommand))
                                        .thenReturn(ValidationResult.valid(validCommand));
                        when(cardRepository.findById(cardId))
                                        .thenReturn(Optional.of(cardToDelete));
                        when(boardListRepository.findById(listId))
                                        .thenReturn(Optional.of(boardList));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                        .thenReturn(Optional.of(board));
                        when(cardRepository.delete(cardId))
                                        .thenReturn(Either.right(null));
                        when(cardRepository.findByListIdAndPositionGreaterThan(listId, 2))
                                        .thenReturn(List.of());
                        when(boardRepository.findById(boardId))
                                        .thenReturn(Optional.of(board));
                        lenient().doThrow(new RuntimeException("활동 로그 기록 실패"))
                                        .when(activityHelper)
                                        .logCardActivity(any(), any(), any(), any(), any(), any(), any());

                        // when
                        Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                }
        }

        @Nested
        @DisplayName("공통 유틸리티 메서드 테스트")
        class UtilityMethodTest {

                private UserId userId;
                private ListId listId;
                private BoardId boardId;
                private BoardList boardList;

                @BeforeEach
                void setUp() {
                        userId = new UserId("user-123");
                        listId = new ListId("list-123");
                        boardId = new BoardId("board-123");

                        boardList = BoardList.builder()
                                        .listId(listId)
                                        .title("테스트 리스트")
                                        .boardId(boardId)
                                        .position(1)
                                        .build();
                }

                @Test
                @DisplayName("리스트가 존재하지 않을 때 실패를 반환한다")
                void shouldReturnFailureWhenListNotFound() {
                        // given
                        DeleteCardCommand command = new DeleteCardCommand(new CardId("card-123"), userId);
                        Card card = Card.builder()
                                        .cardId(new CardId("card-123"))
                                        .title("테스트 카드")
                                        .listId(listId)
                                        .position(1)
                                        .build();

                        when(cardValidator.validateDelete(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(cardRepository.findById(new CardId("card-123")))
                                        .thenReturn(Optional.of(card));
                        when(boardListRepository.findById(listId))
                                        .thenReturn(Optional.empty());

                        // when
                        Either<Failure, Void> result = deleteCardService.deleteCard(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.NotFound.class);
                        Failure.NotFound notFound = (Failure.NotFound) failure;
                        assertThat(notFound.getErrorCode()).isEqualTo("NOT_FOUND");
                        assertThat(notFound.getMessage()).isEqualTo("리스트를 찾을 수 없습니다.");
                }

                @Test
                @DisplayName("보드 접근 권한이 없을 때 실패를 반환한다")
                void shouldReturnFailureWhenBoardAccessDenied() {
                        // given
                        DeleteCardCommand command = new DeleteCardCommand(new CardId("card-123"), userId);
                        Card card = Card.builder()
                                        .cardId(new CardId("card-123"))
                                        .title("테스트 카드")
                                        .listId(listId)
                                        .position(1)
                                        .build();

                        when(cardValidator.validateDelete(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(cardRepository.findById(new CardId("card-123")))
                                        .thenReturn(Optional.of(card));
                        when(boardListRepository.findById(listId))
                                        .thenReturn(Optional.of(boardList));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                        .thenReturn(Optional.empty());

                        // when
                        Either<Failure, Void> result = deleteCardService.deleteCard(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.PermissionDenied.class);
                        Failure.PermissionDenied permissionDenied = (Failure.PermissionDenied) failure;
                        assertThat(permissionDenied.getErrorCode()).isEqualTo("PERMISSION_DENIED");
                        assertThat(permissionDenied.getMessage()).isEqualTo("보드 접근 권한이 없습니다.");
                }

                @Test
                @DisplayName("아카이브된 보드일 때 실패를 반환한다")
                void shouldReturnFailureWhenBoardIsArchived() {
                        // given
                        DeleteCardCommand command = new DeleteCardCommand(new CardId("card-123"), userId);
                        Card card = Card.builder()
                                        .cardId(new CardId("card-123"))
                                        .title("테스트 카드")
                                        .listId(listId)
                                        .position(1)
                                        .build();

                        Board archivedBoard = Board.builder()
                                        .boardId(boardId)
                                        .title("아카이브된 보드")
                                        .ownerId(userId)
                                        .isArchived(true)
                                        .build();

                        when(cardValidator.validateDelete(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(cardRepository.findById(new CardId("card-123")))
                                        .thenReturn(Optional.of(card));
                        when(boardListRepository.findById(listId))
                                        .thenReturn(Optional.of(boardList));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                        .thenReturn(Optional.of(archivedBoard));

                        // when
                        Either<Failure, Void> result = deleteCardService.deleteCard(command);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.BusinessRuleViolation.class);
                        Failure.BusinessRuleViolation businessRuleViolation = (Failure.BusinessRuleViolation) failure;
                        assertThat(businessRuleViolation.getErrorCode()).isEqualTo("BUSINESS_RULE_VIOLATION");
                        assertThat(businessRuleViolation.getMessage()).isEqualTo("아카이브된 보드에서는 카드를 삭제할 수 없습니다.");
                }
        }

        @Nested
        @DisplayName("카드 위치 재정렬 테스트")
        class CardReorderTest {

                private Card cardToDelete;
                private ListId listId;
                private UserId userId;
                private CardId cardId;

                @BeforeEach
                void setUp() {
                        userId = new UserId("user-123");
                        cardId = new CardId("card-123");
                        listId = new ListId("list-123");

                        cardToDelete = Card.builder()
                                        .cardId(cardId)
                                        .title("삭제될 카드")
                                        .listId(listId)
                                        .position(2)
                                        .build();
                }

                @Test
                @DisplayName("재정렬할 카드가 없을 때 아무것도 하지 않는다")
                void shouldDoNothingWhenNoCardsToReorder() {
                        // given
                        DeleteCardCommand command = new DeleteCardCommand(cardId, userId);
                        BoardList boardList = BoardList.builder()
                                        .listId(listId)
                                        .title("테스트 리스트")
                                        .boardId(new BoardId("board-123"))
                                        .build();
                        Board board = Board.builder()
                                        .boardId(new BoardId("board-123"))
                                        .ownerId(userId)
                                        .isArchived(false)
                                        .build();

                        when(cardValidator.validateDelete(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(cardRepository.findById(cardId))
                                        .thenReturn(Optional.of(cardToDelete));
                        when(boardListRepository.findById(listId))
                                        .thenReturn(Optional.of(boardList));
                        when(boardRepository.findByIdAndOwnerId(any(), eq(userId)))
                                        .thenReturn(Optional.of(board));
                        when(cardRepository.delete(cardId))
                                        .thenReturn(Either.right(null));
                        lenient().when(cardRepository.findByListIdAndPositionGreaterThan(listId, 2))
                                        .thenReturn(List.of());
                        lenient().when(boardRepository.findById(any()))
                                        .thenReturn(Optional.of(board));

                        // when
                        Either<Failure, Void> result = deleteCardService.deleteCard(command);

                        // then
                        assertThat(result.isRight()).isTrue();
                        verify(cardRepository).findByListIdAndPositionGreaterThan(listId, 2);
                        verify(cardRepository, times(0)).saveAll(any());
                }

                @Test
                @DisplayName("재정렬 중 예외가 발생해도 삭제는 성공한다")
                void shouldSucceedEvenWhenReorderThrowsException() {
                        // given
                        DeleteCardCommand command = new DeleteCardCommand(cardId, userId);
                        BoardList boardList = BoardList.builder()
                                        .listId(listId)
                                        .title("테스트 리스트")
                                        .boardId(new BoardId("board-123"))
                                        .build();
                        Board board = Board.builder()
                                        .boardId(new BoardId("board-123"))
                                        .ownerId(userId)
                                        .isArchived(false)
                                        .build();

                        when(cardValidator.validateDelete(command))
                                        .thenReturn(ValidationResult.valid(command));
                        when(cardRepository.findById(cardId))
                                        .thenReturn(Optional.of(cardToDelete));
                        when(boardListRepository.findById(listId))
                                        .thenReturn(Optional.of(boardList));
                        when(boardRepository.findByIdAndOwnerId(any(), eq(userId)))
                                        .thenReturn(Optional.of(board));
                        when(cardRepository.delete(cardId))
                                        .thenReturn(Either.right(null));
                        lenient().when(cardRepository.findByListIdAndPositionGreaterThan(listId, 2))
                                        .thenThrow(new RuntimeException("재정렬 중 오류"));
                        lenient().when(boardRepository.findById(any()))
                                        .thenReturn(Optional.of(board));

                        // when
                        Either<Failure, Void> result = deleteCardService.deleteCard(command);

                        // then
                        assertThat(result.isRight()).isTrue();
                }
        }
}
package com.boardly.features.card.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
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
import com.boardly.features.card.application.port.input.UpdateCardCommand;
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
@DisplayName("UpdateCardService updateCard 메서드 테스트")
class UpdateCardServiceUpdateCardTest {

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

        private UpdateCardService updateCardService;

        @BeforeEach
        void setUp() {
                updateCardService = new UpdateCardService(
                                cardValidator,
                                null, // CardMovePolicy는 updateCard 테스트에서 사용하지 않음
                                cardRepository,
                                boardListRepository,
                                boardRepository,
                                validationMessageResolver,
                                activityHelper);

                // 공통으로 사용되는 메시지 설정
                lenient().when(validationMessageResolver.getMessage("validation.input.invalid"))
                                .thenReturn("입력값이 유효하지 않습니다.");
                lenient().when(validationMessageResolver.getMessage("error.service.card.update.not_found"))
                                .thenReturn("카드를 찾을 수 없습니다.");
                lenient().when(validationMessageResolver.getMessage("error.service.card.update.list_not_found"))
                                .thenReturn("리스트를 찾을 수 없습니다.");
                lenient().when(validationMessageResolver.getMessage("error.service.card.update.board_not_found"))
                                .thenReturn("보드를 찾을 수 없습니다.");
                lenient().when(validationMessageResolver.getMessage("error.service.card.update.access_denied"))
                                .thenReturn("보드 접근 권한이 없습니다.");
                lenient().when(validationMessageResolver.getMessage("error.service.card.update.archived_board"))
                                .thenReturn("아카이브된 보드의 카드는 수정할 수 없습니다.");
                lenient().when(validationMessageResolver.getMessage("error.service.card.update.error"))
                                .thenReturn("카드 수정 중 오류가 발생했습니다.");
                lenient().when(validationMessageResolver.getMessage("error.service.board.unknown"))
                                .thenReturn("알 수 없는 보드");
        }

        @Nested
        @DisplayName("updateCard 메서드 테스트")
        class UpdateCardTest {

                private UpdateCardCommand validCommand;
                private Card existingCard;
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

                        validCommand = UpdateCardCommand.of(
                                        cardId,
                                        "새로운 카드 제목",
                                        "새로운 카드 설명",
                                        userId);

                        // existingCard를 Mock으로 생성
                        existingCard = org.mockito.Mockito.mock(Card.class);
                        lenient().when(existingCard.getCardId()).thenReturn(cardId);
                        lenient().when(existingCard.getTitle()).thenReturn("기존 카드 제목");
                        lenient().when(existingCard.getDescription()).thenReturn("기존 카드 설명");
                        lenient().when(existingCard.getListId()).thenReturn(listId);
                        lenient().when(existingCard.getPosition()).thenReturn(0);

                        boardList = BoardList.builder()
                                        .listId(listId)
                                        .title("테스트 리스트")
                                        .boardId(boardId)
                                        .position(0)
                                        .build();

                        board = Board.builder()
                                        .boardId(boardId)
                                        .title("테스트 보드")
                                        .ownerId(userId)
                                        .isArchived(false)
                                        .build();
                }

                @Test
                @DisplayName("유효한 커맨드로 카드 수정 시 성공한다")
                void shouldUpdateCardSuccessfully() {
                        // given
                        ValidationResult<UpdateCardCommand> validResult = ValidationResult.valid(validCommand);
                        when(cardValidator.validateUpdate(validCommand)).thenReturn(validResult);
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(board));
                        when(cardRepository.save(any(Card.class))).thenReturn(Either.right(existingCard));

                        // when
                        Either<Failure, Card> result = updateCardService.updateCard(validCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(result.get()).isEqualTo(existingCard);

                        verify(cardValidator).validateUpdate(validCommand);
                        verify(cardRepository).findById(cardId);
                        verify(boardListRepository).findById(listId);
                        verify(boardRepository).findById(boardId);
                        verify(boardRepository).findByIdAndOwnerId(boardId, userId);
                        verify(cardRepository).save(existingCard);
                        verify(existingCard).updateTitle("새로운 카드 제목");
                        verify(existingCard).updateDescription("새로운 카드 설명");
                        verify(activityHelper).logCardActivity(
                                        eq(ActivityType.CARD_RENAME),
                                        eq(userId),
                                        any(),
                                        eq("테스트 보드"),
                                        eq(boardId),
                                        eq(listId),
                                        eq(cardId));
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
                                                        .field("title")
                                                        .message("제목은 1-200자여야 합니다.")
                                                        .rejectedValue("")
                                                        .build());
                        ValidationResult<UpdateCardCommand> invalidResult = ValidationResult
                                        .invalid(io.vavr.collection.List.ofAll(validationErrors));
                        when(cardValidator.validateUpdate(validCommand)).thenReturn(invalidResult);

                        // when
                        Either<Failure, Card> result = updateCardService.updateCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.InputError.class);
                        assertThat(failure.getMessage()).isEqualTo("입력값이 유효하지 않습니다.");
                        assertThat(((Failure.InputError) failure).getViolations()).hasSize(2);

                        verify(cardValidator).validateUpdate(validCommand);
                        verifyNoInteractions(cardRepository, boardListRepository, boardRepository, activityHelper);
                }

                @Test
                @DisplayName("카드가 존재하지 않을 때 실패를 반환한다")
                void shouldReturnFailureWhenCardNotFound() {
                        // given
                        ValidationResult<UpdateCardCommand> validResult = ValidationResult.valid(validCommand);
                        when(cardValidator.validateUpdate(validCommand)).thenReturn(validResult);
                        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

                        // when
                        Either<Failure, Card> result = updateCardService.updateCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.NotFound.class);
                        assertThat(failure.getMessage()).isEqualTo("카드를 찾을 수 없습니다.");

                        verify(cardValidator).validateUpdate(validCommand);
                        verify(cardRepository).findById(cardId);
                        verifyNoInteractions(boardListRepository, boardRepository, activityHelper);
                }

                @Test
                @DisplayName("리스트가 존재하지 않을 때 실패를 반환한다")
                void shouldReturnFailureWhenListNotFound() {
                        // given
                        ValidationResult<UpdateCardCommand> validResult = ValidationResult.valid(validCommand);
                        when(cardValidator.validateUpdate(validCommand)).thenReturn(validResult);
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.empty());

                        // when
                        Either<Failure, Card> result = updateCardService.updateCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.NotFound.class);
                        assertThat(failure.getMessage()).isEqualTo("리스트를 찾을 수 없습니다.");

                        verify(cardValidator).validateUpdate(validCommand);
                        verify(cardRepository).findById(cardId);
                        verify(boardListRepository).findById(listId);
                        verifyNoInteractions(boardRepository, activityHelper);
                }

                @Test
                @DisplayName("보드가 존재하지 않을 때 실패를 반환한다")
                void shouldReturnFailureWhenBoardNotFound() {
                        // given
                        ValidationResult<UpdateCardCommand> validResult = ValidationResult.valid(validCommand);
                        when(cardValidator.validateUpdate(validCommand)).thenReturn(validResult);
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

                        // when
                        Either<Failure, Card> result = updateCardService.updateCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.NotFound.class);
                        assertThat(failure.getMessage()).isEqualTo("보드를 찾을 수 없습니다.");

                        verify(cardValidator).validateUpdate(validCommand);
                        verify(cardRepository).findById(cardId);
                        verify(boardListRepository).findById(listId);
                        verify(boardRepository).findById(boardId);
                        verifyNoInteractions(activityHelper);
                }

                @Test
                @DisplayName("보드 접근 권한이 없을 때 실패를 반환한다")
                void shouldReturnFailureWhenBoardAccessDenied() {
                        // given
                        ValidationResult<UpdateCardCommand> validResult = ValidationResult.valid(validCommand);
                        when(cardValidator.validateUpdate(validCommand)).thenReturn(validResult);
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.empty());

                        // when
                        Either<Failure, Card> result = updateCardService.updateCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.PermissionDenied.class);
                        assertThat(failure.getMessage()).isEqualTo("보드 접근 권한이 없습니다.");

                        verify(cardValidator).validateUpdate(validCommand);
                        verify(cardRepository).findById(cardId);
                        verify(boardListRepository).findById(listId);
                        verify(boardRepository).findById(boardId);
                        verify(boardRepository).findByIdAndOwnerId(boardId, userId);
                        verifyNoInteractions(activityHelper);
                }

                @Test
                @DisplayName("아카이브된 보드의 카드 수정 시도 시 실패를 반환한다")
                void shouldReturnFailureWhenBoardIsArchived() {
                        // given
                        Board archivedBoard = Board.builder()
                                        .boardId(boardId)
                                        .title("아카이브된 보드")
                                        .ownerId(userId)
                                        .isArchived(true)
                                        .build();

                        ValidationResult<UpdateCardCommand> validResult = ValidationResult.valid(validCommand);
                        when(cardValidator.validateUpdate(validCommand)).thenReturn(validResult);
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(archivedBoard));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                        .thenReturn(Optional.of(archivedBoard));

                        // when
                        Either<Failure, Card> result = updateCardService.updateCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.ResourceConflict.class);
                        assertThat(failure.getMessage()).isEqualTo("아카이브된 보드의 카드는 수정할 수 없습니다.");

                        verify(cardValidator).validateUpdate(validCommand);
                        verify(cardRepository).findById(cardId);
                        verify(boardListRepository).findById(listId);
                        verify(boardRepository).findById(boardId);
                        verify(boardRepository).findByIdAndOwnerId(boardId, userId);
                        verifyNoInteractions(activityHelper);
                }

                @Test
                @DisplayName("카드 저장 실패 시 실패를 반환한다")
                void shouldReturnFailureWhenCardSaveFails() {
                        // given
                        ValidationResult<UpdateCardCommand> validResult = ValidationResult.valid(validCommand);
                        when(cardValidator.validateUpdate(validCommand)).thenReturn(validResult);
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(board));
                        when(cardRepository.save(any(Card.class)))
                                        .thenReturn(Either.left(Failure.ofInternalServerError("저장 실패")));

                        // when
                        Either<Failure, Card> result = updateCardService.updateCard(validCommand);

                        // then
                        assertThat(result.isLeft()).isTrue();
                        Failure failure = result.getLeft();
                        assertThat(failure).isInstanceOf(Failure.InternalError.class);
                        assertThat(failure.getMessage()).isEqualTo("저장 실패");

                        verify(cardValidator).validateUpdate(validCommand);
                        verify(cardRepository).findById(cardId);
                        verify(boardListRepository).findById(listId);
                        verify(boardRepository).findById(boardId);
                        verify(boardRepository).findByIdAndOwnerId(boardId, userId);
                        verify(cardRepository).save(existingCard);
                        verifyNoInteractions(activityHelper);
                }

                @Test
                @DisplayName("카드 제목과 설명이 성공적으로 업데이트된다")
                void shouldUpdateCardTitleAndDescriptionSuccessfully() {
                        // given
                        String newTitle = "업데이트된 제목";
                        String newDescription = "업데이트된 설명";
                        UpdateCardCommand updateCommand = UpdateCardCommand.of(cardId, newTitle, newDescription,
                                        userId);

                        ValidationResult<UpdateCardCommand> validResult = ValidationResult.valid(updateCommand);
                        when(cardValidator.validateUpdate(updateCommand)).thenReturn(validResult);
                        when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
                        when(boardListRepository.findById(listId)).thenReturn(Optional.of(boardList));
                        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
                        when(boardRepository.findByIdAndOwnerId(boardId, userId)).thenReturn(Optional.of(board));
                        when(cardRepository.save(any(Card.class))).thenReturn(Either.right(existingCard));

                        // when
                        Either<Failure, Card> result = updateCardService.updateCard(updateCommand);

                        // then
                        assertThat(result.isRight()).isTrue();
                        assertThat(result.get()).isEqualTo(existingCard);

                        // 카드의 제목과 설명이 업데이트되었는지 확인
                        verify(existingCard).updateTitle(newTitle);
                        verify(existingCard).updateDescription(newDescription);
                }
        }
}
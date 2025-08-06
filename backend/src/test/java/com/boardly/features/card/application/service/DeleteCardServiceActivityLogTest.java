package com.boardly.features.card.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import com.boardly.features.comment.domain.repository.CommentRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteCardService - 활동 로그 테스트")
class DeleteCardServiceActivityLogTest {

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
        private CommentRepository commentRepository;

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
                                commentRepository,
                                validationMessageResolver,
                                activityHelper);
        }

        @Test
        @DisplayName("활동 로그 기록 시 보드 정보를 찾을 수 없어도 삭제는 성공한다")
        @SuppressWarnings("unchecked")
        void shouldSucceedEvenWhenBoardInfoNotFoundForActivityLog() {
                // given
                UserId userId = new UserId("user-123");
                CardId cardId = new CardId("card-123");
                ListId listId = new ListId("list-123");
                BoardId boardId = new BoardId("board-123");
                DeleteCardCommand command = new DeleteCardCommand(cardId, userId);

                Card cardToDelete = Card.builder()
                                .cardId(cardId)
                                .title("테스트 카드")
                                .listId(listId)
                                .position(1)
                                .build();

                BoardList boardList = BoardList.builder()
                                .listId(listId)
                                .title("테스트 리스트")
                                .boardId(boardId)
                                .position(1)
                                .build();

                Board board = Board.builder()
                                .boardId(boardId)
                                .title("테스트 보드")
                                .ownerId(userId)
                                .isArchived(false)
                                .build();

                when(cardValidator.validateDelete(command))
                                .thenReturn(ValidationResult.valid(command));
                when(cardRepository.findById(cardId))
                                .thenReturn(Optional.of(cardToDelete));
                when(boardListRepository.findById(listId))
                                .thenReturn(Optional.of(boardList));
                when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                .thenReturn(Optional.of(board));
                when(commentRepository.deleteByCardId(cardId))
                                .thenReturn(Either.right(null));
                when(cardRepository.delete(cardId))
                                .thenReturn(Either.right(null));
                when(cardRepository.findByListIdAndPositionGreaterThan(listId, 1))
                                .thenReturn(List.of());
                when(boardRepository.findById(boardId))
                                .thenReturn(Optional.empty());

                // when
                Either<Failure, Void> result = deleteCardService.deleteCard(command);

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
                UserId userId = new UserId("user-123");
                CardId cardId = new CardId("card-123");
                ListId listId = new ListId("list-123");
                BoardId boardId = new BoardId("board-123");
                DeleteCardCommand command = new DeleteCardCommand(cardId, userId);

                Card cardToDelete = Card.builder()
                                .cardId(cardId)
                                .title("테스트 카드")
                                .listId(listId)
                                .position(1)
                                .build();

                BoardList boardList = BoardList.builder()
                                .listId(listId)
                                .title("테스트 리스트")
                                .boardId(boardId)
                                .position(1)
                                .build();

                Board board = Board.builder()
                                .boardId(boardId)
                                .title("테스트 보드")
                                .ownerId(userId)
                                .isArchived(false)
                                .build();

                when(cardValidator.validateDelete(command))
                                .thenReturn(ValidationResult.valid(command));
                when(cardRepository.findById(cardId))
                                .thenReturn(Optional.of(cardToDelete));
                when(boardListRepository.findById(listId))
                                .thenReturn(Optional.of(boardList));
                when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                .thenReturn(Optional.of(board));
                when(commentRepository.deleteByCardId(cardId))
                                .thenReturn(Either.right(null));
                when(cardRepository.delete(cardId))
                                .thenReturn(Either.right(null));
                when(cardRepository.findByListIdAndPositionGreaterThan(listId, 1))
                                .thenReturn(List.of());
                when(boardRepository.findById(boardId))
                                .thenReturn(Optional.of(board));
                lenient().doThrow(new RuntimeException("활동 로그 기록 실패"))
                                .when(activityHelper)
                                .logCardActivity(any(), any(), any(), any(), any(), any(), any());

                // when
                Either<Failure, Void> result = deleteCardService.deleteCard(command);

                // then
                assertThat(result.isRight()).isTrue();
        }

        @Test
        @DisplayName("활동 로그용 리스트 정보를 찾을 수 없어도 삭제는 성공한다")
        void shouldSucceedEvenWhenBoardListNotFoundForActivityLog() {
                // given
                UserId userId = new UserId("user-123");
                CardId cardId = new CardId("card-123");
                ListId listId = new ListId("list-123");
                BoardId boardId = new BoardId("board-123");
                DeleteCardCommand command = new DeleteCardCommand(cardId, userId);

                Card cardToDelete = Card.builder()
                                .cardId(cardId)
                                .title("테스트 카드")
                                .listId(listId)
                                .position(1)
                                .build();

                BoardList boardList = BoardList.builder()
                                .listId(listId)
                                .title("테스트 리스트")
                                .boardId(boardId)
                                .position(1)
                                .build();

                Board board = Board.builder()
                                .boardId(boardId)
                                .title("테스트 보드")
                                .ownerId(userId)
                                .isArchived(false)
                                .build();

                when(cardValidator.validateDelete(command))
                                .thenReturn(ValidationResult.valid(command));
                when(cardRepository.findById(cardId))
                                .thenReturn(Optional.of(cardToDelete));
                when(boardListRepository.findById(listId))
                                .thenReturn(Optional.of(boardList));
                when(boardRepository.findByIdAndOwnerId(boardId, userId))
                                .thenReturn(Optional.of(board));
                when(commentRepository.deleteByCardId(cardId))
                                .thenReturn(Either.right(null));
                when(cardRepository.delete(cardId))
                                .thenReturn(Either.right(null));
                when(cardRepository.findByListIdAndPositionGreaterThan(listId, 1))
                                .thenReturn(List.of());
                // 활동 로그 기록 시에는 리스트를 찾을 수 없음
                when(boardListRepository.findById(listId))
                                .thenReturn(Optional.of(boardList)) // 첫 번째 호출 (권한 검증)
                                .thenReturn(Optional.empty()); // 두 번째 호출 (활동 로그)

                // when
                Either<Failure, Void> result = deleteCardService.deleteCard(command);

                // then
                assertThat(result.isRight()).isTrue();
        }
}
package com.boardly.features.card.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
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

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListColor;
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

    private DeleteCardService deleteCardService;

    @BeforeEach
    void setUp() {
        deleteCardService = new DeleteCardService(
                cardValidator,
                cardRepository,
                boardListRepository,
                boardRepository,
                validationMessageResolver);

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

        @BeforeEach
        void setUp() {
            // 테스트 데이터 설정
            CardId cardId = new CardId("card-123");
            UserId userId = new UserId("user-123");
            ListId listId = new ListId("list-123");
            BoardId boardId = new BoardId("board-123");

            validCommand = DeleteCardCommand.of(cardId, userId);

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
                    .position(0)
                    .color(ListColor.of("#B04632"))
                    .build();

            board = Board.builder()
                    .boardId(boardId)
                    .title("테스트 보드")
                    .description("테스트 보드 설명")
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
            when(cardRepository.findById(validCommand.cardId()))
                    .thenReturn(Optional.of(cardToDelete));
            when(boardListRepository.findById(cardToDelete.getListId()))
                    .thenReturn(Optional.of(boardList));
            when(boardRepository.findByIdAndOwnerId(boardList.getBoardId(), validCommand.userId()))
                    .thenReturn(Optional.of(board));
            when(cardRepository.delete(validCommand.cardId()))
                    .thenReturn(Either.right(null));
            when(cardRepository.findByListIdAndPositionGreaterThan(cardToDelete.getListId(),
                    cardToDelete.getPosition()))
                    .thenReturn(List.of());

            // when
            Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

            // then
            assertThat(result.isRight()).isTrue();
            verify(cardRepository).delete(validCommand.cardId());
            verify(cardRepository).findByListIdAndPositionGreaterThan(cardToDelete.getListId(),
                    cardToDelete.getPosition());
        }

        @Test
        @DisplayName("입력 검증 실패 시 실패를 반환한다")
        void shouldReturnFailureWhenValidationFails() {
            // given
            ValidationResult<DeleteCardCommand> invalidResult = ValidationResult.invalid(
                    io.vavr.collection.List.of(Failure.FieldViolation.builder()
                            .field("cardId")
                            .message("카드 ID는 필수입니다")
                            .rejectedValue(null)
                            .build()));

            when(cardValidator.validateDelete(validCommand))
                    .thenReturn(invalidResult);

            // when
            Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.InputError.class);
            assertThat(failure.getMessage()).isEqualTo("입력값이 유효하지 않습니다.");
        }

        @Test
        @DisplayName("카드가 존재하지 않을 때 실패를 반환한다")
        void shouldReturnFailureWhenCardNotFound() {
            // given
            when(cardValidator.validateDelete(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(cardRepository.findById(validCommand.cardId()))
                    .thenReturn(Optional.empty());

            // when
            Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.NotFound.class);
            assertThat(failure.getMessage()).isEqualTo("삭제할 카드를 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("리스트가 존재하지 않을 때 실패를 반환한다")
        void shouldReturnFailureWhenListNotFound() {
            // given
            when(cardValidator.validateDelete(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(cardRepository.findById(validCommand.cardId()))
                    .thenReturn(Optional.of(cardToDelete));
            when(boardListRepository.findById(cardToDelete.getListId()))
                    .thenReturn(Optional.empty());

            // when
            Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.NotFound.class);
            assertThat(failure.getMessage()).isEqualTo("리스트를 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("보드 접근 권한이 없을 때 실패를 반환한다")
        void shouldReturnFailureWhenBoardAccessDenied() {
            // given
            when(cardValidator.validateDelete(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(cardRepository.findById(validCommand.cardId()))
                    .thenReturn(Optional.of(cardToDelete));
            when(boardListRepository.findById(cardToDelete.getListId()))
                    .thenReturn(Optional.of(boardList));
            when(boardRepository.findByIdAndOwnerId(boardList.getBoardId(), validCommand.userId()))
                    .thenReturn(Optional.empty());

            // when
            Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.PermissionDenied.class);
            assertThat(failure.getMessage()).isEqualTo("보드 접근 권한이 없습니다.");
        }

        @Test
        @DisplayName("아카이브된 보드에서 카드 삭제 시도 시 실패를 반환한다")
        void shouldReturnFailureWhenBoardIsArchived() {
            // given
            Board archivedBoard = Board.builder()
                    .boardId(board.getBoardId())
                    .title("아카이브된 보드")
                    .description("아카이브된 보드 설명")
                    .ownerId(validCommand.userId())
                    .isArchived(true)
                    .build();

            when(cardValidator.validateDelete(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(cardRepository.findById(validCommand.cardId()))
                    .thenReturn(Optional.of(cardToDelete));
            when(boardListRepository.findById(cardToDelete.getListId()))
                    .thenReturn(Optional.of(boardList));
            when(boardRepository.findByIdAndOwnerId(boardList.getBoardId(), validCommand.userId()))
                    .thenReturn(Optional.of(archivedBoard));

            // when
            Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.BusinessRuleViolation.class);
            assertThat(failure.getMessage()).isEqualTo("아카이브된 보드에서는 카드를 삭제할 수 없습니다.");
        }

        @Test
        @DisplayName("카드 삭제 실패 시 실패를 반환한다")
        void shouldReturnFailureWhenCardDeleteFails() {
            // given
            when(cardValidator.validateDelete(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(cardRepository.findById(validCommand.cardId()))
                    .thenReturn(Optional.of(cardToDelete));
            when(boardListRepository.findById(cardToDelete.getListId()))
                    .thenReturn(Optional.of(boardList));
            when(boardRepository.findByIdAndOwnerId(boardList.getBoardId(), validCommand.userId()))
                    .thenReturn(Optional.of(board));
            when(cardRepository.delete(validCommand.cardId()))
                    .thenReturn(Either.left(Failure.ofInternalServerError("카드 삭제 중 오류가 발생했습니다.")));

            // when
            Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.InternalError.class);
            assertThat(failure.getMessage()).isEqualTo("카드 삭제 중 오류가 발생했습니다.");
        }

        @Test
        @DisplayName("카드 삭제 후 남은 카드들의 위치를 재정렬한다")
        void shouldReorderRemainingCardsAfterDeletion() {
            // given
            Card remainingCard1 = Card.builder()
                    .cardId(new CardId("card-456"))
                    .title("남은 카드 1")
                    .listId(cardToDelete.getListId())
                    .position(3)
                    .build();

            Card remainingCard2 = Card.builder()
                    .cardId(new CardId("card-789"))
                    .title("남은 카드 2")
                    .listId(cardToDelete.getListId())
                    .position(4)
                    .build();

            List<Card> cardsToReorder = List.of(remainingCard1, remainingCard2);

            when(cardValidator.validateDelete(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(cardRepository.findById(validCommand.cardId()))
                    .thenReturn(Optional.of(cardToDelete));
            when(boardListRepository.findById(cardToDelete.getListId()))
                    .thenReturn(Optional.of(boardList));
            when(boardRepository.findByIdAndOwnerId(boardList.getBoardId(), validCommand.userId()))
                    .thenReturn(Optional.of(board));
            when(cardRepository.delete(validCommand.cardId()))
                    .thenReturn(Either.right(null));
            when(cardRepository.findByListIdAndPositionGreaterThan(cardToDelete.getListId(),
                    cardToDelete.getPosition()))
                    .thenReturn(cardsToReorder);
            when(cardRepository.saveAll(any()))
                    .thenReturn(cardsToReorder);

            // when
            Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

            // then
            assertThat(result.isRight()).isTrue();
            verify(cardRepository).findByListIdAndPositionGreaterThan(cardToDelete.getListId(),
                    cardToDelete.getPosition());
            verify(cardRepository).saveAll(cardsToReorder);

            // 위치가 1씩 감소했는지 확인
            assertThat(remainingCard1.getPosition()).isEqualTo(2);
            assertThat(remainingCard2.getPosition()).isEqualTo(3);
        }

        @Test
        @DisplayName("재정렬할 카드가 없을 때는 위치 조정을 건너뛴다")
        void shouldSkipReorderWhenNoCardsToReorder() {
            // given
            when(cardValidator.validateDelete(validCommand))
                    .thenReturn(ValidationResult.valid(validCommand));
            when(cardRepository.findById(validCommand.cardId()))
                    .thenReturn(Optional.of(cardToDelete));
            when(boardListRepository.findById(cardToDelete.getListId()))
                    .thenReturn(Optional.of(boardList));
            when(boardRepository.findByIdAndOwnerId(boardList.getBoardId(), validCommand.userId()))
                    .thenReturn(Optional.of(board));
            when(cardRepository.delete(validCommand.cardId()))
                    .thenReturn(Either.right(null));
            when(cardRepository.findByListIdAndPositionGreaterThan(cardToDelete.getListId(),
                    cardToDelete.getPosition()))
                    .thenReturn(List.of());

            // when
            Either<Failure, Void> result = deleteCardService.deleteCard(validCommand);

            // then
            assertThat(result.isRight()).isTrue();
            verify(cardRepository).findByListIdAndPositionGreaterThan(cardToDelete.getListId(),
                    cardToDelete.getPosition());
            // 재정렬할 카드가 없을 때는 saveAll이 호출되지 않음
        }
    }

    @Nested
    @DisplayName("validateBoardAccess 메서드 테스트")
    class ValidateBoardAccessTest {

        private ListId listId;
        private UserId userId;
        private BoardList boardList;
        private Board board;

        @BeforeEach
        void setUp() {
            listId = new ListId("list-123");
            userId = new UserId("user-123");
            BoardId boardId = new BoardId("board-123");

            boardList = BoardList.builder()
                    .listId(listId)
                    .title("테스트 리스트")
                    .boardId(boardId)
                    .position(0)
                    .color(ListColor.of("#B04632"))
                    .build();

            board = Board.builder()
                    .boardId(boardId)
                    .title("테스트 보드")
                    .description("테스트 보드 설명")
                    .ownerId(userId)
                    .isArchived(false)
                    .build();
        }

        @Test
        @DisplayName("리스트가 존재하지 않을 때 실패를 반환한다")
        void shouldReturnFailureWhenListNotFound() {
            // given
            DeleteCardCommand command = DeleteCardCommand.of(new CardId("card-123"), userId);
            Card testCard = Card.builder()
                    .cardId(new CardId("card-123"))
                    .title("테스트 카드")
                    .listId(listId)
                    .position(0)
                    .build();

            when(cardValidator.validateDelete(command))
                    .thenReturn(ValidationResult.valid(command));
            when(cardRepository.findById(command.cardId()))
                    .thenReturn(Optional.of(testCard));
            when(boardListRepository.findById(listId))
                    .thenReturn(Optional.empty());

            // when
            Either<Failure, Void> result = deleteCardService.deleteCard(command);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.NotFound.class);
            assertThat(failure.getMessage()).isEqualTo("리스트를 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("보드 접근 권한이 없을 때 실패를 반환한다")
        void shouldReturnFailureWhenBoardAccessDenied() {
            // given
            when(cardValidator.validateDelete(any()))
                    .thenReturn(ValidationResult.valid(DeleteCardCommand.of(new CardId("card-123"), userId)));
            when(cardRepository.findById(any()))
                    .thenReturn(Optional.of(Card.builder()
                            .cardId(new CardId("card-123"))
                            .title("테스트 카드")
                            .listId(listId)
                            .position(0)
                            .build()));
            when(boardListRepository.findById(listId))
                    .thenReturn(Optional.of(boardList));
            when(boardRepository.findByIdAndOwnerId(boardList.getBoardId(), userId))
                    .thenReturn(Optional.empty());

            // when
            Either<Failure, Void> result = deleteCardService.deleteCard(
                    DeleteCardCommand.of(new CardId("card-123"), userId));

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.PermissionDenied.class);
            assertThat(failure.getMessage()).isEqualTo("보드 접근 권한이 없습니다.");
        }

        @Test
        @DisplayName("아카이브된 보드일 때 실패를 반환한다")
        void shouldReturnFailureWhenBoardIsArchived() {
            // given
            Board archivedBoard = Board.builder()
                    .boardId(board.getBoardId())
                    .title("아카이브된 보드")
                    .description("아카이브된 보드 설명")
                    .ownerId(userId)
                    .isArchived(true)
                    .build();

            when(cardValidator.validateDelete(any()))
                    .thenReturn(ValidationResult.valid(DeleteCardCommand.of(new CardId("card-123"), userId)));
            when(cardRepository.findById(any()))
                    .thenReturn(Optional.of(Card.builder()
                            .cardId(new CardId("card-123"))
                            .title("테스트 카드")
                            .listId(listId)
                            .position(0)
                            .build()));
            when(boardListRepository.findById(listId))
                    .thenReturn(Optional.of(boardList));
            when(boardRepository.findByIdAndOwnerId(boardList.getBoardId(), userId))
                    .thenReturn(Optional.of(archivedBoard));

            // when
            Either<Failure, Void> result = deleteCardService.deleteCard(
                    DeleteCardCommand.of(new CardId("card-123"), userId));

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.BusinessRuleViolation.class);
            assertThat(failure.getMessage()).isEqualTo("아카이브된 보드에서는 카드를 삭제할 수 없습니다.");
        }
    }
}
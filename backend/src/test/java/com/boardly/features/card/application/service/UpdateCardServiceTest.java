package com.boardly.features.card.application.service;

import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.domain.repository.BoardRepository;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.domain.repository.BoardListRepository;
import com.boardly.features.card.application.port.input.UpdateCardCommand;
import com.boardly.features.card.application.validation.UpdateCardValidator;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateCardService 테스트")
class UpdateCardServiceTest {

    @Mock
    private UpdateCardValidator cardValidator;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private BoardListRepository boardListRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private ValidationMessageResolver validationMessageResolver;

    @InjectMocks
    private UpdateCardService updateCardService;

    private UserId testUserId;
    private CardId testCardId;
    private ListId testListId;
    private BoardId testBoardId;
    private Card testCard;
    private BoardList testBoardList;
    private Board testBoard;
    private UpdateCardCommand validCommand;

    @BeforeEach
    void setUp() {
        testUserId = new UserId("test-user-123");
        testCardId = new CardId("test-card-123");
        testListId = new ListId("test-list-123");
        testBoardId = new BoardId("test-board-123");

        Instant now = Instant.now();

        testCard = Card.builder()
                .cardId(testCardId)
                .title("기존 카드 제목")
                .description("기존 카드 설명")
                .position(0)
                .listId(testListId)
                .createdAt(now)
                .updatedAt(now)
                .build();

        testBoardList = BoardList.builder()
                .listId(testListId)
                .title("테스트 리스트")
                .position(1)
                .boardId(testBoardId)
                .createdAt(now)
                .updatedAt(now)
                .build();

        testBoard = Board.builder()
                .boardId(testBoardId)
                .title("테스트 보드")
                .description("테스트 보드 설명")
                .isArchived(false)
                .ownerId(testUserId)
                .createdAt(now)
                .updatedAt(now)
                .build();

        validCommand = UpdateCardCommand.of(
                testCardId,
                "수정된 카드 제목",
                "수정된 카드 설명",
                testUserId);
    }

    @Nested
    @DisplayName("updateCard 메서드 테스트")
    class UpdateCardTest {

        @Test
        @DisplayName("카드 수정 성공")
        void updateCard_Success() {
            // given
            ValidationResult<UpdateCardCommand> validResult = ValidationResult.valid(validCommand);
            when(cardValidator.validate(validCommand)).thenReturn(validResult);
            when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));
            when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(testBoard));
            when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
                Card card = invocation.getArgument(0);
                return Either.right(card);
            });

            // when
            Either<Failure, Card> result = updateCardService.updateCard(validCommand);

            // then
            assertThat(result.isRight()).isTrue();
            Card updatedCard = result.get();
            assertThat(updatedCard.getTitle()).isEqualTo("수정된 카드 제목");
            assertThat(updatedCard.getDescription()).isEqualTo("수정된 카드 설명");
            assertThat(updatedCard.getCardId()).isEqualTo(testCardId);

            verify(cardValidator).validate(validCommand);
            verify(cardRepository).findById(testCardId);
            verify(boardListRepository, times(2)).findById(testListId);
            verify(boardRepository).findByIdAndOwnerId(testBoardId, testUserId);
            verify(cardRepository).save(any(Card.class));
        }

        @Test
        @DisplayName("입력 검증 실패 - INVALID_INPUT")
        void updateCard_ValidationFailure_InvalidInput() {
            // given
            ValidationResult<UpdateCardCommand> invalidResult = ValidationResult.invalid("title", "제목은 필수입니다", null);
            when(cardValidator.validate(validCommand)).thenReturn(invalidResult);
            when(validationMessageResolver.getMessage("validation.input.invalid"))
                    .thenReturn("입력 데이터가 올바르지 않습니다");

            // when
            Either<Failure, Card> result = updateCardService.updateCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.InputError.class);

            Failure.InputError inputError = (Failure.InputError) failure;
            assertThat(inputError.getMessage()).isEqualTo("입력 데이터가 올바르지 않습니다");
            assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");

            verify(cardValidator).validate(validCommand);
            verify(cardRepository, never()).findById(any());
        }

        @Test
        @DisplayName("카드를 찾을 수 없는 경우 - NOT_FOUND")
        void updateCard_CardNotFound_NotFound() {
            // given
            ValidationResult<UpdateCardCommand> validResult = ValidationResult.valid(validCommand);
            when(cardValidator.validate(validCommand)).thenReturn(validResult);
            when(cardRepository.findById(testCardId)).thenReturn(Optional.empty());
            when(validationMessageResolver.getMessage("error.service.card.move.not_found"))
                    .thenReturn("카드를 찾을 수 없습니다");

            // when
            Either<Failure, Card> result = updateCardService.updateCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.NotFound.class);

            Failure.NotFound notFound = (Failure.NotFound) failure;
            assertThat(notFound.getMessage()).isEqualTo("카드를 찾을 수 없습니다");

            verify(cardValidator).validate(validCommand);
            verify(cardRepository).findById(testCardId);
            verify(boardListRepository, never()).findById(any());
        }

        @Test
        @DisplayName("리스트를 찾을 수 없는 경우 - NOT_FOUND")
        void updateCard_ListNotFound_NotFound() {
            // given
            ValidationResult<UpdateCardCommand> validResult = ValidationResult.valid(validCommand);
            when(cardValidator.validate(validCommand)).thenReturn(validResult);
            when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));
            when(boardListRepository.findById(testListId)).thenReturn(Optional.empty());
            when(validationMessageResolver.getMessage("error.service.card.move.list_not_found"))
                    .thenReturn("리스트를 찾을 수 없습니다");

            // when
            Either<Failure, Card> result = updateCardService.updateCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.NotFound.class);

            Failure.NotFound notFound = (Failure.NotFound) failure;
            assertThat(notFound.getMessage()).isEqualTo("리스트를 찾을 수 없습니다");

            verify(cardValidator).validate(validCommand);
            verify(cardRepository).findById(testCardId);
            verify(boardListRepository).findById(testListId);
            verify(boardRepository, never()).findByIdAndOwnerId(any(), any());
        }

        @Test
        @DisplayName("보드 접근 권한이 없는 경우 - PERMISSION_DENIED")
        void updateCard_AccessDenied_PermissionDenied() {
            // given
            UserId unauthorizedUserId = new UserId("unauthorized-user-456");
            UpdateCardCommand unauthorizedCommand = UpdateCardCommand.of(
                    testCardId, "수정된 제목", "수정된 설명", unauthorizedUserId);

            ValidationResult<UpdateCardCommand> validResult = ValidationResult.valid(unauthorizedCommand);
            when(cardValidator.validate(unauthorizedCommand)).thenReturn(validResult);
            when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));
            when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, unauthorizedUserId)).thenReturn(Optional.empty());
            when(validationMessageResolver.getMessage("error.service.card.move.access_denied"))
                    .thenReturn("보드 접근 권한이 없습니다");

            // when
            Either<Failure, Card> result = updateCardService.updateCard(unauthorizedCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.PermissionDenied.class);

            Failure.PermissionDenied permissionDenied = (Failure.PermissionDenied) failure;
            assertThat(permissionDenied.getMessage()).isEqualTo("보드 접근 권한이 없습니다");

            verify(cardValidator).validate(unauthorizedCommand);
            verify(cardRepository).findById(testCardId);
            verify(boardListRepository).findById(testListId);
            verify(boardRepository).findByIdAndOwnerId(testBoardId, unauthorizedUserId);
        }

        @Test
        @DisplayName("아카이브된 보드의 카드 수정 시도 - RESOURCE_CONFLICT")
        void updateCard_ArchivedBoard_ResourceConflict() {
            // given
            Board archivedBoard = Board.builder()
                    .boardId(testBoardId)
                    .title("테스트 보드")
                    .description("테스트 보드 설명")
                    .isArchived(true)
                    .ownerId(testUserId)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            ValidationResult<UpdateCardCommand> validResult = ValidationResult.valid(validCommand);
            when(cardValidator.validate(validCommand)).thenReturn(validResult);
            when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));
            when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(archivedBoard));
            when(boardRepository.findById(testBoardId)).thenReturn(Optional.of(archivedBoard));
            when(validationMessageResolver.getMessage("error.service.card.move.archived_board"))
                    .thenReturn("아카이브된 보드의 카드는 수정할 수 없습니다");

            // when
            Either<Failure, Card> result = updateCardService.updateCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.ResourceConflict.class);

            Failure.ResourceConflict resourceConflict = (Failure.ResourceConflict) failure;
            assertThat(resourceConflict.getMessage()).isEqualTo("아카이브된 보드의 카드는 수정할 수 없습니다");

            verify(cardValidator).validate(validCommand);
            verify(cardRepository).findById(testCardId);
            verify(boardListRepository, times(2)).findById(testListId);
            verify(boardRepository).findByIdAndOwnerId(testBoardId, testUserId);
            verify(boardRepository).findById(testBoardId);
        }

        @Test
        @DisplayName("카드 저장 실패")
        void updateCard_SaveFailure() {
            // given
            ValidationResult<UpdateCardCommand> validResult = ValidationResult.valid(validCommand);
            when(cardValidator.validate(validCommand)).thenReturn(validResult);
            when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));
            when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(testBoard));
            when(cardRepository.save(any(Card.class))).thenReturn(
                    Either.left(Failure.ofInternalServerError("데이터베이스 저장 실패")));

            // when
            Either<Failure, Card> result = updateCardService.updateCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            Failure failure = result.getLeft();
            assertThat(failure).isInstanceOf(Failure.InternalError.class);

            Failure.InternalError internalError = (Failure.InternalError) failure;
            assertThat(internalError.getErrorCode()).isEqualTo("INTERNAL_ERROR");
            assertThat(internalError.getMessage()).isEqualTo("데이터베이스 저장 실패");

            verify(cardValidator).validate(validCommand);
            verify(cardRepository).findById(testCardId);
            verify(boardListRepository, times(2)).findById(testListId);
            verify(boardRepository).findByIdAndOwnerId(testBoardId, testUserId);
            verify(cardRepository).save(any(Card.class));
        }

        @Test
        @DisplayName("description이 null인 경우에도 수정 성공")
        void updateCard_NullDescription_Success() {
            // given
            UpdateCardCommand commandWithNullDescription = UpdateCardCommand.of(
                    testCardId, "수정된 제목", null, testUserId);

            ValidationResult<UpdateCardCommand> validResult = ValidationResult.valid(commandWithNullDescription);
            when(cardValidator.validate(commandWithNullDescription)).thenReturn(validResult);
            when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));
            when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(testBoard));
            when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
                Card card = invocation.getArgument(0);
                return Either.right(card);
            });

            // when
            Either<Failure, Card> result = updateCardService.updateCard(commandWithNullDescription);

            // then
            assertThat(result.isRight()).isTrue();
            Card updatedCard = result.get();
            assertThat(updatedCard.getTitle()).isEqualTo("수정된 제목");
            assertThat(updatedCard.getDescription()).isNull();

            verify(cardValidator).validate(commandWithNullDescription);
            verify(boardListRepository, times(2)).findById(testListId);
            verify(cardRepository).save(any(Card.class));
        }

        @Test
        @DisplayName("description이 빈 문자열인 경우에도 수정 성공")
        void updateCard_EmptyDescription_Success() {
            // given
            UpdateCardCommand commandWithEmptyDescription = UpdateCardCommand.of(
                    testCardId, "수정된 제목", "", testUserId);

            ValidationResult<UpdateCardCommand> validResult = ValidationResult.valid(commandWithEmptyDescription);
            when(cardValidator.validate(commandWithEmptyDescription)).thenReturn(validResult);
            when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));
            when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(testBoard));
            when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
                Card card = invocation.getArgument(0);
                return Either.right(card);
            });

            // when
            Either<Failure, Card> result = updateCardService.updateCard(commandWithEmptyDescription);

            // then
            assertThat(result.isRight()).isTrue();
            Card updatedCard = result.get();
            assertThat(updatedCard.getTitle()).isEqualTo("수정된 제목");
            assertThat(updatedCard.getDescription()).isEqualTo("");

            verify(cardValidator).validate(commandWithEmptyDescription);
            verify(boardListRepository, times(2)).findById(testListId);
            verify(cardRepository).save(any(Card.class));
        }
    }

    @Nested
    @DisplayName("로그 메시지 테스트")
    class LoggingTest {

        @Test
        @DisplayName("성공 시 로그 메시지 확인")
        void updateCard_Success_LogMessages() {
            // given
            ValidationResult<UpdateCardCommand> validResult = ValidationResult.valid(validCommand);
            when(cardValidator.validate(validCommand)).thenReturn(validResult);
            when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));
            when(boardListRepository.findById(testListId)).thenReturn(Optional.of(testBoardList));
            when(boardRepository.findByIdAndOwnerId(testBoardId, testUserId)).thenReturn(Optional.of(testBoard));
            when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
                Card card = invocation.getArgument(0);
                return Either.right(card);
            });

            // when
            Either<Failure, Card> result = updateCardService.updateCard(validCommand);

            // then
            assertThat(result.isRight()).isTrue();
            // 로그 메시지는 실제로는 확인할 수 없지만, 메서드가 정상적으로 호출되었는지 확인
            verify(cardRepository).save(any(Card.class));
        }

        @Test
        @DisplayName("실패 시 로그 메시지 확인")
        void updateCard_Failure_LogMessages() {
            // given
            ValidationResult<UpdateCardCommand> invalidResult = ValidationResult.invalid("title", "제목은 필수입니다", null);
            when(cardValidator.validate(validCommand)).thenReturn(invalidResult);
            when(validationMessageResolver.getMessage("validation.input.invalid"))
                    .thenReturn("입력 데이터가 올바르지 않습니다");

            // when
            Either<Failure, Card> result = updateCardService.updateCard(validCommand);

            // then
            assertThat(result.isLeft()).isTrue();
            // 로그 메시지는 실제로는 확인할 수 없지만, 메서드가 정상적으로 호출되었는지 확인
            verify(cardValidator).validate(validCommand);
        }
    }
}
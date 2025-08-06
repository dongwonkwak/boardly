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

    @Test
    @DisplayName("유효한 커맨드로 카드 삭제 시 성공한다")
    @SuppressWarnings("unchecked")
    void shouldDeleteCardSuccessfully() {
        // given
        UserId userId = new UserId("user-123");
        CardId cardId = new CardId("card-123");
        ListId listId = new ListId("list-123");
        BoardId boardId = new BoardId("board-123");

        DeleteCardCommand validCommand = new DeleteCardCommand(cardId, userId);

        Card cardToDelete = Card.builder()
                .cardId(cardId)
                .title("테스트 카드")
                .description("테스트 설명")
                .listId(listId)
                .position(2)
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

        when(cardValidator.validateDelete(validCommand))
                .thenReturn(ValidationResult.valid(validCommand));
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
        verify(commentRepository).deleteByCardId(cardId);
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
        UserId userId = new UserId("user-123");
        CardId cardId = new CardId("card-123");
        DeleteCardCommand validCommand = new DeleteCardCommand(cardId, userId);

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
}
package com.boardly.features.card.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boardly.features.activity.application.helper.ActivityHelper;
import com.boardly.features.board.domain.port.BoardRepository;
import com.boardly.features.boardlist.domain.port.BoardListRepository;
import com.boardly.features.card.application.command.DeleteCardCommand;
import com.boardly.features.card.application.validation.CardValidator;
import com.boardly.shared.common.value.CardId;
import com.boardly.features.card.domain.port.CardRepository;
import com.boardly.features.comment.domain.port.CommentRepository;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.validation.MessageResolver;
import com.boardly.shared.validation.ValidationResult;
import com.boardly.shared.common.error.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteCardService - 카드 없음 테스트")
class DeleteCardServiceCardNotFoundTest {

    @Mock
    private CardValidator cardValidator;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private BoardListRepository boardListRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private MessageResolver validationMessageResolver;

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

        when(validationMessageResolver.getMessage("error.service.card.delete.not_found"))
                .thenReturn("삭제할 카드를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("카드가 존재하지 않을 때 실패를 반환한다")
    void shouldReturnFailureWhenCardNotFound() {
        // given
        UserId userId = new UserId("user-123");
        CardId cardId = new CardId("card-123");
        DeleteCardCommand command = new DeleteCardCommand(cardId, userId);

        when(cardValidator.validateDelete(command))
                .thenReturn(ValidationResult.valid(command));
        when(cardRepository.findById(cardId))
                .thenReturn(Optional.empty());

        // when
        Either<Failure, Void> result = deleteCardService.deleteCard(command);

        // then
        assertThat(result.isLeft()).isTrue();
        Failure failure = result.getLeft();
        assertThat(failure).isInstanceOf(Failure.NotFound.class);
        Failure.NotFound notFound = (Failure.NotFound) failure;
        assertThat(notFound.getErrorCode()).isEqualTo("NOT_FOUND");
        assertThat(notFound.getMessage()).isEqualTo("삭제할 카드를 찾을 수 없습니다.");

        verify(cardValidator).validateDelete(command);
        verify(cardRepository).findById(cardId);
        verifyNoInteractions(boardListRepository, boardRepository, activityHelper);
    }
}
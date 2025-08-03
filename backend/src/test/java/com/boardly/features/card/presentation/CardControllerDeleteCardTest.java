package com.boardly.features.card.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.jwt.Jwt;

import com.boardly.features.card.application.port.input.DeleteCardCommand;
import com.boardly.features.card.application.usecase.DeleteCardUseCase;
import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.presentation.ApiFailureHandler;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
class CardControllerDeleteCardTest {

    private CardController cardController;

    @Mock
    private DeleteCardUseCase deleteCardUseCase;

    @Mock
    private ApiFailureHandler failureHandler;

    @Mock
    private Jwt jwt;

    @Mock
    private MockHttpServletRequest httpRequest;

    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_CARD_ID = "test-card-id";

    @BeforeEach
    void setUp() {
        cardController = new CardController(
                null, // createCardUseCase
                null, // cardQueryUseCase
                null, // updateCardUseCase
                null, // moveCardUseCase
                null, // cloneCardUseCase
                deleteCardUseCase,
                null, // manageCardMemberUseCase
                null, // manageCardLabelUseCase
                failureHandler);

        when(jwt.getSubject()).thenReturn(TEST_USER_ID);
    }

    @Test
    @DisplayName("카드 삭제 성공 시 204 응답을 반환해야 한다")
    void deleteCard_withValidRequest_shouldReturn204() {
        // given
        when(deleteCardUseCase.deleteCard(any(DeleteCardCommand.class)))
                .thenReturn(Either.right(null));

        // when
        ResponseEntity<?> response = cardController.deleteCard(TEST_CARD_ID, httpRequest, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        verify(deleteCardUseCase).deleteCard(any(DeleteCardCommand.class));
    }

    @Test
    @DisplayName("카드 삭제 실패 시 failureHandler가 호출되어야 한다")
    void deleteCard_withFailure_shouldCallFailureHandler() {
        // given
        Failure failure = Failure.ofNotFound("카드를 찾을 수 없습니다");

        when(deleteCardUseCase.deleteCard(any(DeleteCardCommand.class)))
                .thenReturn(Either.left(failure));
        when(failureHandler.handleFailure(failure))
                .thenReturn(ResponseEntity.notFound().build());

        // when
        ResponseEntity<?> response = cardController.deleteCard(TEST_CARD_ID, httpRequest, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(deleteCardUseCase).deleteCard(any(DeleteCardCommand.class));
        verify(failureHandler).handleFailure(failure);
    }

    @Test
    @DisplayName("JWT에서 사용자 ID를 올바르게 추출해야 한다")
    void deleteCard_shouldExtractUserIdFromJwt() {
        // given
        when(deleteCardUseCase.deleteCard(any(DeleteCardCommand.class)))
                .thenReturn(Either.right(null));

        // when
        cardController.deleteCard(TEST_CARD_ID, httpRequest, jwt);

        // then
        verify(deleteCardUseCase).deleteCard(any(DeleteCardCommand.class));
    }
}
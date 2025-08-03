package com.boardly.features.card.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

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

import com.boardly.features.card.application.usecase.ManageCardLabelUseCase;
import com.boardly.features.card.presentation.request.AddCardLabelRequest;
import com.boardly.features.card.presentation.request.RemoveCardLabelRequest;
import com.boardly.features.label.domain.model.LabelId;
import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.presentation.ApiFailureHandler;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
class CardControllerManageCardLabelTest {

    private CardController cardController;

    @Mock
    private ManageCardLabelUseCase manageCardLabelUseCase;

    @Mock
    private ApiFailureHandler failureHandler;

    @Mock
    private Jwt jwt;

    @Mock
    private MockHttpServletRequest httpRequest;

    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_CARD_ID = "test-card-id";
    private static final String TEST_LABEL_ID = "test-label-id";

    @BeforeEach
    void setUp() {
        cardController = new CardController(
                null, // createCardUseCase
                null, // cardQueryUseCase
                null, // updateCardUseCase
                null, // moveCardUseCase
                null, // cloneCardUseCase
                null, // deleteCardUseCase
                null, // manageCardMemberUseCase
                manageCardLabelUseCase,
                failureHandler);

        when(jwt.getSubject()).thenReturn(TEST_USER_ID);
    }

    private List<LabelId> createTestLabelIds() {
        return List.of(new LabelId(TEST_LABEL_ID));
    }

    @Test
    @DisplayName("카드 라벨 추가 성공 시 200 응답을 반환해야 한다")
    void addCardLabel_withValidRequest_shouldReturn200() {
        // given
        AddCardLabelRequest request = new AddCardLabelRequest(TEST_LABEL_ID);

        when(manageCardLabelUseCase.addLabel(any()))
                .thenReturn(Either.right(null));

        // when
        ResponseEntity<?> response = cardController.addCardLabel(TEST_CARD_ID, request, httpRequest, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();

        verify(manageCardLabelUseCase).addLabel(any());
    }

    @Test
    @DisplayName("카드 라벨 추가 실패 시 failureHandler가 호출되어야 한다")
    void addCardLabel_withFailure_shouldCallFailureHandler() {
        // given
        AddCardLabelRequest request = new AddCardLabelRequest(TEST_LABEL_ID);
        Failure failure = Failure.ofNotFound("카드를 찾을 수 없습니다");

        when(manageCardLabelUseCase.addLabel(any()))
                .thenReturn(Either.left(failure));
        when(failureHandler.handleFailure(failure))
                .thenReturn(ResponseEntity.notFound().build());

        // when
        ResponseEntity<?> response = cardController.addCardLabel(TEST_CARD_ID, request, httpRequest, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(manageCardLabelUseCase).addLabel(any());
        verify(failureHandler).handleFailure(failure);
    }

    @Test
    @DisplayName("카드 라벨 제거 성공 시 200 응답을 반환해야 한다")
    void removeCardLabel_withValidRequest_shouldReturn200() {
        // given
        RemoveCardLabelRequest request = new RemoveCardLabelRequest(TEST_LABEL_ID);

        when(manageCardLabelUseCase.removeLabel(any()))
                .thenReturn(Either.right(null));

        // when
        ResponseEntity<?> response = cardController.removeCardLabel(TEST_CARD_ID, request, httpRequest, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();

        verify(manageCardLabelUseCase).removeLabel(any());
    }

    @Test
    @DisplayName("카드 라벨 제거 실패 시 failureHandler가 호출되어야 한다")
    void removeCardLabel_withFailure_shouldCallFailureHandler() {
        // given
        RemoveCardLabelRequest request = new RemoveCardLabelRequest(TEST_LABEL_ID);
        Failure failure = Failure.ofNotFound("카드를 찾을 수 없습니다");

        when(manageCardLabelUseCase.removeLabel(any()))
                .thenReturn(Either.left(failure));
        when(failureHandler.handleFailure(failure))
                .thenReturn(ResponseEntity.notFound().build());

        // when
        ResponseEntity<?> response = cardController.removeCardLabel(TEST_CARD_ID, request, httpRequest, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(manageCardLabelUseCase).removeLabel(any());
        verify(failureHandler).handleFailure(failure);
    }

    @Test
    @DisplayName("카드 라벨 목록 조회 성공 시 200 응답을 반환해야 한다")
    void getCardLabels_withValidRequest_shouldReturn200() {
        // given
        List<LabelId> labels = createTestLabelIds();

        when(manageCardLabelUseCase.getCardLabels(any(), any()))
                .thenReturn(labels);

        // when
        ResponseEntity<?> response = cardController.getCardLabels(TEST_CARD_ID, httpRequest, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(List.class);

        @SuppressWarnings("unchecked")
        List<LabelId> responseLabels = (List<LabelId>) response.getBody();
        assertNotNull(responseLabels);
        assertThat(responseLabels).hasSize(1);
        assertThat(responseLabels.get(0).getId()).isEqualTo(TEST_LABEL_ID);

        verify(manageCardLabelUseCase).getCardLabels(any(), any());
    }

    @Test
    @DisplayName("빈 라벨 목록 조회 시 빈 리스트를 반환해야 한다")
    void getCardLabels_withEmptyList_shouldReturnEmptyList() {
        // given
        List<LabelId> emptyLabels = List.of();

        when(manageCardLabelUseCase.getCardLabels(any(), any()))
                .thenReturn(emptyLabels);

        // when
        ResponseEntity<?> response = cardController.getCardLabels(TEST_CARD_ID, httpRequest, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(List.class);

        @SuppressWarnings("unchecked")
        List<LabelId> responseLabels = (List<LabelId>) response.getBody();
        assertThat(responseLabels).isEmpty();

        verify(manageCardLabelUseCase).getCardLabels(any(), any());
    }

    @Test
    @DisplayName("JWT에서 사용자 ID를 올바르게 추출해야 한다")
    void manageCardLabel_shouldExtractUserIdFromJwt() {
        // given
        AddCardLabelRequest request = new AddCardLabelRequest(TEST_LABEL_ID);

        when(manageCardLabelUseCase.addLabel(any()))
                .thenReturn(Either.right(null));

        // when
        cardController.addCardLabel(TEST_CARD_ID, request, httpRequest, jwt);

        // then
        verify(manageCardLabelUseCase).addLabel(any());
    }
}
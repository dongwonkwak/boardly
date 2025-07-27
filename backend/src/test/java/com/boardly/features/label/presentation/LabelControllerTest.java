package com.boardly.features.label.presentation;

import com.boardly.features.label.application.port.input.CreateLabelCommand;
import com.boardly.features.label.application.port.input.UpdateLabelCommand;
import com.boardly.features.label.application.port.input.DeleteLabelCommand;
import com.boardly.features.label.application.usecase.CreateLabelUseCase;
import com.boardly.features.label.application.usecase.GetLabelUseCase;
import com.boardly.features.label.application.usecase.UpdateLabelUseCase;
import com.boardly.features.label.application.usecase.DeleteLabelUseCase;
import com.boardly.features.label.domain.model.Label;
import com.boardly.features.label.domain.model.LabelId;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.features.label.presentation.request.CreateLabelRequest;
import com.boardly.features.label.presentation.request.UpdateLabelRequest;
import com.boardly.features.label.presentation.response.LabelResponse;
import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.presentation.ApiFailureHandler;
import com.boardly.shared.presentation.response.ErrorResponse;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LabelControllerTest {

    @Mock
    private CreateLabelUseCase createLabelUseCase;

    @Mock
    private GetLabelUseCase getLabelUseCase;

    @Mock
    private UpdateLabelUseCase updateLabelUseCase;

    @Mock
    private DeleteLabelUseCase deleteLabelUseCase;

    @Mock
    private ApiFailureHandler failureHandler;

    @Mock
    private Jwt jwt;

    private LabelController controller;

    @BeforeEach
    void setUp() {
        controller = new LabelController(
                createLabelUseCase,
                getLabelUseCase,
                updateLabelUseCase,
                deleteLabelUseCase,
                failureHandler);
    }

    private Label createSampleLabel(String labelId, String boardId, String name, String color, String userId) {
        return Label.restore(
                new LabelId(labelId),
                new BoardId(boardId),
                name,
                color,
                Instant.now(),
                Instant.now());
    }

    private Failure createSampleFailure(String message) {
        return Failure.ofInputError(message);
    }

    // ==================== CREATE LABEL TESTS ====================

    @Test
    @DisplayName("라벨 생성 성공")
    void createLabel_Success() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        String name = "긴급";
        String color = "#FF0000";
        String labelId = "label-123";

        CreateLabelRequest request = new CreateLabelRequest(boardId, name, color);
        Label createdLabel = createSampleLabel(labelId, boardId, name, color, userId);

        when(jwt.getSubject()).thenReturn(userId);
        when(createLabelUseCase.createLabel(any(CreateLabelCommand.class)))
                .thenReturn(Either.right(createdLabel));

        // when
        ResponseEntity<?> response = controller.createLabel(request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isInstanceOf(LabelResponse.class);

        LabelResponse labelResponse = (LabelResponse) response.getBody();
        assertThat(labelResponse.labelId()).isEqualTo(labelId);
        assertThat(labelResponse.boardId()).isEqualTo(boardId);
        assertThat(labelResponse.name()).isEqualTo(name);
        assertThat(labelResponse.color()).isEqualTo(color);
    }

    @Test
    @DisplayName("라벨 생성 실패 - 검증 오류")
    void createLabel_ValidationError() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        String name = "긴급";
        String color = "#FF0000";

        CreateLabelRequest request = new CreateLabelRequest(boardId, name, color);
        Failure failure = createSampleFailure("라벨 생성에 실패했습니다");
        ErrorResponse errorResponse = ErrorResponse.of("VALIDATION_ERROR", "라벨 생성에 실패했습니다");

        when(jwt.getSubject()).thenReturn(userId);
        when(createLabelUseCase.createLabel(any(CreateLabelCommand.class)))
                .thenReturn(Either.left(failure));
        when(failureHandler.handleFailure(failure))
                .thenReturn(ResponseEntity.badRequest().body(errorResponse));

        // when
        ResponseEntity<?> response = controller.createLabel(request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(ErrorResponse.class);
    }

    // ==================== GET LABEL TESTS ====================

    @Test
    @DisplayName("라벨 조회 성공")
    void getLabel_Success() throws Exception {
        // given
        String userId = "user-123";
        String labelId = "label-123";
        String boardId = "board-123";
        String name = "긴급";
        String color = "#FF0000";

        Label label = createSampleLabel(labelId, boardId, name, color, userId);

        when(jwt.getSubject()).thenReturn(userId);
        when(getLabelUseCase.getLabel(any(LabelId.class), any(UserId.class)))
                .thenReturn(Either.right(label));

        // when
        ResponseEntity<?> response = controller.getLabel(labelId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(LabelResponse.class);

        LabelResponse labelResponse = (LabelResponse) response.getBody();
        assertThat(labelResponse.labelId()).isEqualTo(labelId);
        assertThat(labelResponse.boardId()).isEqualTo(boardId);
        assertThat(labelResponse.name()).isEqualTo(name);
        assertThat(labelResponse.color()).isEqualTo(color);
    }

    @Test
    @DisplayName("라벨 조회 실패 - 라벨 없음")
    void getLabel_NotFound() throws Exception {
        // given
        String userId = "user-123";
        String labelId = "label-123";
        Failure failure = createSampleFailure("라벨을 찾을 수 없습니다");
        ErrorResponse errorResponse = ErrorResponse.of("NOT_FOUND", "라벨을 찾을 수 없습니다");

        when(jwt.getSubject()).thenReturn(userId);
        when(getLabelUseCase.getLabel(any(LabelId.class), any(UserId.class)))
                .thenReturn(Either.left(failure));
        when(failureHandler.handleFailure(failure))
                .thenReturn(ResponseEntity.notFound().build());

        // when
        ResponseEntity<?> response = controller.getLabel(labelId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ==================== GET BOARD LABELS TESTS ====================

    @Test
    @DisplayName("보드 라벨 목록 조회 성공")
    void getBoardLabels_Success() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        List<Label> labels = List.of(
                createSampleLabel("label-1", boardId, "긴급", "#FF0000", userId),
                createSampleLabel("label-2", boardId, "버그", "#FFA500", userId),
                createSampleLabel("label-3", boardId, "개선", "#008000", userId));

        when(jwt.getSubject()).thenReturn(userId);
        when(getLabelUseCase.getBoardLabels(any(BoardId.class), any(UserId.class)))
                .thenReturn(Either.right(labels));

        // when
        ResponseEntity<?> response = controller.getBoardLabels(boardId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(List.class);

        @SuppressWarnings("unchecked")
        List<LabelResponse> labelResponses = (List<LabelResponse>) response.getBody();
        assertThat(labelResponses).hasSize(3);
        assertThat(labelResponses.get(0).name()).isEqualTo("긴급");
        assertThat(labelResponses.get(1).name()).isEqualTo("버그");
        assertThat(labelResponses.get(2).name()).isEqualTo("개선");
    }

    @Test
    @DisplayName("보드 라벨 목록 조회 성공 - 빈 목록")
    void getBoardLabels_Success_EmptyList() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        List<Label> labels = List.of();

        when(jwt.getSubject()).thenReturn(userId);
        when(getLabelUseCase.getBoardLabels(any(BoardId.class), any(UserId.class)))
                .thenReturn(Either.right(labels));

        // when
        ResponseEntity<?> response = controller.getBoardLabels(boardId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(List.class);

        @SuppressWarnings("unchecked")
        List<LabelResponse> labelResponses = (List<LabelResponse>) response.getBody();
        assertThat(labelResponses).isEmpty();
    }

    @Test
    @DisplayName("보드 라벨 목록 조회 실패 - 보드 없음")
    void getBoardLabels_NotFound() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        Failure failure = createSampleFailure("보드를 찾을 수 없습니다");
        ErrorResponse errorResponse = ErrorResponse.of("NOT_FOUND", "보드를 찾을 수 없습니다");

        when(jwt.getSubject()).thenReturn(userId);
        when(getLabelUseCase.getBoardLabels(any(BoardId.class), any(UserId.class)))
                .thenReturn(Either.left(failure));
        when(failureHandler.handleFailure(failure))
                .thenReturn(ResponseEntity.notFound().build());

        // when
        ResponseEntity<?> response = controller.getBoardLabels(boardId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ==================== UPDATE LABEL TESTS ====================

    @Test
    @DisplayName("라벨 수정 성공")
    void updateLabel_Success() throws Exception {
        // given
        String userId = "user-123";
        String labelId = "label-123";
        String boardId = "board-123";
        String newName = "매우 긴급";
        String newColor = "#FF0000";

        UpdateLabelRequest request = new UpdateLabelRequest(newName, newColor);
        Label updatedLabel = createSampleLabel(labelId, boardId, newName, newColor, userId);

        when(jwt.getSubject()).thenReturn(userId);
        when(updateLabelUseCase.updateLabel(any(UpdateLabelCommand.class)))
                .thenReturn(Either.right(updatedLabel));

        // when
        ResponseEntity<?> response = controller.updateLabel(labelId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(LabelResponse.class);

        LabelResponse labelResponse = (LabelResponse) response.getBody();
        assertThat(labelResponse.labelId()).isEqualTo(labelId);
        assertThat(labelResponse.name()).isEqualTo(newName);
        assertThat(labelResponse.color()).isEqualTo(newColor);
    }

    @Test
    @DisplayName("라벨 수정 실패 - 권한 없음")
    void updateLabel_Forbidden() throws Exception {
        // given
        String userId = "user-123";
        String labelId = "label-123";
        String newName = "매우 긴급";
        String newColor = "#FF0000";

        UpdateLabelRequest request = new UpdateLabelRequest(newName, newColor);
        Failure failure = createSampleFailure("라벨 수정 권한이 없습니다");
        ErrorResponse errorResponse = ErrorResponse.of("FORBIDDEN", "라벨 수정 권한이 없습니다");

        when(jwt.getSubject()).thenReturn(userId);
        when(updateLabelUseCase.updateLabel(any(UpdateLabelCommand.class)))
                .thenReturn(Either.left(failure));
        when(failureHandler.handleFailure(failure))
                .thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse));

        // when
        ResponseEntity<?> response = controller.updateLabel(labelId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isInstanceOf(ErrorResponse.class);
    }

    @Test
    @DisplayName("라벨 수정 실패 - 라벨 없음")
    void updateLabel_NotFound() throws Exception {
        // given
        String userId = "user-123";
        String labelId = "label-123";
        String newName = "매우 긴급";
        String newColor = "#FF0000";

        UpdateLabelRequest request = new UpdateLabelRequest(newName, newColor);
        Failure failure = createSampleFailure("라벨을 찾을 수 없습니다");
        ErrorResponse errorResponse = ErrorResponse.of("NOT_FOUND", "라벨을 찾을 수 없습니다");

        when(jwt.getSubject()).thenReturn(userId);
        when(updateLabelUseCase.updateLabel(any(UpdateLabelCommand.class)))
                .thenReturn(Either.left(failure));
        when(failureHandler.handleFailure(failure))
                .thenReturn(ResponseEntity.notFound().build());

        // when
        ResponseEntity<?> response = controller.updateLabel(labelId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ==================== DELETE LABEL TESTS ====================

    @Test
    @DisplayName("라벨 삭제 성공")
    void deleteLabel_Success() throws Exception {
        // given
        String userId = "user-123";
        String labelId = "label-123";

        when(jwt.getSubject()).thenReturn(userId);
        when(deleteLabelUseCase.deleteLabel(any(DeleteLabelCommand.class)))
                .thenReturn(Either.right(null));

        // when
        ResponseEntity<?> response = controller.deleteLabel(labelId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    @DisplayName("라벨 삭제 실패 - 권한 없음")
    void deleteLabel_Forbidden() throws Exception {
        // given
        String userId = "user-123";
        String labelId = "label-123";
        Failure failure = createSampleFailure("라벨 삭제 권한이 없습니다");
        ErrorResponse errorResponse = ErrorResponse.of("FORBIDDEN", "라벨 삭제 권한이 없습니다");

        when(jwt.getSubject()).thenReturn(userId);
        when(deleteLabelUseCase.deleteLabel(any(DeleteLabelCommand.class)))
                .thenReturn(Either.left(failure));
        when(failureHandler.handleFailure(failure))
                .thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse));

        // when
        ResponseEntity<?> response = controller.deleteLabel(labelId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isInstanceOf(ErrorResponse.class);
    }

    @Test
    @DisplayName("라벨 삭제 실패 - 라벨 없음")
    void deleteLabel_NotFound() throws Exception {
        // given
        String userId = "user-123";
        String labelId = "label-123";
        Failure failure = createSampleFailure("라벨을 찾을 수 없습니다");
        ErrorResponse errorResponse = ErrorResponse.of("NOT_FOUND", "라벨을 찾을 수 없습니다");

        when(jwt.getSubject()).thenReturn(userId);
        when(deleteLabelUseCase.deleteLabel(any(DeleteLabelCommand.class)))
                .thenReturn(Either.left(failure));
        when(failureHandler.handleFailure(failure))
                .thenReturn(ResponseEntity.notFound().build());

        // when
        ResponseEntity<?> response = controller.deleteLabel(labelId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
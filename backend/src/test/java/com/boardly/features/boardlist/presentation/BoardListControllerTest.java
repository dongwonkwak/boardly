package com.boardly.features.boardlist.presentation;

import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.boardlist.application.port.input.*;
import com.boardly.features.boardlist.application.usecase.*;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListColor;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.presentation.request.CreateBoardListRequest;
import com.boardly.features.boardlist.presentation.request.UpdateBoardListRequest;
import com.boardly.features.boardlist.presentation.request.UpdateBoardListPositionRequest;
import com.boardly.features.boardlist.presentation.response.BoardListResponse;
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
class BoardListControllerTest {

    @Mock
    private CreateBoardListUseCase createBoardListUseCase;

    @Mock
    private GetBoardListsUseCase getBoardListsUseCase;

    @Mock
    private UpdateBoardListUseCase updateBoardListUseCase;

    @Mock
    private DeleteBoardListUseCase deleteBoardListUseCase;

    @Mock
    private UpdateBoardListPositionUseCase updateBoardListPositionUseCase;

    @Mock
    private ApiFailureHandler failureHandler;

    @Mock
    private Jwt jwt;

    private BoardListController controller;

    @BeforeEach
    void setUp() {
        controller = new BoardListController(
                createBoardListUseCase,
                getBoardListsUseCase,
                updateBoardListUseCase,
                deleteBoardListUseCase,
                updateBoardListPositionUseCase,
                failureHandler);
    }

    private BoardList createSampleBoardList(String listId, String title, String description,
            int position, String color, String boardId) {
        return BoardList.builder()
                .listId(new ListId(listId))
                .title(title)
                .description(description)
                .position(position)
                .color(ListColor.of(color))
                .boardId(new BoardId(boardId))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("보드 리스트 목록 조회 성공")
    void getBoardLists_Success() throws Exception {
        // given
        String boardId = "board-123";
        String userId = "user-123";
        List<BoardList> boardLists = List.of(
                createSampleBoardList("list-1", "할 일", "해야 할 일들", 0, "#0079BF", boardId),
                createSampleBoardList("list-2", "진행 중", "진행 중인 작업들", 1, "#FF6B6B", boardId),
                createSampleBoardList("list-3", "완료", "완료된 작업들", 2, "#70B500", boardId));

        when(jwt.getSubject()).thenReturn(userId);
        when(getBoardListsUseCase.getBoardLists(any(GetBoardListsCommand.class)))
                .thenReturn(Either.right(boardLists));

        // when
        ResponseEntity<?> response = controller.getBoardLists(boardId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<BoardListResponse> responses = (List<BoardListResponse>) response.getBody();
        assertThat(responses).hasSize(3);
        assertThat(responses.get(0).listId()).isEqualTo("list-1");
        assertThat(responses.get(0).title()).isEqualTo("할 일");
        assertThat(responses.get(0).position()).isEqualTo(0);
        assertThat(responses.get(1).listId()).isEqualTo("list-2");
        assertThat(responses.get(1).title()).isEqualTo("진행 중");
        assertThat(responses.get(1).position()).isEqualTo(1);
        assertThat(responses.get(2).listId()).isEqualTo("list-3");
        assertThat(responses.get(2).title()).isEqualTo("완료");
        assertThat(responses.get(2).position()).isEqualTo(2);
    }

    @Test
    @DisplayName("보드 리스트 목록 조회 성공 - 빈 목록")
    void getBoardLists_Success_EmptyList() throws Exception {
        // given
        String boardId = "board-123";
        String userId = "user-123";

        when(jwt.getSubject()).thenReturn(userId);
        when(getBoardListsUseCase.getBoardLists(any(GetBoardListsCommand.class)))
                .thenReturn(Either.right(List.of()));

        // when
        ResponseEntity<?> response = controller.getBoardLists(boardId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<BoardListResponse> responses = (List<BoardListResponse>) response.getBody();
        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("보드 리스트 목록 조회 실패 - 권한 없음")
    void getBoardLists_Forbidden() throws Exception {
        // given
        String boardId = "board-123";
        String userId = "user-123";
        Failure forbiddenFailure = Failure.ofForbidden("BOARD_ACCESS_DENIED");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(getBoardListsUseCase.getBoardLists(any(GetBoardListsCommand.class)))
                .thenReturn(Either.left(forbiddenFailure));
        when(failureHandler.handleFailure(forbiddenFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.getBoardLists(boardId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("보드 리스트 목록 조회 실패 - 보드 없음")
    void getBoardLists_NotFound() throws Exception {
        // given
        String boardId = "board-123";
        String userId = "user-123";
        Failure notFoundFailure = Failure.ofNotFound("BOARD_NOT_FOUND");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(getBoardListsUseCase.getBoardLists(any(GetBoardListsCommand.class)))
                .thenReturn(Either.left(notFoundFailure));
        when(failureHandler.handleFailure(notFoundFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.getBoardLists(boardId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("보드 리스트 생성 성공")
    void createBoardList_Success() throws Exception {
        // given
        String boardId = "board-123";
        String userId = "user-123";
        CreateBoardListRequest request = new CreateBoardListRequest("새 리스트", "새 리스트 설명", "#0079BF");
        BoardList createdList = createSampleBoardList("list-new", "새 리스트", "새 리스트 설명", 0, "#0079BF", boardId);

        when(jwt.getSubject()).thenReturn(userId);
        when(createBoardListUseCase.createBoardList(any(CreateBoardListCommand.class)))
                .thenReturn(Either.right(createdList));

        // when
        ResponseEntity<?> response = controller.createBoardList(boardId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        BoardListResponse responseBody = (BoardListResponse) response.getBody();
        assertThat(responseBody.listId()).isEqualTo("list-new");
        assertThat(responseBody.title()).isEqualTo("새 리스트");
        assertThat(responseBody.description()).isEqualTo("새 리스트 설명");
        assertThat(responseBody.color()).isEqualTo("#0079BF");
        assertThat(responseBody.boardId()).isEqualTo(boardId);
    }

    @Test
    @DisplayName("보드 리스트 생성 성공 - 기본 색상 사용")
    void createBoardList_Success_DefaultColor() throws Exception {
        // given
        String boardId = "board-123";
        String userId = "user-123";
        CreateBoardListRequest request = new CreateBoardListRequest("새 리스트", "새 리스트 설명", null);
        BoardList createdList = createSampleBoardList("list-new", "새 리스트", "새 리스트 설명", 0, "#0079BF", boardId);

        when(jwt.getSubject()).thenReturn(userId);
        when(createBoardListUseCase.createBoardList(any(CreateBoardListCommand.class)))
                .thenReturn(Either.right(createdList));

        // when
        ResponseEntity<?> response = controller.createBoardList(boardId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        BoardListResponse responseBody = (BoardListResponse) response.getBody();
        assertThat(responseBody.title()).isEqualTo("새 리스트");
        assertThat(responseBody.color()).isEqualTo("#0079BF");
    }

    @Test
    @DisplayName("보드 리스트 생성 실패 - 검증 오류")
    void createBoardList_ValidationError() throws Exception {
        // given
        String boardId = "board-123";
        String userId = "user-123";
        CreateBoardListRequest request = new CreateBoardListRequest("", "", "#0079BF"); // 빈 제목
        Failure.FieldViolation violation = Failure.FieldViolation.builder()
                .field("title")
                .message("리스트 제목은 필수입니다")
                .rejectedValue("")
                .build();
        Failure validationFailure = Failure.ofValidation("INVALID_INPUT", List.of(violation));
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .build();

        when(jwt.getSubject()).thenReturn(userId);
        when(createBoardListUseCase.createBoardList(any(CreateBoardListCommand.class)))
                .thenReturn(Either.left(validationFailure));
        when(failureHandler.handleFailure(validationFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.createBoardList(boardId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("보드 리스트 생성 실패 - 권한 없음")
    void createBoardList_Forbidden() throws Exception {
        // given
        String boardId = "board-123";
        String userId = "user-123";
        CreateBoardListRequest request = new CreateBoardListRequest("새 리스트", "설명", "#0079BF");
        Failure forbiddenFailure = Failure.ofForbidden("BOARD_ACCESS_DENIED");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(createBoardListUseCase.createBoardList(any(CreateBoardListCommand.class)))
                .thenReturn(Either.left(forbiddenFailure));
        when(failureHandler.handleFailure(forbiddenFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.createBoardList(boardId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("보드 리스트 수정 성공")
    void updateBoardList_Success() throws Exception {
        // given
        String listId = "list-123";
        String userId = "user-123";
        UpdateBoardListRequest request = new UpdateBoardListRequest("수정된 리스트", "수정된 설명", "#FF6B6B");
        BoardList updatedList = createSampleBoardList(listId, "수정된 리스트", "수정된 설명", 0, "#FF6B6B", "board-123");

        when(jwt.getSubject()).thenReturn(userId);
        when(updateBoardListUseCase.updateBoardList(any(UpdateBoardListCommand.class)))
                .thenReturn(Either.right(updatedList));

        // when
        ResponseEntity<?> response = controller.updateBoardList(listId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BoardListResponse responseBody = (BoardListResponse) response.getBody();
        assertThat(responseBody.listId()).isEqualTo(listId);
        assertThat(responseBody.title()).isEqualTo("수정된 리스트");
        assertThat(responseBody.description()).isEqualTo("수정된 설명");
        assertThat(responseBody.color()).isEqualTo("#0079BF");
    }

    @Test
    @DisplayName("보드 리스트 수정 실패 - 리스트 없음")
    void updateBoardList_NotFound() throws Exception {
        // given
        String listId = "list-123";
        String userId = "user-123";
        UpdateBoardListRequest request = new UpdateBoardListRequest("수정된 리스트", "수정된 설명", "#FF6B6B");
        Failure notFoundFailure = Failure.ofNotFound("BOARD_LIST_NOT_FOUND");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(updateBoardListUseCase.updateBoardList(any(UpdateBoardListCommand.class)))
                .thenReturn(Either.left(notFoundFailure));
        when(failureHandler.handleFailure(notFoundFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.updateBoardList(listId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("보드 리스트 수정 실패 - 권한 없음")
    void updateBoardList_Forbidden() throws Exception {
        // given
        String listId = "list-123";
        String userId = "user-123";
        UpdateBoardListRequest request = new UpdateBoardListRequest("수정된 리스트", "수정된 설명", "#FF6B6B");
        Failure forbiddenFailure = Failure.ofForbidden("BOARD_LIST_ACCESS_DENIED");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(updateBoardListUseCase.updateBoardList(any(UpdateBoardListCommand.class)))
                .thenReturn(Either.left(forbiddenFailure));
        when(failureHandler.handleFailure(forbiddenFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.updateBoardList(listId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("보드 리스트 삭제 성공")
    void deleteBoardList_Success() throws Exception {
        // given
        String listId = "list-123";
        String userId = "user-123";

        when(jwt.getSubject()).thenReturn(userId);
        when(deleteBoardListUseCase.deleteBoardList(any(DeleteBoardListCommand.class)))
                .thenReturn(Either.right(null));

        // when
        ResponseEntity<?> response = controller.deleteBoardList(listId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    @DisplayName("보드 리스트 삭제 실패 - 리스트 없음")
    void deleteBoardList_NotFound() throws Exception {
        // given
        String listId = "list-123";
        String userId = "user-123";
        Failure notFoundFailure = Failure.ofNotFound("BOARD_LIST_NOT_FOUND");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(deleteBoardListUseCase.deleteBoardList(any(DeleteBoardListCommand.class)))
                .thenReturn(Either.left(notFoundFailure));
        when(failureHandler.handleFailure(notFoundFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.deleteBoardList(listId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("보드 리스트 삭제 실패 - 권한 없음")
    void deleteBoardList_Forbidden() throws Exception {
        // given
        String listId = "list-123";
        String userId = "user-123";
        Failure forbiddenFailure = Failure.ofForbidden("BOARD_LIST_ACCESS_DENIED");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(deleteBoardListUseCase.deleteBoardList(any(DeleteBoardListCommand.class)))
                .thenReturn(Either.left(forbiddenFailure));
        when(failureHandler.handleFailure(forbiddenFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.deleteBoardList(listId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("보드 리스트 위치 변경 성공")
    void updateBoardListPosition_Success() throws Exception {
        // given
        String listId = "list-123";
        String userId = "user-123";
        String boardId = "board-123";
        UpdateBoardListPositionRequest request = new UpdateBoardListPositionRequest(2);

        List<BoardList> updatedLists = List.of(
                createSampleBoardList("list-1", "할 일", "해야 할 일들", 0, "#0079BF", boardId),
                createSampleBoardList("list-2", "진행 중", "진행 중인 작업들", 1, "#FF6B6B", boardId),
                createSampleBoardList("list-123", "수정된 리스트", "설명", 2, "#70B500", boardId));

        when(jwt.getSubject()).thenReturn(userId);
        when(updateBoardListPositionUseCase.updateBoardListPosition(any(UpdateBoardListPositionCommand.class)))
                .thenReturn(Either.right(updatedLists));

        // when
        ResponseEntity<?> response = controller.updateBoardListPosition(listId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BoardListResponse responseBody = (BoardListResponse) response.getBody();
        assertThat(responseBody.listId()).isEqualTo("list-123");
        assertThat(responseBody.position()).isEqualTo(2);
    }

    @Test
    @DisplayName("보드 리스트 위치 변경 성공 - 첫 번째 위치로 이동")
    void updateBoardListPosition_Success_FirstPosition() throws Exception {
        // given
        String listId = "list-123";
        String userId = "user-123";
        String boardId = "board-123";
        UpdateBoardListPositionRequest request = new UpdateBoardListPositionRequest(0);

        List<BoardList> updatedLists = List.of(
                createSampleBoardList("list-123", "수정된 리스트", "설명", 0, "#70B500", boardId),
                createSampleBoardList("list-1", "할 일", "해야 할 일들", 1, "#0079BF", boardId),
                createSampleBoardList("list-2", "진행 중", "진행 중인 작업들", 2, "#FF6B6B", boardId));

        when(jwt.getSubject()).thenReturn(userId);
        when(updateBoardListPositionUseCase.updateBoardListPosition(any(UpdateBoardListPositionCommand.class)))
                .thenReturn(Either.right(updatedLists));

        // when
        ResponseEntity<?> response = controller.updateBoardListPosition(listId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BoardListResponse responseBody = (BoardListResponse) response.getBody();
        assertThat(responseBody.listId()).isEqualTo("list-123");
        assertThat(responseBody.position()).isEqualTo(0);
    }

    @Test
    @DisplayName("보드 리스트 위치 변경 실패 - 리스트 없음")
    void updateBoardListPosition_NotFound() throws Exception {
        // given
        String listId = "list-123";
        String userId = "user-123";
        UpdateBoardListPositionRequest request = new UpdateBoardListPositionRequest(2);
        Failure notFoundFailure = Failure.ofNotFound("BOARD_LIST_NOT_FOUND");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(updateBoardListPositionUseCase.updateBoardListPosition(any(UpdateBoardListPositionCommand.class)))
                .thenReturn(Either.left(notFoundFailure));
        when(failureHandler.handleFailure(notFoundFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.updateBoardListPosition(listId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("보드 리스트 위치 변경 실패 - 권한 없음")
    void updateBoardListPosition_Forbidden() throws Exception {
        // given
        String listId = "list-123";
        String userId = "user-123";
        UpdateBoardListPositionRequest request = new UpdateBoardListPositionRequest(2);
        Failure forbiddenFailure = Failure.ofForbidden("BOARD_LIST_ACCESS_DENIED");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(updateBoardListPositionUseCase.updateBoardListPosition(any(UpdateBoardListPositionCommand.class)))
                .thenReturn(Either.left(forbiddenFailure));
        when(failureHandler.handleFailure(forbiddenFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.updateBoardListPosition(listId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("보드 리스트 위치 변경 실패 - 잘못된 위치")
    void updateBoardListPosition_InvalidPosition() throws Exception {
        // given
        String listId = "list-123";
        String userId = "user-123";
        UpdateBoardListPositionRequest request = new UpdateBoardListPositionRequest(-1);
        Failure.FieldViolation violation = Failure.FieldViolation.builder()
                .field("position")
                .message("위치는 0 이상이어야 합니다")
                .rejectedValue(-1)
                .build();
        Failure validationFailure = Failure.ofValidation("INVALID_POSITION", List.of(violation));
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .build();

        when(jwt.getSubject()).thenReturn(userId);
        when(updateBoardListPositionUseCase.updateBoardListPosition(any(UpdateBoardListPositionCommand.class)))
                .thenReturn(Either.left(validationFailure));
        when(failureHandler.handleFailure(validationFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.updateBoardListPosition(listId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("보드 리스트 생성 - 설명 없이")
    void createBoardList_WithoutDescription() throws Exception {
        // given
        String boardId = "board-123";
        String userId = "user-123";
        CreateBoardListRequest request = new CreateBoardListRequest("새 리스트", null, "#0079BF");
        BoardList createdList = createSampleBoardList("list-new", "새 리스트", null, 0, "#0079BF", boardId);

        when(jwt.getSubject()).thenReturn(userId);
        when(createBoardListUseCase.createBoardList(any(CreateBoardListCommand.class)))
                .thenReturn(Either.right(createdList));

        // when
        ResponseEntity<?> response = controller.createBoardList(boardId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        BoardListResponse responseBody = (BoardListResponse) response.getBody();
        assertThat(responseBody.title()).isEqualTo("새 리스트");
        assertThat(responseBody.description()).isNull();
    }

    @Test
    @DisplayName("보드 리스트 수정 - 색상 없이")
    void updateBoardList_WithoutColor() throws Exception {
        // given
        String listId = "list-123";
        String userId = "user-123";
        UpdateBoardListRequest request = new UpdateBoardListRequest("수정된 리스트", "수정된 설명", null);
        BoardList updatedList = createSampleBoardList(listId, "수정된 리스트", "수정된 설명", 0, "#0079BF", "board-123");

        when(jwt.getSubject()).thenReturn(userId);
        when(updateBoardListUseCase.updateBoardList(any(UpdateBoardListCommand.class)))
                .thenReturn(Either.right(updatedList));

        // when
        ResponseEntity<?> response = controller.updateBoardList(listId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BoardListResponse responseBody = (BoardListResponse) response.getBody();
        assertThat(responseBody.title()).isEqualTo("수정된 리스트");
        assertThat(responseBody.description()).isEqualTo("수정된 설명");
    }
}
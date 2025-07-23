package com.boardly.features.boardlist.presentation;

import com.boardly.features.boardlist.application.port.input.*;
import com.boardly.features.boardlist.application.usecase.*;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.boardlist.domain.model.ListColor;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.presentation.request.CreateBoardListRequest;
import com.boardly.features.boardlist.presentation.request.UpdateBoardListRequest;
import com.boardly.features.boardlist.presentation.request.UpdateBoardListPositionRequest;
import com.boardly.features.boardlist.presentation.response.BoardListResponse;
import com.boardly.features.board.domain.model.BoardId;
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

    // ==================== GET BOARD LISTS TESTS ====================

    @Test
    @DisplayName("보드 리스트 목록 조회 성공")
    void getBoardLists_Success() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        List<BoardList> boardLists = List.of(
                createSampleBoardList("list-1", "할 일", "해야 할 일들", 0, "#0079BF", boardId),
                createSampleBoardList("list-2", "진행 중", "진행 중인 작업들", 1, "#FF6B6B", boardId),
                createSampleBoardList("list-3", "완료", "완료된 작업들", 2, "#51C878", boardId));

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
        String userId = "user-123";
        String boardId = "board-123";

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
        String userId = "user-123";
        String boardId = "board-123";
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
        String userId = "user-123";
        String boardId = "board-123";
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

    // ==================== CREATE BOARD LIST TESTS ====================

    @Test
    @DisplayName("보드 리스트 생성 성공")
    void createBoardList_Success() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        String expectedColor = "#B04632"; // Red (유효한 색상)
        CreateBoardListRequest request = new CreateBoardListRequest("새 리스트", "새 리스트 설명", expectedColor);

        when(jwt.getSubject()).thenReturn(userId);
        when(createBoardListUseCase.createBoardList(any(CreateBoardListCommand.class)))
                .thenAnswer(invocation -> {
                    CreateBoardListCommand cmd = invocation.getArgument(0);
                    // 실제 요청된 색상을 사용하여 BoardList 생성
                    String color = cmd.color() != null ? cmd.color().color() : "#0079BF";
                    BoardList list = createSampleBoardList("list-new", cmd.title(),
                            cmd.description(), 0, color,
                            cmd.boardId().getId());
                    return Either.right(list);
                });

        // when
        ResponseEntity<?> response = controller.createBoardList(boardId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        BoardListResponse responseBody = (BoardListResponse) response.getBody();
        assertThat(responseBody.listId()).isEqualTo("list-new");
        assertThat(responseBody.title()).isEqualTo("새 리스트");
        assertThat(responseBody.description()).isEqualTo("새 리스트 설명");
        assertThat(responseBody.color()).isEqualTo(expectedColor);
        assertThat(responseBody.boardId()).isEqualTo(boardId);
    }

    @Test
    @DisplayName("보드 리스트 생성 성공 - 기본 색상 사용")
    void createBoardList_Success_DefaultColor() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        String defaultColor = "#0079BF";
        CreateBoardListRequest request = new CreateBoardListRequest("새 리스트", "새 리스트 설명", null);
        BoardList createdList = createSampleBoardList("list-new", "새 리스트", "새 리스트 설명", 0, defaultColor,
                boardId);

        when(jwt.getSubject()).thenReturn(userId);
        when(createBoardListUseCase.createBoardList(any(CreateBoardListCommand.class)))
                .thenReturn(Either.right(createdList));

        // when
        ResponseEntity<?> response = controller.createBoardList(boardId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        BoardListResponse responseBody = (BoardListResponse) response.getBody();
        assertThat(responseBody.color()).isEqualTo(defaultColor);
    }

    @Test
    @DisplayName("보드 리스트 생성 실패 - 권한 없음")
    void createBoardList_Forbidden() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        CreateBoardListRequest request = new CreateBoardListRequest("새 리스트", "새 리스트 설명", "#FF6B6B");
        Failure forbiddenFailure = Failure.ofForbidden("BOARD_WRITE_DENIED");
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
    @DisplayName("보드 리스트 생성 실패 - 보드 없음")
    void createBoardList_NotFound() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        CreateBoardListRequest request = new CreateBoardListRequest("새 리스트", "새 리스트 설명", "#FF6B6B");
        Failure notFoundFailure = Failure.ofNotFound("BOARD_NOT_FOUND");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(createBoardListUseCase.createBoardList(any(CreateBoardListCommand.class)))
                .thenReturn(Either.left(notFoundFailure));
        when(failureHandler.handleFailure(notFoundFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.createBoardList(boardId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("보드 리스트 생성 실패 - 검증 오류")
    void createBoardList_ValidationError() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        CreateBoardListRequest request = new CreateBoardListRequest("", "새 리스트 설명", "#FF6B6B");
        List<Failure.FieldViolation> violations = List.of(
                Failure.FieldViolation.builder()
                        .field("title")
                        .message("리스트 제목은 필수입니다")
                        .rejectedValue("")
                        .build());
        Failure validationFailure = Failure.ofValidation("TITLE_REQUIRED", violations);
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

    // ==================== UPDATE BOARD LIST TESTS ====================

    @Test
    @DisplayName("보드 리스트 수정 성공")
    void updateBoardList_Success() throws Exception {
        // given
        String userId = "user-123";
        String listId = "list-123";
        String expectedColor = "#519839"; // Green (유효한 색상)
        UpdateBoardListRequest request = new UpdateBoardListRequest("수정된 리스트", "수정된 설명", expectedColor);

        when(jwt.getSubject()).thenReturn(userId);
        when(updateBoardListUseCase.updateBoardList(any(UpdateBoardListCommand.class)))
                .thenAnswer(invocation -> {
                    UpdateBoardListCommand cmd = invocation.getArgument(0);
                    // 실제 요청된 색상을 사용하여 BoardList 생성
                    String color = cmd.color() != null ? cmd.color().color() : "#0079BF";
                    BoardList list = createSampleBoardList(cmd.listId().getId(), cmd.title(),
                            cmd.description(), 0,
                            color, "board-123");
                    return Either.right(list);
                });

        // when
        ResponseEntity<?> response = controller.updateBoardList(listId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BoardListResponse responseBody = (BoardListResponse) response.getBody();
        assertThat(responseBody.listId()).isEqualTo(listId);
        assertThat(responseBody.title()).isEqualTo("수정된 리스트");
        assertThat(responseBody.description()).isEqualTo("수정된 설명");
        assertThat(responseBody.color()).isEqualTo(expectedColor);
    }

    @Test
    @DisplayName("보드 리스트 수정 성공 - 색상 변경 없음")
    void updateBoardList_Success_NoColorChange() throws Exception {
        // given
        String userId = "user-123";
        String listId = "list-123";
        String originalColor = "#0079BF";
        UpdateBoardListRequest request = new UpdateBoardListRequest("수정된 리스트", "수정된 설명", null);
        BoardList updatedList = createSampleBoardList(listId, "수정된 리스트", "수정된 설명", 0, originalColor,
                "board-123");

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

    @Test
    @DisplayName("보드 리스트 수정 실패 - 권한 없음")
    void updateBoardList_Forbidden() throws Exception {
        // given
        String userId = "user-123";
        String listId = "list-123";
        UpdateBoardListRequest request = new UpdateBoardListRequest("수정된 리스트", "수정된 설명", "#51C878");
        Failure forbiddenFailure = Failure.ofForbidden("LIST_UPDATE_DENIED");
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
    @DisplayName("보드 리스트 수정 실패 - 리스트 없음")
    void updateBoardList_NotFound() throws Exception {
        // given
        String userId = "user-123";
        String listId = "list-123";
        UpdateBoardListRequest request = new UpdateBoardListRequest("수정된 리스트", "수정된 설명", "#51C878");
        Failure notFoundFailure = Failure.ofNotFound("LIST_NOT_FOUND");
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
    @DisplayName("보드 리스트 수정 실패 - 검증 오류")
    void updateBoardList_ValidationError() throws Exception {
        // given
        String userId = "user-123";
        String listId = "list-123";
        UpdateBoardListRequest request = new UpdateBoardListRequest("", "수정된 설명", "#51C878");
        List<Failure.FieldViolation> violations = List.of(
                Failure.FieldViolation.builder()
                        .field("title")
                        .message("리스트 제목은 필수입니다")
                        .rejectedValue("")
                        .build());
        Failure validationFailure = Failure.ofValidation("TITLE_REQUIRED", violations);
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .build();

        when(jwt.getSubject()).thenReturn(userId);
        when(updateBoardListUseCase.updateBoardList(any(UpdateBoardListCommand.class)))
                .thenReturn(Either.left(validationFailure));
        when(failureHandler.handleFailure(validationFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.updateBoardList(listId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // ==================== DELETE BOARD LIST TESTS ====================

    @Test
    @DisplayName("보드 리스트 삭제 성공")
    void deleteBoardList_Success() throws Exception {
        // given
        String userId = "user-123";
        String listId = "list-123";

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
    @DisplayName("보드 리스트 삭제 실패 - 권한 없음")
    void deleteBoardList_Forbidden() throws Exception {
        // given
        String userId = "user-123";
        String listId = "list-123";
        Failure forbiddenFailure = Failure.ofForbidden("LIST_DELETE_DENIED");
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
    @DisplayName("보드 리스트 삭제 실패 - 리스트 없음")
    void deleteBoardList_NotFound() throws Exception {
        // given
        String userId = "user-123";
        String listId = "list-123";
        Failure notFoundFailure = Failure.ofNotFound("LIST_NOT_FOUND");
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
    @DisplayName("보드 리스트 삭제 실패 - 카드가 있는 리스트")
    void deleteBoardList_Conflict_ListHasCards() throws Exception {
        // given
        String userId = "user-123";
        String listId = "list-123";
        Failure conflictFailure = Failure.ofConflict("LIST_HAS_CARDS");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.CONFLICT).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(deleteBoardListUseCase.deleteBoardList(any(DeleteBoardListCommand.class)))
                .thenReturn(Either.left(conflictFailure));
        when(failureHandler.handleFailure(conflictFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.deleteBoardList(listId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    // ==================== UPDATE BOARD LIST POSITION TESTS ====================

    @Test
    @DisplayName("보드 리스트 위치 변경 성공")
    void updateBoardListPosition_Success() throws Exception {
        // given
        String userId = "user-123";
        String listId = "list-123";
        UpdateBoardListPositionRequest request = new UpdateBoardListPositionRequest(2);
        List<BoardList> updatedLists = List.of(
                createSampleBoardList("list-1", "할 일", "해야 할 일들", 0, "#0079BF", "board-123"),
                createSampleBoardList("list-2", "진행 중", "진행 중인 작업들", 1, "#FF6B6B", "board-123"),
                createSampleBoardList(listId, "완료", "완료된 작업들", 2, "#51C878", "board-123"));

        when(jwt.getSubject()).thenReturn(userId);
        when(updateBoardListPositionUseCase.updateBoardListPosition(any(UpdateBoardListPositionCommand.class)))
                .thenReturn(Either.right(updatedLists));

        // when
        ResponseEntity<?> response = controller.updateBoardListPosition(listId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BoardListResponse responseBody = (BoardListResponse) response.getBody();
        assertThat(responseBody.listId()).isEqualTo(listId);
        assertThat(responseBody.position()).isEqualTo(2);
        assertThat(responseBody.title()).isEqualTo("완료");
    }

    @Test
    @DisplayName("보드 리스트 위치 변경 성공 - 첫 번째 위치로")
    void updateBoardListPosition_Success_FirstPosition() throws Exception {
        // given
        String userId = "user-123";
        String listId = "list-123";
        UpdateBoardListPositionRequest request = new UpdateBoardListPositionRequest(0);
        List<BoardList> updatedLists = List.of(
                createSampleBoardList(listId, "완료", "완료된 작업들", 0, "#51C878", "board-123"),
                createSampleBoardList("list-1", "할 일", "해야 할 일들", 1, "#0079BF", "board-123"),
                createSampleBoardList("list-2", "진행 중", "진행 중인 작업들", 2, "#FF6B6B", "board-123"));

        when(jwt.getSubject()).thenReturn(userId);
        when(updateBoardListPositionUseCase.updateBoardListPosition(any(UpdateBoardListPositionCommand.class)))
                .thenReturn(Either.right(updatedLists));

        // when
        ResponseEntity<?> response = controller.updateBoardListPosition(listId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BoardListResponse responseBody = (BoardListResponse) response.getBody();
        assertThat(responseBody.listId()).isEqualTo(listId);
        assertThat(responseBody.position()).isEqualTo(0);
    }

    @Test
    @DisplayName("보드 리스트 위치 변경 실패 - 권한 없음")
    void updateBoardListPosition_Forbidden() throws Exception {
        // given
        String userId = "user-123";
        String listId = "list-123";
        UpdateBoardListPositionRequest request = new UpdateBoardListPositionRequest(2);
        Failure forbiddenFailure = Failure.ofForbidden("LIST_POSITION_UPDATE_DENIED");
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
    @DisplayName("보드 리스트 위치 변경 실패 - 리스트 없음")
    void updateBoardListPosition_NotFound() throws Exception {
        // given
        String userId = "user-123";
        String listId = "list-123";
        UpdateBoardListPositionRequest request = new UpdateBoardListPositionRequest(2);
        Failure notFoundFailure = Failure.ofNotFound("LIST_NOT_FOUND");
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
    @DisplayName("보드 리스트 위치 변경 실패 - 유효하지 않은 위치")
    void updateBoardListPosition_InvalidPosition() throws Exception {
        // given
        String userId = "user-123";
        String listId = "list-123";
        UpdateBoardListPositionRequest request = new UpdateBoardListPositionRequest(-1);
        List<Failure.FieldViolation> violations = List.of(
                Failure.FieldViolation.builder()
                        .field("position")
                        .message("위치는 0 이상이어야 합니다")
                        .rejectedValue(-1)
                        .build());
        Failure validationFailure = Failure.ofValidation("INVALID_POSITION", violations);
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
    @DisplayName("보드 리스트 위치 변경 실패 - 위치 범위 초과")
    void updateBoardListPosition_PositionOutOfRange() throws Exception {
        // given
        String userId = "user-123";
        String listId = "list-123";
        UpdateBoardListPositionRequest request = new UpdateBoardListPositionRequest(10);
        Failure conflictFailure = Failure.ofConflict("POSITION_OUT_OF_RANGE");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.CONFLICT).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(updateBoardListPositionUseCase.updateBoardListPosition(any(UpdateBoardListPositionCommand.class)))
                .thenReturn(Either.left(conflictFailure));
        when(failureHandler.handleFailure(conflictFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.updateBoardListPosition(listId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
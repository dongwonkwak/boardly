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
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private GetBoardListsUseCase getBoardListsUseCase;

    @Mock
    private CreateBoardListUseCase createBoardListUseCase;

    @Mock
    private UpdateBoardListUseCase updateBoardListUseCase;

    @Mock
    private DeleteBoardListUseCase deleteBoardListUseCase;

    @Mock
    private UpdateBoardListPositionUseCase updateBoardListPositionUseCase;

    @Mock
    private Jwt jwt;

    private BoardListController controller;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        controller = new BoardListController(
                getBoardListsUseCase,
                createBoardListUseCase,
                updateBoardListUseCase,
                deleteBoardListUseCase,
                updateBoardListPositionUseCase
        );
    }

    private BoardList createSampleBoardList(String listId, String boardId, int position) {
        return BoardList.builder()
                .listId(new ListId(listId))
                .title("테스트 리스트 " + position)
                .description("테스트 리스트 설명 " + position)
                .position(position)
                .color(ListColor.of("#0079BF"))
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
                createSampleBoardList("list-1", boardId, 0),
                createSampleBoardList("list-2", boardId, 1)
        );

        when(jwt.getSubject()).thenReturn(userId);
        when(getBoardListsUseCase.getBoardLists(any(GetBoardListsCommand.class)))
                .thenReturn(Either.right(boardLists));

        // when
        ResponseEntity<?> response = controller.getBoardLists(boardId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<BoardListResponse> responses = (List<BoardListResponse>) response.getBody();
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).listId()).isEqualTo("list-1");
        assertThat(responses.get(0).title()).isEqualTo("테스트 리스트 0");
        assertThat(responses.get(1).listId()).isEqualTo("list-2");
        assertThat(responses.get(1).title()).isEqualTo("테스트 리스트 1");
    }

    @Test
    @DisplayName("보드 리스트 생성 성공")
    void createBoardList_Success() throws Exception {
        // given
        String boardId = "board-123";
        String userId = "user-123";
        CreateBoardListRequest request = new CreateBoardListRequest("새 리스트", "새 리스트 설명", "#FF6B6B");
        BoardList createdList = createSampleBoardList("list-new", boardId, 0);

        when(jwt.getSubject()).thenReturn(userId);
        when(createBoardListUseCase.createBoardList(any(CreateBoardListCommand.class)))
                .thenReturn(Either.right(createdList));

        // when
        ResponseEntity<?> response = controller.createBoardList(boardId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        BoardListResponse responseBody = (BoardListResponse) response.getBody();
        assertThat(responseBody.listId()).isEqualTo("list-new");
        assertThat(responseBody.title()).isEqualTo("테스트 리스트 0");
    }

    @Test
    @DisplayName("보드 리스트 수정 성공")
    void updateBoardList_Success() throws Exception {
        // given
        String listId = "list-123";
        String userId = "user-123";
        UpdateBoardListRequest request = new UpdateBoardListRequest("수정된 리스트", "수정된 설명", "#FF6B6B");
        BoardList updatedList = BoardList.builder()
                .listId(new ListId(listId))
                .title("수정된 리스트")
                .description("수정된 설명")
                .position(1)
                .color(ListColor.of("#FF6B6B"))
                .boardId(new BoardId("board-123"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

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
    }

    @Test
    @DisplayName("보드 리스트 위치 변경 성공")
    void updateBoardListPosition_Success() throws Exception {
        // given
        String listId = "list-123";
        String userId = "user-123";
        UpdateBoardListPositionRequest request = new UpdateBoardListPositionRequest(2);
        List<BoardList> updatedLists = List.of(
                createSampleBoardList("list-1", "board-123", 0),
                createSampleBoardList("list-2", "board-123", 1),
                createSampleBoardList(listId, "board-123", 2)
        );

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
    }

    @Test
    @DisplayName("보드 리스트 목록 조회 실패 - 권한 없음")
    void getBoardLists_Forbidden() throws Exception {
        // given
        String boardId = "board-123";
        String userId = "user-123";
        Failure forbiddenFailure = Failure.ofForbidden("UNAUTHORIZED_ACCESS");

        when(jwt.getSubject()).thenReturn(userId);
        when(getBoardListsUseCase.getBoardLists(any(GetBoardListsCommand.class)))
                .thenReturn(Either.left(forbiddenFailure));

        // when
        ResponseEntity<?> response = controller.getBoardLists(boardId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("보드 리스트 생성 실패 - 검증 오류")
    void createBoardList_ValidationError() throws Exception {
        // given
        String boardId = "board-123";
        String userId = "user-123";
        CreateBoardListRequest request = new CreateBoardListRequest("", "", null); // 빈 제목
        Failure.FieldViolation violation = Failure.FieldViolation.builder()
                .field("title")
                .message("리스트 제목은 필수입니다")
                .rejectedValue("")
                .build();
        Failure validationFailure = Failure.ofValidation("INVALID_INPUT", List.of(violation));

        when(jwt.getSubject()).thenReturn(userId);
        when(createBoardListUseCase.createBoardList(any(CreateBoardListCommand.class)))
                .thenReturn(Either.left(validationFailure));

        // when
        ResponseEntity<?> response = controller.createBoardList(boardId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("보드 리스트 수정 실패 - 리스트 없음")
    void updateBoardList_NotFound() throws Exception {
        // given
        String listId = "list-123";
        String userId = "user-123";
        UpdateBoardListRequest request = new UpdateBoardListRequest("수정된 리스트", "수정된 설명", "#FF6B6B");
        Failure notFoundFailure = Failure.ofNotFound("LIST_NOT_FOUND");

        when(jwt.getSubject()).thenReturn(userId);
        when(updateBoardListUseCase.updateBoardList(any(UpdateBoardListCommand.class)))
                .thenReturn(Either.left(notFoundFailure));

        // when
        ResponseEntity<?> response = controller.updateBoardList(listId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("보드 리스트 삭제 실패 - 서버 오류")
    void deleteBoardList_InternalServerError() throws Exception {
        // given
        String listId = "list-123";
        String userId = "user-123";
        Failure serverError = Failure.ofInternalServerError("데이터베이스 오류");

        when(jwt.getSubject()).thenReturn(userId);
        when(deleteBoardListUseCase.deleteBoardList(any(DeleteBoardListCommand.class)))
                .thenReturn(Either.left(serverError));

        // when
        ResponseEntity<?> response = controller.deleteBoardList(listId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("보드 리스트 위치 변경 실패 - 잘못된 위치값")
    void updateBoardListPosition_InvalidPosition() throws Exception {
        // given
        String listId = "list-123";
        String userId = "user-123";
        UpdateBoardListPositionRequest request = new UpdateBoardListPositionRequest(-1); // 잘못된 위치값
        Failure.FieldViolation violation = Failure.FieldViolation.builder()
                .field("position")
                .message("위치는 0 이상이어야 합니다")
                .rejectedValue(-1)
                .build();
        Failure validationFailure = Failure.ofValidation("INVALID_INPUT", List.of(violation));

        when(jwt.getSubject()).thenReturn(userId);
        when(updateBoardListPositionUseCase.updateBoardListPosition(any(UpdateBoardListPositionCommand.class)))
                .thenReturn(Either.left(validationFailure));

        // when
        ResponseEntity<?> response = controller.updateBoardListPosition(listId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("빈 리스트 목록 조회 성공")
    void getBoardLists_EmptyList() throws Exception {
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
    @DisplayName("보드 리스트 생성 - 색상 없이")
    void createBoardList_WithoutColor() throws Exception {
        // given
        String boardId = "board-123";
        String userId = "user-123";
        CreateBoardListRequest request = new CreateBoardListRequest("새 리스트", "새 리스트 설명", null);
        BoardList createdList = createSampleBoardList("list-new", boardId, 0);

        when(jwt.getSubject()).thenReturn(userId);
        when(createBoardListUseCase.createBoardList(any(CreateBoardListCommand.class)))
                .thenReturn(Either.right(createdList));

        // when
        ResponseEntity<?> response = controller.createBoardList(boardId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        BoardListResponse responseBody = (BoardListResponse) response.getBody();
        assertThat(responseBody.listId()).isEqualTo("list-new");
    }
} 
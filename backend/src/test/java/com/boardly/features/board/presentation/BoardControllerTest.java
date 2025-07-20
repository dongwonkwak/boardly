package com.boardly.features.board.presentation;

import com.boardly.features.board.application.port.input.*;
import com.boardly.features.board.application.usecase.*;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.presentation.request.CreateBoardRequest;
import com.boardly.features.board.presentation.request.UpdateBoardRequest;
import com.boardly.features.board.presentation.response.BoardResponse;
import com.boardly.features.user.domain.model.UserId;
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
class BoardControllerTest {

  @Mock
  private CreateBoardUseCase createBoardUseCase;

  @Mock
  private GetUserBoardsUseCase getUserBoardsUseCase;

  @Mock
  private UpdateBoardUseCase updateBoardUseCase;

  @Mock
  private ArchiveBoardUseCase archiveBoardUseCase;

  @Mock
  private ToggleStarBoardUseCase toggleStarBoardUseCase;

  @Mock
  private ApiFailureHandler failureHandler;

  @Mock
  private Jwt jwt;

  private BoardController controller;

  @BeforeEach
  void setUp() {
    controller = new BoardController(
        createBoardUseCase,
        getUserBoardsUseCase,
        updateBoardUseCase,
        archiveBoardUseCase,
        toggleStarBoardUseCase,
        failureHandler);
  }

  private Board createSampleBoard(String boardId, String title, String description, boolean isArchived,
      boolean isStarred) {
    return Board.builder()
        .boardId(new BoardId(boardId))
        .title(title)
        .description(description)
        .isArchived(isArchived)
        .ownerId(new UserId("user-123"))
        .isStarred(isStarred)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();
  }

  @Test
  @DisplayName("내 보드 목록 조회 성공")
  void getMyBoards_Success() throws Exception {
    // given
    String userId = "user-123";
    boolean includeArchived = false;
    List<Board> boards = List.of(
        createSampleBoard("board-1", "테스트 보드 1", "설명 1", false, false),
        createSampleBoard("board-2", "테스트 보드 2", "설명 2", false, true));

    when(jwt.getSubject()).thenReturn(userId);
    when(getUserBoardsUseCase.getUserBoards(any(GetUserBoardsCommand.class)))
        .thenReturn(Either.right(boards));

    // when
    ResponseEntity<?> response = controller.getMyBoards(includeArchived, null, jwt);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    List<BoardResponse> responses = (List<BoardResponse>) response.getBody();
    assertThat(responses).hasSize(2);
    assertThat(responses.get(0).boardId()).isEqualTo("board-1");
    assertThat(responses.get(0).title()).isEqualTo("테스트 보드 1");
    assertThat(responses.get(1).boardId()).isEqualTo("board-2");
    assertThat(responses.get(1).title()).isEqualTo("테스트 보드 2");
    assertThat(responses.get(1).isStarred()).isTrue();
  }

  @Test
  @DisplayName("내 보드 목록 조회 성공 - 아카이브 포함")
  void getMyBoards_Success_IncludeArchived() throws Exception {
    // given
    String userId = "user-123";
    boolean includeArchived = true;
    List<Board> boards = List.of(
        createSampleBoard("board-1", "활성 보드", "설명 1", false, false),
        createSampleBoard("board-2", "아카이브 보드", "설명 2", true, false));

    when(jwt.getSubject()).thenReturn(userId);
    when(getUserBoardsUseCase.getUserBoards(any(GetUserBoardsCommand.class)))
        .thenReturn(Either.right(boards));

    // when
    ResponseEntity<?> response = controller.getMyBoards(includeArchived, null, jwt);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    List<BoardResponse> responses = (List<BoardResponse>) response.getBody();
    assertThat(responses).hasSize(2);
    assertThat(responses.get(0).isArchived()).isFalse();
    assertThat(responses.get(1).isArchived()).isTrue();
  }

  @Test
  @DisplayName("내 보드 목록 조회 실패 - 권한 없음")
  void getMyBoards_Forbidden() throws Exception {
    // given
    String userId = "user-123";
    boolean includeArchived = false;
    Failure forbiddenFailure = Failure.ofForbidden("UNAUTHORIZED_ACCESS");
    ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.FORBIDDEN).build();

    when(jwt.getSubject()).thenReturn(userId);
    when(getUserBoardsUseCase.getUserBoards(any(GetUserBoardsCommand.class)))
        .thenReturn(Either.left(forbiddenFailure));
    when(failureHandler.handleFailure(forbiddenFailure)).thenReturn(expectedResponse);

    // when
    ResponseEntity<?> response = controller.getMyBoards(includeArchived, null, jwt);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  @DisplayName("보드 생성 성공")
  void createBoard_Success() throws Exception {
    // given
    String userId = "user-123";
    CreateBoardRequest request = new CreateBoardRequest("새 보드", "새 보드 설명");
    Board createdBoard = createSampleBoard("board-new", "새 보드", "새 보드 설명", false, false);

    when(jwt.getSubject()).thenReturn(userId);
    when(createBoardUseCase.createBoard(any(CreateBoardCommand.class)))
        .thenReturn(Either.right(createdBoard));

    // when
    ResponseEntity<?> response = controller.createBoard(request, null, jwt);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    BoardResponse responseBody = (BoardResponse) response.getBody();
    assertThat(responseBody.boardId()).isEqualTo("board-new");
    assertThat(responseBody.title()).isEqualTo("새 보드");
    assertThat(responseBody.description()).isEqualTo("새 보드 설명");
    assertThat(responseBody.ownerId()).isEqualTo(userId);
  }

  @Test
  @DisplayName("보드 생성 실패 - 검증 오류")
  void createBoard_ValidationError() throws Exception {
    // given
    String userId = "user-123";
    CreateBoardRequest request = new CreateBoardRequest("", ""); // 빈 제목
    Failure.FieldViolation violation = Failure.FieldViolation.builder()
        .field("title")
        .message("보드 제목은 필수입니다")
        .rejectedValue("")
        .build();
    Failure validationFailure = Failure.ofValidation("INVALID_INPUT", List.of(violation));
    ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();

    when(jwt.getSubject()).thenReturn(userId);
    when(createBoardUseCase.createBoard(any(CreateBoardCommand.class)))
        .thenReturn(Either.left(validationFailure));
    when(failureHandler.handleFailure(validationFailure)).thenReturn(expectedResponse);

    // when
    ResponseEntity<?> response = controller.createBoard(request, null, jwt);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
  }

  @Test
  @DisplayName("보드 업데이트 성공")
  void updateBoard_Success() throws Exception {
    // given
    String boardId = "board-123";
    String userId = "user-123";
    UpdateBoardRequest request = new UpdateBoardRequest("수정된 보드", "수정된 설명");
    Board updatedBoard = createSampleBoard(boardId, "수정된 보드", "수정된 설명", false, false);

    when(jwt.getSubject()).thenReturn(userId);
    when(updateBoardUseCase.updateBoard(any(UpdateBoardCommand.class)))
        .thenReturn(Either.right(updatedBoard));

    // when
    ResponseEntity<?> response = controller.updateBoard(boardId, request, null, jwt);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    BoardResponse responseBody = (BoardResponse) response.getBody();
    assertThat(responseBody.boardId()).isEqualTo(boardId);
    assertThat(responseBody.title()).isEqualTo("수정된 보드");
    assertThat(responseBody.description()).isEqualTo("수정된 설명");
  }

  @Test
  @DisplayName("보드 업데이트 실패 - 보드 없음")
  void updateBoard_NotFound() throws Exception {
    // given
    String boardId = "board-123";
    String userId = "user-123";
    UpdateBoardRequest request = new UpdateBoardRequest("수정된 보드", "수정된 설명");
    Failure notFoundFailure = Failure.ofNotFound("BOARD_NOT_FOUND");
    ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();

    when(jwt.getSubject()).thenReturn(userId);
    when(updateBoardUseCase.updateBoard(any(UpdateBoardCommand.class)))
        .thenReturn(Either.left(notFoundFailure));
    when(failureHandler.handleFailure(notFoundFailure)).thenReturn(expectedResponse);

    // when
    ResponseEntity<?> response = controller.updateBoard(boardId, request, null, jwt);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  @DisplayName("보드 업데이트 실패 - 권한 없음")
  void updateBoard_Forbidden() throws Exception {
    // given
    String boardId = "board-123";
    String userId = "user-123";
    UpdateBoardRequest request = new UpdateBoardRequest("수정된 보드", "수정된 설명");
    Failure forbiddenFailure = Failure.ofForbidden("BOARD_ACCESS_DENIED");
    ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.FORBIDDEN).build();

    when(jwt.getSubject()).thenReturn(userId);
    when(updateBoardUseCase.updateBoard(any(UpdateBoardCommand.class)))
        .thenReturn(Either.left(forbiddenFailure));
    when(failureHandler.handleFailure(forbiddenFailure)).thenReturn(expectedResponse);

    // when
    ResponseEntity<?> response = controller.updateBoard(boardId, request, null, jwt);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  @DisplayName("보드 아카이브 성공")
  void archiveBoard_Success() throws Exception {
    // given
    String boardId = "board-123";
    String userId = "user-123";
    Board archivedBoard = createSampleBoard(boardId, "아카이브된 보드", "설명", true, false);

    when(jwt.getSubject()).thenReturn(userId);
    when(archiveBoardUseCase.archiveBoard(any(ArchiveBoardCommand.class)))
        .thenReturn(Either.right(archivedBoard));

    // when
    ResponseEntity<?> response = controller.archiveBoard(boardId, null, jwt);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    BoardResponse responseBody = (BoardResponse) response.getBody();
    assertThat(responseBody.boardId()).isEqualTo(boardId);
    assertThat(responseBody.isArchived()).isTrue();
  }

  @Test
  @DisplayName("보드 언아카이브 성공")
  void unarchiveBoard_Success() throws Exception {
    // given
    String boardId = "board-123";
    String userId = "user-123";
    Board unarchivedBoard = createSampleBoard(boardId, "언아카이브된 보드", "설명", false, false);

    when(jwt.getSubject()).thenReturn(userId);
    when(archiveBoardUseCase.unarchiveBoard(any(ArchiveBoardCommand.class)))
        .thenReturn(Either.right(unarchivedBoard));

    // when
    ResponseEntity<?> response = controller.unarchiveBoard(boardId, null, jwt);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    BoardResponse responseBody = (BoardResponse) response.getBody();
    assertThat(responseBody.boardId()).isEqualTo(boardId);
    assertThat(responseBody.isArchived()).isFalse();
  }

  @Test
  @DisplayName("보드 즐겨찾기 추가 성공")
  void starBoard_Success() throws Exception {
    // given
    String boardId = "board-123";
    String userId = "user-123";
    Board starredBoard = createSampleBoard(boardId, "즐겨찾기 보드", "설명", false, true);

    when(jwt.getSubject()).thenReturn(userId);
    when(toggleStarBoardUseCase.starringBoard(any(ToggleStarBoardCommand.class)))
        .thenReturn(Either.right(starredBoard));

    // when
    ResponseEntity<?> response = controller.starBoard(boardId, null, jwt);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    BoardResponse responseBody = (BoardResponse) response.getBody();
    assertThat(responseBody.boardId()).isEqualTo(boardId);
    assertThat(responseBody.isStarred()).isTrue();
  }

  @Test
  @DisplayName("보드 즐겨찾기 제거 성공")
  void unstarBoard_Success() throws Exception {
    // given
    String boardId = "board-123";
    String userId = "user-123";
    Board unstarredBoard = createSampleBoard(boardId, "즐겨찾기 해제 보드", "설명", false, false);

    when(jwt.getSubject()).thenReturn(userId);
    when(toggleStarBoardUseCase.unstarringBoard(any(ToggleStarBoardCommand.class)))
        .thenReturn(Either.right(unstarredBoard));

    // when
    ResponseEntity<?> response = controller.unstarBoard(boardId, null, jwt);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    BoardResponse responseBody = (BoardResponse) response.getBody();
    assertThat(responseBody.boardId()).isEqualTo(boardId);
    assertThat(responseBody.isStarred()).isFalse();
  }

  @Test
  @DisplayName("보드 아카이브 실패 - 이미 아카이브됨")
  void archiveBoard_AlreadyArchived() throws Exception {
    // given
    String boardId = "board-123";
    String userId = "user-123";
    Failure conflictFailure = Failure.ofConflict("BOARD_ALREADY_ARCHIVED");
    ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.CONFLICT).build();

    when(jwt.getSubject()).thenReturn(userId);
    when(archiveBoardUseCase.archiveBoard(any(ArchiveBoardCommand.class)))
        .thenReturn(Either.left(conflictFailure));
    when(failureHandler.handleFailure(conflictFailure)).thenReturn(expectedResponse);

    // when
    ResponseEntity<?> response = controller.archiveBoard(boardId, null, jwt);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  @DisplayName("보드 즐겨찾기 추가 실패 - 이미 즐겨찾기됨")
  void starBoard_AlreadyStarred() throws Exception {
    // given
    String boardId = "board-123";
    String userId = "user-123";
    Failure conflictFailure = Failure.ofConflict("BOARD_ALREADY_STARRED");
    ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.CONFLICT).build();

    when(jwt.getSubject()).thenReturn(userId);
    when(toggleStarBoardUseCase.starringBoard(any(ToggleStarBoardCommand.class)))
        .thenReturn(Either.left(conflictFailure));
    when(failureHandler.handleFailure(conflictFailure)).thenReturn(expectedResponse);

    // when
    ResponseEntity<?> response = controller.starBoard(boardId, null, jwt);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  @DisplayName("빈 보드 목록 조회 성공")
  void getMyBoards_EmptyList() throws Exception {
    // given
    String userId = "user-123";
    boolean includeArchived = false;

    when(jwt.getSubject()).thenReturn(userId);
    when(getUserBoardsUseCase.getUserBoards(any(GetUserBoardsCommand.class)))
        .thenReturn(Either.right(List.of()));

    // when
    ResponseEntity<?> response = controller.getMyBoards(includeArchived, null, jwt);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    List<BoardResponse> responses = (List<BoardResponse>) response.getBody();
    assertThat(responses).isEmpty();
  }

  @Test
  @DisplayName("보드 생성 - 설명 없이")
  void createBoard_WithoutDescription() throws Exception {
    // given
    String userId = "user-123";
    CreateBoardRequest request = new CreateBoardRequest("새 보드", null);
    Board createdBoard = createSampleBoard("board-new", "새 보드", null, false, false);

    when(jwt.getSubject()).thenReturn(userId);
    when(createBoardUseCase.createBoard(any(CreateBoardCommand.class)))
        .thenReturn(Either.right(createdBoard));

    // when
    ResponseEntity<?> response = controller.createBoard(request, null, jwt);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    BoardResponse responseBody = (BoardResponse) response.getBody();
    assertThat(responseBody.title()).isEqualTo("새 보드");
    assertThat(responseBody.description()).isNull();
  }
}
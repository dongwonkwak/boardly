package com.boardly.features.board.presentation;

import com.boardly.features.board.application.port.input.*;
import com.boardly.features.board.application.service.BoardManagementService;
import com.boardly.features.board.application.service.BoardMemberService;
import com.boardly.features.board.application.service.BoardQueryService;
import com.boardly.features.board.application.service.BoardInteractionService;
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
    private BoardManagementService boardManagementService;

    @Mock
    private BoardMemberService boardMemberService;

    @Mock
    private BoardQueryService boardQueryService;

    @Mock
    private BoardInteractionService boardInteractionService;

    @Mock
    private ApiFailureHandler failureHandler;

    @Mock
    private Jwt jwt;

    private BoardController controller;

    @BeforeEach
    void setUp() {
        controller = new BoardController(
                boardManagementService,
                boardMemberService,
                boardQueryService,
                boardInteractionService,
                failureHandler);
    }

    private Board createSampleBoard(String boardId, String title, String description, String ownerId,
            boolean isArchived, boolean isStarred) {
        return Board.builder()
                .boardId(new BoardId(boardId))
                .title(title)
                .description(description)
                .ownerId(new UserId(ownerId))
                .isArchived(isArchived)
                .isStarred(isStarred)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // ==================== GET MY BOARDS TESTS ====================

    @Test
    @DisplayName("내 보드 목록 조회 성공 - 아카이브 제외")
    void getMyBoards_Success_ExcludeArchived() throws Exception {
        // given
        String userId = "user-123";
        boolean includeArchived = false;
        List<Board> boards = List.of(
                createSampleBoard("board-1", "프로젝트 A", "프로젝트 A 설명", userId, false, false),
                createSampleBoard("board-2", "프로젝트 B", "프로젝트 B 설명", userId, false, true));

        when(jwt.getSubject()).thenReturn(userId);
        when(boardQueryService.getUserBoards(any(GetUserBoardsCommand.class)))
                .thenReturn(Either.right(boards));

        // when
        ResponseEntity<?> response = controller.getMyBoards(includeArchived, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<BoardResponse> responses = (List<BoardResponse>) response.getBody();
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).boardId()).isEqualTo("board-1");
        assertThat(responses.get(0).title()).isEqualTo("프로젝트 A");
        assertThat(responses.get(0).isArchived()).isFalse();
        assertThat(responses.get(1).boardId()).isEqualTo("board-2");
        assertThat(responses.get(1).title()).isEqualTo("프로젝트 B");
        assertThat(responses.get(1).isStarred()).isTrue();
    }

    @Test
    @DisplayName("내 보드 목록 조회 성공 - 아카이브 포함")
    void getMyBoards_Success_IncludeArchived() throws Exception {
        // given
        String userId = "user-123";
        boolean includeArchived = true;
        List<Board> boards = List.of(
                createSampleBoard("board-1", "활성 보드", "활성 보드 설명", userId, false, false),
                createSampleBoard("board-2", "아카이브된 보드", "아카이브된 보드 설명", userId, true, false));

        when(jwt.getSubject()).thenReturn(userId);
        when(boardQueryService.getUserBoards(any(GetUserBoardsCommand.class)))
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
    @DisplayName("내 보드 목록 조회 성공 - 빈 목록")
    void getMyBoards_Success_EmptyList() throws Exception {
        // given
        String userId = "user-123";
        boolean includeArchived = false;

        when(jwt.getSubject()).thenReturn(userId);
        when(boardQueryService.getUserBoards(any(GetUserBoardsCommand.class)))
                .thenReturn(Either.right(List.of()));

        // when
        ResponseEntity<?> response = controller.getMyBoards(includeArchived, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<BoardResponse> responses = (List<BoardResponse>) response.getBody();
        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("내 보드 목록 조회 실패 - 검증 오류")
    void getMyBoards_ValidationError() throws Exception {
        // given
        String userId = "user-123";
        boolean includeArchived = false;
        List<Failure.FieldViolation> violations = List.of(
                Failure.FieldViolation.builder()
                        .field("userId")
                        .message("사용자 ID가 유효하지 않습니다.")
                        .rejectedValue(userId)
                        .build());
        Failure validationFailure = Failure.ofValidation("INVALID_USER_ID", violations);
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(boardQueryService.getUserBoards(any(GetUserBoardsCommand.class)))
                .thenReturn(Either.left(validationFailure));
        when(failureHandler.handleFailure(validationFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.getMyBoards(includeArchived, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // ==================== CREATE BOARD TESTS ====================

    @Test
    @DisplayName("보드 생성 성공")
    void createBoard_Success() throws Exception {
        // given
        String userId = "user-123";
        CreateBoardRequest request = new CreateBoardRequest("새 보드", "새 보드 설명");
        Board createdBoard = createSampleBoard("board-new", "새 보드", "새 보드 설명", userId, false, false);

        when(jwt.getSubject()).thenReturn(userId);
        when(boardManagementService.createBoard(any(CreateBoardCommand.class)))
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
        CreateBoardRequest request = new CreateBoardRequest("", "보드 설명");
        List<Failure.FieldViolation> violations = List.of(
                Failure.FieldViolation.builder()
                        .field("title")
                        .message("제목은 필수입니다.")
                        .rejectedValue("")
                        .build());
        Failure validationFailure = Failure.ofValidation("TITLE_REQUIRED", violations);
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(boardManagementService.createBoard(any(CreateBoardCommand.class)))
                .thenReturn(Either.left(validationFailure));
        when(failureHandler.handleFailure(validationFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.createBoard(request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // ==================== UPDATE BOARD TESTS ====================

    @Test
    @DisplayName("보드 업데이트 성공")
    void updateBoard_Success() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        UpdateBoardRequest request = new UpdateBoardRequest("수정된 보드", "수정된 설명");
        Board updatedBoard = createSampleBoard(boardId, "수정된 보드", "수정된 설명", userId, false, false);

        when(jwt.getSubject()).thenReturn(userId);
        when(boardManagementService.updateBoard(any(UpdateBoardCommand.class)))
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
    @DisplayName("보드 업데이트 실패 - 권한 없음")
    void updateBoard_Forbidden() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        UpdateBoardRequest request = new UpdateBoardRequest("수정된 보드", "수정된 설명");
        Failure forbiddenFailure = Failure.ofForbidden("BOARD_UPDATE_DENIED");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(boardManagementService.updateBoard(any(UpdateBoardCommand.class)))
                .thenReturn(Either.left(forbiddenFailure));
        when(failureHandler.handleFailure(forbiddenFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.updateBoard(boardId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("보드 업데이트 실패 - 보드 없음")
    void updateBoard_NotFound() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        UpdateBoardRequest request = new UpdateBoardRequest("수정된 보드", "수정된 설명");
        Failure notFoundFailure = Failure.ofNotFound("BOARD_NOT_FOUND");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(boardManagementService.updateBoard(any(UpdateBoardCommand.class)))
                .thenReturn(Either.left(notFoundFailure));
        when(failureHandler.handleFailure(notFoundFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.updateBoard(boardId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("보드 업데이트 실패 - 아카이브된 보드")
    void updateBoard_ArchivedBoard() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        UpdateBoardRequest request = new UpdateBoardRequest("수정된 보드", "수정된 설명");
        Failure conflictFailure = Failure.ofConflict("ARCHIVED_BOARD_CANNOT_BE_UPDATED");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.CONFLICT).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(boardManagementService.updateBoard(any(UpdateBoardCommand.class)))
                .thenReturn(Either.left(conflictFailure));
        when(failureHandler.handleFailure(conflictFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.updateBoard(boardId, request, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    // ==================== ARCHIVE BOARD TESTS ====================

    @Test
    @DisplayName("보드 아카이브 성공")
    void archiveBoard_Success() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        Board archivedBoard = createSampleBoard(boardId, "아카이브된 보드", "설명", userId, true, false);

        when(jwt.getSubject()).thenReturn(userId);
        when(boardManagementService.archiveBoard(any(ArchiveBoardCommand.class)))
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
    @DisplayName("보드 아카이브 실패 - 이미 아카이브됨")
    void archiveBoard_AlreadyArchived() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        Failure conflictFailure = Failure.ofConflict("BOARD_ALREADY_ARCHIVED");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.CONFLICT).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(boardManagementService.archiveBoard(any(ArchiveBoardCommand.class)))
                .thenReturn(Either.left(conflictFailure));
        when(failureHandler.handleFailure(conflictFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.archiveBoard(boardId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    // ==================== UNARCHIVE BOARD TESTS ====================

    @Test
    @DisplayName("보드 언아카이브 성공")
    void unarchiveBoard_Success() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        Board unarchivedBoard = createSampleBoard(boardId, "언아카이브된 보드", "설명", userId, false, false);

        when(jwt.getSubject()).thenReturn(userId);
        when(boardManagementService.unarchiveBoard(any(ArchiveBoardCommand.class)))
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
    @DisplayName("보드 언아카이브 실패 - 이미 활성화됨")
    void unarchiveBoard_AlreadyActive() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        Failure conflictFailure = Failure.ofConflict("BOARD_ALREADY_ACTIVE");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.CONFLICT).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(boardManagementService.unarchiveBoard(any(ArchiveBoardCommand.class)))
                .thenReturn(Either.left(conflictFailure));
        when(failureHandler.handleFailure(conflictFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.unarchiveBoard(boardId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    // ==================== STAR BOARD TESTS ====================

    @Test
    @DisplayName("보드 즐겨찾기 추가 성공")
    void starBoard_Success() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        Board starredBoard = createSampleBoard(boardId, "즐겨찾기 보드", "설명", userId, false, true);

        when(jwt.getSubject()).thenReturn(userId);
        when(boardInteractionService.starringBoard(any(ToggleStarBoardCommand.class)))
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
    @DisplayName("보드 즐겨찾기 추가 실패 - 이미 즐겨찾기됨")
    void starBoard_AlreadyStarred() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        Failure conflictFailure = Failure.ofConflict("BOARD_ALREADY_STARRED");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.CONFLICT).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(boardInteractionService.starringBoard(any(ToggleStarBoardCommand.class)))
                .thenReturn(Either.left(conflictFailure));
        when(failureHandler.handleFailure(conflictFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.starBoard(boardId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    // ==================== UNSTAR BOARD TESTS ====================

    @Test
    @DisplayName("보드 즐겨찾기 제거 성공")
    void unstarBoard_Success() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        Board unstarredBoard = createSampleBoard(boardId, "즐겨찾기 해제된 보드", "설명", userId, false, false);

        when(jwt.getSubject()).thenReturn(userId);
        when(boardInteractionService.unstarringBoard(any(ToggleStarBoardCommand.class)))
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
    @DisplayName("보드 즐겨찾기 제거 실패 - 즐겨찾기에 없음")
    void unstarBoard_NotStarred() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        Failure conflictFailure = Failure.ofConflict("BOARD_NOT_STARRED");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.CONFLICT).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(boardInteractionService.unstarringBoard(any(ToggleStarBoardCommand.class)))
                .thenReturn(Either.left(conflictFailure));
        when(failureHandler.handleFailure(conflictFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.unstarBoard(boardId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    // ==================== REMOVE BOARD MEMBER TESTS ====================

    @Test
    @DisplayName("보드 멤버 삭제 성공")
    void removeBoardMember_Success() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        String targetUserId = "target-user-123";

        when(jwt.getSubject()).thenReturn(userId);
        when(boardMemberService.removeBoardMember(any(RemoveBoardMemberCommand.class)))
                .thenReturn(Either.right(null));

        // when
        ResponseEntity<?> response = controller.removeBoardMember(boardId, targetUserId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
    }

    @Test
    @DisplayName("보드 멤버 삭제 실패 - 권한 없음")
    void removeBoardMember_Forbidden() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        String targetUserId = "target-user-123";
        Failure forbiddenFailure = Failure.ofForbidden("MEMBER_REMOVAL_DENIED");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(boardMemberService.removeBoardMember(any(RemoveBoardMemberCommand.class)))
                .thenReturn(Either.left(forbiddenFailure));
        when(failureHandler.handleFailure(forbiddenFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.removeBoardMember(boardId, targetUserId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("보드 멤버 삭제 실패 - OWNER 역할 멤버 삭제 불가")
    void removeBoardMember_OwnerCannotBeRemoved() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        String targetUserId = "target-user-123";
        Failure conflictFailure = Failure.ofConflict("OWNER_CANNOT_BE_REMOVED");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.CONFLICT).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(boardMemberService.removeBoardMember(any(RemoveBoardMemberCommand.class)))
                .thenReturn(Either.left(conflictFailure));
        when(failureHandler.handleFailure(conflictFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.removeBoardMember(boardId, targetUserId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    // ==================== DELETE BOARD TESTS ====================

    @Test
    @DisplayName("보드 삭제 성공")
    void deleteBoard_Success() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";

        when(jwt.getSubject()).thenReturn(userId);
        when(boardManagementService.deleteBoard(any(DeleteBoardCommand.class)))
                .thenReturn(Either.right(null));

        // when
        ResponseEntity<?> response = controller.deleteBoard(boardId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    @DisplayName("보드 삭제 실패 - 권한 없음")
    void deleteBoard_Forbidden() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        Failure forbiddenFailure = Failure.ofForbidden("BOARD_DELETE_DENIED");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(boardManagementService.deleteBoard(any(DeleteBoardCommand.class)))
                .thenReturn(Either.left(forbiddenFailure));
        when(failureHandler.handleFailure(forbiddenFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.deleteBoard(boardId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("보드 삭제 실패 - 보드 없음")
    void deleteBoard_NotFound() throws Exception {
        // given
        String userId = "user-123";
        String boardId = "board-123";
        Failure notFoundFailure = Failure.ofNotFound("BOARD_NOT_FOUND");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        when(jwt.getSubject()).thenReturn(userId);
        when(boardManagementService.deleteBoard(any(DeleteBoardCommand.class)))
                .thenReturn(Either.left(notFoundFailure));
        when(failureHandler.handleFailure(notFoundFailure)).thenReturn(expectedResponse);

        // when
        ResponseEntity<?> response = controller.deleteBoard(boardId, null, jwt);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
package com.boardly.features.board.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;

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

import com.boardly.features.board.application.dto.BoardDetailDto;
import com.boardly.features.board.application.port.input.ArchiveBoardCommand;
import com.boardly.features.board.application.port.input.CreateBoardCommand;
import com.boardly.features.board.application.port.input.DeleteBoardCommand;
import com.boardly.features.board.application.port.input.GetBoardDetailCommand;
import com.boardly.features.board.application.port.input.GetUserBoardsCommand;
import com.boardly.features.board.application.port.input.RemoveBoardMemberCommand;
import com.boardly.features.board.application.port.input.ToggleStarBoardCommand;
import com.boardly.features.board.application.port.input.UpdateBoardCommand;
import com.boardly.features.board.application.service.BoardInteractionService;
import com.boardly.features.board.application.service.BoardManagementService;
import com.boardly.features.board.application.service.BoardMemberService;
import com.boardly.features.board.application.service.BoardQueryService;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.presentation.request.CreateBoardRequest;
import com.boardly.features.board.presentation.request.UpdateBoardRequest;
import com.boardly.features.board.presentation.response.BoardDetailResponse;
import com.boardly.features.board.presentation.response.BoardResponse;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.presentation.ApiFailureHandler;
import com.boardly.shared.presentation.response.ErrorResponse;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
class BoardControllerTest {

        private BoardController boardController;

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

        @Mock
        private MockHttpServletRequest httpRequest;

        private static final String TEST_USER_ID = "test-user-id";
        private static final String TEST_BOARD_ID = "test-board-id";
        private static final String TEST_TITLE = "테스트 보드";
        private static final String TEST_DESCRIPTION = "테스트 보드 설명";

        @BeforeEach
        void setUp() {
                boardController = new BoardController(
                                boardManagementService,
                                boardMemberService,
                                boardQueryService,
                                boardInteractionService,
                                failureHandler);

                when(jwt.getSubject()).thenReturn(TEST_USER_ID);
        }

        private Board createTestBoard() {
                return Board.builder()
                                .boardId(new BoardId(TEST_BOARD_ID))
                                .title(TEST_TITLE)
                                .description(TEST_DESCRIPTION)
                                .ownerId(new UserId(TEST_USER_ID))
                                .isArchived(false)
                                .isStarred(false)
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();
        }

        private BoardDetailDto createTestBoardDetailDto() {
                Board board = createTestBoard();
                return new BoardDetailDto(
                                board,
                                List.of(),
                                List.of(),
                                List.of(),
                                List.of(),
                                Map.of(),
                                Map.of(),
                                Map.of(),
                                Map.of(),
                                List.of());
        }

        @Test
        @DisplayName("내 보드 목록 조회 성공 시 200 응답을 반환해야 한다")
        void getMyBoards_withValidRequest_shouldReturn200() {
                // given
                boolean includeArchived = false;
                List<Board> boards = List.of(createTestBoard());
                GetUserBoardsCommand command = new GetUserBoardsCommand(
                                new UserId(TEST_USER_ID),
                                includeArchived);

                when(boardQueryService.getUserBoards(command))
                                .thenReturn(Either.right(boards));

                // when
                ResponseEntity<?> response = boardController.getMyBoards(includeArchived, httpRequest, jwt);

                // then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isInstanceOf(List.class);

                @SuppressWarnings("unchecked")
                List<BoardResponse> boardResponses = (List<BoardResponse>) response.getBody();
                assertThat(boardResponses).isNotNull();
                assertThat(boardResponses).hasSize(1);
                if (boardResponses != null) {
                        assertThat(boardResponses.get(0).boardId()).isEqualTo(TEST_BOARD_ID);
                        assertThat(boardResponses.get(0).title()).isEqualTo(TEST_TITLE);
                }

                verify(boardQueryService).getUserBoards(command);
                verify(failureHandler, never()).handleFailure(any());
        }

        @Test
        @DisplayName("내 보드 목록 조회 실패 시 failureHandler가 호출되어야 한다")
        void getMyBoards_withFailure_shouldCallFailureHandler() {
                // given
                boolean includeArchived = false;
                Failure failure = Failure.ofInputError("입력 오류");
                GetUserBoardsCommand command = new GetUserBoardsCommand(
                                new UserId(TEST_USER_ID),
                                includeArchived);
                ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.badRequest().build();

                when(boardQueryService.getUserBoards(command))
                                .thenReturn(Either.left(failure));
                when(failureHandler.handleFailure(failure))
                                .thenReturn(expectedResponse);

                // when
                ResponseEntity<?> response = boardController.getMyBoards(includeArchived, httpRequest, jwt);

                // then
                assertThat(response).isEqualTo(expectedResponse);
                verify(boardQueryService).getUserBoards(command);
                verify(failureHandler).handleFailure(failure);
        }

        @Test
        @DisplayName("보드 상세 조회 성공 시 200 응답을 반환해야 한다")
        void getBoardDetail_withValidRequest_shouldReturn200() {
                // given
                BoardDetailDto boardDetailDto = createTestBoardDetailDto();
                GetBoardDetailCommand command = new GetBoardDetailCommand(
                                new BoardId(TEST_BOARD_ID),
                                new UserId(TEST_USER_ID));

                when(boardQueryService.getBoardDetail(command))
                                .thenReturn(Either.right(boardDetailDto));

                // when
                ResponseEntity<?> response = boardController.getBoardDetail(TEST_BOARD_ID, httpRequest, jwt);

                // then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isInstanceOf(BoardDetailResponse.class);

                BoardDetailResponse boardDetailResponse = (BoardDetailResponse) response.getBody();
                assertThat(boardDetailResponse).isNotNull();
                if (boardDetailResponse != null) {
                        assertThat(boardDetailResponse.boardId()).isEqualTo(TEST_BOARD_ID);
                        assertThat(boardDetailResponse.boardName()).isEqualTo(TEST_TITLE);
                }

                verify(boardQueryService).getBoardDetail(command);
                verify(failureHandler, never()).handleFailure(any());
        }

        @Test
        @DisplayName("보드 상세 조회 실패 시 failureHandler가 호출되어야 한다")
        void getBoardDetail_withFailure_shouldCallFailureHandler() {
                // given
                Failure failure = Failure.ofNotFound("보드를 찾을 수 없습니다");
                GetBoardDetailCommand command = new GetBoardDetailCommand(
                                new BoardId(TEST_BOARD_ID),
                                new UserId(TEST_USER_ID));
                ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.notFound().build();

                when(boardQueryService.getBoardDetail(command))
                                .thenReturn(Either.left(failure));
                when(failureHandler.handleFailure(failure))
                                .thenReturn(expectedResponse);

                // when
                ResponseEntity<?> response = boardController.getBoardDetail(TEST_BOARD_ID, httpRequest, jwt);

                // then
                assertThat(response).isEqualTo(expectedResponse);
                verify(boardQueryService).getBoardDetail(command);
                verify(failureHandler).handleFailure(failure);
        }

        @Test
        @DisplayName("보드 생성 성공 시 201 응답을 반환해야 한다")
        void createBoard_withValidRequest_shouldReturn201() {
                // given
                CreateBoardRequest request = new CreateBoardRequest(TEST_TITLE, TEST_DESCRIPTION);
                Board createdBoard = createTestBoard();
                CreateBoardCommand command = CreateBoardCommand.of(
                                TEST_TITLE,
                                TEST_DESCRIPTION,
                                new UserId(TEST_USER_ID));

                when(boardManagementService.createBoard(command))
                                .thenReturn(Either.right(createdBoard));

                // when
                ResponseEntity<?> response = boardController.createBoard(request, httpRequest, jwt);

                // then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                assertThat(response.getBody()).isInstanceOf(BoardResponse.class);

                BoardResponse boardResponse = (BoardResponse) response.getBody();
                assertThat(boardResponse).isNotNull();
                if (boardResponse != null) {
                        assertThat(boardResponse.boardId()).isEqualTo(TEST_BOARD_ID);
                        assertThat(boardResponse.title()).isEqualTo(TEST_TITLE);
                }

                verify(boardManagementService).createBoard(command);
                verify(failureHandler, never()).handleFailure(any());
        }

        @Test
        @DisplayName("보드 생성 실패 시 failureHandler가 호출되어야 한다")
        void createBoard_withFailure_shouldCallFailureHandler() {
                // given
                CreateBoardRequest request = new CreateBoardRequest(TEST_TITLE, TEST_DESCRIPTION);
                Failure failure = Failure.ofInputError("제목이 유효하지 않습니다");
                CreateBoardCommand command = CreateBoardCommand.of(
                                TEST_TITLE,
                                TEST_DESCRIPTION,
                                new UserId(TEST_USER_ID));
                ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.badRequest().build();

                when(boardManagementService.createBoard(command))
                                .thenReturn(Either.left(failure));
                when(failureHandler.handleFailure(failure))
                                .thenReturn(expectedResponse);

                // when
                ResponseEntity<?> response = boardController.createBoard(request, httpRequest, jwt);

                // then
                assertThat(response).isEqualTo(expectedResponse);
                verify(boardManagementService).createBoard(command);
                verify(failureHandler).handleFailure(failure);
        }

        @Test
        @DisplayName("보드 업데이트 성공 시 200 응답을 반환해야 한다")
        void updateBoard_withValidRequest_shouldReturn200() {
                // given
                UpdateBoardRequest request = new UpdateBoardRequest(TEST_TITLE, TEST_DESCRIPTION);
                Board updatedBoard = createTestBoard();
                UpdateBoardCommand command = UpdateBoardCommand.of(
                                new BoardId(TEST_BOARD_ID),
                                TEST_TITLE,
                                TEST_DESCRIPTION,
                                new UserId(TEST_USER_ID));

                when(boardManagementService.updateBoard(command))
                                .thenReturn(Either.right(updatedBoard));

                // when
                ResponseEntity<?> response = boardController.updateBoard(TEST_BOARD_ID, request, httpRequest, jwt);

                // then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isInstanceOf(BoardResponse.class);

                BoardResponse boardResponse = (BoardResponse) response.getBody();
                assertThat(boardResponse).isNotNull();
                if (boardResponse != null) {
                        assertThat(boardResponse.boardId()).isEqualTo(TEST_BOARD_ID);
                        assertThat(boardResponse.title()).isEqualTo(TEST_TITLE);
                }

                verify(boardManagementService).updateBoard(command);
                verify(failureHandler, never()).handleFailure(any());
        }

        @Test
        @DisplayName("보드 업데이트 실패 시 failureHandler가 호출되어야 한다")
        void updateBoard_withFailure_shouldCallFailureHandler() {
                // given
                UpdateBoardRequest request = new UpdateBoardRequest(TEST_TITLE, TEST_DESCRIPTION);
                Failure failure = Failure.ofNotFound("보드를 찾을 수 없습니다");
                UpdateBoardCommand command = UpdateBoardCommand.of(
                                new BoardId(TEST_BOARD_ID),
                                TEST_TITLE,
                                TEST_DESCRIPTION,
                                new UserId(TEST_USER_ID));
                ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.notFound().build();

                when(boardManagementService.updateBoard(command))
                                .thenReturn(Either.left(failure));
                when(failureHandler.handleFailure(failure))
                                .thenReturn(expectedResponse);

                // when
                ResponseEntity<?> response = boardController.updateBoard(TEST_BOARD_ID, request, httpRequest, jwt);

                // then
                assertThat(response).isEqualTo(expectedResponse);
                verify(boardManagementService).updateBoard(command);
                verify(failureHandler).handleFailure(failure);
        }

        @Test
        @DisplayName("보드 아카이브 성공 시 200 응답을 반환해야 한다")
        void archiveBoard_withValidRequest_shouldReturn200() {
                // given
                Board archivedBoard = createTestBoard();
                ArchiveBoardCommand command = ArchiveBoardCommand.of(
                                new BoardId(TEST_BOARD_ID),
                                new UserId(TEST_USER_ID));

                when(boardManagementService.archiveBoard(command))
                                .thenReturn(Either.right(archivedBoard));

                // when
                ResponseEntity<?> response = boardController.archiveBoard(TEST_BOARD_ID, httpRequest, jwt);

                // then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isInstanceOf(BoardResponse.class);

                BoardResponse boardResponse = (BoardResponse) response.getBody();
                assertThat(boardResponse).isNotNull();
                if (boardResponse != null) {
                        assertThat(boardResponse.boardId()).isEqualTo(TEST_BOARD_ID);
                }

                verify(boardManagementService).archiveBoard(command);
                verify(failureHandler, never()).handleFailure(any());
        }

        @Test
        @DisplayName("보드 언아카이브 성공 시 200 응답을 반환해야 한다")
        void unarchiveBoard_withValidRequest_shouldReturn200() {
                // given
                Board unarchivedBoard = createTestBoard();
                ArchiveBoardCommand command = ArchiveBoardCommand.of(
                                new BoardId(TEST_BOARD_ID),
                                new UserId(TEST_USER_ID));

                when(boardManagementService.unarchiveBoard(command))
                                .thenReturn(Either.right(unarchivedBoard));

                // when
                ResponseEntity<?> response = boardController.unarchiveBoard(TEST_BOARD_ID, httpRequest, jwt);

                // then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isInstanceOf(BoardResponse.class);

                BoardResponse boardResponse = (BoardResponse) response.getBody();
                assertThat(boardResponse).isNotNull();
                if (boardResponse != null) {
                        assertThat(boardResponse.boardId()).isEqualTo(TEST_BOARD_ID);
                }

                verify(boardManagementService).unarchiveBoard(command);
                verify(failureHandler, never()).handleFailure(any());
        }

        @Test
        @DisplayName("보드 즐겨찾기 추가 성공 시 200 응답을 반환해야 한다")
        void starBoard_withValidRequest_shouldReturn200() {
                // given
                Board starredBoard = createTestBoard();
                ToggleStarBoardCommand command = ToggleStarBoardCommand.of(
                                new BoardId(TEST_BOARD_ID),
                                new UserId(TEST_USER_ID));

                when(boardInteractionService.starringBoard(command))
                                .thenReturn(Either.right(starredBoard));

                // when
                ResponseEntity<?> response = boardController.starBoard(TEST_BOARD_ID, httpRequest, jwt);

                // then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isInstanceOf(BoardResponse.class);

                BoardResponse boardResponse = (BoardResponse) response.getBody();
                assertThat(boardResponse).isNotNull();
                if (boardResponse != null) {
                        assertThat(boardResponse.boardId()).isEqualTo(TEST_BOARD_ID);
                }

                verify(boardInteractionService).starringBoard(command);
                verify(failureHandler, never()).handleFailure(any());
        }

        @Test
        @DisplayName("보드 즐겨찾기 제거 성공 시 200 응답을 반환해야 한다")
        void unstarBoard_withValidRequest_shouldReturn200() {
                // given
                Board unstarredBoard = createTestBoard();
                ToggleStarBoardCommand command = ToggleStarBoardCommand.of(
                                new BoardId(TEST_BOARD_ID),
                                new UserId(TEST_USER_ID));

                when(boardInteractionService.unstarringBoard(command))
                                .thenReturn(Either.right(unstarredBoard));

                // when
                ResponseEntity<?> response = boardController.unstarBoard(TEST_BOARD_ID, httpRequest, jwt);

                // then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isInstanceOf(BoardResponse.class);

                BoardResponse boardResponse = (BoardResponse) response.getBody();
                assertThat(boardResponse).isNotNull();
                if (boardResponse != null) {
                        assertThat(boardResponse.boardId()).isEqualTo(TEST_BOARD_ID);
                }

                verify(boardInteractionService).unstarringBoard(command);
                verify(failureHandler, never()).handleFailure(any());
        }

        @Test
        @DisplayName("보드 멤버 삭제 성공 시 200 응답을 반환해야 한다")
        void removeBoardMember_withValidRequest_shouldReturn200() {
                // given
                String targetUserId = "target-user-id";
                RemoveBoardMemberCommand command = new RemoveBoardMemberCommand(
                                new BoardId(TEST_BOARD_ID),
                                new UserId(targetUserId),
                                new UserId(TEST_USER_ID));

                when(boardMemberService.removeBoardMember(command))
                                .thenReturn(Either.right(null));

                // when
                ResponseEntity<?> response = boardController.removeBoardMember(TEST_BOARD_ID, targetUserId, httpRequest,
                                jwt);

                // then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNull();

                verify(boardMemberService).removeBoardMember(command);
                verify(failureHandler, never()).handleFailure(any());
        }

        @Test
        @DisplayName("보드 멤버 삭제 실패 시 failureHandler가 호출되어야 한다")
        void removeBoardMember_withFailure_shouldCallFailureHandler() {
                // given
                String targetUserId = "target-user-id";
                Failure failure = Failure.ofForbidden("권한이 없습니다");
                RemoveBoardMemberCommand command = new RemoveBoardMemberCommand(
                                new BoardId(TEST_BOARD_ID),
                                new UserId(targetUserId),
                                new UserId(TEST_USER_ID));
                ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.FORBIDDEN).build();

                when(boardMemberService.removeBoardMember(command))
                                .thenReturn(Either.left(failure));
                when(failureHandler.handleFailure(failure))
                                .thenReturn(expectedResponse);

                // when
                ResponseEntity<?> response = boardController.removeBoardMember(TEST_BOARD_ID, targetUserId, httpRequest,
                                jwt);

                // then
                assertThat(response).isEqualTo(expectedResponse);
                verify(boardMemberService).removeBoardMember(command);
                verify(failureHandler).handleFailure(failure);
        }

        @Test
        @DisplayName("보드 삭제 성공 시 204 응답을 반환해야 한다")
        void deleteBoard_withValidRequest_shouldReturn204() {
                // given
                DeleteBoardCommand command = DeleteBoardCommand.of(
                                new BoardId(TEST_BOARD_ID),
                                new UserId(TEST_USER_ID));

                when(boardManagementService.deleteBoard(command))
                                .thenReturn(Either.right(null));

                // when
                ResponseEntity<?> response = boardController.deleteBoard(TEST_BOARD_ID, httpRequest, jwt);

                // then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                assertThat(response.getBody()).isNull();

                verify(boardManagementService).deleteBoard(command);
                verify(failureHandler, never()).handleFailure(any());
        }

        @Test
        @DisplayName("보드 삭제 실패 시 failureHandler가 호출되어야 한다")
        void deleteBoard_withFailure_shouldCallFailureHandler() {
                // given
                Failure failure = Failure.ofNotFound("보드를 찾을 수 없습니다");
                DeleteBoardCommand command = DeleteBoardCommand.of(
                                new BoardId(TEST_BOARD_ID),
                                new UserId(TEST_USER_ID));
                ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.notFound().build();

                when(boardManagementService.deleteBoard(command))
                                .thenReturn(Either.left(failure));
                when(failureHandler.handleFailure(failure))
                                .thenReturn(expectedResponse);

                // when
                ResponseEntity<?> response = boardController.deleteBoard(TEST_BOARD_ID, httpRequest, jwt);

                // then
                assertThat(response).isEqualTo(expectedResponse);
                verify(boardManagementService).deleteBoard(command);
                verify(failureHandler).handleFailure(failure);
        }

        @Test
        @DisplayName("JWT에서 사용자 ID를 올바르게 추출해야 한다")
        void allEndpoints_shouldExtractUserIdFromJwt() {
                // given
                String expectedUserId = "jwt-user-id";
                when(jwt.getSubject()).thenReturn(expectedUserId);

                CreateBoardRequest request = new CreateBoardRequest(TEST_TITLE, TEST_DESCRIPTION);
                Board createdBoard = createTestBoard();
                CreateBoardCommand expectedCommand = CreateBoardCommand.of(
                                TEST_TITLE,
                                TEST_DESCRIPTION,
                                new UserId(expectedUserId));

                when(boardManagementService.createBoard(any(CreateBoardCommand.class)))
                                .thenReturn(Either.right(createdBoard));

                // when
                boardController.createBoard(request, httpRequest, jwt);

                // then
                verify(boardManagementService).createBoard(expectedCommand);
                verify(jwt, times(1)).getSubject();
        }

        @Test
        @DisplayName("아카이브 포함 여부에 따라 올바른 명령이 생성되어야 한다")
        void getMyBoards_withIncludeArchived_shouldCreateCorrectCommand() {
                // given
                boolean includeArchived = true;
                List<Board> boards = List.of(createTestBoard());
                GetUserBoardsCommand expectedCommand = new GetUserBoardsCommand(
                                new UserId(TEST_USER_ID),
                                includeArchived);

                when(boardQueryService.getUserBoards(any(GetUserBoardsCommand.class)))
                                .thenReturn(Either.right(boards));

                // when
                boardController.getMyBoards(includeArchived, httpRequest, jwt);

                // then
                verify(boardQueryService).getUserBoards(expectedCommand);
        }
}
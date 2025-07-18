package com.boardly.features.board.presentation;

import com.boardly.features.board.application.port.input.CreateBoardCommand;
import com.boardly.features.board.application.port.input.UpdateBoardCommand;
import com.boardly.features.board.application.port.input.ArchiveBoardCommand;
import com.boardly.features.board.application.port.input.GetUserBoardsCommand;
import com.boardly.features.board.application.port.input.ToggleStarBoardCommand;
import com.boardly.features.board.application.usecase.CreateBoardUseCase;
import com.boardly.features.board.application.usecase.UpdateBoardUseCase;
import com.boardly.features.board.application.usecase.ArchiveBoardUseCase;
import com.boardly.features.board.application.usecase.GetUserBoardsUseCase;
import com.boardly.features.board.application.usecase.ToggleStarBoardUseCase;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.presentation.request.CreateBoardRequest;
import com.boardly.features.board.presentation.request.UpdateBoardRequest;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.presentation.Path;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("BoardController 테스트")
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateBoardUseCase createBoardUseCase;

    @MockitoBean
    private UpdateBoardUseCase updateBoardUseCase;

    @MockitoBean
    private ArchiveBoardUseCase archiveBoardUseCase;

    @MockitoBean
    private GetUserBoardsUseCase getUserBoardsUseCase;

    @MockitoBean
    private ToggleStarBoardUseCase toggleStarBoardUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateBoardRequest createBoardRequest;
    private UpdateBoardRequest updateBoardRequest;
    private Board testBoard;
    private Board testBoard2;
    private Board archivedBoard;
    private final String TEST_USER_ID = "test-user-id";
    private final String TEST_BOARD_ID = "test-board-id";
    private final String TEST_BOARD_ID_2 = "test-board-id-2";
    private final String ARCHIVED_BOARD_ID = "archived-board-id";

    @BeforeEach
    void setUp() {
        createBoardRequest = new CreateBoardRequest(
            "Test Board",
            "Test Description"
        );
        
        updateBoardRequest = new UpdateBoardRequest(
            "Updated Board",
            "Updated Description"
        );
        
        Instant now = Instant.now();
        
        testBoard = Board.builder()
            .boardId(new BoardId(TEST_BOARD_ID))
            .title("Test Board")
            .description("Test Description")
            .isArchived(false)
            .isStarred(false)
            .ownerId(new UserId(TEST_USER_ID))
            .createdAt(now.minus(2, ChronoUnit.HOURS))
            .updatedAt(now.minus(30, ChronoUnit.MINUTES))
            .build();
            
        testBoard2 = Board.builder()
            .boardId(new BoardId(TEST_BOARD_ID_2))
            .title("Test Board 2")
            .description("Test Description 2")
            .isArchived(false)
            .isStarred(true)
            .ownerId(new UserId(TEST_USER_ID))
            .createdAt(now.minus(1, ChronoUnit.HOURS))
            .updatedAt(now) // 가장 최근 수정
            .build();
            
        archivedBoard = Board.builder()
            .boardId(new BoardId(ARCHIVED_BOARD_ID))
            .title("Archived Board")
            .description("Archived Description")
            .isArchived(true)
            .isStarred(false)
            .ownerId(new UserId(TEST_USER_ID))
            .createdAt(now.minus(3, ChronoUnit.HOURS))
            .updatedAt(now.minus(1, ChronoUnit.HOURS))
            .build();
    }

    @Nested
    @DisplayName("보드 생성 테스트")
    class CreateBoardTests {

        @Test
        @DisplayName("보드 생성 성공")
        void createBoard_Success() throws Exception {
            // given
            given(createBoardUseCase.createBoard(any(CreateBoardCommand.class)))
                .willReturn(Either.right(testBoard));

            // when & then
            mockMvc.perform(post(Path.BOARDS)
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createBoardRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.boardId").value(TEST_BOARD_ID))
                .andExpect(jsonPath("$.title").value("Test Board"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.isArchived").value(false))
                .andExpect(jsonPath("$.isStarred").value(false))
                .andExpect(jsonPath("$.ownerId").value(TEST_USER_ID));
        }

        @Test
        @DisplayName("보드 생성 실패 - 인증 없음")
        void createBoard_Unauthorized() throws Exception {
            // when & then
            mockMvc.perform(post(Path.BOARDS)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createBoardRequest)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("보드 생성 실패 - write 권한 없음")
        void createBoard_Forbidden_NoWriteScope() throws Exception {
            // when & then
            mockMvc.perform(post(Path.BOARDS)
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "read openid")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createBoardRequest)))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("보드 생성 실패 - openid 권한 없음")
        void createBoard_Forbidden_NoOpenidScope() throws Exception {
            // when & then
            mockMvc.perform(post(Path.BOARDS)
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createBoardRequest)))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("보드 생성 실패 - 유효성 검증 실패")
        void createBoard_ValidationFailure() throws Exception {
            // given
            given(createBoardUseCase.createBoard(any(CreateBoardCommand.class)))
                .willReturn(Either.left(new Failure.ValidationFailure("제목은 필수입니다", List.of())));

            // when & then
            mockMvc.perform(post(Path.BOARDS)
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createBoardRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("제목은 필수입니다"));
        }

        @Test
        @DisplayName("보드 생성 실패 - 서버 오류")
        void createBoard_InternalServerError() throws Exception {
            // given
            given(createBoardUseCase.createBoard(any(CreateBoardCommand.class)))
                .willReturn(Either.left(new Failure.InternalServerError("서버 오류가 발생했습니다")));

            // when & then
            mockMvc.perform(post(Path.BOARDS)
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createBoardRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("서버 오류가 발생했습니다"));
        }

        @Test
        @DisplayName("보드 생성 실패 - 잘못된 JSON 형식")
        void createBoard_InvalidJson() throws Exception {
            // when & then
            mockMvc.perform(post(Path.BOARDS)
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("invalid json"))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("보드 업데이트 테스트")
    class UpdateBoardTests {

        @Test
        @DisplayName("보드 업데이트 성공")
        void updateBoard_Success() throws Exception {
            // given
            Board updatedBoard = Board.builder()
                .boardId(new BoardId(TEST_BOARD_ID))
                .title("Updated Board")
                .description("Updated Description")
                .isArchived(false)
                .isStarred(true)
                .ownerId(new UserId(TEST_USER_ID))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

            given(updateBoardUseCase.updateBoard(any(UpdateBoardCommand.class)))
                .willReturn(Either.right(updatedBoard));

            // when & then
            mockMvc.perform(put(Path.BOARDS + "/" + TEST_BOARD_ID)
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateBoardRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.boardId").value(TEST_BOARD_ID))
                .andExpect(jsonPath("$.title").value("Updated Board"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.isArchived").value(false))
                .andExpect(jsonPath("$.isStarred").value(true))
                .andExpect(jsonPath("$.ownerId").value(TEST_USER_ID));
        }

        @Test
        @DisplayName("보드 업데이트 실패 - 인증 없음")
        void updateBoard_Unauthorized() throws Exception {
            // when & then
            mockMvc.perform(put(Path.BOARDS + "/" + TEST_BOARD_ID)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateBoardRequest)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("보드 업데이트 실패 - 권한 없음")
        void updateBoard_Forbidden() throws Exception {
            // when & then
            mockMvc.perform(put(Path.BOARDS + "/" + TEST_BOARD_ID)
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "read openid")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateBoardRequest)))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("보드 업데이트 실패 - 보드 찾을 수 없음")
        void updateBoard_NotFound() throws Exception {
            // given
            given(updateBoardUseCase.updateBoard(any(UpdateBoardCommand.class)))
                .willReturn(Either.left(new Failure.NotFoundFailure("보드를 찾을 수 없습니다")));

            // when & then
            mockMvc.perform(put(Path.BOARDS + "/" + TEST_BOARD_ID)
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateBoardRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("보드를 찾을 수 없습니다"));
        }

        @Test
        @DisplayName("보드 업데이트 실패 - 수정 권한 없음")
        void updateBoard_Forbidden_NotOwner() throws Exception {
            // given
            given(updateBoardUseCase.updateBoard(any(UpdateBoardCommand.class)))
                .willReturn(Either.left(new Failure.ForbiddenFailure("보드 수정 권한이 없습니다")));

            // when & then
            mockMvc.perform(put(Path.BOARDS + "/" + TEST_BOARD_ID)
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateBoardRequest)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("보드 수정 권한이 없습니다"));
        }

        @Test
        @DisplayName("보드 업데이트 실패 - 아카이브된 보드는 수정 불가")
        void updateBoard_Conflict_ArchivedBoard() throws Exception {
            // given
            given(updateBoardUseCase.updateBoard(any(UpdateBoardCommand.class)))
                .willReturn(Either.left(new Failure.ConflictFailure("아카이브된 보드는 수정할 수 없습니다")));

            // when & then
            mockMvc.perform(put(Path.BOARDS + "/" + TEST_BOARD_ID)
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateBoardRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("아카이브된 보드는 수정할 수 없습니다"));
        }
    }

    @Nested
    @DisplayName("보드 아카이브 테스트")
    class ArchiveBoardTests {

        @Test
        @DisplayName("보드 아카이브 성공")
        void archiveBoard_Success() throws Exception {
            // given
            Board archivedBoard = Board.builder()
                .boardId(new BoardId(TEST_BOARD_ID))
                .title("Test Board")
                .description("Test Description")
                .isArchived(true)
                .isStarred(false)
                .ownerId(new UserId(TEST_USER_ID))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

            given(archiveBoardUseCase.archiveBoard(any(ArchiveBoardCommand.class)))
                .willReturn(Either.right(archivedBoard));

            // when & then
            mockMvc.perform(post(Path.BOARDS + "/" + TEST_BOARD_ID + "/archive")
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.boardId").value(TEST_BOARD_ID))
                .andExpect(jsonPath("$.isArchived").value(true))
                .andExpect(jsonPath("$.isStarred").value(false))
                .andExpect(jsonPath("$.ownerId").value(TEST_USER_ID));
        }

        @Test
        @DisplayName("보드 아카이브 실패 - 인증 없음")
        void archiveBoard_Unauthorized() throws Exception {
            // when & then
            mockMvc.perform(post(Path.BOARDS + "/" + TEST_BOARD_ID + "/archive")
                    .with(csrf()))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("보드 아카이브 실패 - 권한 없음")
        void archiveBoard_Forbidden() throws Exception {
            // when & then
            mockMvc.perform(post(Path.BOARDS + "/" + TEST_BOARD_ID + "/archive")
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "read openid"))))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("보드 아카이브 실패 - 보드 찾을 수 없음")
        void archiveBoard_NotFound() throws Exception {
            // given
            given(archiveBoardUseCase.archiveBoard(any(ArchiveBoardCommand.class)))
                .willReturn(Either.left(new Failure.NotFoundFailure("보드를 찾을 수 없습니다")));

            // when & then
            mockMvc.perform(post(Path.BOARDS + "/" + TEST_BOARD_ID + "/archive")
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid"))))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("보드를 찾을 수 없습니다"));
        }

        @Test
        @DisplayName("보드 아카이브 실패 - 아카이브 권한 없음")
        void archiveBoard_Forbidden_NotOwner() throws Exception {
            // given
            given(archiveBoardUseCase.archiveBoard(any(ArchiveBoardCommand.class)))
                .willReturn(Either.left(new Failure.ForbiddenFailure("보드 아카이브 권한이 없습니다")));

            // when & then
            mockMvc.perform(post(Path.BOARDS + "/" + TEST_BOARD_ID + "/archive")
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid"))))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("보드 아카이브 권한이 없습니다"));
        }

        @Test
        @DisplayName("보드 아카이브 실패 - 이미 아카이브된 보드")
        void archiveBoard_Conflict_AlreadyArchived() throws Exception {
            // given
            given(archiveBoardUseCase.archiveBoard(any(ArchiveBoardCommand.class)))
                .willReturn(Either.left(new Failure.ConflictFailure("이미 아카이브된 보드입니다")));

            // when & then
            mockMvc.perform(post(Path.BOARDS + "/" + TEST_BOARD_ID + "/archive")
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid"))))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("이미 아카이브된 보드입니다"));
        }
    }

    @Nested
    @DisplayName("보드 언아카이브 테스트")
    class UnarchiveBoardTests {

        @Test
        @DisplayName("보드 언아카이브 성공")
        void unarchiveBoard_Success() throws Exception {
            // given
            Board unarchivedBoard = Board.builder()
                .boardId(new BoardId(TEST_BOARD_ID))
                .title("Test Board")
                .description("Test Description")
                .isArchived(false)
                .isStarred(false)
                .ownerId(new UserId(TEST_USER_ID))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

            given(archiveBoardUseCase.unarchiveBoard(any(ArchiveBoardCommand.class)))
                .willReturn(Either.right(unarchivedBoard));

            // when & then
            mockMvc.perform(post(Path.BOARDS + "/" + TEST_BOARD_ID + "/unarchive")
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.boardId").value(TEST_BOARD_ID))
                .andExpect(jsonPath("$.isArchived").value(false))
                .andExpect(jsonPath("$.isStarred").value(false))
                .andExpect(jsonPath("$.ownerId").value(TEST_USER_ID));
        }

        @Test
        @DisplayName("보드 언아카이브 실패 - 인증 없음")
        void unarchiveBoard_Unauthorized() throws Exception {
            // when & then
            mockMvc.perform(post(Path.BOARDS + "/" + TEST_BOARD_ID + "/unarchive")
                    .with(csrf()))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("보드 언아카이브 실패 - 권한 없음")
        void unarchiveBoard_Forbidden() throws Exception {
            // when & then
            mockMvc.perform(post(Path.BOARDS + "/" + TEST_BOARD_ID + "/unarchive")
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "read openid"))))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("보드 언아카이브 실패 - 보드 찾을 수 없음")
        void unarchiveBoard_NotFound() throws Exception {
            // given
            given(archiveBoardUseCase.unarchiveBoard(any(ArchiveBoardCommand.class)))
                .willReturn(Either.left(new Failure.NotFoundFailure("보드를 찾을 수 없습니다")));

            // when & then
            mockMvc.perform(post(Path.BOARDS + "/" + TEST_BOARD_ID + "/unarchive")
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid"))))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("보드를 찾을 수 없습니다"));
        }

        @Test
        @DisplayName("보드 언아카이브 실패 - 언아카이브 권한 없음")
        void unarchiveBoard_Forbidden_NotOwner() throws Exception {
            // given
            given(archiveBoardUseCase.unarchiveBoard(any(ArchiveBoardCommand.class)))
                .willReturn(Either.left(Failure.ofForbidden("보드 언아카이브 권한이 없습니다")));

            // when & then
            mockMvc.perform(post(Path.BOARDS + "/" + TEST_BOARD_ID + "/unarchive")
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid"))))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("보드 언아카이브 권한이 없습니다"));
        }

        @Test
        @DisplayName("보드 언아카이브 실패 - 이미 활성화된 보드")
        void unarchiveBoard_Conflict_AlreadyActive() throws Exception {
            // given
            given(archiveBoardUseCase.unarchiveBoard(any(ArchiveBoardCommand.class)))
                .willReturn(Either.left(new Failure.ConflictFailure("이미 활성화된 보드입니다")));

            // when & then
            mockMvc.perform(post(Path.BOARDS + "/" + TEST_BOARD_ID + "/unarchive")
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid"))))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("이미 활성화된 보드입니다"));
        }
    }

    @Nested
    @DisplayName("내 보드 목록 조회 테스트")
    class GetMyBoardsTests {

        @Test
        @DisplayName("활성 보드만 조회 성공")
        void getMyBoards_ActiveOnly_Success() throws Exception {
            // given
            List<Board> activeBoards = List.of(testBoard2, testBoard); // 최신 수정 순
            given(getUserBoardsUseCase.getUserBoards(any(GetUserBoardsCommand.class)))
                .willReturn(Either.right(activeBoards));

            // when & then
            mockMvc.perform(get(Path.BOARDS)
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "read openid"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].boardId").value(TEST_BOARD_ID_2))
                .andExpect(jsonPath("$[0].title").value("Test Board 2"))
                .andExpect(jsonPath("$[0].isArchived").value(false))
                .andExpect(jsonPath("$[0].isStarred").value(true))
                .andExpect(jsonPath("$[1].boardId").value(TEST_BOARD_ID))
                .andExpect(jsonPath("$[1].title").value("Test Board"))
                .andExpect(jsonPath("$[1].isArchived").value(false))
                .andExpect(jsonPath("$[1].isStarred").value(false));
        }

        @Test
        @DisplayName("모든 보드 조회 성공 (아카이브 포함)")
        void getMyBoards_IncludeArchived_Success() throws Exception {
            // given
            List<Board> allBoards = List.of(testBoard2, testBoard, archivedBoard); // 최신 수정 순
            given(getUserBoardsUseCase.getUserBoards(any(GetUserBoardsCommand.class)))
                .willReturn(Either.right(allBoards));

            // when & then
            mockMvc.perform(get(Path.BOARDS)
                    .param("includeArchived", "true")
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "read openid"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].boardId").value(TEST_BOARD_ID_2))
                .andExpect(jsonPath("$[0].isArchived").value(false))
                .andExpect(jsonPath("$[0].isStarred").value(true))
                .andExpect(jsonPath("$[1].boardId").value(TEST_BOARD_ID))
                .andExpect(jsonPath("$[1].isArchived").value(false))
                .andExpect(jsonPath("$[1].isStarred").value(false))
                .andExpect(jsonPath("$[2].boardId").value(ARCHIVED_BOARD_ID))
                .andExpect(jsonPath("$[2].isArchived").value(true))
                .andExpect(jsonPath("$[2].isStarred").value(false));
        }

        @Test
        @DisplayName("빈 목록 조회 성공")
        void getMyBoards_EmptyList_Success() throws Exception {
            // given
            given(getUserBoardsUseCase.getUserBoards(any(GetUserBoardsCommand.class)))
                .willReturn(Either.right(List.of()));

            // when & then
            mockMvc.perform(get(Path.BOARDS)
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "read openid"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("보드 목록 조회 실패 - 인증 없음")
        void getMyBoards_Unauthorized() throws Exception {
            // when & then
            mockMvc.perform(get(Path.BOARDS))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("보드 목록 조회 실패 - read 권한 없음")
        void getMyBoards_Forbidden_NoReadScope() throws Exception {
            // when & then
            mockMvc.perform(get(Path.BOARDS)
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid"))))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("보드 목록 조회 실패 - openid 권한 없음")
        void getMyBoards_Forbidden_NoOpenidScope() throws Exception {
            // when & then
            mockMvc.perform(get(Path.BOARDS)
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "read"))))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("보드 목록 조회 실패 - 유효성 검증 실패")
        void getMyBoards_ValidationFailure() throws Exception {
            // given
            given(getUserBoardsUseCase.getUserBoards(any(GetUserBoardsCommand.class)))
                .willReturn(Either.left(new Failure.ValidationFailure("INVALID_INPUT", List.of())));

            // when & then
            mockMvc.perform(get(Path.BOARDS)
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "read openid"))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("INVALID_INPUT"));
        }

        @Test
        @DisplayName("보드 목록 조회 실패 - 서버 오류")
        void getMyBoards_InternalServerError() throws Exception {
            // given
            given(getUserBoardsUseCase.getUserBoards(any(GetUserBoardsCommand.class)))
                .willReturn(Either.left(new Failure.InternalServerError("Database connection failed")));

            // when & then
            mockMvc.perform(get(Path.BOARDS)
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "read openid"))))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Database connection failed"));
        }

        @Test
        @DisplayName("includeArchived 파라미터 기본값 테스트")
        void getMyBoards_DefaultIncludeArchived() throws Exception {
            // given
            List<Board> activeBoards = List.of(testBoard);
            given(getUserBoardsUseCase.getUserBoards(any(GetUserBoardsCommand.class)))
                .willReturn(Either.right(activeBoards));

            // when & then
            mockMvc.perform(get(Path.BOARDS)
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "read openid"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].isArchived").value(false))
                .andExpect(jsonPath("$[0].isStarred").value(false));
        }

        @Test
        @DisplayName("includeArchived=false 명시적 설정 테스트")
        void getMyBoards_ExplicitIncludeArchivedFalse() throws Exception {
            // given
            List<Board> activeBoards = List.of(testBoard);
            given(getUserBoardsUseCase.getUserBoards(any(GetUserBoardsCommand.class)))
                .willReturn(Either.right(activeBoards));

            // when & then
            mockMvc.perform(get(Path.BOARDS)
                    .param("includeArchived", "false")
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "read openid"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].isArchived").value(false))
                .andExpect(jsonPath("$[0].isStarred").value(false));
        }
    }

    @Nested
    @DisplayName("HTTP 메서드 테스트")
    class HttpMethodTests {

        @Test
        @DisplayName("보드 업데이트 - POST 메서드 사용 시 405 Method Not Allowed")
        void updateBoard_PostMethod_MethodNotAllowed() throws Exception {
            // when & then
            mockMvc.perform(post(Path.BOARDS + "/" + TEST_BOARD_ID)
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateBoardRequest)))
                .andExpect(status().isMethodNotAllowed());
        }
    }

    @Nested
    @DisplayName("Content-Type 테스트")
    class ContentTypeTests {

        @Test
        @DisplayName("보드 생성 - Content-Type이 application/json이 아닌 경우")
        void createBoard_InvalidContentType() throws Exception {
            // when & then
            mockMvc.perform(post(Path.BOARDS)
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid")))
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("invalid content type"))
                .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("보드 업데이트 - Content-Type이 application/json이 아닌 경우")
        void updateBoard_InvalidContentType() throws Exception {
            // when & then
            mockMvc.perform(put(Path.BOARDS + "/" + TEST_BOARD_ID)
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid")))
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("invalid content type"))
                .andExpect(status().isUnsupportedMediaType());
        }
    }

    @Nested
    @DisplayName("보드 즐겨찾기 추가 테스트")
    class StarBoardTests {

        @Test
        @DisplayName("보드 즐겨찾기 추가 성공")
        void starBoard_Success() throws Exception {
            // given
            Board starredBoard = Board.builder()
                .boardId(new BoardId(TEST_BOARD_ID))
                .title("Test Board")
                .description("Test Description")
                .isArchived(false)
                .isStarred(true)
                .ownerId(new UserId(TEST_USER_ID))
                .createdAt(Instant.now().minus(2, ChronoUnit.HOURS))
                .updatedAt(Instant.now())
                .build();

            given(toggleStarBoardUseCase.starringBoard(any(ToggleStarBoardCommand.class)))
                .willReturn(Either.right(starredBoard));

            // when & then
            mockMvc.perform(post(Path.BOARDS + "/" + TEST_BOARD_ID + "/star")
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid")))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.boardId").value(TEST_BOARD_ID))
                .andExpect(jsonPath("$.title").value("Test Board"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.isArchived").value(false))
                .andExpect(jsonPath("$.isStarred").value(true))
                .andExpect(jsonPath("$.ownerId").value(TEST_USER_ID));
        }

        @Test
        @DisplayName("보드 즐겨찾기 추가 실패 - 인증 없음")
        void starBoard_Unauthorized() throws Exception {
            // when & then
            mockMvc.perform(post(Path.BOARDS + "/" + TEST_BOARD_ID + "/star")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("보드 즐겨찾기 추가 실패 - 권한 없음")
        void starBoard_Forbidden() throws Exception {
            // when & then
            mockMvc.perform(post(Path.BOARDS + "/" + TEST_BOARD_ID + "/star")
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "read openid")))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("보드 즐겨찾기 추가 실패 - 보드 찾을 수 없음")
        void starBoard_NotFound() throws Exception {
            // given
            given(toggleStarBoardUseCase.starringBoard(any(ToggleStarBoardCommand.class)))
                .willReturn(Either.left(new Failure.NotFoundFailure("보드를 찾을 수 없습니다")));

            // when & then
            mockMvc.perform(post(Path.BOARDS + "/" + TEST_BOARD_ID + "/star")
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid")))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("보드를 찾을 수 없습니다"));
        }

        @Test
        @DisplayName("보드 즐겨찾기 추가 실패 - 즐겨찾기 권한 없음")
        void starBoard_Forbidden_NotOwner() throws Exception {
            // given
            given(toggleStarBoardUseCase.starringBoard(any(ToggleStarBoardCommand.class)))
                .willReturn(Either.left(new Failure.ForbiddenFailure("보드 즐겨찾기 권한이 없습니다")));

            // when & then
            mockMvc.perform(post(Path.BOARDS + "/" + TEST_BOARD_ID + "/star")
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid")))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("보드 즐겨찾기 권한이 없습니다"));
        }

        @Test
        @DisplayName("보드 즐겨찾기 추가 실패 - 이미 즐겨찾기에 추가된 보드")
        void starBoard_Conflict_AlreadyStarred() throws Exception {
            // given
            given(toggleStarBoardUseCase.starringBoard(any(ToggleStarBoardCommand.class)))
                .willReturn(Either.left(new Failure.ConflictFailure("이미 즐겨찾기에 추가된 보드입니다")));

            // when & then
            mockMvc.perform(post(Path.BOARDS + "/" + TEST_BOARD_ID + "/star")
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid")))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("이미 즐겨찾기에 추가된 보드입니다"));
        }

        @Test
        @DisplayName("보드 즐겨찾기 추가 실패 - 서버 오류")
        void starBoard_InternalServerError() throws Exception {
            // given
            given(toggleStarBoardUseCase.starringBoard(any(ToggleStarBoardCommand.class)))
                .willReturn(Either.left(new Failure.InternalServerError("서버 오류가 발생했습니다")));

            // when & then
            mockMvc.perform(post(Path.BOARDS + "/" + TEST_BOARD_ID + "/star")
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid")))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("서버 오류가 발생했습니다"));
        }
    }

    @Nested
    @DisplayName("보드 즐겨찾기 제거 테스트")
    class UnstarBoardTests {

        @Test
        @DisplayName("보드 즐겨찾기 제거 성공")
        void unstarBoard_Success() throws Exception {
            // given
            Board unstarredBoard = Board.builder()
                .boardId(new BoardId(TEST_BOARD_ID))
                .title("Test Board")
                .description("Test Description")
                .isArchived(false)
                .isStarred(false)
                .ownerId(new UserId(TEST_USER_ID))
                .createdAt(Instant.now().minus(2, ChronoUnit.HOURS))
                .updatedAt(Instant.now())
                .build();

            given(toggleStarBoardUseCase.unstarringBoard(any(ToggleStarBoardCommand.class)))
                .willReturn(Either.right(unstarredBoard));

            // when & then
            mockMvc.perform(post(Path.BOARDS + "/" + TEST_BOARD_ID + "/unstar")
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid")))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.boardId").value(TEST_BOARD_ID))
                .andExpect(jsonPath("$.title").value("Test Board"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.isArchived").value(false))
                .andExpect(jsonPath("$.isStarred").value(false))
                .andExpect(jsonPath("$.ownerId").value(TEST_USER_ID));
        }

        @Test
        @DisplayName("보드 즐겨찾기 제거 실패 - 인증 없음")
        void unstarBoard_Unauthorized() throws Exception {
            // when & then
            mockMvc.perform(post(Path.BOARDS + "/" + TEST_BOARD_ID + "/unstar")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("보드 즐겨찾기 제거 실패 - 권한 없음")
        void unstarBoard_Forbidden() throws Exception {
            // when & then
            mockMvc.perform(post(Path.BOARDS + "/" + TEST_BOARD_ID + "/unstar")
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "read openid")))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("보드 즐겨찾기 제거 실패 - 보드 찾을 수 없음")
        void unstarBoard_NotFound() throws Exception {
            // given
            given(toggleStarBoardUseCase.unstarringBoard(any(ToggleStarBoardCommand.class)))
                .willReturn(Either.left(new Failure.NotFoundFailure("보드를 찾을 수 없습니다")));

            // when & then
            mockMvc.perform(post(Path.BOARDS + "/" + TEST_BOARD_ID + "/unstar")
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid")))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("보드를 찾을 수 없습니다"));
        }

        @Test
        @DisplayName("보드 즐겨찾기 제거 실패 - 즐겨찾기 권한 없음")
        void unstarBoard_Forbidden_NotOwner() throws Exception {
            // given
            given(toggleStarBoardUseCase.unstarringBoard(any(ToggleStarBoardCommand.class)))
                .willReturn(Either.left(new Failure.ForbiddenFailure("보드 즐겨찾기 권한이 없습니다")));

            // when & then
            mockMvc.perform(post(Path.BOARDS + "/" + TEST_BOARD_ID + "/unstar")
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid")))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("보드 즐겨찾기 권한이 없습니다"));
        }

        @Test
        @DisplayName("보드 즐겨찾기 제거 실패 - 즐겨찾기에 없는 보드")
        void unstarBoard_Conflict_NotStarred() throws Exception {
            // given
            given(toggleStarBoardUseCase.unstarringBoard(any(ToggleStarBoardCommand.class)))
                .willReturn(Either.left(new Failure.ConflictFailure("즐겨찾기에 없는 보드입니다")));

            // when & then
            mockMvc.perform(post(Path.BOARDS + "/" + TEST_BOARD_ID + "/unstar")
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid")))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("즐겨찾기에 없는 보드입니다"));
        }

        @Test
        @DisplayName("보드 즐겨찾기 제거 실패 - 서버 오류")
        void unstarBoard_InternalServerError() throws Exception {
            // given
            given(toggleStarBoardUseCase.unstarringBoard(any(ToggleStarBoardCommand.class)))
                .willReturn(Either.left(new Failure.InternalServerError("서버 오류가 발생했습니다")));

            // when & then
            mockMvc.perform(post(Path.BOARDS + "/" + TEST_BOARD_ID + "/unstar")
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid")))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("서버 오류가 발생했습니다"));
        }
    }
} 
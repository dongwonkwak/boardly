package com.boardly.features.board.presentation;

import com.boardly.features.board.application.port.input.CreateBoardCommand;
import com.boardly.features.board.application.port.input.UpdateBoardCommand;
import com.boardly.features.board.application.port.input.ArchiveBoardCommand;
import com.boardly.features.board.application.usecase.CreateBoardUseCase;
import com.boardly.features.board.application.usecase.UpdateBoardUseCase;
import com.boardly.features.board.application.usecase.ArchiveBoardUseCase;
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

import java.time.LocalDateTime;
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

    @Autowired
    private ObjectMapper objectMapper;

    private CreateBoardRequest createBoardRequest;
    private UpdateBoardRequest updateBoardRequest;
    private Board testBoard;
    private final String TEST_USER_ID = "test-user-id";
    private final String TEST_BOARD_ID = "test-board-id";

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
        
        testBoard = Board.builder()
            .boardId(new BoardId(TEST_BOARD_ID))
            .title("Test Board")
            .description("Test Description")
            .isArchived(false)
            .ownerId(new UserId(TEST_USER_ID))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
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
                .ownerId(new UserId(TEST_USER_ID))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
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
                .ownerId(new UserId(TEST_USER_ID))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
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
                .ownerId(new UserId(TEST_USER_ID))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
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
    @DisplayName("HTTP 메서드 테스트")
    class HttpMethodTests {

        @Test
        @DisplayName("보드 생성 - GET 메서드 사용 시 405 Method Not Allowed")
        void createBoard_GetMethod_MethodNotAllowed() throws Exception {
            // when & then
            mockMvc.perform(get(Path.BOARDS)
                    .with(csrf())
                    .with(jwt().jwt(jwt -> jwt.subject(TEST_USER_ID).claim("scope", "write openid"))))
                .andExpect(status().isMethodNotAllowed());
        }

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
} 
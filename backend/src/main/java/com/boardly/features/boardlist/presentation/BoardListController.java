package com.boardly.features.boardlist.presentation;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import com.boardly.features.boardlist.application.port.input.GetBoardListsCommand;
import com.boardly.features.boardlist.application.port.input.CreateBoardListCommand;
import com.boardly.features.boardlist.application.port.input.UpdateBoardListCommand;
import com.boardly.features.boardlist.application.port.input.DeleteBoardListCommand;
import com.boardly.features.boardlist.application.port.input.UpdateBoardListPositionCommand;
import com.boardly.features.boardlist.application.usecase.GetBoardListsUseCase;
import com.boardly.features.boardlist.application.usecase.CreateBoardListUseCase;
import com.boardly.features.boardlist.application.usecase.UpdateBoardListUseCase;
import com.boardly.features.boardlist.application.usecase.DeleteBoardListUseCase;
import com.boardly.features.boardlist.application.usecase.UpdateBoardListPositionUseCase;
import com.boardly.features.boardlist.domain.model.BoardList;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.boardlist.presentation.response.BoardListResponse;
import com.boardly.features.boardlist.presentation.request.CreateBoardListRequest;
import com.boardly.features.boardlist.presentation.request.UpdateBoardListRequest;
import com.boardly.features.boardlist.presentation.request.UpdateBoardListPositionRequest;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.presentation.ApiFailureHandler;
import com.boardly.shared.presentation.Path;
import com.boardly.shared.presentation.response.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.vavr.control.Either;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(Path.BOARD_LISTS)
@RequiredArgsConstructor
@Tag(name = "BoardList", description = "보드 리스트 CRUD 관리 API")
public class BoardListController {

        private static final String TAGS = "BoardList";

        // CRUD 서비스 UseCase 인터페이스들
        private final CreateBoardListUseCase createBoardListUseCase; // Create
        private final GetBoardListsUseCase getBoardListsUseCase; // Read
        private final UpdateBoardListUseCase updateBoardListUseCase; // Update
        private final DeleteBoardListUseCase deleteBoardListUseCase; // Delete
        private final UpdateBoardListPositionUseCase updateBoardListPositionUseCase; // Update (Position)
        private final ApiFailureHandler failureHandler;

        @Operation(summary = "보드 리스트 목록 조회 (Read)", description = "특정 보드에 속한 모든 리스트를 position 순서대로 조회합니다.", tags = {
                        TAGS }, security = @SecurityRequirement(name = "oauth2", scopes = { "read", "openid" }))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "보드 리스트 목록 조회 성공", content = @Content(mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BoardListResponse.class)))),
                        @ApiResponse(responseCode = "403", description = "보드 접근 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "보드를 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
        })
        @PreAuthorize("hasAuthority('SCOPE_read') and hasAuthority('SCOPE_openid')")
        @GetMapping("/{boardId}")
        public ResponseEntity<?> getBoardLists(
                        @Parameter(description = "조회할 보드 ID", required = true) @PathVariable String boardId,
                        HttpServletRequest httpRequest,
                        @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

                String userId = jwt.getSubject();
                log.info("[READ] 보드 리스트 목록 조회 요청: userId={}, boardId={}", userId, boardId);

                GetBoardListsCommand command = new GetBoardListsCommand(
                                new BoardId(boardId),
                                new UserId(userId));

                Either<Failure, List<BoardList>> result = getBoardListsUseCase.getBoardLists(command);

                return result.fold(
                                failureHandler::handleFailure,
                                boardLists -> {
                                        log.info("[READ] 보드 리스트 목록 조회 성공: userId={}, boardId={}, listCount={}",
                                                        userId, boardId, boardLists.size());
                                        List<BoardListResponse> responses = boardLists.stream()
                                                        .map(BoardListResponse::from)
                                                        .toList();
                                        return ResponseEntity.ok(responses);
                                });
        }

        @Operation(summary = "보드 리스트 생성 (Create)", description = "특정 보드에 새로운 리스트를 생성합니다.", tags = {
                        TAGS }, security = @SecurityRequirement(name = "oauth2", scopes = { "read", "write",
                                        "openid" }))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "보드 리스트 생성 성공", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = BoardListResponse.class))),
                        @ApiResponse(responseCode = "403", description = "보드 접근 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "보드를 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
        })
        @PreAuthorize("hasAuthority('SCOPE_write') and hasAuthority('SCOPE_openid')")
        @PostMapping("/{boardId}")
        public ResponseEntity<?> createBoardList(
                        @Parameter(description = "리스트를 생성할 보드 ID", required = true) @PathVariable String boardId,
                        @Parameter(description = "보드 리스트 생성 정보", required = true) @RequestBody CreateBoardListRequest request,
                        HttpServletRequest httpRequest,
                        @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

                String userId = jwt.getSubject();
                log.info("[CREATE] 보드 리스트 생성 요청: userId={}, boardId={}, title={}", userId, boardId, request.getTitle());

                CreateBoardListCommand command = new CreateBoardListCommand(
                                new BoardId(boardId),
                                new UserId(userId),
                                request.getTitle(),
                                request.getDescription(),
                                request.getListColor());

                Either<Failure, BoardList> result = createBoardListUseCase.createBoardList(command);

                return result.fold(
                                failureHandler::handleFailure,
                                boardList -> {
                                        log.info("[CREATE] 보드 리스트 생성 성공: userId={}, boardId={}, listId={}",
                                                        userId, boardId, boardList.getListId().getId());
                                        BoardListResponse response = BoardListResponse.from(boardList);
                                        return ResponseEntity.status(201).body(response);
                                });
        }

        @Operation(summary = "보드 리스트 수정 (Update)", description = "특정 보드 리스트의 정보를 수정합니다.", tags = {
                        TAGS }, security = @SecurityRequirement(name = "oauth2", scopes = { "read", "write",
                                        "openid" }))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "보드 리스트 수정 성공", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = BoardListResponse.class))),
                        @ApiResponse(responseCode = "403", description = "보드 접근 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "보드 또는 리스트를 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
        })
        @PreAuthorize("hasAuthority('SCOPE_write') and hasAuthority('SCOPE_openid')")
        @PutMapping("/{listId}")
        public ResponseEntity<?> updateBoardList(
                        @Parameter(description = "수정할 리스트 ID", required = true) @PathVariable String listId,
                        @Parameter(description = "보드 리스트 수정 정보", required = true) @RequestBody UpdateBoardListRequest request,
                        HttpServletRequest httpRequest,
                        @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

                String userId = jwt.getSubject();
                log.info("[UPDATE] 보드 리스트 수정 요청: userId={}, listId={}, title={}",
                                userId, listId, request.getTitle());

                UpdateBoardListCommand command = new UpdateBoardListCommand(
                                new ListId(listId),
                                new UserId(userId),
                                request.getTitle(),
                                request.getDescription(),
                                request.getListColor());

                Either<Failure, BoardList> result = updateBoardListUseCase.updateBoardList(command);

                return result.fold(
                                failureHandler::handleFailure,
                                boardList -> {
                                        log.info("[UPDATE] 보드 리스트 수정 성공: userId={}, listId={}",
                                                        userId, boardList.getListId().getId());
                                        BoardListResponse response = BoardListResponse.from(boardList);
                                        return ResponseEntity.ok(response);
                                });
        }

        @Operation(summary = "보드 리스트 삭제 (Delete)", description = "특정 보드 리스트를 삭제합니다.", tags = {
                        TAGS }, security = @SecurityRequirement(name = "oauth2", scopes = { "write", "openid" }))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "보드 리스트 삭제 성공"),
                        @ApiResponse(responseCode = "403", description = "보드 접근 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "보드 또는 리스트를 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
        })
        @PreAuthorize("hasAuthority('SCOPE_write') and hasAuthority('SCOPE_openid')")
        @DeleteMapping("/{listId}")
        public ResponseEntity<?> deleteBoardList(
                        @Parameter(description = "삭제할 리스트 ID", required = true) @PathVariable String listId,
                        HttpServletRequest httpRequest,
                        @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

                String userId = jwt.getSubject();
                log.info("[DELETE] 보드 리스트 삭제 요청: userId={}, listId={}", userId, listId);

                DeleteBoardListCommand command = new DeleteBoardListCommand(
                                new ListId(listId),
                                new UserId(userId));

                Either<Failure, Void> result = deleteBoardListUseCase.deleteBoardList(command);

                return result.fold(
                                failureHandler::handleFailure,
                                success -> {
                                        log.info("[DELETE] 보드 리스트 삭제 성공: userId={}, listId={}", userId, listId);
                                        return ResponseEntity.noContent().build();
                                });
        }

        @Operation(summary = "보드 리스트 위치 변경 (Update Position)", description = "특정 보드 리스트의 위치를 변경합니다.", tags = {
                        TAGS }, security = @SecurityRequirement(name = "oauth2", scopes = { "write", "openid" }))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "보드 리스트 위치 변경 성공", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = BoardListResponse.class))),
                        @ApiResponse(responseCode = "403", description = "보드 접근 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "보드 또는 리스트를 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
        })
        @PreAuthorize("hasAuthority('SCOPE_write') and hasAuthority('SCOPE_openid')")
        @PutMapping("/{listId}/position")
        public ResponseEntity<?> updateBoardListPosition(
                        @Parameter(description = "위치를 변경할 리스트 ID", required = true) @PathVariable String listId,
                        @Parameter(description = "보드 리스트 위치 변경 정보", required = true) @RequestBody UpdateBoardListPositionRequest request,
                        HttpServletRequest httpRequest,
                        @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

                String userId = jwt.getSubject();
                log.info("[UPDATE-POSITION] 보드 리스트 위치 변경 요청: userId={}, listId={}, position={}",
                                userId, listId, request.getPosition());

                UpdateBoardListPositionCommand command = new UpdateBoardListPositionCommand(
                                new ListId(listId),
                                new UserId(userId),
                                request.getPosition());

                Either<Failure, List<BoardList>> result = updateBoardListPositionUseCase
                                .updateBoardListPosition(command);

                return result.fold(
                                failureHandler::handleFailure,
                                boardLists -> {
                                        // 변경된 리스트 중에서 요청한 리스트를 찾아서 반환
                                        BoardList updatedList = boardLists.stream()
                                                        .filter(list -> list.getListId().getId().equals(listId))
                                                        .findFirst()
                                                        .orElse(boardLists.get(0)); // fallback

                                        log.info("[UPDATE-POSITION] 보드 리스트 위치 변경 성공: userId={}, listId={}, newPosition={}, totalUpdated={}",
                                                        userId, updatedList.getListId().getId(),
                                                        updatedList.getPosition(), boardLists.size());
                                        BoardListResponse response = BoardListResponse.from(updatedList);
                                        return ResponseEntity.ok(response);
                                });
        }
}

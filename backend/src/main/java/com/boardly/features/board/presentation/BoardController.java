package com.boardly.features.board.presentation;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import com.boardly.features.board.application.port.input.CreateBoardCommand;
import com.boardly.features.board.application.port.input.UpdateBoardCommand;
import com.boardly.features.board.application.port.input.ArchiveBoardCommand;
import com.boardly.features.board.application.port.input.GetUserBoardsCommand;
import com.boardly.features.board.application.port.input.ToggleStarBoardCommand;
import com.boardly.features.board.application.port.input.RemoveBoardMemberCommand;
import com.boardly.features.board.application.usecase.CreateBoardUseCase;
import com.boardly.features.board.application.usecase.UpdateBoardUseCase;
import com.boardly.features.board.application.usecase.ArchiveBoardUseCase;
import com.boardly.features.board.application.usecase.GetUserBoardsUseCase;
import com.boardly.features.board.application.usecase.ToggleStarBoardUseCase;
import com.boardly.features.board.application.usecase.RemoveBoardMemberUseCase;
import com.boardly.features.board.domain.model.Board;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.board.presentation.request.CreateBoardRequest;
import com.boardly.features.board.presentation.request.UpdateBoardRequest;
import com.boardly.features.board.presentation.response.BoardResponse;
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
@RequestMapping(Path.BOARDS)
@RequiredArgsConstructor
@Tag(name = "Board", description = "보드 관리 API")
public class BoardController {

    private static final String TAGS = "Board";

    private final CreateBoardUseCase createBoardUseCase;
    private final GetUserBoardsUseCase getUserBoardsUseCase;
    private final UpdateBoardUseCase updateBoardUseCase;
    private final ArchiveBoardUseCase archiveBoardUseCase;
    private final ToggleStarBoardUseCase toggleStarBoardUseCase;
    private final RemoveBoardMemberUseCase removeBoardMemberUseCase;
    private final ApiFailureHandler failureHandler;

    @Operation(summary = "내 보드 목록 조회", description = "현재 사용자가 소유한 보드 목록을 조회합니다. 쿼리 파라미터로 아카이브된 보드 포함 여부를 설정할 수 있습니다.", tags = {
            TAGS }, security = @SecurityRequirement(name = "oauth2", scopes = { "read", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보드 목록 조회 성공", content = @Content(mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BoardResponse.class)))),
            @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_read') and hasAuthority('SCOPE_openid')")
    @GetMapping
    public ResponseEntity<?> getMyBoards(
            @Parameter(description = "아카이브된 보드 포함 여부 (기본값: false)", required = false) @RequestParam(defaultValue = "false") boolean includeArchived,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("사용자 보드 목록 조회 요청: userId={}, includeArchived={}", userId, includeArchived);

        GetUserBoardsCommand command = new GetUserBoardsCommand(
                new UserId(userId),
                includeArchived);

        Either<Failure, List<Board>> result = getUserBoardsUseCase.getUserBoards(command);

        return result.fold(
                failureHandler::handleFailure,
                boards -> {
                    log.info("사용자 보드 목록 조회 성공: userId={}, boardCount={}, includeArchived={}",
                            userId, boards.size(), includeArchived);
                    List<BoardResponse> responses = boards.stream()
                            .map(BoardResponse::from)
                            .toList();
                    return ResponseEntity.ok(responses);
                });
    }

    @Operation(summary = "보드 생성", description = "새로운 보드를 생성합니다.", tags = {
            TAGS }, security = @SecurityRequirement(name = "oauth2", scopes = { "write", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "보드 생성 성공", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = BoardResponse.class))),
            @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_write') and hasAuthority('SCOPE_openid')")
    @PostMapping
    public ResponseEntity<?> createBoard(
            @Parameter(description = "보드 생성 요청 정보", required = true) @RequestBody CreateBoardRequest request,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("보드 생성 요청: userId={}, title={}", userId, request.title());

        CreateBoardCommand command = CreateBoardCommand.of(
                request.title(),
                request.description(),
                new UserId(userId));

        Either<Failure, Board> result = createBoardUseCase.createBoard(command);

        return result.fold(
                failureHandler::handleFailure,
                board -> {
                    log.info("보드 생성 성공: boardId={}, title={}", board.getBoardId().getId(), board.getTitle());
                    return ResponseEntity.status(HttpStatus.CREATED).body(BoardResponse.from(board));
                });
    }

    @Operation(summary = "보드 업데이트", description = "기존 보드의 제목과 설명을 업데이트합니다.", tags = {
            TAGS }, security = @SecurityRequirement(name = "oauth2", scopes = { "write", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보드 업데이트 성공", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = BoardResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "보드 수정 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "보드를 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "아카이브된 보드는 수정 불가", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_write') and hasAuthority('SCOPE_openid')")
    @PutMapping("/{boardId}")
    public ResponseEntity<?> updateBoard(
            @Parameter(description = "업데이트할 보드 ID", required = true) @PathVariable String boardId,
            @Parameter(description = "보드 업데이트 요청 정보", required = true) @RequestBody UpdateBoardRequest request,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("보드 업데이트 요청: userId={}, boardId={}, title={}", userId, boardId, request.title());

        UpdateBoardCommand command = UpdateBoardCommand.of(
                new BoardId(boardId),
                request.title(),
                request.description(),
                new UserId(userId));

        Either<Failure, Board> result = updateBoardUseCase.updateBoard(command);

        return result.fold(
                failureHandler::handleFailure,
                board -> {
                    log.info("보드 업데이트 성공: boardId={}, title={}", board.getBoardId().getId(), board.getTitle());
                    return ResponseEntity.ok(BoardResponse.from(board));
                });
    }

    @Operation(summary = "보드 아카이브", description = "보드를 아카이브합니다. 아카이브된 보드는 읽기 전용이 되며, 내용 수정이 불가능합니다.", tags = {
            TAGS }, security = @SecurityRequirement(name = "oauth2", scopes = { "write", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보드 아카이브 성공", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = BoardResponse.class))),
            @ApiResponse(responseCode = "403", description = "보드 아카이브 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "보드를 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 아카이브된 보드", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_write') and hasAuthority('SCOPE_openid')")
    @PostMapping("/{boardId}/archive")
    public ResponseEntity<?> archiveBoard(
            @Parameter(description = "아카이브할 보드 ID", required = true) @PathVariable String boardId,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("보드 아카이브 요청: userId={}, boardId={}", userId, boardId);

        ArchiveBoardCommand command = ArchiveBoardCommand.of(
                new BoardId(boardId),
                new UserId(userId));

        Either<Failure, Board> result = archiveBoardUseCase.archiveBoard(command);

        return result.fold(
                failureHandler::handleFailure,
                board -> {
                    log.info("보드 아카이브 성공: boardId={}", board.getBoardId().getId());
                    return ResponseEntity.ok(BoardResponse.from(board));
                });
    }

    @Operation(summary = "보드 언아카이브", description = "보드를 언아카이브합니다. 언아카이브된 보드는 다시 활성 상태가 되며, 내용 수정이 가능합니다.", tags = {
            TAGS }, security = @SecurityRequirement(name = "oauth2", scopes = { "write", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보드 언아카이브 성공", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = BoardResponse.class))),
            @ApiResponse(responseCode = "403", description = "보드 언아카이브 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "보드를 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 활성화된 보드", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_write') and hasAuthority('SCOPE_openid')")
    @PostMapping("/{boardId}/unarchive")
    public ResponseEntity<?> unarchiveBoard(
            @Parameter(description = "언아카이브할 보드 ID", required = true) @PathVariable String boardId,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("보드 언아카이브 요청: userId={}, boardId={}", userId, boardId);

        ArchiveBoardCommand command = ArchiveBoardCommand.of(
                new BoardId(boardId),
                new UserId(userId));

        Either<Failure, Board> result = archiveBoardUseCase.unarchiveBoard(command);

        return result.fold(
                failureHandler::handleFailure,
                board -> {
                    log.info("보드 언아카이브 성공: boardId={}", board.getBoardId().getId());
                    return ResponseEntity.ok(BoardResponse.from(board));
                });
    }

    @Operation(summary = "보드 즐겨찾기 추가", description = "보드를 즐겨찾기에 추가합니다.", tags = {
            TAGS }, security = @SecurityRequirement(name = "oauth2", scopes = { "write", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보드 즐겨찾기 추가 성공", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = BoardResponse.class))),
            @ApiResponse(responseCode = "403", description = "보드 즐겨찾기 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "보드를 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 즐겨찾기에 추가된 보드", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_write') and hasAuthority('SCOPE_openid')")
    @PostMapping("/{boardId}/star")
    public ResponseEntity<?> starBoard(
            @Parameter(description = "즐겨찾기에 추가할 보드 ID", required = true) @PathVariable String boardId,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("보드 즐겨찾기 추가 요청: userId={}, boardId={}", userId, boardId);

        ToggleStarBoardCommand command = ToggleStarBoardCommand.of(
                new BoardId(boardId),
                new UserId(userId));

        Either<Failure, Board> result = toggleStarBoardUseCase.starringBoard(command);

        return result.fold(
                failureHandler::handleFailure,
                board -> {
                    log.info("보드 즐겨찾기 추가 성공: boardId={}", board.getBoardId().getId());
                    return ResponseEntity.ok(BoardResponse.from(board));
                });
    }

    @Operation(summary = "보드 즐겨찾기 제거", description = "보드를 즐겨찾기에서 제거합니다.", tags = {
            TAGS }, security = @SecurityRequirement(name = "oauth2", scopes = { "write", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보드 즐겨찾기 제거 성공", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = BoardResponse.class))),
            @ApiResponse(responseCode = "403", description = "보드 즐겨찾기 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "보드를 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "즐겨찾기에 없는 보드", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_write') and hasAuthority('SCOPE_openid')")
    @PostMapping("/{boardId}/unstar")
    public ResponseEntity<?> unstarBoard(
            @Parameter(description = "즐겨찾기에서 제거할 보드 ID", required = true) @PathVariable String boardId,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("보드 즐겨찾기 제거 요청: userId={}, boardId={}", userId, boardId);

        ToggleStarBoardCommand command = ToggleStarBoardCommand.of(
                new BoardId(boardId),
                new UserId(userId));

        Either<Failure, Board> result = toggleStarBoardUseCase.unstarringBoard(command);

        return result.fold(
                failureHandler::handleFailure,
                board -> {
                    log.info("보드 즐겨찾기 제거 성공: boardId={}", board.getBoardId().getId());
                    return ResponseEntity.ok(BoardResponse.from(board));
                });
    }

    @Operation(summary = "보드 멤버 삭제", description = "보드에서 특정 멤버를 삭제합니다. 보드 소유자만 멤버를 삭제할 수 있습니다.", tags = {
            TAGS }, security = @SecurityRequirement(name = "oauth2", scopes = { "write", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보드 멤버 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "보드 멤버 삭제 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "보드 또는 멤버를 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "아카이브된 보드 또는 OWNER 역할 멤버 삭제 불가", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_write') and hasAuthority('SCOPE_openid')")
    @DeleteMapping("/{boardId}/members/{targetUserId}")
    public ResponseEntity<?> removeBoardMember(
            @Parameter(description = "보드 ID", required = true) @PathVariable String boardId,
            @Parameter(description = "삭제할 멤버의 사용자 ID", required = true) @PathVariable String targetUserId,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("보드 멤버 삭제 요청: boardId={}, targetUserId={}, requestedBy={}", boardId, targetUserId, userId);

        RemoveBoardMemberCommand command = new RemoveBoardMemberCommand(
                new BoardId(boardId),
                new UserId(targetUserId),
                new UserId(userId));

        Either<Failure, Void> result = removeBoardMemberUseCase.removeBoardMember(command);

        return result.fold(
                failureHandler::handleFailure,
                success -> {
                    log.info("보드 멤버 삭제 성공: boardId={}, targetUserId={}, requestedBy={}", boardId, targetUserId, userId);
                    return ResponseEntity.ok().build();
                });
    }
}

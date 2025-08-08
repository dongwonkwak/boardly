package com.boardly.features.activity.presentation;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import com.boardly.features.activity.application.port.input.GetActivityQuery;
import com.boardly.features.activity.application.port.output.ActivityListResponse;
import com.boardly.features.activity.application.usecase.GetActivityUseCase;
import com.boardly.features.board.domain.model.BoardId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.presentation.ApiFailureHandler;
import com.boardly.shared.presentation.Path;
import com.boardly.shared.presentation.response.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "Activities", description = "활동 관리 API")
@Slf4j
@RestController
@RequestMapping(Path.ACTIVITIES)
@RequiredArgsConstructor
public class ActivityController {

    private final GetActivityUseCase getActivityUseCase;
    private final ApiFailureHandler failureHandler;

    @Operation(summary = "보드 활동 목록 조회", description = "특정 보드의 활동 목록을 페이징과 시간 필터링을 통해 조회합니다.", tags = {
            "Activities" }, security = @SecurityRequirement(name = "oauth2", scopes = { "read", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "활동 목록 조회 성공", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ActivityListResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "활동 조회 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "보드를 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_read') and hasAuthority('SCOPE_openid')")
    @GetMapping("/boards/{boardId}")
    public ResponseEntity<?> getBoardActivities(
            @Parameter(description = "조회할 보드 ID", required = true) @PathVariable String boardId,
            @Parameter(description = "페이지 번호 (0부터 시작, 기본값: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (기본값: 50, 최대: 100)") @RequestParam(defaultValue = "50") int size,
            @Parameter(description = "특정 시점 이후 활동 조회 (ISO 8601 형식)") @RequestParam(required = false) Instant since,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("보드 활동 목록 조회 요청: userId={}, boardId={}, page={}, size={}, since={}",
                userId, boardId, page, size, since);

        GetActivityQuery query = GetActivityQuery.forBoardWithPagination(new BoardId(boardId), page, size);

        Either<Failure, ActivityListResponse> result = getActivityUseCase.getActivities(query);

        return result.fold(
                failureHandler::handleFailure,
                response -> {
                    log.info("보드 활동 목록 조회 성공: boardId={}, 활동 개수={}",
                            boardId, response.activities().size());
                    return ResponseEntity.ok(response);
                });
    }

    @Operation(summary = "내 활동 목록 조회", description = "현재 사용자의 모든 활동 목록을 최신순으로 조회합니다.", tags = {
            "Activities" }, security = @SecurityRequirement(name = "oauth2", scopes = { "read", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "활동 목록 조회 성공", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ActivityListResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "활동 조회 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_read') and hasAuthority('SCOPE_openid')")
    @GetMapping("/me")
    public ResponseEntity<?> getMyActivities(
            @Parameter(description = "페이지 번호 (0부터 시작, 기본값: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (기본값: 50, 최대: 100)") @RequestParam(defaultValue = "50") int size,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("내 활동 목록 조회 요청: userId={}, page={}, size={}",
                userId, page, size);

        GetActivityQuery query = GetActivityQuery.forUserWithPagination(new UserId(userId), page, size);

        Either<Failure, ActivityListResponse> result = getActivityUseCase.getActivities(query);

        return result.fold(
                failureHandler::handleFailure,
                response -> {
                    log.info("내 활동 목록 조회 성공: userId={}, 활동 개수={}",
                            userId, response.activities().size());
                    return ResponseEntity.ok(response);
                });
    }
}

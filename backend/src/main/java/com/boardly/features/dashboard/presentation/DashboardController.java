package com.boardly.features.dashboard.presentation;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import com.boardly.features.dashboard.application.dto.DashboardResponse;
import com.boardly.features.dashboard.application.port.input.GetDashboardCommand;
import com.boardly.features.dashboard.application.usecase.GetDashboardUseCase;
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

/**
 * 대시보드 컨트롤러
 * 
 * <p>
 * 대시보드 관련 API 엔드포인트를 제공합니다.
 * 사용자의 대시보드 정보를 조회하는 기능을 담당합니다.
 * </p>
 */
@Tag(name = "Dashboard", description = "대시보드 관리 API")
@Slf4j
@RestController
@RequestMapping(Path.DASHBOARD)
@RequiredArgsConstructor
public class DashboardController {

    private static final String TAGS = "Dashboard";

    private final GetDashboardUseCase getDashboardUseCase;
    private final ApiFailureHandler failureHandler;

    @Operation(summary = "대시보드 조회", description = "현재 사용자의 대시보드 정보를 조회합니다. 보드 목록, 최근 활동, 통계 정보를 포함합니다.", tags = {
            TAGS }, security = @SecurityRequirement(name = "oauth2", scopes = { "read", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "대시보드 조회 성공", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = DashboardResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "대시보드 조회 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_read') and hasAuthority('SCOPE_openid')")
    @GetMapping
    public ResponseEntity<?> getDashboard(
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("대시보드 조회 요청: userId={}", userId);

        GetDashboardCommand command = new GetDashboardCommand(new UserId(userId));

        Either<Failure, DashboardResponse> result = getDashboardUseCase.getDashboard(command);

        return result.fold(
                failureHandler::handleFailure,
                dashboard -> {
                    log.info("대시보드 조회 성공: userId={}, boardCount={}, activityCount={}",
                            userId, dashboard.boards().size(), dashboard.recentActivity().size());
                    return ResponseEntity.ok(dashboard);
                });
    }
}
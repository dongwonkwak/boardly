package com.boardly.features.label.presentation;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import com.boardly.features.label.application.port.input.CreateLabelCommand;
import com.boardly.features.label.application.port.input.UpdateLabelCommand;
import com.boardly.features.label.application.port.input.DeleteLabelCommand;
import com.boardly.features.label.presentation.request.CreateLabelRequest;
import com.boardly.features.label.presentation.request.UpdateLabelRequest;
import com.boardly.features.label.presentation.response.LabelResponse;
import com.boardly.features.label.application.usecase.CreateLabelUseCase;
import com.boardly.features.label.application.usecase.GetLabelUseCase;
import com.boardly.features.label.application.usecase.UpdateLabelUseCase;
import com.boardly.features.label.application.usecase.DeleteLabelUseCase;
import com.boardly.features.label.domain.model.Label;
import com.boardly.features.label.domain.model.LabelId;
import com.boardly.features.board.domain.model.BoardId;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(Path.LABELS)
@RequiredArgsConstructor
@Tag(name = "Label", description = "라벨 관리 API")
public class LabelController {

    private static final String TAGS = "Label";

    private final CreateLabelUseCase createLabelUseCase;
    private final GetLabelUseCase getLabelUseCase;
    private final UpdateLabelUseCase updateLabelUseCase;
    private final DeleteLabelUseCase deleteLabelUseCase;
    private final ApiFailureHandler failureHandler;

    @Operation(summary = "라벨 생성", description = "새로운 라벨을 생성합니다.", tags = {
            TAGS }, security = @SecurityRequirement(name = "oauth2", scopes = { "write", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "라벨 생성 성공", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = LabelResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "라벨 생성 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "보드를 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_write') and hasAuthority('SCOPE_openid')")
    @PostMapping
    public ResponseEntity<?> createLabel(
            @Parameter(description = "라벨 생성 요청 정보", required = true) @Valid @RequestBody CreateLabelRequest request,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("라벨 생성 요청: userId={}, boardId={}, name={}, color={}",
                userId, request.boardId(), request.name(), request.color());

        CreateLabelCommand command = CreateLabelCommand.of(
                new BoardId(request.boardId()),
                new UserId(userId),
                request.name(),
                request.color());

        Either<Failure, Label> result = createLabelUseCase.createLabel(command);

        return result.fold(
                failureHandler::handleFailure,
                label -> {
                    log.info("라벨 생성 성공: labelId={}, userId={}, boardId={}, name={}",
                            label.getLabelId(), userId, request.boardId(), request.name());
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body(LabelResponse.from(label));
                });
    }

    @Operation(summary = "라벨 조회", description = "특정 라벨의 정보를 조회합니다.", tags = {
            TAGS }, security = @SecurityRequirement(name = "oauth2", scopes = { "read", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "라벨 조회 성공", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = LabelResponse.class))),
            @ApiResponse(responseCode = "403", description = "라벨 조회 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "라벨을 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_read') and hasAuthority('SCOPE_openid')")
    @GetMapping("/{labelId}")
    public ResponseEntity<?> getLabel(
            @Parameter(description = "조회할 라벨 ID", required = true) @PathVariable String labelId,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("라벨 조회 요청: userId={}, labelId={}", userId, labelId);

        Either<Failure, Label> result = getLabelUseCase.getLabel(
                new LabelId(labelId),
                new UserId(userId));

        return result.fold(
                failureHandler::handleFailure,
                label -> {
                    log.info("라벨 조회 성공: labelId={}, userId={}", labelId, userId);
                    return ResponseEntity.ok(LabelResponse.from(label));
                });
    }

    @Operation(summary = "보드 라벨 목록 조회", description = "특정 보드의 모든 라벨 목록을 조회합니다.", tags = {
            TAGS }, security = @SecurityRequirement(name = "oauth2", scopes = { "read", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "라벨 목록 조회 성공", content = @Content(mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = LabelResponse.class)))),
            @ApiResponse(responseCode = "403", description = "라벨 조회 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "보드를 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_read') and hasAuthority('SCOPE_openid')")
    @GetMapping("/board/{boardId}")
    public ResponseEntity<?> getBoardLabels(
            @Parameter(description = "조회할 보드 ID", required = true) @PathVariable String boardId,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("보드 라벨 목록 조회 요청: userId={}, boardId={}", userId, boardId);

        Either<Failure, List<Label>> result = getLabelUseCase.getBoardLabels(
                new BoardId(boardId),
                new UserId(userId));

        return result.fold(
                failureHandler::handleFailure,
                labels -> {
                    log.info("보드 라벨 목록 조회 성공: userId={}, boardId={}, labelCount={}",
                            userId, boardId, labels.size());
                    List<LabelResponse> responses = labels.stream()
                            .map(LabelResponse::from)
                            .toList();
                    return ResponseEntity.ok(responses);
                });
    }

    @Operation(summary = "라벨 수정", description = "기존 라벨의 이름과 색상을 수정합니다.", tags = {
            TAGS }, security = @SecurityRequirement(name = "oauth2", scopes = { "write", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "라벨 수정 성공", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = LabelResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "라벨 수정 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "라벨을 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_write') and hasAuthority('SCOPE_openid')")
    @PutMapping("/{labelId}")
    public ResponseEntity<?> updateLabel(
            @Parameter(description = "수정할 라벨 ID", required = true) @PathVariable String labelId,
            @Parameter(description = "라벨 수정 요청 정보", required = true) @Valid @RequestBody UpdateLabelRequest request,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("라벨 수정 요청: userId={}, labelId={}, name={}, color={}",
                userId, labelId, request.name(), request.color());

        UpdateLabelCommand command = UpdateLabelCommand.of(
                new LabelId(labelId),
                new UserId(userId),
                request.name(),
                request.color());

        Either<Failure, Label> result = updateLabelUseCase.updateLabel(command);

        return result.fold(
                failureHandler::handleFailure,
                label -> {
                    log.info("라벨 수정 성공: labelId={}, userId={}, name={}",
                            labelId, userId, request.name());
                    return ResponseEntity.ok(LabelResponse.from(label));
                });
    }

    @Operation(summary = "라벨 삭제", description = "라벨을 삭제합니다.", tags = {
            TAGS }, security = @SecurityRequirement(name = "oauth2", scopes = { "write", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "라벨 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "라벨 삭제 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "라벨을 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_write') and hasAuthority('SCOPE_openid')")
    @DeleteMapping("/{labelId}")
    public ResponseEntity<?> deleteLabel(
            @Parameter(description = "삭제할 라벨 ID", required = true) @PathVariable String labelId,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("라벨 삭제 요청: userId={}, labelId={}", userId, labelId);

        DeleteLabelCommand command = new DeleteLabelCommand(
                new LabelId(labelId),
                new UserId(userId));

        Either<Failure, Void> result = deleteLabelUseCase.deleteLabel(command);

        return result.fold(
                failureHandler::handleFailure,
                success -> {
                    log.info("라벨 삭제 성공: labelId={}, userId={}", labelId, userId);
                    return ResponseEntity.noContent().build();
                });
    }
}

package com.boardly.features.user.presentation;

import com.boardly.features.user.application.port.input.RegisterUserCommand;
import com.boardly.features.user.application.port.input.UpdateUserCommand;
import com.boardly.features.user.application.usecase.RegisterUserUseCase;
import com.boardly.features.user.application.usecase.UpdateUserUseCase;
import com.boardly.features.user.domain.model.User;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.features.user.presentation.request.RegisterUserRequest;
import com.boardly.features.user.presentation.request.UpdateUserRequest;
import com.boardly.features.user.presentation.response.UserResponse;
import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.presentation.ApiFailureHandler;
import com.boardly.shared.presentation.Path;
import com.boardly.shared.presentation.response.ErrorResponse;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(Path.USERS)
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관리 API")
public class UserController {

    private static final String TAGS = "User";

    private final RegisterUserUseCase registerUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;

    @Operation(
        summary = "사용자 등록",
        description = "사용자를 등록합니다.",
        security = {},
        tags = {TAGS})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "사용자 등록 성공",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Parameter(description = "회원가입 요청 정보", required = true)
            @RequestBody RegisterUserRequest request,
            HttpServletRequest httpRequest) {
        log.info("사용자 등록 요청: email={}", request.email());

        RegisterUserCommand command = new RegisterUserCommand(
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName()
        );

        Either<Failure, User> result = registerUserUseCase.register(command);

        return result.fold(
                ApiFailureHandler::handleFailure,
                user -> {
                    log.info("사용자 등록 성공: userId={}, email={}", user.getUserId().getId(), user.getEmail());
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body(UserResponse.from(user));
                }
        );
    }

    @Operation(
        summary = "사용자 업데이트",
        description = "사용자를 업데이트합니다.",
        tags = {TAGS},
        security = @SecurityRequirement(name = "oauth2", scopes = {"write", "openid"}))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 업데이트 성공",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_write') and hasAuthority('SCOPE_openid')")
    @PutMapping
    public ResponseEntity<?> updateUser(
            @Parameter(description = "사용자 업데이트 요청 정보", required = true)
            @RequestBody UpdateUserRequest request,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("사용자 업데이트 요청: userId={}", userId);

        UpdateUserCommand command = new UpdateUserCommand(
                new UserId(userId),
                request.firstName(),
                request.lastName()
        );

        Either<Failure, User> result = updateUserUseCase.update(command);

        return result.fold(
                ApiFailureHandler::handleFailure,
                user -> {
                    log.info("사용자 업데이트 성공: userId={}, email={}", user.getUserId().getId(), user.getEmail());
                    return ResponseEntity.ok(UserResponse.from(user));
                }
        );
    }
} 
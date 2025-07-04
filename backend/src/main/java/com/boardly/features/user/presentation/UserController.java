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
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterUserRequest request, HttpServletRequest httpRequest) {
        log.info("사용자 등록 요청: email={}", request.email());

        RegisterUserCommand command = new RegisterUserCommand(
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName()
        );

        Either<Failure, User> result = registerUserUseCase.register(command);

        return result.fold(
                failure -> ApiFailureHandler.handleFailure(failure, httpRequest.getRequestURI()),
                user -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(UserResponse.from(user))
        );
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable String userId, @RequestBody UpdateUserRequest request, HttpServletRequest httpRequest) {
        log.info("사용자 업데이트 요청: userId={}", userId);

        UpdateUserCommand command = new UpdateUserCommand(
                new UserId(userId),
                request.firstName(),
                request.lastName()
        );

        Either<Failure, User> result = updateUserUseCase.update(command);

        return result.fold(
                failure -> ApiFailureHandler.handleFailure(failure, httpRequest.getRequestURI()),
                user -> ResponseEntity.ok(UserResponse.from(user))
        );
    }
} 
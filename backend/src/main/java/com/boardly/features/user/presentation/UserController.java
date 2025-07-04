package com.boardly.features.user.presentation;

import com.boardly.features.user.application.port.input.RegisterUserCommand;
import com.boardly.features.user.application.port.input.UpdateUserCommand;
import com.boardly.features.user.application.usecase.RegisterUserUseCase;
import com.boardly.features.user.application.usecase.UpdateUserUseCase;
import com.boardly.features.user.domain.model.User;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterUserRequest request) {
        log.info("사용자 등록 요청: email={}", request.email());

        RegisterUserCommand command = new RegisterUserCommand(
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName()
        );

        Either<Failure, User> result = registerUserUseCase.register(command);

        return result.fold(
                failure -> handleFailure(failure),
                user -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(UserResponse.from(user))
        );
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable String userId, @RequestBody UpdateUserRequest request) {
        log.info("사용자 업데이트 요청: userId={}", userId);

        UpdateUserCommand command = new UpdateUserCommand(
                new UserId(userId),
                request.firstName(),
                request.lastName()
        );

        Either<Failure, User> result = updateUserUseCase.update(command);

        return result.fold(
                failure -> handleFailure(failure),
                user -> ResponseEntity.ok(UserResponse.from(user))
        );
    }

    private ResponseEntity<?> handleFailure(Failure failure) {
        return switch (failure) {
            case Failure.ValidationFailure validationFailure -> 
                ResponseEntity.badRequest().body(ErrorResponse.validation(validationFailure));
            case Failure.ConflictFailure conflictFailure -> 
                ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.conflict(conflictFailure));
            case Failure.InternalServerError internalServerError -> 
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.internal(internalServerError));
            default -> 
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.internal(new Failure.InternalServerError("알 수 없는 오류가 발생했습니다.")));
        };
    }

    // DTO 클래스들
    public record RegisterUserRequest(
            String email,
            String password,
            String firstName,
            String lastName
    ) {}

    public record UpdateUserRequest(
            String firstName,
            String lastName
    ) {}

    public record UserResponse(
            String userId,
            String email,
            String firstName,
            String lastName,
            boolean isActive
    ) {
        public static UserResponse from(User user) {
            return new UserResponse(
                    user.getUserId().getId(),
                    user.getEmail(),
                    user.getUserProfile().firstName(),
                    user.getUserProfile().lastName(),
                    user.isActive()
            );
        }
    }

    public record ErrorResponse(
            String message,
            Object details
    ) {
        public static ErrorResponse validation(Failure.ValidationFailure failure) {
            return new ErrorResponse(failure.message(), failure.violations());
        }

        public static ErrorResponse conflict(Failure.ConflictFailure failure) {
            return new ErrorResponse(failure.message(), null);
        }

        public static ErrorResponse internal(Failure.InternalServerError failure) {
            return new ErrorResponse("서버 내부 오류가 발생했습니다.", null);
        }
    }
} 
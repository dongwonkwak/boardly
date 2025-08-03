package com.boardly.features.user.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import com.boardly.features.user.application.port.input.RegisterUserCommand;
import com.boardly.features.user.application.port.input.UpdateUserCommand;
import com.boardly.features.user.application.usecase.RegisterUserUseCase;
import com.boardly.features.user.application.usecase.UpdateUserUseCase;
import com.boardly.features.user.domain.model.User;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.features.user.domain.model.UserProfile;
import com.boardly.features.user.presentation.request.RegisterUserRequest;
import com.boardly.features.user.presentation.request.UpdateUserRequest;
import com.boardly.features.user.presentation.response.UserResponse;
import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.presentation.ApiFailureHandler;
import com.boardly.shared.presentation.response.ErrorResponse;

import io.vavr.control.Either;
import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController 테스트")
class UserControllerTest {

        @Mock
        private RegisterUserUseCase registerUserUseCase;

        @Mock
        private UpdateUserUseCase updateUserUseCase;

        @Mock
        private ApiFailureHandler failureHandler;

        @Mock
        private HttpServletRequest httpRequest;

        @Mock
        private Jwt jwt;

        private UserController controller;

        @BeforeEach
        void setUp() {
                controller = new UserController(registerUserUseCase, updateUserUseCase, failureHandler);
        }

        private User createTestUser(String userId, String email, String firstName, String lastName) {
                UserProfile userProfile = new UserProfile(firstName, lastName);
                return User.builder()
                                .userId(new UserId(userId))
                                .email(email)
                                .hashedPassword("hashedPassword123!")
                                .userProfile(userProfile)
                                .isActive(true)
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();
        }

        private Failure createTestFailure(String message) {
                return Failure.ofInputError(message);
        }

        @Nested
        @DisplayName("사용자 등록 테스트")
        class RegisterUserTest {

                @Test
                @DisplayName("유효한 정보로 사용자 등록이 성공해야 한다")
                void registerUser_WithValidData_ShouldReturnCreatedUser() {
                        // given
                        RegisterUserRequest request = new RegisterUserRequest(
                                        "test@example.com",
                                        "Password123!",
                                        "길동",
                                        "홍");

                        User createdUser = createTestUser("user-123", "test@example.com", "길동", "홍");

                        when(registerUserUseCase.register(any(RegisterUserCommand.class)))
                                        .thenReturn(Either.right(createdUser));

                        // when
                        ResponseEntity<?> response = controller.registerUser(request, httpRequest);

                        // then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                        assertThat(response.getBody()).isInstanceOf(UserResponse.class);

                        UserResponse actualResponse = (UserResponse) response.getBody();
                        if (actualResponse != null) {
                                assertThat(actualResponse.userId()).isEqualTo("user-123");
                                assertThat(actualResponse.email()).isEqualTo("test@example.com");
                                assertThat(actualResponse.firstName()).isEqualTo("길동");
                                assertThat(actualResponse.lastName()).isEqualTo("홍");
                                assertThat(actualResponse.isActive()).isTrue();
                        }

                        verify(registerUserUseCase).register(any(RegisterUserCommand.class));
                        verify(failureHandler, never()).handleFailure(any(Failure.class));
                }

                @Test
                @DisplayName("사용자 등록 실패 시 실패 응답을 반환해야 한다")
                void registerUser_WhenRegistrationFails_ShouldReturnFailureResponse() {
                        // given
                        RegisterUserRequest request = new RegisterUserRequest(
                                        "test@example.com",
                                        "Password123!",
                                        "길동",
                                        "홍");

                        Failure failure = createTestFailure("이미 존재하는 이메일입니다");
                        ErrorResponse errorResponse = ErrorResponse.of("VALIDATION_ERROR", "이미 존재하는 이메일입니다");
                        ResponseEntity<ErrorResponse> failureResponse = ResponseEntity.status(HttpStatus.CONFLICT)
                                        .body(errorResponse);

                        when(registerUserUseCase.register(any(RegisterUserCommand.class)))
                                        .thenReturn(Either.left(failure));
                        when(failureHandler.handleFailure(failure))
                                        .thenReturn(failureResponse);

                        // when
                        ResponseEntity<?> response = controller.registerUser(request, httpRequest);

                        // then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                        assertThat(response.getBody()).isInstanceOf(ErrorResponse.class);

                        ErrorResponse actualErrorResponse = (ErrorResponse) response.getBody();
                        if (actualErrorResponse != null) {
                                assertThat(actualErrorResponse.code()).isEqualTo("VALIDATION_ERROR");
                                assertThat(actualErrorResponse.message()).isEqualTo("이미 존재하는 이메일입니다");
                        }

                        verify(registerUserUseCase).register(any(RegisterUserCommand.class));
                        verify(failureHandler).handleFailure(failure);
                }

                @Test
                @DisplayName("RegisterUserCommand가 올바른 파라미터로 생성되어야 한다")
                void registerUser_ShouldCreateCorrectCommand() {
                        // given
                        RegisterUserRequest request = new RegisterUserRequest(
                                        "test@example.com",
                                        "Password123!",
                                        "길동",
                                        "홍");

                        User createdUser = createTestUser("user-123", "test@example.com", "길동", "홍");

                        when(registerUserUseCase.register(any(RegisterUserCommand.class)))
                                        .thenReturn(Either.right(createdUser));

                        // when
                        controller.registerUser(request, httpRequest);

                        // then
                        verify(registerUserUseCase)
                                        .register(argThat(command -> command.email().equals("test@example.com") &&
                                                        command.password().equals("Password123!") &&
                                                        command.firstName().equals("길동") &&
                                                        command.lastName().equals("홍")));
                }
        }

        @Nested
        @DisplayName("사용자 업데이트 테스트")
        class UpdateUserTest {

                @Test
                @DisplayName("유효한 정보로 사용자 업데이트가 성공해야 한다")
                void updateUser_WithValidData_ShouldReturnUpdatedUser() {
                        // given
                        String userId = "user-123";
                        UpdateUserRequest request = new UpdateUserRequest(
                                        "업데이트된길동",
                                        "업데이트된홍");

                        User updatedUser = createTestUser(userId, "test@example.com", "업데이트된길동", "업데이트된홍");

                        when(jwt.getSubject()).thenReturn(userId);
                        when(updateUserUseCase.update(any(UpdateUserCommand.class)))
                                        .thenReturn(Either.right(updatedUser));

                        // when
                        ResponseEntity<?> response = controller.updateUser(request, httpRequest, jwt);

                        // then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.getBody()).isInstanceOf(UserResponse.class);

                        UserResponse actualResponse = (UserResponse) response.getBody();
                        if (actualResponse != null) {
                                assertThat(actualResponse.userId()).isEqualTo(userId);
                                assertThat(actualResponse.email()).isEqualTo("test@example.com");
                                assertThat(actualResponse.firstName()).isEqualTo("업데이트된길동");
                                assertThat(actualResponse.lastName()).isEqualTo("업데이트된홍");
                                assertThat(actualResponse.isActive()).isTrue();
                        }

                        verify(jwt).getSubject();
                        verify(updateUserUseCase).update(any(UpdateUserCommand.class));
                        verify(failureHandler, never()).handleFailure(any(Failure.class));
                }

                @Test
                @DisplayName("사용자 업데이트 실패 시 실패 응답을 반환해야 한다")
                void updateUser_WhenUpdateFails_ShouldReturnFailureResponse() {
                        // given
                        String userId = "user-123";
                        UpdateUserRequest request = new UpdateUserRequest(
                                        "업데이트된길동",
                                        "업데이트된홍");

                        Failure failure = createTestFailure("사용자를 찾을 수 없습니다");
                        ErrorResponse errorResponse = ErrorResponse.of("NOT_FOUND", "사용자를 찾을 수 없습니다");
                        ResponseEntity<ErrorResponse> failureResponse = ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(errorResponse);

                        when(jwt.getSubject()).thenReturn(userId);
                        when(updateUserUseCase.update(any(UpdateUserCommand.class)))
                                        .thenReturn(Either.left(failure));
                        when(failureHandler.handleFailure(failure))
                                        .thenReturn(failureResponse);

                        // when
                        ResponseEntity<?> response = controller.updateUser(request, httpRequest, jwt);

                        // then
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(response.getBody()).isInstanceOf(ErrorResponse.class);

                        ErrorResponse actualErrorResponse = (ErrorResponse) response.getBody();
                        if (actualErrorResponse != null) {
                                assertThat(actualErrorResponse.code()).isEqualTo("NOT_FOUND");
                                assertThat(actualErrorResponse.message()).isEqualTo("사용자를 찾을 수 없습니다");
                        }

                        verify(jwt).getSubject();
                        verify(updateUserUseCase).update(any(UpdateUserCommand.class));
                        verify(failureHandler).handleFailure(failure);
                }

                @Test
                @DisplayName("UpdateUserCommand가 올바른 파라미터로 생성되어야 한다")
                void updateUser_ShouldCreateCorrectCommand() {
                        // given
                        String userId = "user-123";
                        UpdateUserRequest request = new UpdateUserRequest(
                                        "업데이트된길동",
                                        "업데이트된홍");

                        User updatedUser = createTestUser(userId, "test@example.com", "업데이트된길동", "업데이트된홍");

                        when(jwt.getSubject()).thenReturn(userId);
                        when(updateUserUseCase.update(any(UpdateUserCommand.class)))
                                        .thenReturn(Either.right(updatedUser));

                        // when
                        controller.updateUser(request, httpRequest, jwt);

                        // then
                        verify(updateUserUseCase).update(argThat(command -> command.userId().getId().equals(userId) &&
                                        command.firstName().equals("업데이트된길동") &&
                                        command.lastName().equals("업데이트된홍")));
                }

                @Test
                @DisplayName("JWT에서 사용자 ID를 올바르게 추출해야 한다")
                void updateUser_ShouldExtractUserIdFromJwt() {
                        // given
                        String userId = "user-456";
                        UpdateUserRequest request = new UpdateUserRequest(
                                        "업데이트된길동",
                                        "업데이트된홍");

                        User updatedUser = createTestUser(userId, "test@example.com", "업데이트된길동", "업데이트된홍");

                        when(jwt.getSubject()).thenReturn(userId);
                        when(updateUserUseCase.update(any(UpdateUserCommand.class)))
                                        .thenReturn(Either.right(updatedUser));

                        // when
                        controller.updateUser(request, httpRequest, jwt);

                        // then
                        verify(jwt).getSubject();
                        verify(updateUserUseCase).update(argThat(command -> command.userId().getId().equals(userId)));
                }
        }

        @Nested
        @DisplayName("UserResponse 변환 테스트")
        class UserResponseConversionTest {

                @Test
                @DisplayName("User 엔티티가 올바르게 UserResponse로 변환되어야 한다")
                void userResponse_ShouldConvertUserEntityCorrectly() {
                        // given
                        User user = createTestUser("user-123", "test@example.com", "길동", "홍");

                        // when
                        UserResponse response = UserResponse.from(user);

                        // then
                        assertThat(response.userId()).isEqualTo("user-123");
                        assertThat(response.email()).isEqualTo("test@example.com");
                        assertThat(response.firstName()).isEqualTo("길동");
                        assertThat(response.lastName()).isEqualTo("홍");
                        assertThat(response.isActive()).isTrue();
                }

                @Test
                @DisplayName("비활성화된 사용자도 올바르게 변환되어야 한다")
                void userResponse_ShouldConvertInactiveUserCorrectly() {
                        // given
                        User user = createTestUser("user-123", "test@example.com", "길동", "홍");
                        user.deactivate();

                        // when
                        UserResponse response = UserResponse.from(user);

                        // then
                        assertThat(response.userId()).isEqualTo("user-123");
                        assertThat(response.email()).isEqualTo("test@example.com");
                        assertThat(response.firstName()).isEqualTo("길동");
                        assertThat(response.lastName()).isEqualTo("홍");
                        assertThat(response.isActive()).isFalse();
                }
        }
}
package com.boardly.features.user.presentation;

import com.boardly.features.user.application.port.input.RegisterUserCommand;
import com.boardly.features.user.application.port.input.UpdateUserCommand;
import com.boardly.features.user.application.usecase.RegisterUserUseCase;
import com.boardly.features.user.application.usecase.UpdateUserUseCase;
import com.boardly.features.user.domain.model.User;
import com.boardly.features.user.domain.model.UserProfile;
import com.boardly.features.user.presentation.request.RegisterUserRequest;
import com.boardly.features.user.presentation.request.UpdateUserRequest;
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

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("UserController 테스트")
class UserControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private RegisterUserUseCase registerUserUseCase;

        @MockitoBean
        private UpdateUserUseCase updateUserUseCase;

        @Autowired
        private ObjectMapper objectMapper;

        private RegisterUserRequest registerUserRequest;
        private UpdateUserRequest updateUserRequest;
        private User testUser;

        @BeforeEach
        void setUp() {
                registerUserRequest = new RegisterUserRequest(
                                "test@example.com",
                                "password123",
                                "홍",
                                "길동");

                updateUserRequest = new UpdateUserRequest(
                                "이",
                                "순신");

                testUser = User.create(
                                "test@example.com",
                                "hashedPassword",
                                new UserProfile("홍", "길동"));
        }

        @Nested
        @DisplayName("사용자 등록 테스트")
        class RegisterUserTests {

                @Test
                @DisplayName("사용자 등록 성공")
                void registerUser_Success() throws Exception {
                        // Given
                        given(registerUserUseCase.register(any(RegisterUserCommand.class)))
                                        .willReturn(Either.right(testUser));

                        // When & Then
                        mockMvc.perform(post(Path.USERS + "/register")
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(registerUserRequest)))
                                        .andExpect(status().isCreated())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(jsonPath("$.userId").exists())
                                        .andExpect(jsonPath("$.email").value("test@example.com"))
                                        .andExpect(jsonPath("$.firstName").value("홍"))
                                        .andExpect(jsonPath("$.lastName").value("길동"))
                                        .andExpect(jsonPath("$.isActive").value(true));
                }

                @Test
                @DisplayName("사용자 등록 실패 - 입력 검증 오류")
                void registerUser_ValidationError() throws Exception {
                        // Given
                        String errorMessage = "입력 데이터가 올바르지 않습니다";
                        Failure validationError = Failure.ofInputError(
                                        errorMessage,
                                        "INVALID_INPUT",
                                        List.of(Failure.FieldViolation.builder()
                                                        .field("email")
                                                        .message("이메일 형식이 올바르지 않습니다")
                                                        .rejectedValue("invalid-email")
                                                        .build()));

                        given(registerUserUseCase.register(any(RegisterUserCommand.class)))
                                        .willReturn(Either.left(validationError));

                        // When & Then
                        mockMvc.perform(post(Path.USERS + "/register")
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(registerUserRequest)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(jsonPath("$.message").value(errorMessage))
                                        .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
                }

                @Test
                @DisplayName("사용자 등록 실패 - 이메일 중복")
                void registerUser_EmailConflict() throws Exception {
                        // Given
                        String errorMessage = "이미 사용 중인 이메일입니다";
                        Failure conflictError = Failure.ofResourceConflict(
                                        errorMessage,
                                        "EMAIL_ALREADY_EXISTS",
                                        Map.of("email", "test@example.com", "conflictType", "EMAIL_DUPLICATE"));

                        given(registerUserUseCase.register(any(RegisterUserCommand.class)))
                                        .willReturn(Either.left(conflictError));

                        // When & Then
                        mockMvc.perform(post(Path.USERS + "/register")
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(registerUserRequest)))
                                        .andExpect(status().isConflict())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(jsonPath("$.message").value(errorMessage))
                                        .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
                }

                @Test
                @DisplayName("사용자 등록 실패 - 서버 오류")
                void registerUser_InternalServerError() throws Exception {
                        // Given
                        String errorMessage = "데이터베이스 연결 오류";
                        Failure internalServerError = Failure.ofInternalError(
                                        errorMessage,
                                        "USER_REGISTRATION_ERROR",
                                        null);

                        given(registerUserUseCase.register(any(RegisterUserCommand.class)))
                                        .willReturn(Either.left(internalServerError));

                        // When & Then
                        mockMvc.perform(post(Path.USERS + "/register")
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(registerUserRequest)))
                                        .andExpect(status().isInternalServerError())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(jsonPath("$.message").value(errorMessage))
                                        .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                                        .andExpect(jsonPath("$.context").doesNotExist());
                }

                @Test
                @DisplayName("사용자 등록 실패 - 잘못된 JSON 형식")
                void registerUser_InvalidJson() throws Exception {
                        // When & Then
                        mockMvc.perform(post(Path.USERS + "/register")
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("invalid json"))
                                        .andExpect(status().isBadRequest());
                }
        }

        @Nested
        @DisplayName("사용자 업데이트 테스트")
        class UpdateUserTests {

                @Test
                @DisplayName("사용자 업데이트 성공")
                void updateUser_Success() throws Exception {
                        // Given
                        User updatedUser = User.create(
                                        "test@example.com",
                                        "hashedPassword",
                                        new UserProfile("이", "순신"));

                        given(updateUserUseCase.update(any(UpdateUserCommand.class)))
                                        .willReturn(Either.right(updatedUser));

                        // When & Then
                        mockMvc.perform(put(Path.USERS)
                                        .with(jwt().jwt(jwt -> jwt
                                                        .claim("sub", "test-user-id")
                                                        .claim("scope", "openid write")))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                                        .andExpect(status().isOk())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(jsonPath("$.firstName").value("이"))
                                        .andExpect(jsonPath("$.lastName").value("순신"));
                }

                @Test
                @DisplayName("사용자 업데이트 실패 - 인증 없음")
                void updateUser_Unauthorized() throws Exception {
                        // When & Then
                        mockMvc.perform(put(Path.USERS)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                                        .andExpect(status().isForbidden());
                }

                @Test
                @DisplayName("사용자 업데이트 실패 - 권한 없음")
                void updateUser_Forbidden() throws Exception {
                        // When & Then
                        mockMvc.perform(put(Path.USERS)
                                        .with(jwt().jwt(jwt -> jwt.claim("sub", "test-user-id")))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                                        .andExpect(status().isForbidden());
                }

                @Test
                @DisplayName("사용자 업데이트 실패 - write 권한 없음 (openid만 있는 경우)")
                void updateUser_Forbidden_OnlyOpenidScope() throws Exception {
                        // When & Then
                        mockMvc.perform(put(Path.USERS)
                                        .with(jwt().jwt(jwt -> jwt
                                                        .claim("sub", "test-user-id")
                                                        .claim("scope", "openid")))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                                        .andExpect(status().isForbidden());
                }

                @Test
                @DisplayName("사용자 업데이트 실패 - write 권한 없음 (완전히 다른 권한)")
                void updateUser_Forbidden_DifferentScope() throws Exception {
                        // When & Then
                        mockMvc.perform(put(Path.USERS)
                                        .with(jwt().jwt(jwt -> jwt
                                                        .claim("sub", "test-user-id")
                                                        .claim("scope", "read profile")))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                                        .andExpect(status().isForbidden());
                }

                @Test
                @DisplayName("사용자 업데이트 실패 - write 권한 없음 (빈 scope)")
                void updateUser_Forbidden_EmptyScope() throws Exception {
                        // When & Then
                        mockMvc.perform(put(Path.USERS)
                                        .with(jwt().jwt(jwt -> jwt
                                                        .claim("sub", "test-user-id")
                                                        .claim("scope", "")))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                                        .andExpect(status().isForbidden());
                }

                @Test
                @DisplayName("사용자 업데이트 실패 - write 권한 없음 (scope claim 없음)")
                void updateUser_Forbidden_NoScopeClaim() throws Exception {
                        // When & Then
                        mockMvc.perform(put(Path.USERS)
                                        .with(jwt().jwt(jwt -> jwt.claim("sub", "test-user-id")))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                                        .andExpect(status().isForbidden());
                }

                @Test
                @DisplayName("사용자 업데이트 실패 - openid 권한 없음 (write만 있는 경우)")
                void updateUser_Forbidden_OnlyWriteScope() throws Exception {
                        // When & Then
                        mockMvc.perform(put(Path.USERS)
                                        .with(jwt().jwt(jwt -> jwt
                                                        .claim("sub", "test-user-id")
                                                        .claim("scope", "write")))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                                        .andExpect(status().isForbidden());
                }

                @Test
                @DisplayName("사용자 업데이트 실패 - 사용자를 찾을 수 없음")
                void updateUser_NotFound() throws Exception {
                        // Given
                        String errorMessage = "사용자를 찾을 수 없습니다";
                        Failure notFoundError = Failure.ofNotFound(
                                        errorMessage,
                                        "USER_NOT_FOUND",
                                        Map.of("userId", "test-user-id"));

                        given(updateUserUseCase.update(any(UpdateUserCommand.class)))
                                        .willReturn(Either.left(notFoundError));

                        // When & Then
                        mockMvc.perform(put(Path.USERS)
                                        .with(jwt().jwt(jwt -> jwt
                                                        .claim("sub", "test-user-id")
                                                        .claim("scope", "openid write")))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                                        .andExpect(status().isNotFound())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(jsonPath("$.message").value(errorMessage))
                                        .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
                }

                @Test
                @DisplayName("사용자 업데이트 실패 - 유효성 검증 실패")
                void updateUser_ValidationFailure() throws Exception {
                        // Given
                        Failure validationFailure = Failure.ofInputError(
                                        "입력 데이터가 올바르지 않습니다.",
                                        "INVALID_INPUT",
                                        List.of(
                                                        Failure.FieldViolation.builder()
                                                                        .field("firstName")
                                                                        .message("이름은 필수입니다.")
                                                                        .rejectedValue("")
                                                                        .build()));

                        given(updateUserUseCase.update(any(UpdateUserCommand.class)))
                                        .willReturn(Either.left(validationFailure));

                        // When & Then
                        mockMvc.perform(put(Path.USERS)
                                        .with(jwt().jwt(jwt -> jwt
                                                        .claim("sub", "test-user-id")
                                                        .claim("scope", "openid write")))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(
                                                        new UpdateUserRequest("", "순신"))))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(jsonPath("$.message").value("입력 데이터가 올바르지 않습니다."))
                                        .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                                        .andExpect(jsonPath("$.details").isArray());
                }

                @Test
                @DisplayName("사용자 업데이트 실패 - 서버 오류")
                void updateUser_InternalServerError() throws Exception {
                        // Given
                        String errorMessage = "데이터베이스 연결 오류";
                        Failure internalServerError = Failure.ofInternalError(
                                        errorMessage,
                                        "USER_UPDATE_ERROR",
                                        null);

                        given(updateUserUseCase.update(any(UpdateUserCommand.class)))
                                        .willReturn(Either.left(internalServerError));

                        // When & Then
                        mockMvc.perform(put(Path.USERS)
                                        .with(jwt().jwt(jwt -> jwt
                                                        .claim("sub", "test-user-id")
                                                        .claim("scope", "openid write")))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                                        .andExpect(status().isInternalServerError())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(jsonPath("$.message").value(errorMessage))
                                        .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                                        .andExpect(jsonPath("$.context").doesNotExist());
                }

                @Test
                @DisplayName("사용자 업데이트 실패 - 잘못된 JSON 형식")
                void updateUser_InvalidJson() throws Exception {
                        // When & Then
                        mockMvc.perform(put(Path.USERS)
                                        .with(jwt().jwt(jwt -> jwt
                                                        .claim("sub", "test-user-id")
                                                        .claim("scope", "openid write")))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("invalid json"))
                                        .andExpect(status().isBadRequest());
                }
        }

        @Nested
        @DisplayName("HTTP 메서드 테스트")
        class HttpMethodTests {

                @Test
                @DisplayName("업데이트 엔드포인트 - POST 메서드 사용 시 405 Method Not Allowed")
                void updateUser_PostMethod_MethodNotAllowed() throws Exception {
                        // When & Then
                        mockMvc.perform(post(Path.USERS)
                                        .with(jwt().jwt(jwt -> jwt
                                                        .claim("sub", "test-user-id")
                                                        .claim("scope", "openid write")))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                                        .andExpect(status().isMethodNotAllowed());
                }
        }

        @Nested
        @DisplayName("Content-Type 테스트")
        class ContentTypeTests {

                @Test
                @DisplayName("등록 - Content-Type이 application/json이 아닌 경우")
                void registerUser_InvalidContentType() throws Exception {
                        // When & Then
                        mockMvc.perform(post(Path.USERS + "/register")
                                        .with(csrf())
                                        .contentType(MediaType.TEXT_PLAIN)
                                        .content("test"))
                                        .andExpect(status().isUnsupportedMediaType());
                }

                @Test
                @DisplayName("업데이트 - Content-Type이 application/json이 아닌 경우")
                void updateUser_InvalidContentType() throws Exception {
                        // When & Then
                        mockMvc.perform(put(Path.USERS)
                                        .with(jwt().jwt(jwt -> jwt
                                                        .claim("sub", "test-user-id")
                                                        .claim("scope", "openid write")))
                                        .contentType(MediaType.TEXT_PLAIN)
                                        .content("test"))
                                        .andExpect(status().isUnsupportedMediaType());
                }
        }
}
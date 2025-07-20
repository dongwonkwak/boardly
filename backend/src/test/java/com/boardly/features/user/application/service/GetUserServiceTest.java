package com.boardly.features.user.application.service;

import com.boardly.features.user.domain.model.User;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.features.user.domain.model.UserProfile;
import com.boardly.features.user.domain.repository.UserRepository;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserServiceTest {

    private GetUserService getUserService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ValidationMessageResolver validationMessageResolver;

    @BeforeEach
    void setUp() {
        getUserService = new GetUserService(userRepository, validationMessageResolver);
    }

    private UserId createValidUserId() {
        return new UserId("test-user-id");
    }

    private User createValidUser() {
        return User.builder()
                .userId(createValidUserId())
                .email("test@example.com")
                .hashedPassword("hashedPassword")
                .userProfile(new UserProfile("John", "Doe"))
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("존재하는 사용자 ID로 조회 시 사용자를 반환해야 한다")
    void get_withExistingUserId_shouldReturnUser() {
        // given
        UserId userId = createValidUserId();
        User expectedUser = createValidUser();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(expectedUser));

        // when
        Either<Failure, User> result = getUserService.get(userId);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isEqualTo(expectedUser);
        assertThat(result.get().getUserId()).isEqualTo(userId);
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");

        verify(userRepository).findById(userId);
        verify(validationMessageResolver, never()).getMessage(anyString());
    }

    @Test
    @DisplayName("사용자를 찾을 수 없을 때 NotFound를 반환해야 한다")
    void get_withUserNotFound_shouldReturnNotFound() {
        // given
        UserId userId = createValidUserId();
        String errorMessage = "사용자를 찾을 수 없습니다";

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());
        when(validationMessageResolver.getMessage("validation.user.email.not.found"))
                .thenReturn(errorMessage);

        // when
        Either<Failure, User> result = getUserService.get(userId);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);

        Failure.NotFound notFound = (Failure.NotFound) result.getLeft();
        assertThat(notFound.getMessage()).isEqualTo(errorMessage);
        assertThat(notFound.getErrorCode()).isEqualTo("USER_NOT_FOUND");
        assertThat(notFound.getContext()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> context = (Map<String, Object>) notFound.getContext();
        assertThat(context.get("userId")).isEqualTo(userId.getId());

        verify(userRepository).findById(userId);
        verify(validationMessageResolver).getMessage("validation.user.email.not.found");
    }

    @Test
    @DisplayName("데이터베이스 예외 발생 시 InternalError를 반환해야 한다")
    void get_withDatabaseException_shouldReturnInternalError() {
        // given
        UserId userId = createValidUserId();
        String errorMessage = "데이터베이스 연결 오류";

        when(userRepository.findById(userId))
                .thenThrow(new RuntimeException(errorMessage));

        // when
        Either<Failure, User> result = getUserService.get(userId);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);

        Failure.InternalError internalError = (Failure.InternalError) result.getLeft();
        assertThat(internalError.getMessage()).isEqualTo(errorMessage);
        assertThat(internalError.getErrorCode()).isEqualTo("USER_QUERY_ERROR");
        assertThat(internalError.getContext()).isNull();

        verify(userRepository).findById(userId);
        verify(validationMessageResolver, never()).getMessage(anyString());
    }

    @Test
    @DisplayName("사용자 조회 시 로그가 기록되어야 한다")
    void get_shouldLogErrorWhenExceptionOccurs() {
        // given
        UserId userId = createValidUserId();
        String errorMessage = "데이터베이스 연결 오류";

        when(userRepository.findById(userId))
                .thenThrow(new RuntimeException(errorMessage));

        // when
        Either<Failure, User> result = getUserService.get(userId);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().getMessage()).isEqualTo(errorMessage);

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("null UserId로 조회 시 예외를 반환해야 한다")
    void get_withNullUserId_shouldReturnInternalError() {
        // given
        UserId userId = null;
        String errorMessage = "User ID cannot be null";

        when(userRepository.findById(userId))
                .thenThrow(new IllegalArgumentException(errorMessage));

        // when
        Either<Failure, User> result = getUserService.get(userId);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);

        Failure.InternalError internalError = (Failure.InternalError) result.getLeft();
        assertThat(internalError.getMessage()).isEqualTo(errorMessage);
        assertThat(internalError.getErrorCode()).isEqualTo("USER_QUERY_ERROR");
        assertThat(internalError.getContext()).isNull();

        verify(userRepository).findById(userId);
    }
}
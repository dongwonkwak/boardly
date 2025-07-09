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

import java.time.LocalDateTime;
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

    private User createValidUser(UserId userId) {
        UserProfile userProfile = new UserProfile("길동", "홍");
        return User.builder()
                .userId(userId)
                .email("test@example.com")
                .hashedPassword("hashedPassword123!")
                .userProfile(userProfile)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("존재하는 사용자 ID로 조회 시 사용자를 반환해야 한다")
    void get_withExistingUserId_shouldReturnUser() {
        // given
        UserId userId = createValidUserId();
        User expectedUser = createValidUser(userId);

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
    @DisplayName("존재하지 않는 사용자 ID로 조회 시 NotFound 오류를 반환해야 한다")
    void get_withNonExistingUserId_shouldReturnNotFoundFailure() {
        // given
        UserId userId = createValidUserId();
        String notFoundMessage = "사용자를 찾을 수 없습니다.";

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());
        when(validationMessageResolver.getMessage("validation.user.email.not.found"))
                .thenReturn(notFoundMessage);

        // when
        Either<Failure, User> result = getUserService.get(userId);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.NotFoundFailure.class);
        assertThat(result.getLeft().message()).isEqualTo(notFoundMessage);

        verify(userRepository).findById(userId);
        verify(validationMessageResolver).getMessage("validation.user.email.not.found");
    }

    @Test
    @DisplayName("데이터베이스 예외 발생 시 InternalServerError를 반환해야 한다")
    void get_withDatabaseException_shouldReturnInternalServerError() {
        // given
        UserId userId = createValidUserId();
        String errorMessage = "데이터베이스 연결 오류";

        when(userRepository.findById(userId))
                .thenThrow(new RuntimeException(errorMessage));

        // when
        Either<Failure, User> result = getUserService.get(userId);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalServerError.class);
        assertThat(result.getLeft().message()).isEqualTo(errorMessage);

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
        assertThat(result.getLeft().message()).isEqualTo(errorMessage);

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("null UserId로 조회 시 예외를 반환해야 한다")
    void get_withNullUserId_shouldReturnInternalServerError() {
        // given
        UserId userId = null;

        when(userRepository.findById(userId))
                .thenThrow(new IllegalArgumentException("User ID cannot be null"));

        // when
        Either<Failure, User> result = getUserService.get(userId);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalServerError.class);
        assertThat(result.getLeft().message()).isEqualTo("User ID cannot be null");

        verify(userRepository).findById(userId);
    }
} 
package com.boardly.features.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.boardly.features.user.domain.model.User;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.features.user.domain.model.UserProfile;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.domain.common.Failure;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
class GetUserServiceTest {

    private GetUserService getUserService;

    @Mock
    private UserFinder userFinder;

    @Mock
    private ValidationMessageResolver validationMessageResolver;

    @BeforeEach
    void setUp() {
        getUserService = new GetUserService(userFinder, validationMessageResolver);
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

        when(userFinder.findUserOrThrow(userId))
                .thenReturn(expectedUser);

        // when
        Either<Failure, User> result = getUserService.get(userId);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isEqualTo(expectedUser);
        assertThat(result.get().getUserId()).isEqualTo(userId);
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");

        verify(userFinder).findUserOrThrow(userId);
        verify(validationMessageResolver, never()).getMessage(anyString());
    }

    @Test
    @DisplayName("사용자를 찾을 수 없을 때 NotFound를 반환해야 한다")
    void get_withUserNotFound_shouldReturnNotFound() {
        // given
        UserId userId = createValidUserId();
        String errorMessage = "사용자를 찾을 수 없습니다";

        when(userFinder.findUserOrThrow(userId))
                .thenThrow(new UsernameNotFoundException(userId.getId()));
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

        verify(userFinder).findUserOrThrow(userId);
        verify(validationMessageResolver).getMessage("validation.user.email.not.found");
    }

    @Test
    @DisplayName("동일한 사용자 ID로 여러 번 조회 시 매번 새로운 조회가 이루어져야 한다")
    void get_withSameUserIdMultipleTimes_shouldCallFinderEachTime() {
        // given
        UserId userId = createValidUserId();
        User expectedUser = createValidUser();

        when(userFinder.findUserOrThrow(userId))
                .thenReturn(expectedUser);

        // when - 첫 번째 조회
        Either<Failure, User> result1 = getUserService.get(userId);

        // when - 두 번째 조회
        Either<Failure, User> result2 = getUserService.get(userId);

        // then
        assertThat(result1.isRight()).isTrue();
        assertThat(result2.isRight()).isTrue();
        assertThat(result1.get()).isEqualTo(result2.get());

        // 캐싱이 없으므로 userFinder는 매번 호출되어야 함
        verify(userFinder, times(2)).findUserOrThrow(userId);
    }
}
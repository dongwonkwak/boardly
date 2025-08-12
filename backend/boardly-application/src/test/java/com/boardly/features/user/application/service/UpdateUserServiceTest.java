package com.boardly.features.user.application.service;

import com.boardly.features.user.application.command.UpdateUserCommand;
import com.boardly.features.user.application.validation.UserValidator;
import com.boardly.features.user.domain.User;
import com.boardly.features.user.domain.UserProfile;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.validation.MessageResolver;
import com.boardly.shared.validation.ValidationResult;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateUserServiceTest {

    private UpdateUserService updateUserService;

    @Mock
    private com.boardly.features.user.domain.port.UserRepository userRepository;

    @Mock
    private UserValidator userValidator;

    @Mock
    private MessageResolver messageResolver;

    @BeforeEach
    void setUp() {
        updateUserService = new UpdateUserService(
                userRepository,
                userValidator,
                messageResolver);
    }

    private UpdateUserCommand createValidCommand() {
        UserId userId = new UserId();
        return new UpdateUserCommand(
                userId,
                "업데이트된길동",
                "업데이트된홍");
    }

    private User createExistingUser(UserId userId) {
        UserProfile userProfile = new UserProfile("길동", "홍");
        return User.builder()
                .userId(userId)
                .email("test@example.com")
                .hashedPassword("hashedPassword123!")
                .userProfile(userProfile)
                .isActive(true)
                .createdAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .updatedAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();
    }

    private User createUpdatedUser(UserId userId, UserProfile newProfile) {
        return User.builder()
                .userId(userId)
                .email("test@example.com")
                .hashedPassword("hashedPassword123!")
                .userProfile(newProfile)
                .isActive(true)
                .createdAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("유효한 정보로 사용자 업데이트가 성공해야 한다")
    void update_withValidData_shouldReturnUpdatedUser() {
        // given
        UpdateUserCommand command = createValidCommand();
        User existingUser = createExistingUser(command.userId());
        UserProfile newUserProfile = new UserProfile(command.firstName(), command.lastName());
        User updatedUser = createUpdatedUser(command.userId(), newUserProfile);

        when(userValidator.validateUserUpdate(command))
                .thenReturn(ValidationResult.valid(command));
        when(userRepository.findById(command.userId()))
                .thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class)))
                .thenReturn(Either.right(updatedUser));

        // when
        Either<Failure, User> result = updateUserService.update(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().getUserProfile().firstName()).isEqualTo(command.firstName());
        assertThat(result.get().getUserProfile().lastName()).isEqualTo(command.lastName());

        verify(userValidator).validateUserUpdate(command);
        verify(userRepository).findById(command.userId());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("입력 검증 실패 시 InputError를 반환해야 한다")
    void update_withInvalidInput_shouldReturnInputError() {
        // given
        UpdateUserCommand command = createValidCommand();
        String errorMessage = "입력 데이터가 올바르지 않습니다";

        when(messageResolver.getMessage("validation.input.invalid"))
                .thenReturn(errorMessage);
        when(userValidator.validateUserUpdate(command))
                .thenReturn(ValidationResult.invalid(io.vavr.collection.List.of(
                        Failure.FieldViolation.builder()
                                .field("firstName")
                                .message("이름은 필수입니다")
                                .rejectedValue(command.firstName())
                                .build())));

        // when
        Either<Failure, User> result = updateUserService.update(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);

        Failure.InputError inputError = (Failure.InputError) result.getLeft();
        assertThat(inputError.getMessage()).isEqualTo(errorMessage);
        assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
        assertThat(inputError.getViolations()).hasSize(1);
        if (inputError.getViolations() != null && !inputError.getViolations().isEmpty()) {
            assertThat(inputError.getViolations().get(0).getField()).isEqualTo("firstName");
        }

        verify(userValidator).validateUserUpdate(command);
        verify(messageResolver).getMessage("validation.input.invalid");
    }

    @Test
    @DisplayName("사용자를 찾을 수 없을 때 NotFound를 반환해야 한다")
    void update_withUserNotFound_shouldReturnNotFound() {
        // given
        UpdateUserCommand command = createValidCommand();
        String errorMessage = "사용자를 찾을 수 없습니다";

        when(messageResolver.getMessage("validation.user.email.not.found"))
                .thenReturn(errorMessage);
        when(userValidator.validateUserUpdate(command))
                .thenReturn(ValidationResult.valid(command));
        when(userRepository.findById(command.userId()))
                .thenReturn(Optional.empty());

        // when
        Either<Failure, User> result = updateUserService.update(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);

        Failure.NotFound notFound = (Failure.NotFound) result.getLeft();
        assertThat(notFound.getMessage()).isEqualTo(errorMessage);
        assertThat(notFound.getErrorCode()).isEqualTo("USER_NOT_FOUND");
        assertThat(notFound.getContext()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> context = (Map<String, Object>) notFound.getContext();
        assertThat(context.get("userId")).isEqualTo(command.userId().getId());

        verify(userValidator).validateUserUpdate(command);
        verify(userRepository).findById(command.userId());
        verify(messageResolver).getMessage("validation.user.email.not.found");
    }
}

package com.boardly.features.user.application.service;

import com.boardly.features.user.application.port.input.UpdateUserCommand;
import com.boardly.features.user.application.validation.UserValidator;
import com.boardly.features.user.domain.model.User;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.features.user.domain.model.UserProfile;
import com.boardly.features.user.domain.repository.UserRepository;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateUserServiceTest {

    private UpdateUserService updateUserService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserValidator userValidator;

    @Mock
    private ValidationMessageResolver validationMessageResolver;

    @BeforeEach
    void setUp() {
        updateUserService = new UpdateUserService(
                userRepository,
                userValidator,
                validationMessageResolver
        );
    }

    private UpdateUserCommand createValidCommand() {
        UserId userId = new UserId();
        return new UpdateUserCommand(
                userId,
                "업데이트된길동",
                "업데이트된홍"
        );
    }

    private User createExistingUser(UserId userId) {
        UserProfile userProfile = new UserProfile("길동", "홍");
        return User.builder()
                .userId(userId)
                .email("test@example.com")
                .hashedPassword("hashedPassword123!")
                .userProfile(userProfile)
                .isActive(true)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    private User createUpdatedUser(UserId userId, UserProfile newProfile) {
        return User.builder()
                .userId(userId)
                .email("test@example.com")
                .hashedPassword("hashedPassword123!")
                .userProfile(newProfile)
                .isActive(true)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
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
    @DisplayName("입력 검증 실패 시 검증 오류를 반환해야 한다")
    void update_withInvalidData_shouldReturnValidationFailure() {
        // given
        UpdateUserCommand command = createValidCommand();
        Failure.FieldViolation violation = Failure.FieldViolation.builder()
                .field("firstName")
                .message("validation.user.firstName.required")
                .rejectedValue(command.firstName())
                .build();
        ValidationResult<UpdateUserCommand> invalidResult = ValidationResult.invalid(violation);

        when(userValidator.validateUserUpdate(command))
                .thenReturn(invalidResult);

        // when
        Either<Failure, User> result = updateUserService.update(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ValidationFailure.class);
        Failure.ValidationFailure validationFailure = (Failure.ValidationFailure) result.getLeft();
        assertThat(validationFailure.message()).contains("INVALID_INPUT");

        verify(userValidator).validateUserUpdate(command);
        verify(userRepository, never()).findById(any(UserId.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 업데이트 시 NotFound 오류를 반환해야 한다")
    void update_withNonExistentUser_shouldReturnNotFoundFailure() {
        // given
        UpdateUserCommand command = createValidCommand();
        String notFoundMessage = "사용자를 찾을 수 없습니다.";

        when(userValidator.validateUserUpdate(command))
                .thenReturn(ValidationResult.valid(command));
        when(userRepository.findById(command.userId()))
                .thenReturn(Optional.empty());
        when(validationMessageResolver.getMessage("validation.user.email.not.found"))
                .thenReturn(notFoundMessage);

        // when
        Either<Failure, User> result = updateUserService.update(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.NotFoundFailure.class);
        assertThat(result.getLeft().message()).isEqualTo(notFoundMessage);

        verify(userValidator).validateUserUpdate(command);
        verify(userRepository).findById(command.userId());
        verify(validationMessageResolver).getMessage("validation.user.email.not.found");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 저장 실패 시 저장소 오류를 반환해야 한다")
    void update_withSaveFailure_shouldReturnRepositoryFailure() {
        // given
        UpdateUserCommand command = createValidCommand();
        User existingUser = createExistingUser(command.userId());
        Failure saveFailure = Failure.ofInternalServerError("데이터베이스 연결 오류");

        when(userValidator.validateUserUpdate(command))
                .thenReturn(ValidationResult.valid(command));
        when(userRepository.findById(command.userId()))
                .thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class)))
                .thenReturn(Either.left(saveFailure));

        // when
        Either<Failure, User> result = updateUserService.update(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalServerError.class);
        assertThat(result.getLeft().message()).isEqualTo("데이터베이스 연결 오류");

        verify(userValidator).validateUserUpdate(command);
        verify(userRepository).findById(command.userId());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 프로필이 올바르게 업데이트되어야 한다")
    void update_shouldUpdateUserProfile() {
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
        
        // 저장되는 User 객체의 프로필이 업데이트되었는지 확인
        verify(userRepository).save(argThat(user -> 
                user.getUserProfile().firstName().equals(command.firstName()) &&
                user.getUserProfile().lastName().equals(command.lastName())
        ));
    }

    @Test
    @DisplayName("기존 사용자의 다른 정보는 변경되지 않아야 한다")
    void update_shouldPreserveOtherUserFields() {
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
        
        // 저장되는 User 객체의 다른 필드들이 보존되었는지 확인
        verify(userRepository).save(argThat(user -> 
                user.getUserId().equals(existingUser.getUserId()) &&
                user.getEmail().equals(existingUser.getEmail()) &&
                user.getHashedPassword().equals(existingUser.getHashedPassword()) &&
                user.isActive() == existingUser.isActive()
        ));
    }

    @Test
    @DisplayName("동일한 이름으로 업데이트해도 성공해야 한다")
    void update_withSameName_shouldSucceed() {
        // given
        UserId userId = new UserId();
        UpdateUserCommand command = new UpdateUserCommand(userId, "길동", "홍");
        User existingUser = createExistingUser(userId);
        UserProfile sameProfile = new UserProfile("길동", "홍");
        User updatedUser = createUpdatedUser(userId, sameProfile);

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
        assertThat(result.get().getUserProfile().firstName()).isEqualTo("길동");
        assertThat(result.get().getUserProfile().lastName()).isEqualTo("홍");

        verify(userValidator).validateUserUpdate(command);
        verify(userRepository).findById(command.userId());
        verify(userRepository).save(any(User.class));
    }
} 
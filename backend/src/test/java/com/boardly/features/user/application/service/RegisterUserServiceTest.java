package com.boardly.features.user.application.service;

import com.boardly.features.user.application.port.input.RegisterUserCommand;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    private RegisterUserService registerUserService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserValidator userValidator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ValidationMessageResolver validationMessageResolver;

    @BeforeEach
    void setUp() {
        registerUserService = new RegisterUserService(
                userRepository,
                userValidator,
                passwordEncoder,
                validationMessageResolver
        );
    }

    private RegisterUserCommand createValidCommand() {
        return new RegisterUserCommand(
                "test@example.com",
                "Password123!",
                "길동",
                "홍"
        );
    }

    private User createValidUser(String email, String hashedPassword) {
        UserProfile userProfile = new UserProfile("길동", "홍");
        return User.builder()
                .userId(new UserId())
                .email(email)
                .hashedPassword(hashedPassword)
                .userProfile(userProfile)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("유효한 정보로 사용자 등록이 성공해야 한다")
    void register_withValidData_shouldReturnUser() {
        // given
        RegisterUserCommand command = createValidCommand();
        String hashedPassword = "hashedPassword123!";
        User savedUser = createValidUser(command.email(), hashedPassword);

        when(userValidator.validateUserRegistration(command))
                .thenReturn(ValidationResult.valid(command));
        when(userRepository.existsByEmail(command.email()))
                .thenReturn(false);
        when(passwordEncoder.encode(command.password()))
                .thenReturn(hashedPassword);
        when(userRepository.save(any(User.class)))
                .thenReturn(Either.right(savedUser));

        // when
        Either<Failure, User> result = registerUserService.register(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get().getEmail()).isEqualTo(command.email());
        assertThat(result.get().getHashedPassword()).isEqualTo(hashedPassword);
        assertThat(result.get().getUserProfile().firstName()).isEqualTo(command.firstName());
        assertThat(result.get().getUserProfile().lastName()).isEqualTo(command.lastName());

        verify(userValidator).validateUserRegistration(command);
        verify(userRepository).existsByEmail(command.email());
        verify(passwordEncoder).encode(command.password());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("입력 검증 실패 시 검증 오류를 반환해야 한다")
    void register_withInvalidData_shouldReturnValidationFailure() {
        // given
        RegisterUserCommand command = createValidCommand();
        Failure.FieldViolation violation = Failure.FieldViolation.builder()
                .field("email")
                .message("validation.user.email.invalid")
                .rejectedValue(command.email())
                .build();
        ValidationResult<RegisterUserCommand> invalidResult = ValidationResult.invalid(violation);

        when(userValidator.validateUserRegistration(command))
                .thenReturn(invalidResult);

        // when
        Either<Failure, User> result = registerUserService.register(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ValidationFailure.class);
        Failure.ValidationFailure validationFailure = (Failure.ValidationFailure) result.getLeft();
        assertThat(validationFailure.message()).contains("INVALID_INPUT");

        verify(userValidator).validateUserRegistration(command);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("이메일이 이미 존재하는 경우 충돌 오류를 반환해야 한다")
    void register_withExistingEmail_shouldReturnConflictFailure() {
        // given
        RegisterUserCommand command = createValidCommand();
        String duplicateMessage = "이미 존재하는 이메일입니다.";

        when(userValidator.validateUserRegistration(command))
                .thenReturn(ValidationResult.valid(command));
        when(userRepository.existsByEmail(command.email()))
                .thenReturn(true);
        when(validationMessageResolver.getMessage("validation.user.email.duplicate"))
                .thenReturn(duplicateMessage);

        // when
        Either<Failure, User> result = registerUserService.register(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ConflictFailure.class);
        assertThat(result.getLeft().message()).isEqualTo(duplicateMessage);

        verify(userValidator).validateUserRegistration(command);
        verify(userRepository).existsByEmail(command.email());
        verify(validationMessageResolver).getMessage("validation.user.email.duplicate");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 저장 실패 시 저장소 오류를 반환해야 한다")
    void register_withSaveFailure_shouldReturnRepositoryFailure() {
        // given
        RegisterUserCommand command = createValidCommand();
        String hashedPassword = "hashedPassword123!";
        Failure saveFailure = Failure.ofInternalServerError("데이터베이스 연결 오류");

        when(userValidator.validateUserRegistration(command))
                .thenReturn(ValidationResult.valid(command));
        when(userRepository.existsByEmail(command.email()))
                .thenReturn(false);
        when(passwordEncoder.encode(command.password()))
                .thenReturn(hashedPassword);
        when(userRepository.save(any(User.class)))
                .thenReturn(Either.left(saveFailure));

        // when
        Either<Failure, User> result = registerUserService.register(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalServerError.class);
        assertThat(result.getLeft().message()).isEqualTo("데이터베이스 연결 오류");

        verify(userValidator).validateUserRegistration(command);
        verify(userRepository).existsByEmail(command.email());
        verify(passwordEncoder).encode(command.password());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("비밀번호가 올바르게 해싱되어야 한다")
    void register_shouldHashPassword() {
        // given
        RegisterUserCommand command = createValidCommand();
        String hashedPassword = "hashedPassword123!";
        User savedUser = createValidUser(command.email(), hashedPassword);

        when(userValidator.validateUserRegistration(command))
                .thenReturn(ValidationResult.valid(command));
        when(userRepository.existsByEmail(command.email()))
                .thenReturn(false);
        when(passwordEncoder.encode(command.password()))
                .thenReturn(hashedPassword);
        when(userRepository.save(any(User.class)))
                .thenReturn(Either.right(savedUser));

        // when
        Either<Failure, User> result = registerUserService.register(command);

        // then
        assertThat(result.isRight()).isTrue();
        verify(passwordEncoder).encode(command.password());
        
        // 저장되는 User 객체의 비밀번호가 해싱된 값인지 확인
        verify(userRepository).save(argThat(user -> 
                user.getHashedPassword().equals(hashedPassword)
        ));
    }

    @Test
    @DisplayName("생성된 사용자는 활성 상태여야 한다")
    void register_shouldCreateActiveUser() {
        // given
        RegisterUserCommand command = createValidCommand();
        String hashedPassword = "hashedPassword123!";
        User savedUser = createValidUser(command.email(), hashedPassword);

        when(userValidator.validateUserRegistration(command))
                .thenReturn(ValidationResult.valid(command));
        when(userRepository.existsByEmail(command.email()))
                .thenReturn(false);
        when(passwordEncoder.encode(command.password()))
                .thenReturn(hashedPassword);
        when(userRepository.save(any(User.class)))
                .thenReturn(Either.right(savedUser));

        // when
        Either<Failure, User> result = registerUserService.register(command);

        // then
        assertThat(result.isRight()).isTrue();
        
        // 저장되는 User 객체가 활성 상태인지 확인
        verify(userRepository).save(argThat(user -> user.isActive()));
    }
} 
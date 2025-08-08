package com.boardly.features.user.application.service;

import com.boardly.features.user.application.port.input.RegisterUserCommand;
import com.boardly.features.user.application.validation.UserValidator;
import com.boardly.features.user.domain.model.User;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

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
                validationMessageResolver);

        // 메시지 모킹 설정 (lenient로 설정하여 사용되지 않는 스텁도 허용)
        lenient().when(validationMessageResolver.getMessage("validation.input.invalid"))
                .thenReturn("입력 데이터가 올바르지 않습니다");
        lenient().when(validationMessageResolver.getMessage("validation.user.email.duplicate"))
                .thenReturn("이미 존재하는 이메일입니다.");
        lenient().when(validationMessageResolver.getMessage("validation.user.registration.error"))
                .thenReturn("사용자 등록 중 오류가 발생했습니다.");
    }

    private RegisterUserCommand createValidCommand() {
        return new RegisterUserCommand(
                "test@example.com",
                "Password123!",
                "길동",
                "홍");
    }

    private User createValidUser() {
        UserProfile userProfile = new UserProfile("길동", "홍");
        return User.create("test@example.com", "hashedPassword", userProfile);
    }

    @Test
    @DisplayName("유효한 정보로 사용자 등록이 성공해야 한다")
    void register_withValidData_shouldReturnUser() {
        // given
        RegisterUserCommand command = createValidCommand();
        String hashedPassword = "hashedPassword123!";
        User savedUser = createValidUser();

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
        assertThat(result.get().getUserProfile().firstName()).isEqualTo(command.firstName());
        assertThat(result.get().getUserProfile().lastName()).isEqualTo(command.lastName());
        assertThat(result.get().isActive()).isTrue();

        verify(userValidator).validateUserRegistration(command);
        verify(userRepository).existsByEmail(command.email());
        verify(passwordEncoder).encode(command.password());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("입력 검증 실패 시 InputError를 반환해야 한다")
    void register_withInvalidInput_shouldReturnInputError() {
        // given
        RegisterUserCommand command = createValidCommand();
        String errorMessage = "입력 데이터가 올바르지 않습니다";

        when(validationMessageResolver.getMessage("validation.input.invalid"))
                .thenReturn(errorMessage);
        when(userValidator.validateUserRegistration(command))
                .thenReturn(ValidationResult.invalid(io.vavr.collection.List.of(
                        Failure.FieldViolation.builder()
                                .field("email")
                                .message("이메일 형식이 올바르지 않습니다")
                                .rejectedValue(command.email())
                                .build())));

        // when
        Either<Failure, User> result = registerUserService.register(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);

        Failure.InputError inputError = (Failure.InputError) result.getLeft();
        assertThat(inputError.getMessage()).isEqualTo(errorMessage);
        assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
        assertThat(inputError.getViolations()).hasSize(1);
        if (inputError.getViolations() != null && !inputError.getViolations().isEmpty()) {
            assertThat(inputError.getViolations().get(0).getField()).isEqualTo("email");
        }

        verify(userValidator).validateUserRegistration(command);
        verify(validationMessageResolver).getMessage("validation.input.invalid");
    }

    @Test
    @DisplayName("이메일이 이미 존재하는 경우 ResourceConflict를 반환해야 한다")
    void register_withExistingEmail_shouldReturnResourceConflict() {
        // given
        RegisterUserCommand command = createValidCommand();
        String duplicateMessage = "이미 존재하는 이메일입니다.";

        when(userValidator.validateUserRegistration(command))
                .thenReturn(ValidationResult.valid(command));
        when(userRepository.existsByEmail(command.email()))
                .thenReturn(true);

        // when
        Either<Failure, User> result = registerUserService.register(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ResourceConflict.class);

        Failure.ResourceConflict conflict = (Failure.ResourceConflict) result.getLeft();
        assertThat(conflict.getMessage()).isEqualTo(duplicateMessage);
        assertThat(conflict.getErrorCode()).isEqualTo("EMAIL_ALREADY_EXISTS");
        assertThat(conflict.getContext()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> context = (Map<String, Object>) conflict.getContext();
        assertThat(context.get("email")).isEqualTo(command.email());
        assertThat(context.get("conflictType")).isEqualTo("EMAIL_DUPLICATE");

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
        String errorMessage = "데이터베이스 연결 오류";
        Failure saveFailure = Failure.ofInternalError(errorMessage, "USER_REGISTRATION_ERROR", null);

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
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);
        assertThat(result.getLeft().getMessage()).isEqualTo(errorMessage);

        verify(userValidator).validateUserRegistration(command);
        verify(userRepository).existsByEmail(command.email());
        verify(passwordEncoder).encode(command.password());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("DataIntegrityViolationException 발생 시 이메일 중복 오류를 반환해야 한다")
    void register_withDataIntegrityViolationException_shouldReturnResourceConflict() {
        // given
        RegisterUserCommand command = createValidCommand();
        String hashedPassword = "hashedPassword123!";
        String duplicateMessage = "이미 존재하는 이메일입니다.";

        when(userValidator.validateUserRegistration(command))
                .thenReturn(ValidationResult.valid(command));
        when(userRepository.existsByEmail(command.email()))
                .thenReturn(false);
        when(passwordEncoder.encode(command.password()))
                .thenReturn(hashedPassword);
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        // when
        Either<Failure, User> result = registerUserService.register(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ResourceConflict.class);

        Failure.ResourceConflict conflict = (Failure.ResourceConflict) result.getLeft();
        assertThat(conflict.getMessage()).isEqualTo(duplicateMessage);
        assertThat(conflict.getErrorCode()).isEqualTo("EMAIL_ALREADY_EXISTS");
        assertThat(conflict.getContext()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> context = (Map<String, Object>) conflict.getContext();
        assertThat(context.get("email")).isEqualTo(command.email());
        assertThat(context.get("conflictType")).isEqualTo("EMAIL_DUPLICATE");

        verify(userValidator).validateUserRegistration(command);
        verify(userRepository).existsByEmail(command.email());
        verify(passwordEncoder).encode(command.password());
        verify(userRepository).save(any(User.class));
        verify(validationMessageResolver).getMessage("validation.user.email.duplicate");
    }

    @Test
    @DisplayName("기타 런타임 예외 발생 시 내부 서버 오류를 반환해야 한다")
    void register_withRuntimeException_shouldReturnInternalServerError() {
        // given
        RegisterUserCommand command = createValidCommand();
        String hashedPassword = "hashedPassword123!";
        String errorMessage = "예상치 못한 오류가 발생했습니다";

        when(userValidator.validateUserRegistration(command))
                .thenReturn(ValidationResult.valid(command));
        when(userRepository.existsByEmail(command.email()))
                .thenReturn(false);
        when(passwordEncoder.encode(command.password()))
                .thenReturn(hashedPassword);
        when(userRepository.save(any(User.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // when
        Either<Failure, User> result = registerUserService.register(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InternalError.class);

        Failure.InternalError internalError = (Failure.InternalError) result.getLeft();
        assertThat(internalError.getMessage()).isEqualTo(errorMessage);
        assertThat(internalError.getErrorCode()).isEqualTo("USER_REGISTRATION_ERROR");
        assertThat(internalError.getContext()).isNull();

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
        User savedUser = createValidUser();

        when(userValidator.validateUserRegistration(command))
                .thenReturn(ValidationResult.valid(command));
        when(userRepository.existsByEmail(command.email()))
                .thenReturn(false);
        when(passwordEncoder.encode(command.password()))
                .thenReturn(hashedPassword);
        when(userRepository.save(any(User.class)))
                .thenReturn(Either.right(savedUser));

        // when
        registerUserService.register(command);

        // then
        verify(passwordEncoder).encode(command.password());
    }

    @Test
    @DisplayName("생성된 사용자는 활성 상태여야 한다")
    void register_shouldCreateActiveUser() {
        // given
        RegisterUserCommand command = createValidCommand();
        String hashedPassword = "hashedPassword123!";
        User savedUser = createValidUser();

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
        assertThat(result.get().isActive()).isTrue();
    }

    @Test
    @DisplayName("User.create() 메서드로 도메인 객체가 생성되어야 한다")
    void register_shouldCreateUserWithCreateMethod() {
        // given
        RegisterUserCommand command = createValidCommand();
        String hashedPassword = "hashedPassword123!";
        User savedUser = createValidUser();

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
        User createdUser = result.get();
        assertThat(createdUser.getEmail()).isEqualTo(command.email());
        assertThat(createdUser.getUserProfile().firstName()).isEqualTo(command.firstName());
        assertThat(createdUser.getUserProfile().lastName()).isEqualTo(command.lastName());
    }

    @Test
    @DisplayName("여러 검증 오류가 있는 경우 모든 오류를 반환해야 한다")
    void register_withMultipleValidationErrors_shouldReturnAllErrors() {
        // given
        RegisterUserCommand command = createValidCommand();
        String errorMessage = "입력 데이터가 올바르지 않습니다";

        when(validationMessageResolver.getMessage("validation.input.invalid"))
                .thenReturn(errorMessage);
        when(userValidator.validateUserRegistration(command))
                .thenReturn(ValidationResult.invalid(io.vavr.collection.List.of(
                        Failure.FieldViolation.builder()
                                .field("email")
                                .message("이메일 형식이 올바르지 않습니다")
                                .rejectedValue(command.email())
                                .build(),
                        Failure.FieldViolation.builder()
                                .field("password")
                                .message("비밀번호는 8자 이상이어야 합니다")
                                .rejectedValue(command.password())
                                .build())));

        // when
        Either<Failure, User> result = registerUserService.register(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);

        Failure.InputError inputError = (Failure.InputError) result.getLeft();
        assertThat(inputError.getMessage()).isEqualTo(errorMessage);
        assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
        assertThat(inputError.getViolations()).hasSize(2);
        if (inputError.getViolations() != null && inputError.getViolations().size() >= 2) {
            assertThat(inputError.getViolations().get(0).getField()).isEqualTo("email");
            assertThat(inputError.getViolations().get(1).getField()).isEqualTo("password");
        }

        verify(userValidator).validateUserRegistration(command);
        verify(validationMessageResolver).getMessage("validation.input.invalid");
    }
}
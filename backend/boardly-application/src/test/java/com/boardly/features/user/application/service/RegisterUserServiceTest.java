package com.boardly.features.user.application.service;

import com.boardly.features.user.application.command.RegisterUserCommand;
import com.boardly.features.user.application.validation.UserValidator;
import com.boardly.features.user.domain.User;
import com.boardly.features.user.domain.UserProfile;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.validation.MessageResolver;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    private RegisterUserService registerUserService;

    @Mock
    private com.boardly.features.user.domain.port.UserRepository userRepository;

    @Mock
    private UserValidator userValidator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MessageResolver messageResolver;

    @BeforeEach
    void setUp() {
        registerUserService = new RegisterUserService(
                userRepository,
                userValidator,
                passwordEncoder,
                messageResolver);

        // 메시지 모킹 설정 (lenient로 설정하여 사용되지 않는 스텁도 허용)
        lenient().when(messageResolver.getMessage("validation.input.invalid"))
                .thenReturn("입력 데이터가 올바르지 않습니다");
        lenient().when(messageResolver.getMessage("validation.user.email.duplicate"))
                .thenReturn("이미 존재하는 이메일입니다.");
        lenient().when(messageResolver.getMessage("validation.user.registration.error"))
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
                .thenReturn(com.boardly.shared.validation.ValidationResult.valid(command));
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

        when(messageResolver.getMessage("validation.input.invalid"))
                .thenReturn(errorMessage);
        when(userValidator.validateUserRegistration(command))
                .thenReturn(com.boardly.shared.validation.ValidationResult.invalid(io.vavr.collection.List.of(
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
        verify(messageResolver).getMessage("validation.input.invalid");
    }

    @Test
    @DisplayName("이메일이 이미 존재하는 경우 ResourceConflict를 반환해야 한다")
    void register_withExistingEmail_shouldReturnResourceConflict() {
        // given
        RegisterUserCommand command = createValidCommand();

        when(userValidator.validateUserRegistration(command))
                .thenReturn(com.boardly.shared.validation.ValidationResult.valid(command));
        when(userRepository.existsByEmail(command.email()))
                .thenReturn(true);

        // when
        Either<Failure, User> result = registerUserService.register(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ResourceConflict.class);

        Failure.ResourceConflict conflict = (Failure.ResourceConflict) result.getLeft();
        assertThat(conflict.getErrorCode()).isEqualTo("EMAIL_ALREADY_EXISTS");
        assertThat(conflict.getContext()).isInstanceOf(Map.class);

        verify(userValidator).validateUserRegistration(command);
        verify(userRepository).existsByEmail(command.email());
        verify(messageResolver).getMessage("validation.user.email.duplicate");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("DataIntegrityViolationException 발생 시 이메일 중복 오류를 반환해야 한다")
    void register_withDataIntegrityViolationException_shouldReturnResourceConflict() {
        // given
        RegisterUserCommand command = createValidCommand();
        String hashedPassword = "hashedPassword123!";

        when(userValidator.validateUserRegistration(command))
                .thenReturn(com.boardly.shared.validation.ValidationResult.valid(command));
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

        verify(userValidator).validateUserRegistration(command);
        verify(userRepository).existsByEmail(command.email());
        verify(passwordEncoder).encode(command.password());
        verify(userRepository).save(any(User.class));
        verify(messageResolver).getMessage("validation.user.email.duplicate");
    }

    @Test
    @DisplayName("기타 런타임 예외 발생 시 내부 서버 오류를 반환해야 한다")
    void register_withRuntimeException_shouldReturnInternalServerError() {
        // given
        RegisterUserCommand command = createValidCommand();
        String hashedPassword = "hashedPassword123!";
        String errorMessage = "예상치 못한 오류가 발생했습니다";

        when(userValidator.validateUserRegistration(command))
                .thenReturn(com.boardly.shared.validation.ValidationResult.valid(command));
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
                .thenReturn(com.boardly.shared.validation.ValidationResult.valid(command));
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
}

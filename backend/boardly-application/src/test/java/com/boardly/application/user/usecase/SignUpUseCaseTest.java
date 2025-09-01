package com.boardly.application.user.usecase;

import com.boardly.application.user.command.SignUpCommand;
import com.boardly.domain.common.Failure;
import com.boardly.domain.user.PasswordEncoder;
import com.boardly.domain.user.User;
import com.boardly.domain.user.UserRepository;
import com.boardly.domain.user.UserStatus;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("회원가입 UseCase 테스트")
class SignUpUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private SignUpUseCase signUpUseCase;

    @BeforeEach
    void setUp() {
        signUpUseCase = new SignUpUseCase(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("정상적인 회원가입을 성공할 수 있다")
    void signUpSuccess() {
        // given
        SignUpCommand command = SignUpCommand.builder()
                .email("test@example.com")
                .username("testuser")
                .password("password123")
                .displayName("테스트 사용자")
                .build();

        User expectedUser = User.create(
                command.getEmail(),
                command.getUsername(),
                "encodedPassword",
                command.getDisplayName(),
                null);

        when(userRepository.existsByEmail(command.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(command.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(command.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        // when
        Either<Failure, User> result = signUpUseCase.execute(command);

        // then
        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isEqualTo(expectedUser);
        assertThat(result.get().getEmail()).isEqualTo(command.getEmail());
        assertThat(result.get().getUsername()).isEqualTo(command.getUsername());
        assertThat(result.get().getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("이메일이 중복되면 실패한다")
    void signUpFailWhenEmailExists() {
        // given
        SignUpCommand command = SignUpCommand.builder()
                .email("existing@example.com")
                .username("testuser")
                .password("password123")
                .build();

        when(userRepository.existsByEmail(command.getEmail())).thenReturn(true);

        // when
        Either<Failure, User> result = signUpUseCase.execute(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ResourceConflict.class);
        assertThat(result.getLeft().getMessage()).isEqualTo("이미 사용 중인 이메일입니다.");
    }

    @Test
    @DisplayName("사용자명이 중복되면 실패한다")
    void signUpFailWhenUsernameExists() {
        // given
        SignUpCommand command = SignUpCommand.builder()
                .email("test@example.com")
                .username("existinguser")
                .password("password123")
                .build();

        when(userRepository.existsByEmail(command.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(command.getUsername())).thenReturn(true);

        // when
        Either<Failure, User> result = signUpUseCase.execute(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.ResourceConflict.class);
        assertThat(result.getLeft().getMessage()).isEqualTo("이미 사용 중인 사용자명입니다.");
    }

    @Test
    @DisplayName("이메일이 유효하지 않으면 실패한다")
    void signUpFailWhenInvalidEmail() {
        // given
        SignUpCommand command = SignUpCommand.builder()
                .email("invalid-email")
                .username("testuser")
                .password("password123")
                .build();

        // when
        Either<Failure, User> result = signUpUseCase.execute(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
        assertThat(result.getLeft().getMessage()).isEqualTo("입력 데이터가 유효하지 않습니다.");
    }

    @Test
    @DisplayName("사용자명이 유효하지 않으면 실패한다")
    void signUpFailWhenInvalidUsername() {
        // given
        SignUpCommand command = SignUpCommand.builder()
                .email("test@example.com")
                .username("te") // 3자 미만
                .password("password123")
                .build();

        // when
        Either<Failure, User> result = signUpUseCase.execute(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
        assertThat(result.getLeft().getMessage()).isEqualTo("입력 데이터가 유효하지 않습니다.");
    }

    @Test
    @DisplayName("비밀번호가 유효하지 않으면 실패한다")
    void signUpFailWhenInvalidPassword() {
        // given
        SignUpCommand command = SignUpCommand.builder()
                .email("test@example.com")
                .username("testuser")
                .password("123") // 8자 미만
                .build();

        // when
        Either<Failure, User> result = signUpUseCase.execute(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
        assertThat(result.getLeft().getMessage()).isEqualTo("입력 데이터가 유효하지 않습니다.");
    }

    @Test
    @DisplayName("표시명이 너무 길면 실패한다")
    void signUpFailWhenDisplayNameTooLong() {
        // given
        String longDisplayName = "a".repeat(51); // 51자
        SignUpCommand command = SignUpCommand.builder()
                .email("test@example.com")
                .username("testuser")
                .password("password123")
                .displayName(longDisplayName)
                .build();

        // when
        Either<Failure, User> result = signUpUseCase.execute(command);

        // then
        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
        assertThat(result.getLeft().getMessage()).isEqualTo("입력 데이터가 유효하지 않습니다.");
    }
}

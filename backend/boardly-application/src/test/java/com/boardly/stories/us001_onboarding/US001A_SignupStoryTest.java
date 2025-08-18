package com.boardly.stories.us001_onboarding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.boardly.features.user.application.command.RegisterUserCommand;
import com.boardly.features.user.application.service.RegisterUserService;
import com.boardly.features.user.application.validation.UserValidator;
import com.boardly.features.user.domain.User;
import com.boardly.features.user.domain.UserProfile;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.validation.MessageResolver;
import com.boardly.shared.validation.ValidationResult;

import io.vavr.control.Either;

@Tag("story")
@Tag("US-001A")
@DisplayName("US-001A 회원가입 스토리 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class US001A_SignupStoryTest {

    private RegisterUserService sut;

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
        sut = new RegisterUserService(userRepository, userValidator, passwordEncoder, messageResolver);
        lenient().when(messageResolver.getMessage("validation.input.invalid"))
                .thenReturn("입력 데이터가 올바르지 않습니다");
        lenient().when(messageResolver.getMessage("validation.user.email.duplicate"))
                .thenReturn("이미 존재하는 이메일입니다.");
    }

    private RegisterUserCommand validCommand() {
        return new RegisterUserCommand(
                "test@example.com",
                "Password123!",
                "길동",
                "홍");
    }

    private User validUser() {
        UserProfile profile = new UserProfile("길동", "홍");
        return User.create("test@example.com", "hashedPassword", profile);
    }

    @Nested
    class 수용기준_성공_시나리오 {
        @Test
        void 유효한_입력값으로_회원가입에_성공한다() {
            RegisterUserCommand command = validCommand();
            String hashed = "hashedPassword123!";
            User saved = validUser();

            when(userValidator.validateUserRegistration(command))
                    .thenReturn(ValidationResult.valid(command));
            when(userRepository.existsByEmail(command.email()))
                    .thenReturn(false);
            when(passwordEncoder.encode(command.password()))
                    .thenReturn(hashed);
            when(userRepository.save(any(User.class)))
                    .thenReturn(Either.right(saved));

            Either<Failure, User> result = sut.register(command);

            assertThat(result.isRight()).isTrue();
            User user = result.get();
            assertThat(user.getEmail()).isEqualTo(command.email());
            assertThat(user.getUserProfile().firstName()).isEqualTo(command.firstName());
            assertThat(user.getUserProfile().lastName()).isEqualTo(command.lastName());
            assertThat(user.isActive()).isTrue();

            verify(passwordEncoder).encode(command.password());
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    class 수용기준_유효성_검증 {
        @Test
        void 이메일_형식_오류_등_입력_검증_실패시_InputError를_반환한다() {
            RegisterUserCommand command = validCommand();

            when(userValidator.validateUserRegistration(command))
                    .thenReturn(ValidationResult.invalid(io.vavr.collection.List.of(
                            Failure.FieldViolation.builder()
                                    .field("email")
                                    .message("이메일 형식이 올바르지 않습니다")
                                    .rejectedValue(command.email())
                                    .build())));

            Either<Failure, User> result = sut.register(command);

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
            Failure.InputError inputError = (Failure.InputError) result.getLeft();
            assertThat(inputError.getErrorCode()).isEqualTo("INVALID_INPUT");
            assertThat(inputError.getViolations()).isNotEmpty();

            verify(messageResolver).getMessage("validation.input.invalid");
        }
    }

    @Nested
    class 수용기준_중복_방지 {
        @Test
        void 이미_존재하는_이메일이면_ResourceConflict를_반환한다() {
            RegisterUserCommand command = validCommand();

            when(userValidator.validateUserRegistration(command))
                    .thenReturn(ValidationResult.valid(command));
            when(userRepository.existsByEmail(command.email()))
                    .thenReturn(true);

            Either<Failure, User> result = sut.register(command);

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.ResourceConflict.class);
            Failure.ResourceConflict conflict = (Failure.ResourceConflict) result.getLeft();
            assertThat(conflict.getErrorCode()).isEqualTo("EMAIL_ALREADY_EXISTS");

            verify(messageResolver).getMessage("validation.user.email.duplicate");
            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        void 데이터베이스_제약_위반시에도_중복_이메일_에러를_반환한다() {
            RegisterUserCommand command = validCommand();

            when(userValidator.validateUserRegistration(command))
                    .thenReturn(ValidationResult.valid(command));
            when(userRepository.existsByEmail(command.email()))
                    .thenReturn(false);
            when(passwordEncoder.encode(command.password()))
                    .thenReturn("hashed");
            when(userRepository.save(any(User.class)))
                    .thenThrow(new DataIntegrityViolationException("Duplicate"));

            Either<Failure, User> result = sut.register(command);

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.ResourceConflict.class);
            verify(messageResolver).getMessage("validation.user.email.duplicate");
        }
    }

    @Nested
    class 수용기준_보안_및_저장 {
        @Test
        void 비밀번호는_해시로_저장되어야_한다() {
            RegisterUserCommand command = validCommand();

            when(userValidator.validateUserRegistration(command))
                    .thenReturn(ValidationResult.valid(command));
            when(userRepository.existsByEmail(command.email()))
                    .thenReturn(false);
            when(passwordEncoder.encode(command.password()))
                    .thenReturn("hashed");
            when(userRepository.save(any(User.class)))
                    .thenReturn(Either.right(validUser()));

            sut.register(command);

            verify(passwordEncoder).encode(command.password());
        }
    }
}

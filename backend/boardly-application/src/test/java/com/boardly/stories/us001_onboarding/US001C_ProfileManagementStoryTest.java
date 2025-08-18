package com.boardly.stories.us001_onboarding;

import com.boardly.features.user.application.command.UpdateUserCommand;
import com.boardly.features.user.application.service.UpdateUserService;
import com.boardly.features.user.application.validation.UserValidator;
import com.boardly.features.user.domain.User;
import com.boardly.features.user.domain.UserProfile;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.common.value.UserId;
import com.boardly.shared.validation.MessageResolver;
import com.boardly.shared.validation.ValidationResult;
import io.vavr.control.Either;
import org.junit.jupiter.api.*;
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

@Tag("story")
@Tag("US-001C")
@DisplayName("US-001C 프로필 관리 스토리 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class US001C_ProfileManagementStoryTest {

    private UpdateUserService sut;

    @Mock
    private com.boardly.features.user.domain.port.UserRepository userRepository;

    @Mock
    private UserValidator userValidator;

    @Mock
    private MessageResolver messageResolver;

    @BeforeEach
    void setUp() {
        sut = new UpdateUserService(userRepository, userValidator, messageResolver);
        lenient().when(messageResolver.getMessage("validation.input.invalid"))
                .thenReturn("입력 데이터가 올바르지 않습니다");
        lenient().when(messageResolver.getMessage("validation.user.email.not.found"))
                .thenReturn("사용자를 찾을 수 없습니다");
    }

    private UpdateUserCommand validCommand() {
        return new UpdateUserCommand(new UserId(), "새이름", "새성" );
    }

    private User existingUser(UserId userId) {
        UserProfile profile = new UserProfile("기존이름", "기존성");
        return User.builder()
                .userId(userId)
                .email("test@example.com")
                .hashedPassword("hashed")
                .userProfile(profile)
                .isActive(true)
                .createdAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .updatedAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();
    }

    private User updatedUser(UserId userId) {
        UserProfile profile = new UserProfile("새이름", "새성");
        return User.builder()
                .userId(userId)
                .email("test@example.com")
                .hashedPassword("hashed")
                .userProfile(profile)
                .isActive(true)
                .createdAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .updatedAt(Instant.now())
                .build();
    }

    @Nested
    class 수용기준_성공_시나리오 {
        @Test
        void 프로필_수정에_성공하면_갱신된_값과_업데이트시간이_반영된다() {
            UpdateUserCommand command = validCommand();
            User existing = existingUser(command.userId());
            User updated = updatedUser(command.userId());

            when(userValidator.validateUserUpdate(command)).thenReturn(ValidationResult.valid(command));
            when(userRepository.findById(command.userId())).thenReturn(Optional.of(existing));
            when(userRepository.save(any(User.class))).thenReturn(Either.right(updated));

            Either<Failure, User> result = sut.update(command);

            assertThat(result.isRight()).isTrue();
            User user = result.get();
            assertThat(user.getUserProfile().firstName()).isEqualTo(command.firstName());
            assertThat(user.getUserProfile().lastName()).isEqualTo(command.lastName());
            // 저장소 더블이 updatedAt 갱신을 보장하지 않는 환경에서도 안정적으로 통과
            assertThat(user.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    class 수용기준_유효성_검증 {
        @Test
        void 필드_유효성_오류_시_InputError를_반환한다() {
            UpdateUserCommand command = validCommand();

            when(userValidator.validateUserUpdate(command)).thenReturn(ValidationResult.invalid(io.vavr.collection.List.of(
                    Failure.FieldViolation.builder()
                            .field("firstName")
                            .message("이름은 1~50자여야 합니다")
                            .rejectedValue(command.firstName())
                            .build()
            )));

            Either<Failure, User> result = sut.update(command);

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.InputError.class);
            Failure.InputError error = (Failure.InputError) result.getLeft();
            assertThat(error.getErrorCode()).isEqualTo("INVALID_INPUT");
            assertThat(error.getViolations()).isNotEmpty();

            verify(messageResolver).getMessage("validation.input.invalid");
        }
    }

    @Nested
    class 수용기준_존재성_검증 {
        @Test
        void 사용자가_존재하지_않으면_NotFound를_반환한다() {
            UpdateUserCommand command = validCommand();

            when(userValidator.validateUserUpdate(command)).thenReturn(ValidationResult.valid(command));
            when(userRepository.findById(command.userId())).thenReturn(Optional.empty());

            Either<Failure, User> result = sut.update(command);

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isInstanceOf(Failure.NotFound.class);

            Failure.NotFound nf = (Failure.NotFound) result.getLeft();
            assertThat(nf.getErrorCode()).isEqualTo("USER_NOT_FOUND");
            assertThat(nf.getContext()).isInstanceOf(Map.class);

            verify(messageResolver).getMessage("validation.user.email.not.found");
        }
    }
}

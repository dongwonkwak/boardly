package com.boardly.features.user.application.service;

import com.boardly.features.user.application.port.input.RegisterUserCommand;
import com.boardly.features.user.application.usecase.RegisterUserUseCase;
import com.boardly.features.user.application.validation.UserValidator;
import com.boardly.features.user.domain.model.User;
import com.boardly.features.user.domain.model.UserProfile;
import com.boardly.features.user.domain.repository.UserRepository;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.ValidationResult;
import com.boardly.shared.domain.common.Failure;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final PasswordEncoder passwordEncoder;
    private final ValidationMessageResolver validationMessageResolver;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Either<Failure, User> register(RegisterUserCommand command) {
        log.info("사용자 등록 시작: email={}", command.email());

        // 1. 입력 검증
        ValidationResult<RegisterUserCommand> validationResult = userValidator.validateUserRegistration(command);
        if (validationResult.isInvalid()) {
            log.warn("사용자 등록 검증 실패: email={}, violations={}", command.email(), validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofValidation("INVALID_INPUT", validationResult.getErrorsAsCollection()));
        }

        // 2. 이메일 중복 확인 (선택적 체크 - 성능 최적화용)
        if (userRepository.existsByEmail(command.email())) {
            log.warn("이미 존재하는 이메일로 등록 시도: email={}", command.email());
            return Either.left(Failure.ofConflict(validationMessageResolver.getMessage("validation.user.email.duplicate")));
        }

        // 3. 비밀번호 해싱
        String hashedPassword = passwordEncoder.encode(command.password());

        // 4. 도메인 객체 생성
        UserProfile userProfile = new UserProfile(command.firstName(), command.lastName());
        User user = User.create(command.email(), hashedPassword, userProfile);

        // 5. 사용자 저장 (데이터베이스 UNIQUE 제약 조건에 의존)
        return Try.of(() -> userRepository.save(user))
            .fold(
                // 예외 발생 시 처리
                throwable -> {
                    if (throwable instanceof DataIntegrityViolationException) {
                        // 데이터베이스 레벨에서 이메일 중복 제약 조건 위반 시
                        log.warn("이메일 중복 제약 조건 위반: email={}", command.email());
                        return Either.left(Failure.ofConflict(validationMessageResolver.getMessage("validation.user.email.duplicate")));
                    } else {
                        // 기타 예외 처리
                        log.error("사용자 등록 중 예외 발생: email={}, error={}", command.email(), throwable.getMessage(), throwable);
                        return Either.left(Failure.ofInternalServerError("사용자 등록 중 오류가 발생했습니다."));
                    }
                },
                // 성공 시 처리
                saveResult -> {
                    if (saveResult.isRight()) {
                        log.info("사용자 등록 완료: userId={}, email={}", saveResult.get().getUserId(), command.email());
                    } else {
                        log.error("사용자 저장 실패: email={}, error={}", command.email(), saveResult.getLeft().message());
                    }
                    return saveResult;
                }
            );
    }
} 
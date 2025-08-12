package com.boardly.features.user.application.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardly.features.user.application.command.UpdateUserCommand;
import com.boardly.features.user.application.usecase.UpdateUserUseCase;
import com.boardly.features.user.application.validation.UserValidator;
import com.boardly.features.user.domain.User;
import com.boardly.features.user.domain.UserProfile;
import com.boardly.shared.common.error.Failure;
import com.boardly.shared.validation.MessageResolver;
import com.boardly.shared.validation.ValidationResult;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateUserService implements UpdateUserUseCase {

    private final com.boardly.features.user.domain.port.UserRepository userRepository;
    private final UserValidator userValidator;
    private final MessageResolver messageResolver;

    @Override
    @Transactional
    public Either<Failure, User> update(UpdateUserCommand command) {
        log.info("사용자 업데이트 시작: userId={}", command.userId().getId());

        // 1. 입력 검증
        ValidationResult<UpdateUserCommand> validationResult = userValidator.validateUserUpdate(command);
        if (validationResult.isInvalid()) {
            log.warn("사용자 업데이트 검증 실패: userId={}, violations={}",
                    command.userId().getId(), validationResult.getErrorsAsCollection());
            return Either.left(Failure.ofInputError(
                    messageResolver.getMessage("validation.input.invalid"),
                    "INVALID_INPUT",
                    List.copyOf(validationResult.getErrorsAsCollection())));
        }

        // 2. 기존 사용자 조회
        Optional<User> existingUserOpt = userRepository.findById(command.userId());
        if (existingUserOpt.isEmpty()) {
            log.warn("업데이트할 사용자를 찾을 수 없음: userId={}", command.userId().getId());
            Map<String, Object> context = Map.of("userId", command.userId().getId());
            return Either.left(Failure.ofNotFound(
                    messageResolver.getMessage("validation.user.email.not.found"),
                    "USER_NOT_FOUND",
                    context));
        }

        // 3. 사용자 정보 업데이트
        User existingUser = existingUserOpt.get();
        UserProfile newUserProfile = new UserProfile(command.firstName(), command.lastName());
        existingUser.updateProfile(newUserProfile);

        // 4. 사용자 저장
        return userRepository.save(existingUser)
                .fold(
                        failure -> {
                            log.error("사용자 업데이트 실패: userId={}, error={}", command.userId().getId(),
                                    failure.getMessage());
                            return Either.left(failure);
                        },
                        user -> {
                            log.info("사용자 업데이트 완료: userId={}", command.userId().getId());
                            return Either.right(user);
                        });
    }
}

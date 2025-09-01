package com.boardly.application.user.usecase;

import com.boardly.application.user.command.SignUpCommand;
import com.boardly.domain.common.Failure;
import com.boardly.domain.user.PasswordEncoder;
import com.boardly.domain.user.User;
import com.boardly.domain.user.UserRepository;
import com.boardly.shared.annotation.UseCase;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 회원가입 UseCase
 */
@Slf4j
@UseCase
@RequiredArgsConstructor
public class SignUpUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입 실행
     */
    public Either<Failure, User> execute(SignUpCommand command) {
        log.info("회원가입 요청: email={}, username={}", command.getEmail(), command.getUsername());

        // 입력 검증
        Either<Failure, Void> validationResult = validateInput(command);
        if (validationResult.isLeft()) {
            return Either.left(validationResult.getLeft());
        }

        // 중복 검사
        Either<Failure, Void> duplicateCheckResult = checkDuplicates(command);
        if (duplicateCheckResult.isLeft()) {
            return Either.left(duplicateCheckResult.getLeft());
        }

        try {
            // 비밀번호 암호화
            String encodedPassword = passwordEncoder.encode(command.getPassword());

            // 사용자 생성
            User user = User.create(
                    command.getEmail(),
                    command.getUsername(),
                    encodedPassword,
                    command.getDisplayName(),
                    command.getProfileImageUrl());

            // 사용자 저장
            User savedUser = userRepository.save(user);

            log.info("회원가입 완료: userId={}, email={}", savedUser.getId(), savedUser.getEmail());
            return Either.right(savedUser);

        } catch (Exception e) {
            log.error("회원가입 중 오류 발생: email={}, username={}", command.getEmail(), command.getUsername(), e);
            return Either.left(Failure.ofInternalError("회원가입 처리 중 오류가 발생했습니다.", "SIGNUP_ERROR", null));
        }
    }

    /**
     * 입력 검증
     */
    private Either<Failure, Void> validateInput(SignUpCommand command) {
        List<Failure.FieldViolation> violations = new ArrayList<>();

        // 이메일 형식 검증
        if (command.getEmail() == null || command.getEmail().trim().isEmpty()) {
            violations.add(Failure.FieldViolation.builder()
                    .field("email")
                    .message("이메일은 필수입니다.")
                    .rejectedValue(command.getEmail())
                    .build());
        } else if (!isValidEmail(command.getEmail())) {
            violations.add(Failure.FieldViolation.builder()
                    .field("email")
                    .message("올바른 이메일 형식이 아닙니다.")
                    .rejectedValue(command.getEmail())
                    .build());
        }

        // 사용자명 검증
        if (command.getUsername() == null || command.getUsername().trim().isEmpty()) {
            violations.add(Failure.FieldViolation.builder()
                    .field("username")
                    .message("사용자명은 필수입니다.")
                    .rejectedValue(command.getUsername())
                    .build());
        } else if (command.getUsername().length() < 3 || command.getUsername().length() > 20) {
            violations.add(Failure.FieldViolation.builder()
                    .field("username")
                    .message("사용자명은 3자 이상 20자 이하여야 합니다.")
                    .rejectedValue(command.getUsername())
                    .build());
        } else if (!command.getUsername().matches("^[a-zA-Z0-9_-]+$")) {
            violations.add(Failure.FieldViolation.builder()
                    .field("username")
                    .message("사용자명은 영문, 숫자, 언더스코어(_), 하이픈(-)만 사용할 수 있습니다.")
                    .rejectedValue(command.getUsername())
                    .build());
        }

        // 비밀번호 검증
        if (command.getPassword() == null || command.getPassword().trim().isEmpty()) {
            violations.add(Failure.FieldViolation.builder()
                    .field("password")
                    .message("비밀번호는 필수입니다.")
                    .rejectedValue(null)
                    .build());
        } else if (command.getPassword().length() < 8 || command.getPassword().length() > 100) {
            violations.add(Failure.FieldViolation.builder()
                    .field("password")
                    .message("비밀번호는 8자 이상 100자 이하여야 합니다.")
                    .rejectedValue(null)
                    .build());
        }

        // 표시명 검증
        if (command.getDisplayName() != null && command.getDisplayName().length() > 50) {
            violations.add(Failure.FieldViolation.builder()
                    .field("displayName")
                    .message("표시명은 50자 이하여야 합니다.")
                    .rejectedValue(command.getDisplayName())
                    .build());
        }

        if (!violations.isEmpty()) {
            return Either.left(Failure.ofInputError("입력 데이터가 유효하지 않습니다.", "VALIDATION_ERROR", violations));
        }

        return Either.right(null);
    }

    /**
     * 중복 검사
     */
    private Either<Failure, Void> checkDuplicates(SignUpCommand command) {
        // 이메일 중복 검사
        if (userRepository.existsByEmail(command.getEmail())) {
            return Either.left(Failure.ofResourceConflict(
                    "이미 사용 중인 이메일입니다.",
                    "EMAIL_ALREADY_EXISTS",
                    command.getEmail()));
        }

        // 사용자명 중복 검사
        if (userRepository.existsByUsername(command.getUsername())) {
            return Either.left(Failure.ofResourceConflict(
                    "이미 사용 중인 사용자명입니다.",
                    "USERNAME_ALREADY_EXISTS",
                    command.getUsername()));
        }

        return Either.right(null);
    }

    /**
     * 이메일 형식 검증
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}

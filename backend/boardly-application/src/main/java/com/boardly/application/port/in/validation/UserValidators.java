package com.boardly.application.port.in.validation;

import com.boardly.application.port.in.validation.validators.DisplayNameValidator;
import com.boardly.application.port.in.validation.validators.EmailValidator;
import com.boardly.application.port.in.validation.validators.IdValidator;
import com.boardly.application.port.in.validation.validators.PasswordValidator;
import com.boardly.application.user.port.in.ChangePasswordCommand;
import com.boardly.application.user.port.in.CreateUserCommand;
import com.boardly.application.user.port.in.UserCommand;

/**
 * 사용자 관련 Validator 모음
 * <p>
 * Command별로 필요한 Validator들을 조합하여 제공합니다.
 * 1안 방식: Command별 Validator 조합 패턴을 사용합니다.
 * </p>
 */
public final class UserValidators {

    private UserValidators() {
        // 유틸리티 클래스
    }

    /**
     * UserCommand의 타입에 따라 적절한 검증을 수행합니다.
     * 
     * @param command 사용자 관련 명령
     * @return 검증 결과
     */
    public static ValidationResult<UserCommand> validate(UserCommand command) {
        return switch (command) {
            case CreateUserCommand createCmd ->
                CreateUserValidator.validate().validate(createCmd)
                        .map(result -> (UserCommand) result);
            case ChangePasswordCommand changeCmd ->
                ChangePasswordValidator.validate().validate(changeCmd)
                        .map(result -> (UserCommand) result);
        };
    }

    /**
     * CreateUserCommand 전용 검증기
     * <p>
     * EmailValidator + PasswordValidator + DisplayNameValidator를 조합합니다.
     * 모든 검증을 수행하고 실패한 검증 결과를 모두 수집합니다.
     * </p>
     */
    public static final class CreateUserValidator {

        private CreateUserValidator() {
            // 유틸리티 클래스
        }

        public static Validator<CreateUserCommand> validate() {
            return Validator.combine(
                    EmailValidator.validate(),
                    PasswordValidator.validate(),
                    DisplayNameValidator.validate());
        }

        /**
         * 첫 번째 실패에서 중단하는 방식
         */
        public static Validator<CreateUserCommand> validateFailFast() {
            return Validator.chain(
                    EmailValidator.validate(),
                    PasswordValidator.validate(),
                    DisplayNameValidator.validate());
        }
    }

    /**
     * ChangePasswordCommand 전용 검증기
     * <p>
     * IdValidator + PasswordValidator를 조합합니다.
     * 모든 검증을 수행하고 실패한 검증 결과를 모두 수집합니다.
     * </p>
     */
    public static final class ChangePasswordValidator {

        private ChangePasswordValidator() {
            // 유틸리티 클래스
        }

        public static Validator<ChangePasswordCommand> validate() {
            return Validator.combine(
                    IdValidator.validate(),
                    PasswordValidator.validate());
        }

        /**
         * 첫 번째 실패에서 중단하는 방식
         */
        public static Validator<ChangePasswordCommand> validateFailFast() {
            return Validator.chain(
                    IdValidator.validate(),
                    PasswordValidator.validate());
        }
    }
}

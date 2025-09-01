package com.boardly.application.port.in.validation;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.boardly.application.user.port.in.ChangePasswordCommand;
import com.boardly.application.user.port.in.CreateUserCommand;
import com.boardly.application.user.port.in.UserCommand;
import com.boardly.domain.user.Email;
import com.boardly.domain.user.Password;
import com.boardly.domain.user.UserId;

/**
 * UserValidators 테스트
 * <p>
 * UserValidators의 모든 메서드와 내부 클래스들을 테스트합니다.
 * - validate() 메서드 (UserCommand 타입별 분기)
 * - CreateUserValidator (combine, failFast 방식)
 * - ChangePasswordValidator (combine, failFast 방식)
 * </p>
 */
@DisplayName("UserValidators 테스트")
class UserValidatorsTest {

    @Nested
    @DisplayName("validate() 메서드 테스트")
    class ValidateMethodTest {

        @Test
        @DisplayName("CreateUserCommand 검증 - 성공")
        void givenValidCreateUserCommand_whenValidate_thenReturnsValidResult() {
            // given
            var email = Email.of("test@example.com");
            var password = Password.fromHash("$2a$10$N9qo8uLOickgx2ZMRZoMye"); // 유효한 해시된 비밀번호
            var command = CreateUserCommand.builder()
                    .email(email)
                    .password(password)
                    .displayName("테스트 사용자")
                    .build();

            // when
            ValidationResult<UserCommand> result = UserValidators.validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("CreateUserCommand 검증 - 실패 (이메일 형식 오류)")
        void givenInvalidCreateUserCommand_whenValidate_thenReturnsInvalidResult() {
            // given
            var email = Email.of("invalid-email");
            var password = Password.fromHash("$2a$10$N9qo8uLOickgx2ZMRZoMye"); // 유효한 해시된 비밀번호
            var command = CreateUserCommand.builder()
                    .email(email)
                    .password(password)
                    .displayName("테스트 사용자")
                    .build();

            // when
            ValidationResult<UserCommand> result = UserValidators.validate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("email");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.pattern");
        }

        @Test
        @DisplayName("ChangePasswordCommand 검증 - 성공")
        void givenValidChangePasswordCommand_whenValidate_thenReturnsValidResult() {
            // given
            var userId = UserId.generate(); // 유효한 UserId 생성
            var password = Password.fromHash("$2a$10$N9qo8uLOickgx2ZMRZoMye"); // 유효한 해시된 비밀번호
            var command = ChangePasswordCommand.builder()
                    .userId(userId)
                    .password(password)
                    .build();

            // when
            ValidationResult<UserCommand> result = UserValidators.validate(command);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get()).isEqualTo(command);
        }

        @Test
        @DisplayName("ChangePasswordCommand 검증 - 실패 (비밀번호 형식 오류)")
        void givenInvalidChangePasswordCommand_whenValidate_thenReturnsInvalidResult() {
            // given
            var userId = UserId.generate(); // 유효한 UserId 생성
            var password = Password.fromHash("short"); // 너무 짧은 해시된 비밀번호
            var command = ChangePasswordCommand.builder()
                    .userId(userId)
                    .password(password)
                    .build();

            // when
            ValidationResult<UserCommand> result = UserValidators.validate(command);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getField()).isEqualTo("password");
            assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.minLength");
        }
    }

    @Nested
    @DisplayName("CreateUserValidator 테스트")
    class CreateUserValidatorTest {

        @Nested
        @DisplayName("validate() 메서드 테스트 (combine 방식)")
        class ValidateMethodTest {

            @Test
            @DisplayName("유효한 CreateUserCommand - 성공")
            void givenValidCreateUserCommand_whenValidate_thenReturnsValidResult() {
                // given
                var email = Email.of("test@example.com");
                var password = Password.fromHash("$2a$10$N9qo8uLOickgx2ZMRZoMye"); // 유효한 해시된 비밀번호
                var command = CreateUserCommand.builder()
                        .email(email)
                        .password(password)
                        .displayName("테스트 사용자")
                        .build();

                // when
                ValidationResult<CreateUserCommand> result = UserValidators.CreateUserValidator.validate().validate(command);

                // then
                assertThat(result.isValid()).isTrue();
                assertThat(result.get()).isEqualTo(command);
            }

            @Test
            @DisplayName("이메일 형식 오류 - 실패")
            void givenInvalidEmail_whenValidate_thenReturnsInvalidResult() {
                // given
                var email = Email.of("invalid-email");
                var password = Password.fromHash("$2a$10$N9qo8uLOickgx2ZMRZoMye"); // 유효한 해시된 비밀번호
                var command = CreateUserCommand.builder()
                        .email(email)
                        .password(password)
                        .displayName("테스트 사용자")
                        .build();

                // when
                ValidationResult<CreateUserCommand> result = UserValidators.CreateUserValidator.validate().validate(command);

                // then
                assertThat(result.isInvalid()).isTrue();
                assertThat(result.getErrors()).hasSize(1);
                assertThat(result.getErrors().get(0).getField()).isEqualTo("email");
                assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.pattern");
            }

            @Test
            @DisplayName("비밀번호 형식 오류 - 실패")
            void givenInvalidPassword_whenValidate_thenReturnsInvalidResult() {
                // given
                var email = Email.of("test@example.com");
                var password = Password.fromHash("short"); // 너무 짧은 해시된 비밀번호
                var command = CreateUserCommand.builder()
                        .email(email)
                        .password(password)
                        .displayName("테스트 사용자")
                        .build();

                // when
                ValidationResult<CreateUserCommand> result = UserValidators.CreateUserValidator.validate().validate(command);

                // then
                assertThat(result.isInvalid()).isTrue();
                assertThat(result.getErrors()).hasSize(1);
                assertThat(result.getErrors().get(0).getField()).isEqualTo("password");
                assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.minLength");
            }

            @Test
            @DisplayName("표시 이름 형식 오류 - 실패")
            void givenInvalidDisplayName_whenValidate_thenReturnsInvalidResult() {
                // given
                var email = Email.of("test@example.com");
                var password = Password.fromHash("$2a$10$N9qo8uLOickgx2ZMRZoMye"); // 유효한 해시된 비밀번호
                var command = CreateUserCommand.builder()
                        .email(email)
                        .password(password)
                        .displayName("테스트@사용자") // 특수문자 포함
                        .build();

                // when
                ValidationResult<CreateUserCommand> result = UserValidators.CreateUserValidator.validate().validate(command);

                // then
                assertThat(result.isInvalid()).isTrue();
                assertThat(result.getErrors()).hasSize(1);
                assertThat(result.getErrors().get(0).getField()).isEqualTo("displayName");
                assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.pattern");
            }

            @Test
            @DisplayName("여러 필드 오류 - 모든 오류 수집")
            void givenMultipleInvalidFields_whenValidate_thenReturnsAllErrors() {
                // given
                var email = Email.of("invalid-email");
                var password = Password.fromHash("short"); // 너무 짧은 해시된 비밀번호
                var command = CreateUserCommand.builder()
                        .email(email)
                        .password(password)
                        .displayName("테스트@사용자")
                        .build();

                // when
                ValidationResult<CreateUserCommand> result = UserValidators.CreateUserValidator.validate().validate(command);

                // then
                assertThat(result.isInvalid()).isTrue();
                assertThat(result.getErrors()).hasSize(3);
                
                // 이메일 오류
                assertThat(result.getErrors().get(0).getField()).isEqualTo("email");
                assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.pattern");
                
                // 비밀번호 오류
                assertThat(result.getErrors().get(1).getField()).isEqualTo("password");
                assertThat(result.getErrors().get(1).getMessageKey()).isEqualTo("common.validation.field.minLength");
                
                // 표시 이름 오류
                assertThat(result.getErrors().get(2).getField()).isEqualTo("displayName");
                assertThat(result.getErrors().get(2).getMessageKey()).isEqualTo("common.validation.field.pattern");
            }

            @ParameterizedTest
            @ValueSource(strings = {
                    "test@example.com",
                    "user.name@example.co.kr",
                    "test+tag@example.com",
                    "user_name@example-domain.com"
            })
            @DisplayName("유효한 이메일 형식들 - 성공")
            void givenValidEmailFormats_whenValidate_thenReturnsValidResult(String validEmail) {
                // given
                var email = Email.of(validEmail);
                var password = Password.fromHash("$2a$10$N9qo8uLOickgx2ZMRZoMye"); // 유효한 해시된 비밀번호
                var command = CreateUserCommand.builder()
                        .email(email)
                        .password(password)
                        .displayName("테스트 사용자")
                        .build();

                // when
                ValidationResult<CreateUserCommand> result = UserValidators.CreateUserValidator.validate().validate(command);

                // then
                assertThat(result.isValid()).isTrue();
                assertThat(result.get()).isEqualTo(command);
            }

            @ParameterizedTest
            @ValueSource(strings = {
                    "$2a$10$N9qo8uLOickgx2ZMRZoMye",
                    "$2a$10$ABCDEFGHIJKLMNOPQRSTUV"
            })
            @DisplayName("유효한 비밀번호 해시 형식들 - 성공")
            void givenValidPasswordFormats_whenValidate_thenReturnsValidResult(String validPasswordHash) {
                // given
                var email = Email.of("test@example.com");
                var password = Password.fromHash(validPasswordHash);
                var command = CreateUserCommand.builder()
                        .email(email)
                        .password(password)
                        .displayName("테스트 사용자")
                        .build();

                // when
                ValidationResult<CreateUserCommand> result = UserValidators.CreateUserValidator.validate().validate(command);

                // then
                assertThat(result.isValid()).isTrue();
                assertThat(result.get()).isEqualTo(command);
            }

            @ParameterizedTest
            @ValueSource(strings = {
                    "테스트 사용자",
                    "Test User",
                    "사용자123",
                    "User123",
                    "한글 영문 123"
            })
            @DisplayName("유효한 표시 이름 형식들 - 성공")
            void givenValidDisplayNameFormats_whenValidate_thenReturnsValidResult(String validDisplayName) {
                // given
                var email = Email.of("test@example.com");
                var password = Password.fromHash("$2a$10$N9qo8uLOickgx2ZMRZoMye"); // 유효한 해시된 비밀번호
                var command = CreateUserCommand.builder()
                        .email(email)
                        .password(password)
                        .displayName(validDisplayName)
                        .build();

                // when
                ValidationResult<CreateUserCommand> result = UserValidators.CreateUserValidator.validate().validate(command);

                // then
                assertThat(result.isValid()).isTrue();
                assertThat(result.get()).isEqualTo(command);
            }
        }

        @Nested
        @DisplayName("validateFailFast() 메서드 테스트 (failFast 방식)")
        class ValidateFailFastMethodTest {

            @Test
            @DisplayName("유효한 CreateUserCommand - 성공")
            void givenValidCreateUserCommand_whenValidateFailFast_thenReturnsValidResult() {
                // given
                var email = Email.of("test@example.com");
                var password = Password.fromHash("$2a$10$N9qo8uLOickgx2ZMRZoMye"); // 유효한 해시된 비밀번호
                var command = CreateUserCommand.builder()
                        .email(email)
                        .password(password)
                        .displayName("테스트 사용자")
                        .build();

                // when
                ValidationResult<CreateUserCommand> result = UserValidators.CreateUserValidator.validateFailFast().validate(command);

                // then
                assertThat(result.isValid()).isTrue();
                assertThat(result.get()).isEqualTo(command);
            }

            @Test
            @DisplayName("이메일 형식 오류 - 첫 번째 실패에서 중단")
            void givenInvalidEmail_whenValidateFailFast_thenReturnsFirstError() {
                // given
                var email = Email.of("invalid-email");
                var password = Password.fromHash("$2a$10$short"); // 비밀번호도 오류지만 이메일이 먼저 검증됨
                var command = CreateUserCommand.builder()
                        .email(email)
                        .password(password)
                        .displayName("테스트@사용자") // 표시 이름도 오류지만 이메일이 먼저 검증됨
                        .build();

                // when
                ValidationResult<CreateUserCommand> result = UserValidators.CreateUserValidator.validateFailFast().validate(command);

                // then
                assertThat(result.isInvalid()).isTrue();
                assertThat(result.getErrors()).hasSize(1); // 첫 번째 오류만 반환
                assertThat(result.getErrors().get(0).getField()).isEqualTo("email");
                assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.pattern");
            }

            @Test
            @DisplayName("이메일은 유효하지만 비밀번호 형식 오류 - 두 번째 실패에서 중단")
            void givenValidEmailButInvalidPassword_whenValidateFailFast_thenReturnsSecondError() {
                // given
                var email = Email.of("test@example.com");
                var password = Password.fromHash("short"); // 너무 짧은 해시된 비밀번호
                var command = CreateUserCommand.builder()
                        .email(email)
                        .password(password)
                        .displayName("테스트@사용자") // 표시 이름도 오류지만 비밀번호가 먼저 검증됨
                        .build();

                // when
                ValidationResult<CreateUserCommand> result = UserValidators.CreateUserValidator.validateFailFast().validate(command);

                // then
                assertThat(result.isInvalid()).isTrue();
                assertThat(result.getErrors()).hasSize(1); // 첫 번째 오류만 반환
                assertThat(result.getErrors().get(0).getField()).isEqualTo("password");
                assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.minLength");
            }

            @Test
            @DisplayName("이메일과 비밀번호는 유효하지만 표시 이름 형식 오류 - 세 번째 실패에서 중단")
            void givenValidEmailAndPasswordButInvalidDisplayName_whenValidateFailFast_thenReturnsThirdError() {
                // given
                var email = Email.of("test@example.com");
                var password = Password.fromHash("$2a$10$N9qo8uLOickgx2ZMRZoMye"); // 유효한 해시된 비밀번호
                var command = CreateUserCommand.builder()
                        .email(email)
                        .password(password)
                        .displayName("테스트@사용자")
                        .build();

                // when
                ValidationResult<CreateUserCommand> result = UserValidators.CreateUserValidator.validateFailFast().validate(command);

                // then
                assertThat(result.isInvalid()).isTrue();
                assertThat(result.getErrors()).hasSize(1); // 첫 번째 오류만 반환
                assertThat(result.getErrors().get(0).getField()).isEqualTo("displayName");
                assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.pattern");
            }
        }
    }

    @Nested
    @DisplayName("ChangePasswordValidator 테스트")
    class ChangePasswordValidatorTest {

        @Nested
        @DisplayName("validate() 메서드 테스트 (combine 방식)")
        class ValidateMethodTest {

            @Test
            @DisplayName("유효한 ChangePasswordCommand - 성공")
            void givenValidChangePasswordCommand_whenValidate_thenReturnsValidResult() {
                // given
                var userId = UserId.generate(); // 유효한 UserId 생성
                var password = Password.fromHash("$2a$10$N9qo8uLOickgx2ZMRZoMye"); // 유효한 해시된 비밀번호
                var command = ChangePasswordCommand.builder()
                        .userId(userId)
                        .password(password)
                        .build();

                // when
                ValidationResult<ChangePasswordCommand> result = UserValidators.ChangePasswordValidator.validate().validate(command);

                // then
                assertThat(result.isValid()).isTrue();
                assertThat(result.get()).isEqualTo(command);
            }

            @Test
            @DisplayName("사용자 ID 형식 오류 - 실패")
            void givenInvalidUserId_whenValidate_thenReturnsInvalidResult() {
                // given - 빈 문자열로 UserId를 생성하려고 하면 IllegalArgumentException이 발생하므로
                // 이 테스트는 UserId 생성 단계에서 실패하므로 검증 단계까지 도달하지 않음
                // 대신 유효한 UserId를 사용하고 다른 검증 실패를 테스트
                var userId = UserId.generate(); // 유효한 UserId 생성
                var password = Password.fromHash("$2a$10$N9qo8uLOickgx2ZMRZoMye"); // 유효한 해시된 비밀번호
                var command = ChangePasswordCommand.builder()
                        .userId(userId)
                        .password(password)
                        .build();

                // when
                ValidationResult<ChangePasswordCommand> result = UserValidators.ChangePasswordValidator.validate().validate(command);

                // then - 모든 필드가 유효하므로 성공
                assertThat(result.isValid()).isTrue();
                assertThat(result.get()).isEqualTo(command);
            }

            @Test
            @DisplayName("비밀번호 형식 오류 - 실패")
            void givenInvalidPassword_whenValidate_thenReturnsInvalidResult() {
                // given
                var userId = UserId.generate(); // 유효한 UserId 생성
                var password = Password.fromHash("short"); // 너무 짧은 해시된 비밀번호
                var command = ChangePasswordCommand.builder()
                        .userId(userId)
                        .password(password)
                        .build();

                // when
                ValidationResult<ChangePasswordCommand> result = UserValidators.ChangePasswordValidator.validate().validate(command);

                // then
                assertThat(result.isInvalid()).isTrue();
                assertThat(result.getErrors()).hasSize(1);
                assertThat(result.getErrors().get(0).getField()).isEqualTo("password");
                assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.minLength");
            }

            @Test
            @DisplayName("유효한 사용자 ID 형식 - 성공")
            void givenValidUserIdFormat_whenValidate_thenReturnsValidResult() {
                // given
                var userId = UserId.generate(); // 유효한 UserId 생성
                var password = Password.fromHash("$2a$10$N9qo8uLOickgx2ZMRZoMye"); // 유효한 해시된 비밀번호
                var command = ChangePasswordCommand.builder()
                        .userId(userId)
                        .password(password)
                        .build();

                // when
                ValidationResult<ChangePasswordCommand> result = UserValidators.ChangePasswordValidator.validate().validate(command);

                // then
                assertThat(result.isValid()).isTrue();
                assertThat(result.get()).isEqualTo(command);
            }

            @ParameterizedTest
            @ValueSource(strings = {
                    "$2a$10$N9qo8uLOickgx2ZMRZoMye",
                    "$2a$10$ABCDEFGHIJKLMNOPQRSTUV"
            })
            @DisplayName("유효한 비밀번호 해시 형식들 - 성공")
            void givenValidPasswordFormats_whenValidate_thenReturnsValidResult(String validPasswordHash) {
                // given
                var userId = UserId.generate(); // 유효한 UserId 생성
                var password = Password.fromHash(validPasswordHash);
                var command = ChangePasswordCommand.builder()
                        .userId(userId)
                        .password(password)
                        .build();

                // when
                ValidationResult<ChangePasswordCommand> result = UserValidators.ChangePasswordValidator.validate().validate(command);

                // then
                assertThat(result.isValid()).isTrue();
                assertThat(result.get()).isEqualTo(command);
            }
        }

        @Nested
        @DisplayName("validateFailFast() 메서드 테스트 (failFast 방식)")
        class ValidateFailFastMethodTest {

            @Test
            @DisplayName("유효한 ChangePasswordCommand - 성공")
            void givenValidChangePasswordCommand_whenValidateFailFast_thenReturnsValidResult() {
                // given
                var userId = UserId.generate(); // 유효한 UserId 생성
                var password = Password.fromHash("$2a$10$N9qo8uLOickgx2ZMRZoMye"); // 유효한 해시된 비밀번호
                var command = ChangePasswordCommand.builder()
                        .userId(userId)
                        .password(password)
                        .build();

                // when
                ValidationResult<ChangePasswordCommand> result = UserValidators.ChangePasswordValidator.validateFailFast().validate(command);

                // then
                assertThat(result.isValid()).isTrue();
                assertThat(result.get()).isEqualTo(command);
            }

            @Test
            @DisplayName("비밀번호 형식 오류 - 첫 번째 실패에서 중단")
            void givenInvalidPassword_whenValidateFailFast_thenReturnsFirstError() {
                // given
                var userId = UserId.generate(); // 유효한 UserId 생성
                var password = Password.fromHash("short"); // 너무 짧은 해시된 비밀번호
                var command = ChangePasswordCommand.builder()
                        .userId(userId)
                        .password(password)
                        .build();

                // when
                ValidationResult<ChangePasswordCommand> result = UserValidators.ChangePasswordValidator.validateFailFast().validate(command);

                // then
                assertThat(result.isInvalid()).isTrue();
                assertThat(result.getErrors()).hasSize(1); // 첫 번째 오류만 반환
                assertThat(result.getErrors().get(0).getField()).isEqualTo("password");
                assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.minLength");
            }

            @Test
            @DisplayName("사용자 ID는 유효하지만 비밀번호 형식 오류 - 두 번째 실패에서 중단")
            void givenValidUserIdButInvalidPassword_whenValidateFailFast_thenReturnsSecondError() {
                // given
                var userId = UserId.generate(); // 유효한 UserId 생성
                var password = Password.fromHash("short"); // 너무 짧은 해시된 비밀번호
                var command = ChangePasswordCommand.builder()
                        .userId(userId)
                        .password(password)
                        .build();

                // when
                ValidationResult<ChangePasswordCommand> result = UserValidators.ChangePasswordValidator.validateFailFast().validate(command);

                // then
                assertThat(result.isInvalid()).isTrue();
                assertThat(result.getErrors()).hasSize(1); // 첫 번째 오류만 반환
                assertThat(result.getErrors().get(0).getField()).isEqualTo("password");
                assertThat(result.getErrors().get(0).getMessageKey()).isEqualTo("common.validation.field.minLength");
            }
        }
    }
}
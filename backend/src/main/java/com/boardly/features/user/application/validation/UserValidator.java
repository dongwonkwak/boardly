package com.boardly.features.user.application.validation;

import java.util.function.Function;

import com.boardly.shared.application.validation.ValidationResult;
import org.springframework.stereotype.Component;

import com.boardly.features.user.application.port.input.RegisterUserCommand;
import com.boardly.features.user.application.port.input.UpdateUserCommand;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.Validator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserValidator {
  private final ValidationMessageResolver messageResolver;

  /**
   * 사용자 등록 검증
   * @param command 등록 명령
   * @return 검증 결과
   */
  public ValidationResult<RegisterUserCommand> validateUserRegistration(RegisterUserCommand command) {
    return getUserRegistrationValidator().validate(command);
  }

  /**
   * 사용자 업데이트 검증
   * @param command 업데이트 명령
   * @return 검증 결과
   */
  public ValidationResult<UpdateUserCommand> validateUserUpdate(UpdateUserCommand command) {
    return getUserUpdateValidator().validate(command);
  }

  private Validator<RegisterUserCommand> getUserRegistrationValidator() {
    return Validator.combine(
            getEmailValidator(),
            getPasswordValidator(),
            createFirstNameValidator(RegisterUserCommand::firstName),
            createLastNameValidator(RegisterUserCommand::lastName)
    );
  }

  private Validator<UpdateUserCommand> getUserUpdateValidator() {
    return Validator.combine(
            createFirstNameValidator(UpdateUserCommand::firstName),
            createLastNameValidator(UpdateUserCommand::lastName)
    );
  }

  // 이메일 검증자 (순차적 검증 - 하나의 에러만 리턴)
  // 우선순위: 필수 입력 > 길이 > 형식
  private Validator<RegisterUserCommand> getEmailValidator() {
    return Validator.chain(
            // 필수 입력 검증
            Validator.fieldWithMessage(
                    RegisterUserCommand::email,
                    email -> email != null && !email.trim().isEmpty(),
                    "email",
                    "validation.user.email.required",
                    messageResolver
            ),
            // 이메일 형식 검증
            Validator.fieldWithMessage(
                    RegisterUserCommand::email,
                    email -> CommonValidationRules.EMAIL_PATTERN.matcher(email).matches(),
                    "email",
                    "validation.user.email.invalid",
                    messageResolver
            ),
            // 길이 검증
            Validator.fieldWithMessage(
                    RegisterUserCommand::email,
                    email -> email.length() <= CommonValidationRules.EMAIL_MAX_LENGTH,
                    "email",
                    "validation.user.email.length",
                    messageResolver
            )
    );
  }

  // 비밀번호 검증자 (순차적 검증 - 하나의 에러만 리턴)
  // 우선순위: 필수 입력 > 길이 > 형식
  private Validator<RegisterUserCommand> getPasswordValidator() {
    return Validator.chain(
            // 필수 입력 검증
            Validator.fieldWithMessage(
                    RegisterUserCommand::password,
                    password -> password != null && !password.trim().isEmpty(),
                    "password",
                    "validation.user.password.required",
                    messageResolver
            ),

            // 최소 길이 검증
            Validator.fieldWithMessage(
                    RegisterUserCommand::password,
                    password -> password == null || password.length() >= CommonValidationRules.PASSWORD_MIN_LENGTH,
                    "password",
                    "validation.user.password.min.length",
                    messageResolver
            ),

            // 최대 길이 검증
            Validator.fieldWithMessage(
                    RegisterUserCommand::password,
                    password -> password == null || password.length() <= CommonValidationRules.PASSWORD_MAX_LENGTH,
                    "password",
                    "validation.user.password.max.length",
                    messageResolver
            ),

            // 패턴 검증
            Validator.fieldWithMessage(
                    RegisterUserCommand::password,
                    password -> password == null || CommonValidationRules.PASSWORD_PATTERN.matcher(password).matches(),
                    "password",
                    "validation.user.password.pattern",
                    messageResolver
            )
    );
  }

  // 이름 검증자 (순차적 검증 - 하나의 에러만 리턴)
  // 우선순위: 필수 입력 > 길이 > 형식
  private <T> Validator<T> createFirstNameValidator(Function<T, String> getter) {
    return Validator.chain(
            // 필수 입력 검증
            Validator.fieldWithMessage(
                    getter,
                    firstName -> firstName != null && !firstName.trim().isEmpty(),
                    "firstName",
                    "validation.user.firstName.required",
                    messageResolver
            ),

            // 길이 검증
            Validator.fieldWithMessage(
                    getter,
                    firstName -> firstName == null || (!firstName.isEmpty() && firstName.length() <= CommonValidationRules.NAME_MAX_LENGTH),
                    "firstName",
                    "validation.common.length.range",
                    messageResolver,
                    1, 50
            ),

            // 패턴 검증 (한글/영문만)
            Validator.fieldWithMessage(
                    getter,
                    firstName -> firstName == null || CommonValidationRules.NAME_PATTERN.matcher(firstName).matches(),
                    "firstName",
                    "validation.user.firstName.pattern",
                    messageResolver
            )
    );
  }

  // 성 검증자 (순차적 검증 - 하나의 에러만 리턴)
  // 우선순위: 필수 입력 > 길이 > 형식
  private <T> Validator<T> createLastNameValidator(Function<T, String> getter) {
    return Validator.chain(
            // 필수 입력 검증
            Validator.fieldWithMessage(
                    getter,
                    lastName -> lastName != null && !lastName.trim().isEmpty(),
                    "lastName",
                    "validation.user.lastName.required",
                    messageResolver
            ),

            // 길이 검증
            Validator.fieldWithMessage(
                    getter,
                    lastName -> lastName == null || (!lastName.isEmpty() && lastName.length() <= CommonValidationRules.NAME_MAX_LENGTH),
                    "lastName",
                    "validation.common.length.range",
                    messageResolver,
                    1, 50
            ),

            // 패턴 검증 (한글/영문만)
            Validator.fieldWithMessage(
                    getter,
                    lastName -> lastName == null || CommonValidationRules.NAME_PATTERN.matcher(lastName).matches(),
                    "lastName",
                    "validation.user.lastName.pattern",
                    messageResolver
            )
    );
  }
}

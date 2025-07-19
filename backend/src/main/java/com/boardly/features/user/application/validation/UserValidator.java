package com.boardly.features.user.application.validation;

import com.boardly.shared.application.validation.ValidationResult;
import org.springframework.stereotype.Component;

import com.boardly.features.user.application.port.input.RegisterUserCommand;
import com.boardly.features.user.application.port.input.UpdateUserCommand;
import com.boardly.shared.application.validation.CommonValidationRules;
import com.boardly.shared.application.validation.Validator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserValidator {
  private final CommonValidationRules commonValidationRules;

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
            commonValidationRules.emailComplete(RegisterUserCommand::email),
            commonValidationRules.passwordComplete(RegisterUserCommand::password),
            commonValidationRules.firstNameComplete(RegisterUserCommand::firstName),
            commonValidationRules.lastNameComplete(RegisterUserCommand::lastName)
    );
  }

  private Validator<UpdateUserCommand> getUserUpdateValidator() {
    return Validator.combine(
            commonValidationRules.firstNameComplete(UpdateUserCommand::firstName),
            commonValidationRules.lastNameComplete(UpdateUserCommand::lastName)
    );
  }
}

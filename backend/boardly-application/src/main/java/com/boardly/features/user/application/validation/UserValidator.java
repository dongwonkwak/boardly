package com.boardly.features.user.application.validation;

import org.springframework.stereotype.Component;

import com.boardly.features.user.application.command.RegisterUserCommand;
import com.boardly.features.user.application.command.UpdateUserCommand;
import com.boardly.infrastructure.validation.CommonValidationRules;
import com.boardly.shared.validation.ValidationResult;
import com.boardly.shared.validation.Validator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserValidator {
  private final CommonValidationRules commonValidationRules;

  public ValidationResult<RegisterUserCommand> validateUserRegistration(RegisterUserCommand command) {
    return getUserRegistrationValidator().validate(command);
  }

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

package com.boardly.features.user.application.validation;

import java.util.regex.Pattern;

import com.boardly.shared.application.validation.ValidationResult;
import org.springframework.stereotype.Component;

import com.boardly.features.user.application.usecase.RegisterUserCommand;
import com.boardly.shared.application.validation.ValidationMessageResolver;
import com.boardly.shared.application.validation.Validator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserValidator {
  private final ValidationMessageResolver messageResolver;

  // === 패턴 정의 ===
    
  private static final Pattern EMAIL_PATTERN = Pattern.compile(
    "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
  );

  // 영문 대소문자, 숫자, 특수문자 조합 8자 이상 20자 이하
  private static final Pattern PASSWORD_PATTERN = Pattern.compile(
    "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$"
  );

  // 한글 영문만 허용
  private static final Pattern NAME_PATTERN = Pattern.compile(
    "^[a-zA-Z가-힣]+$"
  );

  /**
   * 사용자 등록 검증
   * @param command 등록 명령
   * @return 검증 결과
   */
  public ValidationResult<RegisterUserCommand> validateUserRegistration(RegisterUserCommand command) {
    return getUserRegistrationValidator().validate(command);
  }

  private Validator<RegisterUserCommand> getUserRegistrationValidator() {
    return Validator.combine(
            getEmailValidator(),
            getPasswordValidator(),
            getFirstNameValidator(),
            getLastNameValidator()
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
                    email -> EMAIL_PATTERN.matcher(email).matches(),
                    "email",
                    "validation.user.email.invalid",
                    messageResolver
            ),
            // 길이 검증
            Validator.fieldWithMessage(
                    RegisterUserCommand::email,
                    email -> email.length() <= 100,
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
                    password -> password == null || password.length() >= 8,
                    "password",
                    "validation.user.password.min.length",
                    messageResolver
            ),

            // 최대 길이 검증
            Validator.fieldWithMessage(
                    RegisterUserCommand::password,
                    password -> password == null || password.length() <= 20,
                    "password",
                    "validation.user.password.max.length",
                    messageResolver
            ),

            // 패턴 검증
            Validator.fieldWithMessage(
                    RegisterUserCommand::password,
                    password -> password == null || PASSWORD_PATTERN.matcher(password).matches(),
                    "password",
                    "validation.user.password.pattern",
                    messageResolver
            )
    );
  }

  // 이름 검증자 (순차적 검증 - 하나의 에러만 리턴)
  // 우선순위: 필수 입력 > 길이 > 형식
  private Validator<RegisterUserCommand> getFirstNameValidator() {
    return Validator.chain(
            // 필수 입력 검증
            Validator.fieldWithMessage(
                    RegisterUserCommand::firstName,
                    firstName -> firstName != null && !firstName.trim().isEmpty(),
                    "firstName",
                    "validation.user.firstName.required",
                    messageResolver
            ),

            // 길이 검증
            Validator.fieldWithMessage(
                    RegisterUserCommand::firstName,
                    firstName -> firstName == null || (!firstName.isEmpty() && firstName.length() <= 50),
                    "firstName",
                    "validation.common.length.range",
                    messageResolver,
                    1, 50
            ),

            // 패턴 검증 (한글/영문만)
            Validator.fieldWithMessage(
                    RegisterUserCommand::firstName,
                    firstName -> firstName == null || NAME_PATTERN.matcher(firstName).matches(),
                    "firstName",
                    "validation.user.firstName.pattern",
                    messageResolver
            )
    );
  }

  // 성 검증자 (순차적 검증 - 하나의 에러만 리턴)
  // 우선순위: 필수 입력 > 길이 > 형식
  private Validator<RegisterUserCommand> getLastNameValidator() {
    return Validator.chain(
            // 필수 입력 검증
            Validator.fieldWithMessage(
                    RegisterUserCommand::lastName,
                    lastName -> lastName != null && !lastName.trim().isEmpty(),
                    "lastName",
                    "validation.user.lastName.required",
                    messageResolver
            ),

            // 길이 검증
            Validator.fieldWithMessage(
                    RegisterUserCommand::lastName,
                    lastName -> lastName == null || (!lastName.isEmpty() && lastName.length() <= 50),
                    "lastName",
                    "validation.common.length.range",
                    messageResolver,
                    1, 50
            ),

            // 패턴 검증 (한글/영문만)
            Validator.fieldWithMessage(
                    RegisterUserCommand::lastName,
                    lastName -> lastName == null || NAME_PATTERN.matcher(lastName).matches(),
                    "lastName",
                    "validation.user.lastName.pattern",
                    messageResolver
            )
    );
  }
}

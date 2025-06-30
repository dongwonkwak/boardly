package com.boardly.shared.application.validation.field;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.boardly.shared.application.validation.rule.CompositeFieldValidator;
import com.boardly.shared.application.validation.rule.ValidationRules;

@Component
public final class FieldValidators {
  private final ValidationRules validationRules;

  public FieldValidators(ValidationRules validationRules) {
    this.validationRules = validationRules;
  }

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

  private static final Pattern HEX_COLOR_PATTERN = Pattern.compile(
    "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$"
  );


  // === 이메일 검증 ===
  public CompositeFieldValidator<String> email() {
    return CompositeFieldValidator.of(
      validationRules.required(),
      validationRules.maxLength(100),
      validationRules.matchesPattern(EMAIL_PATTERN.pattern(), "validation.email.invalid")
    );
  }

  // === 비밀번호 검증 ===
  public CompositeFieldValidator<String> password() {
    return CompositeFieldValidator.of(
      validationRules.required(),
      validationRules.minLength(8),
      validationRules.maxLength(20),
      validationRules.matchesPattern(PASSWORD_PATTERN.pattern(), "validation.password.pattern")
    );
  }

  // === 이름 검증 ===
  public CompositeFieldValidator<String> name() {
    return CompositeFieldValidator.of(
      validationRules.required(),
      validationRules.maxLength(50),
      validationRules.matchesPattern(NAME_PATTERN.pattern(), "validation.name.pattern")
    );
  }

  // === 제목 검증 ===
  public CompositeFieldValidator<String> title(int maxLength) {
    return CompositeFieldValidator.of(
      validationRules.required(),
      validationRules.lengthBetween(1, maxLength)
    );
  }

  public CompositeFieldValidator<String> boardTitle() {
    return title(100);
  }

  public CompositeFieldValidator<String> listTitle() {
    return title(100);
  }

  public CompositeFieldValidator<String> cardTitle() {
    return title(50);
  }

  public CompositeFieldValidator<String> description(int maxLength) {
    return CompositeFieldValidator.of(
      validationRules.maxLength(maxLength)
    );
  }

  public CompositeFieldValidator<String> boardDescription() {
    return description(500);
  }
  
  public CompositeFieldValidator<String> cardDescription() {
    return description(2000);
  }
  
  // === 색상 검증 ===
  public CompositeFieldValidator<String> color() {
    return CompositeFieldValidator.of(
      validationRules.matchesPattern(HEX_COLOR_PATTERN.pattern(), "validation.color.invalid")
    );
  }

  // === 위치/순서 검증자 ===
  public CompositeFieldValidator<Integer> position() {
    return CompositeFieldValidator.of(
      validationRules.required(),
      validationRules.min(0)
    );
  }

  // === 커스텀 필드 검장자 ===

  /**
   * 정수 범위 검증
   * 
   * @param min 최소값
   * @param max 최대값
   * @return 정수 범위 검증자
   */
  public CompositeFieldValidator<Integer> integerRange(int min, int max) {
    return CompositeFieldValidator.of(
      validationRules.required(),
      validationRules.min(min),
      validationRules.max(max)
    );
  }

  /**
   * 선택적 문자열 검증
   * 
   * @param maxLength 최대 길이
   * @return 선택적 문자열 검증자
   */
  public CompositeFieldValidator<String> optionalString(int maxLength) {
    return CompositeFieldValidator.of(
      validationRules.maxLength(maxLength)
    );
  }

  /**
   * 커스텀 패턴 검증
   * 
   * @param pattern 패턴
   * @param messageKey 메시지 키
   * @param maxLength 최대 길이
   * @return 커스텀 패턴 검증자
   */
  public CompositeFieldValidator<String> customPattern(Pattern pattern, String messageKey, int maxLength) {
    return CompositeFieldValidator.of(
      validationRules.required(),
      validationRules.maxLength(maxLength),
      validationRules.matchesPattern(pattern.pattern(), messageKey)
    );
  }
}

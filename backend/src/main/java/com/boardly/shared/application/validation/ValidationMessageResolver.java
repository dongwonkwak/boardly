package com.boardly.shared.application.validation;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import io.vavr.control.Try;

@Component
public class ValidationMessageResolver {
  private final MessageSource messageSource;


  public ValidationMessageResolver(MessageSource messageSource) {
    this.messageSource = messageSource;
  }


  /**
   * 현재 로케일에 맞는 메시지를 반환
   *
   * @param code 메시지 코드 (예: "validation.user.email.required")
   * @param args 메시지 파라미터
   * @return 국제화된 메시지
   */
  public String getMessage(String code, Object... args) {
    return getMessage(code, LocaleContextHolder.getLocale(), args);
  }

  /**
   * 지정된 로케일에 맞는 메시지를 반환
   *
   * @param code 메시지 코드
   * @param locale 로케일
   * @param args 메시지 파라미터
   * @return 국제화된 메시지
   */
  public String getMessage(String code, Locale locale, Object... args) {
    return Try.of(() -> messageSource.getMessage(code, args, locale))
        .getOrElse(code);
  }

  /**
   * 기본 메시지와 함께 현재 로케일에 맞는 메시지를 반환
   * 메시지 코드가 없으면 기본 메시지 사용
   *
   * @param code 메시지 코드
   * @param defaultMessage 기본 메시지
   * @param args 메시지 파라미터
   * @return 국제화된 메시지 또는 기본 메시지
   */
  public String getMessageWithDefault(String code, String defaultMessage, Object... args) {
    return Try.of(() -> messageSource.getMessage(code, args, defaultMessage, LocaleContextHolder.getLocale()))
            .getOrElse(defaultMessage);
  }

  /**
   * 검증 오류 메시지를 생성 (validation 네임스페이스 자동 추가)
   *
   * @param field 필드명
   * @param rule 검증 규칙명 (예: "required", "email", "length")
   * @param args 메시지 파라미터
   * @return 국제화된 검증 오류 메시지
   */
  public String getValidationMessage(String field, String rule, Object... args) {
    String code = String.format("validation.%s.%s", field, rule);
    return getMessage(code, args);
  }

  /**
   * 검증 오류 메시지를 생성 (기본 메시지와 함께)
   *
   * @param field 필드명
   * @param rule 검증 규칙명
   * @param defaultMessage 기본 메시지
   * @param args 메시지 파라미터
   * @return 국제화된 검증 오류 메시지 또는 기본 메시지
   */
  public String getValidationMessageWithDefault(String field, String rule, String defaultMessage, Object... args) {
    String code = String.format("validation.%s.%s", field, rule);
    return getMessageWithDefault(code, defaultMessage, args);
  }

  /**
   * 일반적인 검증 규칙 메시지를 생성 (validation.common 네임스페이스)
   *
   * @param rule 검증 규칙명 (예: "required", "email", "min.length")
   * @param args 메시지 파라미터
   * @return 국제화된 일반 검증 메시지
   */
  public String getCommonValidationMessage(String rule, Object... args) {
    String code = String.format("validation.common.%s", rule);
    return getMessage(code, args);
  }

  /**
   * 일반적인 검증 규칙 메시지를 생성 (기본 메시지와 함께)
   *
   * @param rule 검증 규칙명
   * @param defaultMessage 기본 메시지
   * @param args 메시지 파라미터
   * @return 국제화된 일반 검증 메시지 또는 기본 메시지
   */
  public String getCommonValidationMessageWithDefault(String rule, String defaultMessage, Object... args) {
    String code = String.format("validation.common.%s", rule);
    return getMessageWithDefault(code, defaultMessage, args);
  }

  /**
   * 도메인별 검증 메시지를 생성
   *
   * @param domain 도메인명 (예: "user", "board", "card")
   * @param field 필드명
   * @param rule 검증 규칙명
   * @param args 메시지 파라미터
   * @return 국제화된 도메인별 검증 메시지
   */
  public String getDomainValidationMessage(String domain, String field, String rule, Object... args) {
    String code = String.format("validation.%s.%s.%s", domain, field, rule);
    return getMessage(code, args);
  }

  /**
   * 도메인별 검증 메시지를 생성 (기본 메시지와 함께)
   *
   * @param domain 도메인명
   * @param field 필드명
   * @param rule 검증 규칙명
   * @param defaultMessage 기본 메시지
   * @param args 메시지 파라미터
   * @return 국제화된 도메인별 검증 메시지 또는 기본 메시지
   */
  public String getDomainValidationMessageWithDefault(String domain, String field, String rule,
                                                      String defaultMessage, Object... args) {
    String code = String.format("validation.%s.%s.%s", domain, field, rule);
    return getMessageWithDefault(code, defaultMessage, args);
  }

  /**
   * 현재 로케일 반환
   */
  public Locale getCurrentLocale() {
    return LocaleContextHolder.getLocale();
  }
}

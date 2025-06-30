package com.boardly.shared.application.validation;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import io.vavr.control.Try;

@Component
public class ValidationMessageResolver {
  private final MessageSource messageSource;
  private static final ThreadLocal<Locale> CURRENT_LOCALE = ThreadLocal.withInitial(Locale::getDefault);

  public ValidationMessageResolver(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public static void setLocale(Locale locale) {
    CURRENT_LOCALE.set(locale);
  }

  public static void setLocale(String language) {
    CURRENT_LOCALE.set(Locale.forLanguageTag(language));
  }

  public static Locale getCurrentLocale() {
    return CURRENT_LOCALE.get();
  }
  
  public static void clearLocale() {
    CURRENT_LOCALE.remove();
  }

  /**
   * 메시지 키로 현지화된 메시지를 조회
   * 
   * @param key 메시지 키
   * @param args 메시지 인자
   * @return 현지화된 메시지
   */
  public String getMessage(String key, Object... args) {
    return Try.of(() -> messageSource.getMessage(key, args, getCurrentLocale()))
        .getOrElse(key);
  }

  /**
   * 필드 메시지 키로 현지화된 메시지를 조회
   * 
   * @param key 메시지 키
   * @param field 필드 이름
   * @param args 메시지 인자
   * @return 현지화된 메시지
   */
  public String getFieldMessage(String key, String field, Object... args) {
    Object[] newArgs = new Object[args.length + 1];
    newArgs[0] = field;
    System.arraycopy(args, 0, newArgs, 1, args.length);
    return getMessage(key, newArgs);
  }
}

package com.boardly.shared.application.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidationMessageResolver 테스트")
class ValidationMessageResolverTest {

  @Mock
  private MessageSource messageSource;

  private ValidationMessageResolver validationMessageResolver;

  @BeforeEach
  void setUp() {
    validationMessageResolver = new ValidationMessageResolver(messageSource);
  }

  @Nested
  @DisplayName("getMessage 메서드 테스트")
  class GetMessageTest {

    @Test
    @DisplayName("현재 로케일로 메시지를 가져올 수 있다")
    void getMessage_WithCurrentLocale_ShouldReturnMessage() {
      // given
      String code = "test.message";
      String expectedMessage = "테스트 메시지";
      when(messageSource.getMessage(eq(code), any(), eq(LocaleContextHolder.getLocale())))
          .thenReturn(expectedMessage);

      // when
      String result = validationMessageResolver.getMessage(code);

      // then
      assertThat(result).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("파라미터와 함께 메시지를 가져올 수 있다")
    void getMessage_WithParameters_ShouldReturnMessage() {
      // given
      String code = "test.message";
      Object[] args = { "파라미터1", "파라미터2" };
      String expectedMessage = "테스트 메시지: 파라미터1, 파라미터2";
      when(messageSource.getMessage(eq(code), eq(args), eq(LocaleContextHolder.getLocale())))
          .thenReturn(expectedMessage);

      // when
      String result = validationMessageResolver.getMessage(code, args);

      // then
      assertThat(result).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("메시지 코드가 없으면 코드를 그대로 반환한다")
    void getMessage_WhenMessageNotFound_ShouldReturnCode() {
      // given
      String code = "nonexistent.message";
      when(messageSource.getMessage(eq(code), any(), eq(LocaleContextHolder.getLocale())))
          .thenThrow(new org.springframework.context.NoSuchMessageException(code));

      // when
      String result = validationMessageResolver.getMessage(code);

      // then
      assertThat(result).isEqualTo(code);
    }
  }

  @Nested
  @DisplayName("getMessage with Locale 메서드 테스트")
  class GetMessageWithLocaleTest {

    @Test
    @DisplayName("지정된 로케일로 메시지를 가져올 수 있다")
    void getMessage_WithSpecificLocale_ShouldReturnMessage() {
      // given
      String code = "test.message";
      Locale locale = Locale.KOREAN;
      String expectedMessage = "테스트 메시지";
      when(messageSource.getMessage(eq(code), any(), eq(locale)))
          .thenReturn(expectedMessage);

      // when
      String result = validationMessageResolver.getMessage(code, locale);

      // then
      assertThat(result).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("지정된 로케일과 파라미터로 메시지를 가져올 수 있다")
    void getMessage_WithLocaleAndParameters_ShouldReturnMessage() {
      // given
      String code = "test.message";
      Locale locale = Locale.ENGLISH;
      Object[] args = { "param1", "param2" };
      String expectedMessage = "Test message: param1, param2";
      when(messageSource.getMessage(eq(code), eq(args), eq(locale)))
          .thenReturn(expectedMessage);

      // when
      String result = validationMessageResolver.getMessage(code, locale, args);

      // then
      assertThat(result).isEqualTo(expectedMessage);
    }
  }

  @Nested
  @DisplayName("getMessageWithDefault 메서드 테스트")
  class GetMessageWithDefaultTest {

    @Test
    @DisplayName("메시지 코드가 있으면 해당 메시지를 반환한다")
    void getMessageWithDefault_WhenMessageExists_ShouldReturnMessage() {
      // given
      String code = "test.message";
      String defaultMessage = "기본 메시지";
      String expectedMessage = "테스트 메시지";
      when(messageSource.getMessage(eq(code), any(), eq(defaultMessage), eq(LocaleContextHolder.getLocale())))
          .thenReturn(expectedMessage);

      // when
      String result = validationMessageResolver.getMessageWithDefault(code, defaultMessage);

      // then
      assertThat(result).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("메시지 코드가 없으면 기본 메시지를 반환한다")
    void getMessageWithDefault_WhenMessageNotFound_ShouldReturnDefaultMessage() {
      // given
      String code = "nonexistent.message";
      String defaultMessage = "기본 메시지";
      when(messageSource.getMessage(eq(code), any(), eq(defaultMessage), eq(LocaleContextHolder.getLocale())))
          .thenReturn(defaultMessage);

      // when
      String result = validationMessageResolver.getMessageWithDefault(code, defaultMessage);

      // then
      assertThat(result).isEqualTo(defaultMessage);
    }

    @Test
    @DisplayName("파라미터와 함께 기본 메시지를 가져올 수 있다")
    void getMessageWithDefault_WithParameters_ShouldReturnMessage() {
      // given
      String code = "test.message";
      String defaultMessage = "기본 메시지: {0}, {1}";
      Object[] args = { "파라미터1", "파라미터2" };
      String expectedMessage = "테스트 메시지: 파라미터1, 파라미터2";
      when(messageSource.getMessage(eq(code), eq(args), eq(defaultMessage), eq(LocaleContextHolder.getLocale())))
          .thenReturn(expectedMessage);

      // when
      String result = validationMessageResolver.getMessageWithDefault(code, defaultMessage, args);

      // then
      assertThat(result).isEqualTo(expectedMessage);
    }
  }

  @Nested
  @DisplayName("getValidationMessage 메서드 테스트")
  class GetValidationMessageTest {

    @Test
    @DisplayName("필드와 규칙으로 검증 메시지를 생성할 수 있다")
    void getValidationMessage_WithFieldAndRule_ShouldReturnValidationMessage() {
      // given
      String field = "email";
      String rule = "required";
      String expectedCode = "validation.email.required";
      String expectedMessage = "이메일은 필수입니다";
      when(messageSource.getMessage(eq(expectedCode), any(), eq(LocaleContextHolder.getLocale())))
          .thenReturn(expectedMessage);

      // when
      String result = validationMessageResolver.getValidationMessage(field, rule);

      // then
      assertThat(result).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("파라미터와 함께 검증 메시지를 생성할 수 있다")
    void getValidationMessage_WithParameters_ShouldReturnValidationMessage() {
      // given
      String field = "password";
      String rule = "min.length";
      Object[] args = { 8 };
      String expectedCode = "validation.password.min.length";
      String expectedMessage = "비밀번호는 최소 8자 이상이어야 합니다";
      when(messageSource.getMessage(eq(expectedCode), eq(args), eq(LocaleContextHolder.getLocale())))
          .thenReturn(expectedMessage);

      // when
      String result = validationMessageResolver.getValidationMessage(field, rule, args);

      // then
      assertThat(result).isEqualTo(expectedMessage);
    }
  }

  @Nested
  @DisplayName("getValidationMessageWithDefault 메서드 테스트")
  class GetValidationMessageWithDefaultTest {

    @Test
    @DisplayName("기본 메시지와 함께 검증 메시지를 생성할 수 있다")
    void getValidationMessageWithDefault_ShouldReturnValidationMessage() {
      // given
      String field = "username";
      String rule = "unique";
      String defaultMessage = "사용자명이 이미 존재합니다";
      String expectedCode = "validation.username.unique";
      String expectedMessage = "이미 사용 중인 사용자명입니다";
      when(messageSource.getMessage(eq(expectedCode), any(), eq(defaultMessage), eq(LocaleContextHolder.getLocale())))
          .thenReturn(expectedMessage);

      // when
      String result = validationMessageResolver.getValidationMessageWithDefault(field, rule, defaultMessage);

      // then
      assertThat(result).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("메시지가 없으면 기본 메시지를 반환한다")
    void getValidationMessageWithDefault_WhenMessageNotFound_ShouldReturnDefaultMessage() {
      // given
      String field = "nonexistent";
      String rule = "rule";
      String defaultMessage = "기본 검증 메시지";
      String expectedCode = "validation.nonexistent.rule";
      when(messageSource.getMessage(eq(expectedCode), any(), eq(defaultMessage), eq(LocaleContextHolder.getLocale())))
          .thenReturn(defaultMessage);

      // when
      String result = validationMessageResolver.getValidationMessageWithDefault(field, rule, defaultMessage);

      // then
      assertThat(result).isEqualTo(defaultMessage);
    }
  }

  @Nested
  @DisplayName("getCommonValidationMessage 메서드 테스트")
  class GetCommonValidationMessageTest {

    @Test
    @DisplayName("일반적인 검증 규칙 메시지를 생성할 수 있다")
    void getCommonValidationMessage_ShouldReturnCommonValidationMessage() {
      // given
      String rule = "required";
      String expectedCode = "validation.common.required";
      String expectedMessage = "필수 입력 항목입니다";
      when(messageSource.getMessage(eq(expectedCode), any(), eq(LocaleContextHolder.getLocale())))
          .thenReturn(expectedMessage);

      // when
      String result = validationMessageResolver.getCommonValidationMessage(rule);

      // then
      assertThat(result).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("복합 규칙으로 일반적인 검증 메시지를 생성할 수 있다")
    void getCommonValidationMessage_WithComplexRule_ShouldReturnCommonValidationMessage() {
      // given
      String rule = "min.length";
      Object[] args = { 5 };
      String expectedCode = "validation.common.min.length";
      String expectedMessage = "최소 5자 이상이어야 합니다";
      when(messageSource.getMessage(eq(expectedCode), eq(args), eq(LocaleContextHolder.getLocale())))
          .thenReturn(expectedMessage);

      // when
      String result = validationMessageResolver.getCommonValidationMessage(rule, args);

      // then
      assertThat(result).isEqualTo(expectedMessage);
    }
  }

  @Nested
  @DisplayName("getCommonValidationMessageWithDefault 메서드 테스트")
  class GetCommonValidationMessageWithDefaultTest {

    @Test
    @DisplayName("기본 메시지와 함께 일반적인 검증 메시지를 생성할 수 있다")
    void getCommonValidationMessageWithDefault_ShouldReturnCommonValidationMessage() {
      // given
      String rule = "email";
      String defaultMessage = "올바른 이메일 형식이 아닙니다";
      String expectedCode = "validation.common.email";
      String expectedMessage = "유효한 이메일 주소를 입력해주세요";
      when(messageSource.getMessage(eq(expectedCode), any(), eq(defaultMessage), eq(LocaleContextHolder.getLocale())))
          .thenReturn(expectedMessage);

      // when
      String result = validationMessageResolver.getCommonValidationMessageWithDefault(rule, defaultMessage);

      // then
      assertThat(result).isEqualTo(expectedMessage);
    }
  }

  @Nested
  @DisplayName("getDomainValidationMessage 메서드 테스트")
  class GetDomainValidationMessageTest {

    @Test
    @DisplayName("도메인별 검증 메시지를 생성할 수 있다")
    void getDomainValidationMessage_ShouldReturnDomainValidationMessage() {
      // given
      String domain = "user";
      String field = "email";
      String rule = "duplicate";
      String expectedCode = "validation.user.email.duplicate";
      String expectedMessage = "이미 등록된 이메일입니다";
      when(messageSource.getMessage(eq(expectedCode), any(), eq(LocaleContextHolder.getLocale())))
          .thenReturn(expectedMessage);

      // when
      String result = validationMessageResolver.getDomainValidationMessage(domain, field, rule);

      // then
      assertThat(result).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("파라미터와 함께 도메인별 검증 메시지를 생성할 수 있다")
    void getDomainValidationMessage_WithParameters_ShouldReturnDomainValidationMessage() {
      // given
      String domain = "board";
      String field = "title";
      String rule = "max.length";
      Object[] args = { 100 };
      String expectedCode = "validation.board.title.max.length";
      String expectedMessage = "보드 제목은 최대 100자까지 입력 가능합니다";
      when(messageSource.getMessage(eq(expectedCode), eq(args), eq(LocaleContextHolder.getLocale())))
          .thenReturn(expectedMessage);

      // when
      String result = validationMessageResolver.getDomainValidationMessage(domain, field, rule, args);

      // then
      assertThat(result).isEqualTo(expectedMessage);
    }
  }

  @Nested
  @DisplayName("getDomainValidationMessageWithDefault 메서드 테스트")
  class GetDomainValidationMessageWithDefaultTest {

    @Test
    @DisplayName("기본 메시지와 함께 도메인별 검증 메시지를 생성할 수 있다")
    void getDomainValidationMessageWithDefault_ShouldReturnDomainValidationMessage() {
      // given
      String domain = "card";
      String field = "description";
      String rule = "required";
      String defaultMessage = "카드 설명은 필수입니다";
      String expectedCode = "validation.card.description.required";
      String expectedMessage = "카드에 대한 설명을 입력해주세요";
      when(messageSource.getMessage(eq(expectedCode), any(), eq(defaultMessage), eq(LocaleContextHolder.getLocale())))
          .thenReturn(expectedMessage);

      // when
      String result = validationMessageResolver.getDomainValidationMessageWithDefault(domain, field, rule,
          defaultMessage);

      // then
      assertThat(result).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("파라미터와 함께 기본 메시지가 있는 도메인별 검증 메시지를 생성할 수 있다")
    void getDomainValidationMessageWithDefault_WithParameters_ShouldReturnDomainValidationMessage() {
      // given
      String domain = "boardlist";
      String field = "name";
      String rule = "min.length";
      String defaultMessage = "리스트 이름은 최소 {0}자 이상이어야 합니다";
      Object[] args = { 2 };
      String expectedCode = "validation.boardlist.name.min.length";
      String expectedMessage = "리스트 이름은 최소 2자 이상이어야 합니다";
      when(
          messageSource.getMessage(eq(expectedCode), eq(args), eq(defaultMessage), eq(LocaleContextHolder.getLocale())))
          .thenReturn(expectedMessage);

      // when
      String result = validationMessageResolver.getDomainValidationMessageWithDefault(domain, field, rule,
          defaultMessage, args);

      // then
      assertThat(result).isEqualTo(expectedMessage);
    }
  }

  @Nested
  @DisplayName("getCurrentLocale 메서드 테스트")
  class GetCurrentLocaleTest {

    @Test
    @DisplayName("현재 로케일을 반환한다")
    void getCurrentLocale_ShouldReturnCurrentLocale() {
      // when
      Locale result = validationMessageResolver.getCurrentLocale();

      // then
      assertThat(result).isEqualTo(LocaleContextHolder.getLocale());
    }
  }

  @Nested
  @DisplayName("예외 처리 테스트")
  class ExceptionHandlingTest {

    @Test
    @DisplayName("MessageSource에서 예외가 발생해도 안전하게 처리한다")
    void getMessage_WhenExceptionOccurs_ShouldHandleSafely() {
      // given
      String code = "test.message";
      when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
          .thenThrow(new RuntimeException("Unexpected error"));

      // when
      String result = validationMessageResolver.getMessage(code);

      // then
      assertThat(result).isEqualTo(code);
    }

    @Test
    @DisplayName("MessageSource에서 예외가 발생해도 기본 메시지로 안전하게 처리한다")
    void getMessageWithDefault_WhenExceptionOccurs_ShouldReturnDefaultMessage() {
      // given
      String code = "test.message";
      String defaultMessage = "기본 메시지";
      when(messageSource.getMessage(anyString(), any(), anyString(), any(Locale.class)))
          .thenThrow(new RuntimeException("Unexpected error"));

      // when
      String result = validationMessageResolver.getMessageWithDefault(code, defaultMessage);

      // then
      assertThat(result).isEqualTo(defaultMessage);
    }
  }
}
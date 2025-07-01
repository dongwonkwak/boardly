package com.boardly.shared.application.validation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidationMessageResolver 단위 테스트")
class ValidationMessageResolverTest {

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private ValidationMessageResolver messageResolver;

    private final Locale KOREAN = Locale.KOREAN;

    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(KOREAN);
    }

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Nested
    @DisplayName("getMessage 메서드는")
    class Describe_getMessage {

        @Test
        @DisplayName("현재 로케일에 맞는 메시지를 반환한다")
        void withCurrentLocale_returnsLocalizedMessage() {
            String code = "test.code";
            Object[] args = {"arg1"};
            String expectedMessage = "테스트 메시지";

            when(messageSource.getMessage(code, args, KOREAN)).thenReturn(expectedMessage);

            String actualMessage = messageResolver.getMessage(code, args);

            assertThat(actualMessage).isEqualTo(expectedMessage);
        }

        @Test
        @DisplayName("지정된 로케일에 맞는 메시지를 반환한다")
        void withSpecificLocale_returnsLocalizedMessage() {
            String code = "test.code";
            Object[] args = {"arg1"};
            String expectedMessage = "Test Message";
            Locale specificLocale = Locale.ENGLISH;

            when(messageSource.getMessage(code, args, specificLocale)).thenReturn(expectedMessage);

            String actualMessage = messageResolver.getMessage(code, specificLocale, args);

            assertThat(actualMessage).isEqualTo(expectedMessage);
        }

        @Test
        @DisplayName("메시지를 찾지 못하면 코드를 그대로 반환한다")
        void whenMessageNotFound_returnsCode() {
            String code = "non.existent.code";
            Object[] args = {};

            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenThrow(new NoSuchMessageException(code, KOREAN));

            String actualMessage = messageResolver.getMessage(code, args);

            assertThat(actualMessage).isEqualTo(code);
        }
    }
    
    @Nested
    @DisplayName("getMessageWithDefault 메서드는")
    class Describe_getMessageWithDefault {

        @Test
        @DisplayName("메시지를 찾으면 현지화된 메시지를 반환한다")
        void whenMessageFound_returnsLocalizedMessage() {
            String code = "test.code";
            String defaultMessage = "Default";
            Object[] args = {};
            String expectedMessage = "테스트 메시지";

            when(messageSource.getMessage(code, args, defaultMessage, KOREAN)).thenReturn(expectedMessage);

            String actualMessage = messageResolver.getMessageWithDefault(code, defaultMessage, args);
            
            assertThat(actualMessage).isEqualTo(expectedMessage);
        }

        @Test
        @DisplayName("메시지를 찾지 못하면 기본 메시지를 반환한다")
        void whenMessageNotFound_returnsDefaultMessage() {
            String code = "non.existent.code";
            String defaultMessage = "기본 메시지";
            Object[] args = {};

            when(messageSource.getMessage(code, args, defaultMessage, KOREAN)).thenThrow(new NoSuchMessageException(code, KOREAN));

            String actualMessage = messageResolver.getMessageWithDefault(code, defaultMessage, args);
            
            assertThat(actualMessage).isEqualTo(defaultMessage);
        }
    }

    @Nested
    @DisplayName("getValidationMessage 메서드는")
    class Describe_getValidationMessage {

        @Test
        @DisplayName("올바른 형식의 검증 메시지 코드를 생성하고 메시지를 반환한다")
        void returnsValidationMessage() {
            String field = "email";
            String rule = "required";
            String expectedCode = "validation.email.required";
            String expectedMessage = "이메일은 필수입니다.";
            
            when(messageSource.getMessage(expectedCode, new Object[]{}, KOREAN)).thenReturn(expectedMessage);

            String actualMessage = messageResolver.getValidationMessage(field, rule);

            assertThat(actualMessage).isEqualTo(expectedMessage);
        }
    }
    
    @Nested
    @DisplayName("getCommonValidationMessage 메서드는")
    class Describe_getCommonValidationMessage {

        @Test
        @DisplayName("올바른 형식의 공통 검증 메시지 코드를 생성하고 메시지를 반환한다")
        void returnsCommonValidationMessage() {
            String rule = "min.length";
            Object[] args = {8};
            String expectedCode = "validation.common.min.length";
            String expectedMessage = "최소 8자 이상이어야 합니다.";
            
            when(messageSource.getMessage(expectedCode, args, KOREAN)).thenReturn(expectedMessage);

            String actualMessage = messageResolver.getCommonValidationMessage(rule, args);

            assertThat(actualMessage).isEqualTo(expectedMessage);
        }
    }
    
    @Nested
    @DisplayName("getDomainValidationMessage 메서드는")
    class Describe_getDomainValidationMessage {

        @Test
        @DisplayName("올바른 형식의 도메인 검증 메시지 코드를 생성하고 메시지를 반환한다")
        void returnsDomainValidationMessage() {
            String domain = "user";
            String field = "password";
            String rule = "strong";
            String expectedCode = "validation.user.password.strong";
            String expectedMessage = "비밀번호는 더 강력해야 합니다.";

            when(messageSource.getMessage(expectedCode, new Object[]{}, KOREAN)).thenReturn(expectedMessage);

            String actualMessage = messageResolver.getDomainValidationMessage(domain, field, rule);

            assertThat(actualMessage).isEqualTo(expectedMessage);
        }
    }

    @Test
    @DisplayName("getCurrentLocale은 LocaleContextHolder의 로케일을 반환한다")
    void getCurrentLocale_returnsLocaleFromContextHolder() {
        assertThat(messageResolver.getCurrentLocale()).isEqualTo(KOREAN);
        
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        
        assertThat(messageResolver.getCurrentLocale()).isEqualTo(Locale.ENGLISH);
    }
} 
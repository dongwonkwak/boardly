package com.boardly.shared.application.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidationMessageResolver 단위 테스트")
class ValidationMessageResolverTest {

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private ValidationMessageResolver validationMessageResolver;

    @BeforeEach
    void setUp() {
        ValidationMessageResolver.setLocale(Locale.KOREAN);
    }

    @AfterEach
    void tearDown() {
        ValidationMessageResolver.clearLocale();
    }

    @Test
    @DisplayName("setLocale과 getCurrentLocale이 올바르게 동작한다")
    void testSetAndGetCurrentLocale() {
        assertThat(ValidationMessageResolver.getCurrentLocale()).isEqualTo(Locale.KOREAN);

        ValidationMessageResolver.setLocale(Locale.ENGLISH);
        assertThat(ValidationMessageResolver.getCurrentLocale()).isEqualTo(Locale.ENGLISH);

        ValidationMessageResolver.setLocale("ja");
        assertThat(ValidationMessageResolver.getCurrentLocale()).isEqualTo(Locale.JAPANESE);
    }

    @Test
    @DisplayName("clearLocale이 로케일을 기본값으로 초기화한다")
    void testClearLocale() {
        Locale initialDefault = Locale.getDefault();
        
        ValidationMessageResolver.setLocale(Locale.GERMAN);
        assertThat(ValidationMessageResolver.getCurrentLocale()).isEqualTo(Locale.GERMAN);

        ValidationMessageResolver.clearLocale();
        // clearLocale()을 호출하면 ThreadLocal이 제거되므로, 다음 get()은 initialValue()를 호출합니다.
        // ThreadLocal.withInitial(Locale::getDefault) 이므로 기본 로케일로 설정됩니다.
        // 테스트 환경에 따라 기본 로케일이 다를 수 있으므로, 테스트 시작 전의 기본 로케일과 같은지만 확인합니다.
        assertThat(ValidationMessageResolver.getCurrentLocale()).isEqualTo(initialDefault);
    }

    @Test
    @DisplayName("getMessage가 현지화된 메시지를 반환한다")
    void testGetMessage() {
        String messageKey = "test.key";
        String expectedMessage = "테스트 메시지";
        Object[] args = { "arg1" };

        when(messageSource.getMessage(eq(messageKey), eq(args), eq(Locale.KOREAN)))
            .thenReturn(expectedMessage);

        String actualMessage = validationMessageResolver.getMessage(messageKey, args);

        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("getMessage가 메시지를 찾지 못하면 키를 그대로 반환한다")
    void testGetMessageNotFound() {
        String messageKey = "non.existent.key";
        Object[] args = { "arg1" };

        when(messageSource.getMessage(eq(messageKey), eq(args), any(Locale.class)))
            .thenThrow(new NoSuchMessageException(messageKey, Locale.KOREAN));

        String actualMessage = validationMessageResolver.getMessage(messageKey, args);

        assertThat(actualMessage).isEqualTo(messageKey);
    }

    @Test
    @DisplayName("getFieldMessage가 필드명을 포함한 현지화된 메시지를 반환한다")
    void testGetFieldMessage() {
        String messageKey = "validation.required";
        String field = "username";
        String expectedMessage = "username은 필수입니다.";

        Object[] expectedArgs = { field };
        when(messageSource.getMessage(eq(messageKey), eq(expectedArgs), eq(Locale.KOREAN)))
            .thenReturn(expectedMessage);

        String actualMessage = validationMessageResolver.getFieldMessage(messageKey, field);

        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("getFieldMessage가 추가 인자를 포함하여 현지화된 메시지를 반환한다")
    void testGetFieldMessageWithArgs() {
        String messageKey = "validation.maxLength";
        String field = "password";
        int maxLength = 20;
        String expectedMessage = "password의 길이는 20자를 넘을 수 없습니다.";

        Object[] additionalArgs = { maxLength };
        Object[] expectedArgs = { field, maxLength };

        when(messageSource.getMessage(eq(messageKey), eq(expectedArgs), eq(Locale.KOREAN)))
            .thenReturn(expectedMessage);

        String actualMessage = validationMessageResolver.getFieldMessage(messageKey, field, additionalArgs);

        assertThat(actualMessage).isEqualTo(expectedMessage);
    }
} 
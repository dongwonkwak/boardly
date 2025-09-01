package com.boardly.infrastructure.message;

import com.boardly.application.port.out.MessageResolver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Locale;

/**
 * Spring MessageSource를 사용한 메시지 국제화 및 중첩 메시지 키 해석 구현체
 * 
 * 이 클래스는 MessageResolver 아웃바운드 포트의 구현체로,
 * Spring의 MessageSource를 사용하여 메시지 국제화를 처리합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class I18nMessageResolver implements MessageResolver {

    private final MessageSource messageSource;

    // 상수 정의
    private static final String DEFAULT_ERROR_MESSAGE = "오류가 발생했습니다.";
    private static final String LOG_MESSAGE_RETRIEVAL_ERROR = "Failed to retrieve message for key: {} - {}";

    @Override
    public String resolveMessage(String messageKey, Object... args) {
        return resolveMessage(messageKey, getCurrentLocale(), args);
    }

    @Override
    public String resolveMessage(String messageKey, Locale locale, Object... args) {
        if (messageKey == null || messageKey.isEmpty()) {
            return DEFAULT_ERROR_MESSAGE;
        }

        try {
            // args에서 메시지 키를 해결하여 실제 메시지로 변환
            Object[] resolvedArgs = resolveMessageArgs(args, locale);
            return messageSource.getMessage(messageKey, resolvedArgs, messageKey, locale);
        } catch (Exception e) {
            log.warn(LOG_MESSAGE_RETRIEVAL_ERROR, messageKey, e.getMessage());
            return messageKey; // messageKey를 fallback으로 사용
        }
    }

    @Override
    public Locale getCurrentLocale() {
        try {
            var requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
                var request = servletRequestAttributes.getRequest();

                if (request != null) {
                    // LocaleResolver를 통해 locale 조회 (Spring이 자동으로 관리)
                    // LocaleChangeInterceptor가 이미 locale을 설정했을 것이므로
                    // LocaleContextHolder에서 가져오는 것이 더 효율적
                    return LocaleContextHolder.getLocale();
                }
            }
        } catch (Exception e) {
            log.debug("Failed to resolve locale from request, using fallback: {}", e.getMessage());
        }

        // Fallback: LocaleContextHolder 사용
        return LocaleContextHolder.getLocale();
    }

    /**
     * 메시지 인자에서 중첩된 메시지 키들을 해결합니다.
     */
    private Object[] resolveMessageArgs(Object[] args, Locale locale) {
        if (args == null || args.length == 0) {
            return new Object[0];
        }

        Object[] resolvedArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof String stringArg && isNestedMessageKey(stringArg)) {
                // 중첩된 메시지 키를 실제 메시지로 변환
                try {
                    resolvedArgs[i] = messageSource.getMessage(stringArg, null, stringArg, locale);
                } catch (Exception e) {
                    log.debug("Failed to resolve nested message key: {}", stringArg);
                    resolvedArgs[i] = stringArg; // fallback to original
                }
            } else {
                resolvedArgs[i] = arg;
            }
        }
        return resolvedArgs;
    }

    /**
     * 문자열이 중첩 메시지 키인지 확인합니다.
     * 현재는 field.xxx 패턴만 지원하지만, 향후 확장 가능
     */
    private boolean isNestedMessageKey(String value) {
        return value.startsWith("field.") ||
                value.startsWith("error.") ||
                value.startsWith("validation.") ||
                value.startsWith("message.");
    }
}

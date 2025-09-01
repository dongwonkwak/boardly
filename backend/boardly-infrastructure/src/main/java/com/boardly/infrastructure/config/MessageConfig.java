package com.boardly.infrastructure.config;

import org.springframework.core.env.Environment;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Arrays;
import java.util.Locale;

@Configuration
public class MessageConfig {

    private final Environment environment;

    public MessageConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames(
                "messages/ValidationMessages",
                "messages/messages");
        // 기본 인코딩 설정(UTF-8)
        messageSource.setDefaultEncoding("UTF-8");
        // 기본 로케일 설정(영어)
        messageSource.setDefaultLocale(Locale.US);
        // 메시지가 없을 때 코드 자체를 반환
        messageSource.setUseCodeAsDefaultMessage(true);

        // 개발환경에서는 캐시 1시간, 운영환경에서는 캐시 없음
        messageSource.setCacheSeconds(isProduction() ? -1 : 3600);

        return messageSource;
    }

    private boolean isProduction() {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }
}

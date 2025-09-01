package com.boardly.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * 웹 설정 클래스
 * 
 * 헥사고날 아키텍처에서 웹 계층의 설정을 담당합니다.
 * - Locale 관리 설정
 * - 인터셉터 설정
 * - 기타 웹 관련 설정
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * LocaleResolver Bean 등록
     * 
     * 세션 기반으로 locale을 관리하며, 기본 locale은 한국어로 설정합니다.
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.KOREAN);
        return resolver;
    }

    /**
     * LocaleChangeInterceptor Bean 등록
     * 
     * URL 파라미터를 통해 언어 변경을 처리합니다.
     * 예: /api/boards?lang=en
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    /**
     * 인터셉터 등록
     * 
     * LocaleChangeInterceptor를 모든 요청에 적용합니다.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}

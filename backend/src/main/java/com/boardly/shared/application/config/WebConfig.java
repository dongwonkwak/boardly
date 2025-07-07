package com.boardly.shared.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 로케일 리졸버 설정 (세션 기반)
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(Locale.KOREAN); // 기본 로케일을 한국어로 설정
        return localeResolver;
    }

    /**
     * 로케일 변경 인터셉터 설정
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang"); // URL 파라미터로 lang=en, lang=ko 사용
        return interceptor;
    }

    /**
     * 인터셉터 등록
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    /**
     * 뷰 컨트롤러 설정 (간단한 매핑)
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 루트 경로를 로그인 페이지로 리다이렉트
        registry.addRedirectViewController("/", "/login");
    }
} 
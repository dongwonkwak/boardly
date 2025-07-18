package com.boardly.shared.application.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.Instant;

/**
 * Jackson JSON 직렬화/역직렬화 설정
 * UTC 기준 시간 처리와 API 응답 형식을 표준화
 */
@Configuration
public class JacksonConfig {

    /**
     * 메인 ObjectMapper 설정
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder
                .json()
                .modules(javaTimeModule())
                // 날짜를 타임스탬프 숫자로 직렬화하지 않음
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                // 알 수 없는 속성이 있어도 역직렬화 실패하지 않음
                .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                // 프로퍼티 네이밍 전략 (camelCase 유지)
                .propertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
                .build();
    }

    /**
     * Java Time 모듈 설정
     * Instant는 ISO-8601 UTC 형식으로 직렬화됩니다. (예: 2024-01-15T10:30:45.123Z)
     */
    @Bean
    public JavaTimeModule javaTimeModule() {
        JavaTimeModule module = new JavaTimeModule();
        
        // Instant 직렬화: ISO-8601 UTC 형식 (2024-01-15T10:30:45.123Z)
        module.addSerializer(Instant.class, InstantSerializer.INSTANCE);
        module.addDeserializer(Instant.class, InstantDeserializer.INSTANT);
        
        return module;
    }
}

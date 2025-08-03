package com.boardly.shared.application.config.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /**
     * CORS 설정
     */
    private Cors cors = new Cors();

    /**
     * 프론트엔드 설정
     */
    private Frontend frontend = new Frontend();

    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOrigins;
    }

    @Getter
    @Setter
    public static class Frontend {
        /**
         * 프론트엔드 애플리케이션 URL
         */
        private String url = "http://localhost:5173";
    }
}
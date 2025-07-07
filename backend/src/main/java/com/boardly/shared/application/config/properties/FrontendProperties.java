package com.boardly.shared.application.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Getter @Setter
@Component
@ConfigurationProperties(prefix = "app.frontend")
public class FrontendProperties {

    /**
     * 프론트엔드 애플리케이션 URL
     */
    private String url = "http://localhost:5173";
} 
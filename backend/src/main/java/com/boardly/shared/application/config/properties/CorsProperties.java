package com.boardly.shared.application.config.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@ConfigurationProperties(prefix = "app.cors")
@Component
public class CorsProperties {
  private List<String> allowedOrigins;
}

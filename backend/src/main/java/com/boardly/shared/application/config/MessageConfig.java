package com.boardly.shared.application.config;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class MessageConfig {
  
  @Bean
  public MessageSource messageSource() {
    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    messageSource.setBasenames(
            "messages/ValidationMessages",
            "messages/messages"
            );
    messageSource.setDefaultEncoding("UTF-8");
    messageSource.setDefaultLocale(Locale.US);
    return messageSource;
  }
}

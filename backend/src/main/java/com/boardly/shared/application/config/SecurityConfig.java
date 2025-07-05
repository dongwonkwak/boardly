package com.boardly.shared.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.boardly.shared.presentation.Path;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final RequestMatcher[] PUBLIC_MATCHERS = {
            PathPatternRequestMatcher.withDefaults().matcher("/login"),
            PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/.well-known/appspecific/com.chrome.devtools.json"),
            // register user
            PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, Path.USERS + "/register"),
    };

    @Bean
    @Order(2)
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(PUBLIC_MATCHERS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PUBLIC_MATCHERS).permitAll()
                        .anyRequest().authenticated());

        return http.build();
    }
}

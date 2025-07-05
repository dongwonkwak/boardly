package com.boardly.shared.application.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.boardly.shared.presentation.Path;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    private static final RequestMatcher[] PUBLIC_MATCHERS = {
            PathPatternRequestMatcher.withDefaults().matcher("/login"),
            PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/.well-known/appspecific/com.chrome.devtools.json"),
            // register user
            PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, Path.USERS + "/register"),
            PathPatternRequestMatcher.withDefaults().matcher("/api-docs/**"),
            PathPatternRequestMatcher.withDefaults().matcher("/swagger-ui/**"),
            PathPatternRequestMatcher.withDefaults().matcher("/swagger-ui.html")
    };

    @Bean
    @Order(2)
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                    .ignoringRequestMatchers(PUBLIC_MATCHERS))
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers(PUBLIC_MATCHERS).permitAll()
                    .anyRequest().authenticated())
            .cors(cors -> cors
                    .configurationSource(corsConfigurationSource))
            .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(Customizer.withDefaults()))
            ;

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}

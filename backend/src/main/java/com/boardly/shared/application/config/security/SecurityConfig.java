package com.boardly.shared.application.config.security;

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

import com.boardly.shared.application.config.security.handler.CustomAuthenticationFailureHandler;
import com.boardly.shared.presentation.Path;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final CustomAuthenticationProvider customAuthenticationProvider;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    private static final RequestMatcher[] PUBLIC_MATCHERS = {
        // register user
        PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, Path.USERS + "/register"),
    };

    @Bean
    @Order(2)
    SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                    .ignoringRequestMatchers(PUBLIC_MATCHERS))
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers(PUBLIC_MATCHERS).permitAll()
                    .requestMatchers(Path.PREFIX + "/**").authenticated()
                    .anyRequest().permitAll())
            .cors(cors -> cors
                    .configurationSource(corsConfigurationSource))
            .formLogin(form -> form
                .loginPage("/login")
                .failureHandler(customAuthenticationFailureHandler)
        .       permitAll())
            .authenticationProvider(customAuthenticationProvider)
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

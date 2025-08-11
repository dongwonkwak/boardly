package com.boardly.infrastructure.config.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationFailureHandler
    extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException, ServletException {
        String errorMessage = switch (exception.getClass().getSimpleName()) {
            case "UsernameNotFoundException" -> "web.login.error.not.found.email";
            case "BadCredentialsException" -> "web.login.error.invalid.credentials";
            default -> "web.login.error.unknown";
        };

        setDefaultFailureUrl("/login?errorMessage=" + errorMessage);

        super.onAuthenticationFailure(request, response, exception);
    }
}

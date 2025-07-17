package com.boardly.shared.application.config.security;

import java.util.function.Function;

import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.boardly.features.user.application.usecase.GetUserUseCase;
import com.boardly.features.user.domain.model.UserId;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OidcUserInfoMapper implements Function<OidcUserInfoAuthenticationContext, OidcUserInfo> {

    private final GetUserUseCase getUserUseCase;

    @Override
    public OidcUserInfo apply(OidcUserInfoAuthenticationContext context) {
        return Try.of(() -> extractUserIdFromContext(context))
            .flatMap(userId -> Try.of(() -> getUserUseCase.get(new UserId(userId))))
            .fold(
                throwable -> {
                    log.error("OIDC UserInfo 조회 중 오류 발생: {}", throwable.getMessage(), throwable);
                    return createMinimalUserInfo("unknown");
                },
                userResult -> userResult.fold(
                    failure -> {
                        log.warn("사용자 정보 조회 실패: {}", failure.message());
                        return createMinimalUserInfo("unknown");
                    },
                    user -> OidcUserInfo.builder()
                        .subject(user.getUserId().getId())
                        .email(user.getEmail())
                        .name(user.getFullName())
                        .emailVerified(true)
                        .build()
                )
            );
    }

    private String extractUserIdFromContext(OidcUserInfoAuthenticationContext context) {
        var authentication = context.getAuthentication();
        if (!(authentication.getPrincipal() instanceof JwtAuthenticationToken jwtToken)) {
            throw new IllegalStateException("Expected JwtAuthenticationToken but got: " + 
                authentication.getPrincipal().getClass().getSimpleName());
        }
        return jwtToken.getToken().getSubject();
    }

    private OidcUserInfo createMinimalUserInfo(String subject) {
        return OidcUserInfo.builder()
            .subject(subject)
            .build();
    }
}

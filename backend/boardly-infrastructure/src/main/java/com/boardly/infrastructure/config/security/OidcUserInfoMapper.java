package com.boardly.infrastructure.config.security;

import com.boardly.features.user.domain.port.UserFinder;
import com.boardly.shared.common.value.UserId;
import io.vavr.control.Try;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OidcUserInfoMapper
    implements Function<OidcUserInfoAuthenticationContext, OidcUserInfo> {

    private final UserFinder userFinder;

    @Override
    public OidcUserInfo apply(OidcUserInfoAuthenticationContext context) {
        return Try.of(() -> extractUserIdFromContext(context))
            .flatMap(userId ->
                Try.of(() -> userFinder.findUserOrThrow(new UserId(userId)))
            )
            .fold(
                throwable -> {
                    log.warn(
                        "사용자 정보 조회 실패: {}",
                        throwable.getMessage()
                    );
                    return createMinimalUserInfo("unknown");
                },
                user ->
                    OidcUserInfo.builder()
                        .subject(user.getUserId().getId())
                        .email(user.getEmail())
                        .name(user.getFullName())
                        .emailVerified(true)
                        .build()
            );
    }

    private String extractUserIdFromContext(
        OidcUserInfoAuthenticationContext context
    ) {
        var authentication = context.getAuthentication();
        if (
            !(authentication.getPrincipal() instanceof
                JwtAuthenticationToken jwtToken)
        ) {
            throw new IllegalStateException(
                "Expected JwtAuthenticationToken but got: " +
                authentication.getPrincipal().getClass().getSimpleName()
            );
        }
        String userId = jwtToken.getToken().getSubject();
        log.info("Extracted user ID: {}", userId);
        return userId;
    }

    private OidcUserInfo createMinimalUserInfo(String subject) {
        return OidcUserInfo.builder().subject(subject).build();
    }
}

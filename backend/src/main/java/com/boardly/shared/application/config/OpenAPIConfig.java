package com.boardly.shared.application.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

  @Value("${boardly.app.version:1.0.0}")
  private String appVersion;

  @Value("${boardly.app-url:http://localhost:8080}")
  private String appUrl;

  @Value("${boardly.oauth2.authorization-uri:http://localhost:8080/oauth2/authorize}")
  private String authorizationUri;

  @Value("${boardly.oauth2.token-uri:http://localhost:8080/oauth2/token}")
  private String tokenUri;

  @Bean
  public OpenAPI boardlyOpenAPI() {
      return new OpenAPI()
              .info(new Info()
                      .title("Boardly API")
                      .description("칸반 보드 스타일의 작업 관리 웹 애플리케이션 API")
                      .version(appVersion)
                      .license(new License()
                              .name("MIT License")
                              .url("https://opensource.org/licenses/MIT")))

              .servers(List.of(new Server().url(appUrl)))
              .components(new Components()
                .addSecuritySchemes("oauth2", 
                  new SecurityScheme()
                    .type(SecurityScheme.Type.OAUTH2)
                    .flows(new OAuthFlows()
                      .authorizationCode(new OAuthFlow()
                        .authorizationUrl(authorizationUri)
                        .tokenUrl(tokenUri)
                        .scopes(new Scopes()
                        .addString("read", "Read access")
                        .addString("write", "Write access")
                        .addString("openid", "OpenID Connect"))))))
                .addSecurityItem(new SecurityRequirement().addList("oauth2", List.of("read", "write", "openid")))
              ;
  }
  
}

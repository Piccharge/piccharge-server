package com.pohyoja.picchargeserver.config.application;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Value("${server.dev.url}")
    private String devServerUrl;
    private static final String SECURITY_SCHEME_NAME = "Authorization";

    @Bean
    public OpenAPI customOpenAPI() {
        Info info = new Info()
                .title("Piccharge Server API")
                .version("1.0.0")
                .description("PicCharge 서버의 API 문서입니다.");

        List<Server> servers = List.of(
                new Server().url("http://localhost:8080").description("로컬 테스트 서버 (HTTP)"),
                new Server().url(devServerUrl).description("원격 개발 서버 (HTTPS)"));

        SecurityRequirement securityRequirement = new SecurityRequirement().addList(SECURITY_SCHEME_NAME);
        Components components = new Components().addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT (Firebase Auth - ID Token)")
        );

        return new OpenAPI()
                .servers(servers)
                .info(info)
                .components(components)
                .security(List.of(securityRequirement));
    }
}
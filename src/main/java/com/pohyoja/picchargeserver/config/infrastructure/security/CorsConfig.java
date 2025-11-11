package com.pohyoja.picchargeserver.config.infrastructure.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 요청 경로에 대해
                .allowedOrigins("*") // 모든 Origin 허용 (필요 시 특정 도메인으로 제한 가능)
                .allowedMethods("*") // GET, POST, PUT, DELETE 등 모두 허용
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(false); // 토큰 기반 인증이면 false
    }
}

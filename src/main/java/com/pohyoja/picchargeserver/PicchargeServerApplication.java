package com.pohyoja.picchargeserver;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableJpaAuditing
public class PicchargeServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(PicchargeServerApplication.class, args);
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

package com.vcs.conductor.config;

import com.vcs.flowpilot.action.database.api.DatabaseApi;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class ApplicationConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public DatabaseApi databaseApi() {
        return new DatabaseApi();
    }

    @Bean
    RestTemplate restTemplate(RestTemplateBuilder b) {
        return b
                .connectTimeout(Duration.ofSeconds(2))
                .readTimeout(Duration.ofSeconds(5))
                .build();
    }
}

package com.conductor.core.config;

import org.modelmapper.ModelMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
class ApplicationConfiguration {
    @Bean
    RestTemplate restTemplate(RestTemplateBuilder b) {
        return b
                .connectTimeout(Duration.ofSeconds(2))
                .readTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Bean
    ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
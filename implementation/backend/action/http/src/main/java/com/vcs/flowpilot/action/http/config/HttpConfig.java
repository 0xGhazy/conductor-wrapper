package com.vcs.flowpilot.action.http.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
class HttpConfig {
    @Bean WebClient webClient(WebClient.Builder b) { return b.build(); }
}

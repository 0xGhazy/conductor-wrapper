package com.vcs.flowpilot.action.http;

import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.vcs.flowpilot.action.http.repository")
@EntityScan(basePackages = "com.vcs.flowpilot.action.http.entity")
public class HttpApplication {

	public static void main(String[] args) {
        MDC.put("action", "http");
		SpringApplication.run(HttpApplication.class, args);
	}

}

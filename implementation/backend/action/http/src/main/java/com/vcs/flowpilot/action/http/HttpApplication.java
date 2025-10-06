package com.vcs.flowpilot.action.http;

import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HttpApplication {

	public static void main(String[] args) {
        MDC.put("action", "http");
		SpringApplication.run(HttpApplication.class, args);
	}

}

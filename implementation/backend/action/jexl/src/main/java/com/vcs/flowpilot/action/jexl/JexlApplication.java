package com.vcs.flowpilot.action.jexl;

import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JexlApplication {

	public static void main(String[] args) {
        MDC.put("action", "jexl");
        MDC.put("traceId", "INTERNAL_OPS");
		SpringApplication.run(JexlApplication.class, args);
	}

}

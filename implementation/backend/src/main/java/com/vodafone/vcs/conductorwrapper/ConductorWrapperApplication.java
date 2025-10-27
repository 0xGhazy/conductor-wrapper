package com.vodafone.vcs.conductorwrapper;

import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.UUID;

@EnableScheduling
@SpringBootApplication
public class ConductorWrapperApplication {

	public static void main(String[] args) {
        MDC.put("traceId", UUID.randomUUID().toString());
        MDC.put("task", UUID.randomUUID().toString());
		SpringApplication.run(ConductorWrapperApplication.class, args);
	}

}

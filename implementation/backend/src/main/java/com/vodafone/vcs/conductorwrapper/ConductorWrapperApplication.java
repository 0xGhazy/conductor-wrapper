package com.vodafone.vcs.conductorwrapper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ConductorWrapperApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConductorWrapperApplication.class, args);
	}

}

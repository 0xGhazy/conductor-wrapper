package com.vcs.flowpilot.action.database;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@Log4j2
@SpringBootApplication
public class DatabaseActionApplication {

	public static void main(String[] args) {
        MDC.put("action", "database");
        MDC.put("traceId", "INTERNAL_OPS");
		SpringApplication.run(DatabaseActionApplication.class, args);
	}

}

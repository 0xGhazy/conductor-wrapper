package com.conductor.core;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.netflix.conductor.client.automator.TaskRunnerConfigurer;
import com.netflix.conductor.client.http.TaskClient;
import com.conductor.core.worker.GreetingWorker;

import java.util.Collections;

@Log4j2
@SpringBootApplication
public class CoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoreApplication.class, args);

		log.info("Starting conductor. . .");
		TaskClient taskClient = new TaskClient();
		taskClient.setRootURI("http://localhost:8080/api/");
		GreetingWorker greetingWorker = new GreetingWorker();
		TaskRunnerConfigurer runner = new TaskRunnerConfigurer
				.Builder(taskClient, Collections.singletonList(greetingWorker))
				.withThreadCount(1)
				.build();
		runner.init();
	}

}

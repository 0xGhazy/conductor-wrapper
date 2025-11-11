package com.vodafone.vcs.conductorwrapper.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.conductor.client.automator.TaskRunnerConfigurer;
import com.netflix.conductor.client.http.TaskClient;
import com.netflix.conductor.client.worker.Worker;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.HibernateValidator;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;
import java.util.List;

@Slf4j
@Configuration
public class BeansConfig {

    public ObjectMapper mapper() {
        return new ObjectMapper();
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    RestTemplate restTemplate(RestTemplateBuilder b) {
        return b
                .connectTimeout(Duration.ofSeconds(2))
                .readTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Bean
    public TaskClient taskClient() {
        TaskClient c = new TaskClient();
        c.setRootURI("http://localhost:8080/api/");
        return c;
    }

    @Bean
    WebClient webClient(WebClient.Builder b) {
        return b.build();
    }

    @Bean
    public TaskRunnerConfigurer taskRunnerConfigurer(TaskClient client, List<Worker> workers) {
        if (workers == null || workers.isEmpty())
            throw new IllegalStateException("No Conductor Worker beans found");

        int threads = Math.max(workers.size(), 2);
        TaskRunnerConfigurer cfg = new TaskRunnerConfigurer.Builder(client, workers)
                .withThreadCount(threads)
                .build();
        cfg.init();
        return cfg;
    }

    @Bean
    public org.springframework.validation.Validator validator() {
        var factory = Validation
                .byProvider(HibernateValidator.class)
                .configure()
                .ignoreXmlConfiguration()
                .buildValidatorFactory();
        Validator jakartaValidator = factory.getValidator();
        return new SpringValidatorAdapter(jakartaValidator);
    }

}

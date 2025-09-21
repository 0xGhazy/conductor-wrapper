package com.conductor.core.config;

import com.netflix.conductor.client.automator.TaskRunnerConfigurer;
import com.netflix.conductor.client.http.MetadataClient;
import com.netflix.conductor.client.http.TaskClient;
import com.netflix.conductor.client.worker.Worker;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ConductorConfig {

    @Bean
    TaskClient taskClient(@Value("${conductor.api.base.url}") String baseUrl) {
        TaskClient c = new TaskClient();
        c.setRootURI(baseUrl);
        return c;
    }

    @Bean(initMethod = "init", destroyMethod = "shutdown")
    TaskRunnerConfigurer taskRunnerConfigurer(TaskClient client, List<Worker> workers) {
        return new TaskRunnerConfigurer
                .Builder(client, workers)
                .withThreadCount(5)
                .build();
    }

    @Bean
    public Validator validator() {
        HibernateValidatorConfiguration cfg =
                Validation.byProvider(HibernateValidator.class).configure().ignoreXmlConfiguration();
        return cfg.buildValidatorFactory().getValidator();
    }
}
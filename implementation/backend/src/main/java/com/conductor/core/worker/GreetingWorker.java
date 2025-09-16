package com.conductor.core.worker;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;

@Log4j2
@Component
public class GreetingWorker implements Worker {

    @Override
    public String getTaskDefName() {
        return "greeting_task";
    }

    @Override
    public TaskResult execute(Task task) {
        System.out.print("Attempting to greet user");
        String name = (String) task.getInputData().getOrDefault("name", "Guest");
        System.out.println("Inputs: " + task.getInputData());

        String greeting = "Greetings, " + name + "!";

        TaskResult result = new TaskResult(task);
        result.setStatus(TaskResult.Status.COMPLETED);
        result.getOutputData().put("greeting", greeting);

        return result;
    }
}

package com.vodafone.vcs.conductorwrapper.common.contract;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.Map;

@Log4j2
public abstract class WrappedWorker implements Worker {

    private final String taskDefName;

    protected WrappedWorker(String taskDefName) {
        this.taskDefName = taskDefName;
    }

    @Override
    public final String getTaskDefName() {
        return taskDefName;
    }

    @Override
    public final TaskResult execute(Task task) {
        long startTime = System.currentTimeMillis();
        String wf = task.getWorkflowType().toUpperCase();

        MDC.put("task", taskDefName);
        try {
            log.info("==================== [{}][STARTED] ====================", wf);
            return doExecute(task);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("==================== [{}][FINISHED in {}ms] ====================", wf, duration);
        }
    }

    public abstract TaskResult doExecute(Task task) ;
}

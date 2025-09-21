package com.conductor.core.conductor.worker.contract;

import com.conductor.core.utils.IdGenerator;
import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;

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
        String traceId = IdGenerator.generateTraceId().substring(0, 7) + "/" + System.currentTimeMillis();
        MDC.put("traceId", traceId);
        MDC.put("task", taskDefName);
        try {
            log.info("==================== [{}][START] ====================", wf);
            log.info("INPUT: {}", task.getInputData());
            return doExecute(task);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("==================== [{}][FINISH in {}ms] ====================", wf, duration);
            MDC.clear();
        }
    }

    protected abstract TaskResult doExecute(Task task);
}

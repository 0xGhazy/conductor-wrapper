package com.vodafone.vcs.conductorwrapper.conductor.worker;

import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.vodafone.vcs.conductorwrapper.action.database.api.DatabaseActionApi;
import com.vodafone.vcs.conductorwrapper.action.database.dto.Query;
import com.vodafone.vcs.conductorwrapper.action.database.dto.QueryResult;
import com.vodafone.vcs.conductorwrapper.common.contract.WrappedWorker;
import com.vodafone.vcs.conductorwrapper.conductor.dto.ConductorWorkerResult;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Log4j2
@Component
public class DatabaseWorker extends WrappedWorker {

    private final DatabaseActionApi databaseAPI;

    public DatabaseWorker(DatabaseActionApi databaseAPI) {
        super("DatabaseWorker");
        this.databaseAPI = databaseAPI;
    }

    // TODO: recreate the DatabaseWorder task input and output structure and naming conventions
    @Override
    public TaskResult doExecute(Task task) {
        long startTime = System.currentTimeMillis();
        TaskResult result = new TaskResult(task);
        ConductorWorkerResult cr = ConductorWorkerResult.builder().build();
        log.info("Database action query execution request started");

        Map<String, Object> input = Map.of();
        Object payload = task.getInputData().get("data");

        if (payload instanceof Map<?, ?> m) {
            Map<String, Object> tmp = new java.util.HashMap<>();
            m.forEach((k, v) -> tmp.put(String.valueOf(k), v));
            input = java.util.Collections.unmodifiableMap(tmp);
        } else {
            result.getOutputData().put("result", "Missing required fields");
            result.setStatus(TaskResult.Status.COMPLETED);
            return result;
        }

        Map<String, Object> queryParams = (Map<String, Object>) input.get("params");
        log.info("worker parameters: {}", input);

        String queryId = input.get("queryId").toString();
        Query query = Query.builder()
                .id(queryId)
                .params(queryParams)
                .build();

        QueryResult qr = databaseAPI.run(query);

        cr.setResult(qr);
        cr.setTraceId(UUID.randomUUID().toString());
        cr.setTimestamp(System.currentTimeMillis());
        result.getOutputData().put("result", cr);
        result.setStatus(TaskResult.Status.COMPLETED);
        return result;
    }

}

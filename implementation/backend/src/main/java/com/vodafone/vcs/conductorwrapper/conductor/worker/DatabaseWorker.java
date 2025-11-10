package com.vodafone.vcs.conductorwrapper.conductor.worker;

import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.vodafone.vcs.conductorwrapper.action.database.dto.Query;
import com.vodafone.vcs.conductorwrapper.action.database.dto.QueryResult;
import com.vodafone.vcs.conductorwrapper.action.database.service.DatabaseService;
import com.vodafone.vcs.conductorwrapper.common.contract.WrappedWorker;
import lombok.Builder;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import java.util.Map;
import static com.vodafone.vcs.conductorwrapper.common.WorkerInputValidator.*;

@Log4j2
@Component
public class DatabaseWorker extends WrappedWorker {

    private final DatabaseService databaseService;
    private static final String K_QUERY_PARAMS = "params";
    private static final String K_QUERY_ID = "queryId";
    private static final String K_DATABASE_CONFIGS = "DatabaseConfigs";

    public DatabaseWorker(DatabaseService databaseService) {
        super("DatabaseWorker");
        this.databaseService = databaseService;
    }

    @Override
    public TaskResult doExecute(Task task) {
        TaskResult result = new TaskResult(task);
        DatabaseActionConfig cfg =inputValidation(task);
        log.debug("Database action cfg: {}", cfg);

        Query query = Query.builder()
                .id(cfg.getQueryId())
                .params(cfg.getParams())
                .build();
        QueryResult qr = databaseService.run(query);

        result.getOutputData().put("result", qr);
        result.setStatus(TaskResult.Status.COMPLETED);
        return result;
    }

    private DatabaseActionConfig inputValidation(Task task) {
        log.debug("Attempting to validate Database worker task: {}", task == null ? null : task.getInputData());
        if (task == null || task.getInputData() == null) return invalid("data is missing, expected object but got null");

        Map<String, Object> cfg = toStringObjectMap(task.getInputData().get(K_DATABASE_CONFIGS));

        String queryId = getString(cfg, K_QUERY_ID);
        if (queryId.isBlank()) return invalid("QueryId is required field");

        return DatabaseActionConfig.builder()
                .params(toStringStringMap(cfg.get(K_QUERY_PARAMS)))
                .queryId(queryId)
                .isValidConfig(true)
                .build();
    }

    private DatabaseActionConfig invalid(String reason) {
        return DatabaseActionConfig.builder().isValidConfig(false).reason(reason).build();
    }

    @Data @Builder
    private static class DatabaseActionConfig {
        private String queryId;
        private Map<String, String> params;
        private boolean isValidConfig;
        private String reason;

        @Override
        public String toString() {
            return "{" +
                    "queryId='" + queryId + '\'' +
                    ", params=" + params +
                    ", isValidConfig=" + isValidConfig +
                    ", reason='" + reason + '\'' +
                    '}';
        }
    }

}

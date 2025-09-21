package com.conductor.core.conductor.worker;

import com.conductor.core.action.db.dto.Query;
import com.conductor.core.action.db.dto.QueryResult;
import com.conductor.core.action.db.enums.QueryExecStatus;
import com.conductor.core.action.db.service.DatabaseActionService;
import com.conductor.core.conductor.worker.contract.WrappedWorker;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/* NOTE:
    Just a simple implementation to be used as blueprint to other db action implementations
*/
@Log4j2
@Component
public class CalculateCustomerTotalPaymentsWorker extends WrappedWorker { // TODO: You must extends WrappedWorker

    @Autowired private DatabaseActionService databaseActionService;

    public CalculateCustomerTotalPaymentsWorker(DatabaseActionService databaseActionService) {
        super("calculateTotalAmountOfCustomerInvoices"); // TODO: Put here task name
        this.databaseActionService = databaseActionService;
    }

    @Override
    protected TaskResult doExecute(Task task) {
        log.info("Attempting to get user total invoices amounts");
        TaskResult result = new TaskResult(task);
        Map<String, Object> inputParameters = task.getInputData();

        Integer customerId = Integer.parseInt(inputParameters.get("customer_id").toString());

        Query query = new Query.builder()
                .id("calculateAllInvoicesAmounts") // TODO: put here the query id
                .withParam("customer_id", customerId) // Fill params as needed
                .build();

        log.debug("Attempting to execute query={} with parameters={}", query.getId(), query.getParams());
        QueryResult queryResult = databaseActionService.run(query);

        if (queryResult.getStatus().equals(QueryExecStatus.SUCCESS)){
            log.info("Query executed successfully");
            log.debug("Query result = {}", queryResult);
        } else {
            result.setStatus(TaskResult.Status.FAILED);
            result.getOutputData().put("reason", "Query " + query.getId() + " failed to execute");
            return result;
        }

        List<Map<String, Object>> resultSet = queryResult.getResultSet();

        result.setStatus(TaskResult.Status.COMPLETED);
        result.getOutputData().put("totalAmount", resultSet.get(0).get("total_amount"));
        return result;
    }

}

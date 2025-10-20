package com.vcs.conductor.worker;

import com.vcs.flowpilot.action.database.api.DatabaseActionApi;
import com.vcs.flowpilot.action.database.internal.dto.Query;
import com.vcs.flowpilot.action.database.internal.dto.QueryResult;
import com.vcs.flowpilot.action.database.internal.enums.QueryExecStatus;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.vcs.conductor.contract.WrappedWorker;

import java.util.List;
import java.util.Map;


/* NOTE:
    Just a simple implementation to be used as blueprint to other db action implementations
*/
@Log4j2
@Component
public class CalculateCustomerTotalPaymentsWorker extends WrappedWorker {

    @Autowired private DatabaseActionApi databaseActionApi;

    public CalculateCustomerTotalPaymentsWorker() {
        // TODO: Must pass the task name here
        super("calculateTotalAmountOfCustomerInvoices");
    }

    @Override
    protected TaskResult doExecute(Task task) {
        log.info("Attempting to get user total invoices amounts");
        TaskResult result = new TaskResult(task);
        Map<String, Object> inputParameters = task.getInputData();

        Integer customerId = Integer.parseInt(inputParameters.get("customer_id").toString());

        Query query = new Query.builder()
                .id("calculateAllInvoicesAmounts")
                .withParam("customer_id", customerId)
                .build();

        log.debug("Attempting to execute query={} with parameters={}", query.getId(), query.getParams());
        QueryResult queryResult = databaseActionApi.run(query);

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

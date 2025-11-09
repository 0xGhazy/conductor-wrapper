# Conductor-Wrapper

Is an easy to use and advanced workflow automation tool based on [Netflix/Conductor](https://github.com/Netflix/conductor) project. We are aiming to provide a drag and drop experience in building simple and advanced workflows with robust modules such as our HTTP and Database Actors.

# Installation guide

- Make sure that you have `docker` installed with `docker compose`.
- Make sure docker demon is up and running.
- Make sure you have `Java 17` installed on your machine.
- Make sure have maven installed or use built-in maven in IntelliJ IDEA.
- Make sure you have setting `JAVA_HOME` correctly.
- Make sure you have installed `Node` and `NPM` to build and run frontend application.

**1. Clone this repository**

```bash
git clone <repository-link>
```

**2. Navigate to repository/deployment**

```bash
cd conductor-wrapper
```

```bash
docker compose up
```

**3. Build java backend application**

```bash
cd repository/implementation/backend/
```

Perform maven clean install to install required dependencies

```bash
mvn clean install
```

**4. Run backend application**

```bash
java -jar repository/implementation/backend/target/core-{version}.jar
```

**5. Build and run frontend application**

Install frontend dependances

```bash
npm install
```

Run frontend application

```bash
npm run dev
```

---

# Usage

Here we will give you simple example of calculating total invoices amount of user.

1. Customers table
   ![Customers table](https://github.com/user-attachments/assets/ec97e9fe-40ff-41d1-bba9-18c28a219dc3)
2. ![Invoices table](https://github.com/user-attachments/assets/38625e7e-a364-45f5-9794-9058e9cc1b7b)
3. ![Payment table](https://github.com/user-attachments/assets/1341b5ae-8c49-44bd-93f5-e2028a4dede9)

## Define data source

In this case I have my customers (Postgres database), to make this accessible to conductor wrapper we need to define this data source in `core.datasource`.

```sql
INSERT INTO core.datasource
("name", url, username, "password", "type")
VALUES('AY_HAGA_DB', 'jdbc:postgresql://127.0.0.1:55432/automation_test_postgres', 'root', 'root', 'POSTGRES'::core."datasource_type");
```

![data source entry](https://github.com/user-attachments/assets/669fc21f-5f71-4369-abf6-929be14cdd22)

## Define required queries

Lets define a query to calculate all invoices amount for a specific customer id.

```sql
SELECT COALESCE(SUM(i.total_amount), 0) AS total_amount
FROM test.invoices AS i
WHERE i.customer_id = :customer_id
```

Add this query to `core.query_store` table using the following query

```sql
INSERT INTO core.query_store
("name", sql_text, datasource_name, "query_type")
VALUES('calculateAllInvoicesAmounts', 'SELECT COALESCE(SUM(i.total_amount), 0) AS total_amount
FROM test.invoices AS i
WHERE i.customer_id = :customer_id', 'AY_HAGA_DB', 'SELECT'::core."query_type");
```

## Refresh database action cache

```bash
curl --location 'http://localhost:9090/api/action/database/cache/refresh'
```

You must get response look like this

```json
{
  "datasource": {
    "AY_HAGA_DB": {
      "name": "AY_HAGA_DB",
      "url": "jdbc:postgresql://127.0.0.1:55432/automation_test_postgres",
      "username": "root",
      "password": "root",
      "type": "POSTGRES"
    }
  },
  "query": {
    "calculateAllInvoicesAmounts": {
      "name": "calculateAllInvoicesAmounts",
      "sql": "SELECT COALESCE(SUM(i.total_amount), 0) AS total_amount\r\nFROM test.invoices AS i\r\nWHERE i.customer_id = :customer_id",
      "dataSource": {
        "name": "AY_HAGA_DB",
        "url": "jdbc:postgresql://127.0.0.1:55432/automation_test_postgres",
        "username": "root",
        "password": "root",
        "type": "POSTGRES"
      },
      "queryType": "SELECT"
    }
  }
}
```

## Define task

Define task using the following cURL request or using postman collection in deployment directory.

Here i have defined a task called `calculateTotalAmountOfCustomerInvoices` with no whitespaces in name as required by the conductor metadata client.

also i have passed list of expected input keys `customer_id` and list of required output keys `totalAmount`.

```bash
curl --location 'http://localhost:9090/api/tasks' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "calculateTotalAmountOfCustomerInvoices",
    "description": "calculate the total amount of customer invoices",
    "timeoutPolicy": "TIME_OUT_WF",
    "retryCount": 1,
    "responseTimeoutSeconds": 600,
    "inputKeys": [
        "customer_id"
    ],
    "outputKeys": [
        "totalAmount"
    ],
    "ownerEmail": "conductor.wrapper@vodafone.com",
    "retryLogic": "FIXED",
    "retryDelaySeconds": 60,
    "timeoutSeconds": 3600,
    "rateLimitFrequencyInSeconds": 1,
    "rateLimitPerFrequency": 0,
    "setConcurrentExecLimit": 5,
    "createdBy": "Conductor Wrapper",
    "updatedBy": "Conductor Wrapper",
    "updateTime": 0
}'
```

Make sure you got `201 CREATED` status code in the response.

## Define workflow

Lets define workflow to include our defined task to be triggered. Use the postman or the following request.

Make sure the following:

1. Task name is the same as the defined task above.
2. Pass a unique `taskReferenceName` to be used in other tasks if your flow consist of multiple tasks.
3. Map your input in a correct way such as the example. I am expecting `customer_id` in workflow input payload so i defined it in the `inputParameters` list. and in the task section I have mapped required task input parameter `customer_id` with `workflow.input.customer_id`. **workflow.input** is a reserved word that represent the current working workflow.
4. Define and map output parameters in `outputParameters`. I have mapped `totalAmount` output parameter to the `t1.output.totalAmount` predefined output parameter from the task definition.

```bash
curl --location 'http://localhost:9090/api/workflows' \
--header 'Content-Type: application/json' \
--data-raw '{
  "name": "fetchCustomerTotalInvoices",
  "description": "Fetch customer invoices and compute totals",
  "version": 1,
  "tasks": [
    {
      "name": "calculateTotalAmountOfCustomerInvoices",
      "taskReferenceName": "t1",
      "type": "SIMPLE",
      "inputParameters": {
          "customer_id": "${workflow.input.customer_id}"
      }
    }
  ],
  "inputParameters": [
    "customer_id"
  ],
  "outputParameters": {
    "totalAmount": "${t1.output.totalAmount}"
  },
  "ownerEmail": "conductor.wrapper@vodafone.com",
  "timeoutPolicy": "ALERT_ONLY",
  "schemaVersion": 2,
  "createTime": 1726650000000,
  "updateTime": 0,
  "createdBy": "Conductor Wrapper",
  "updatedBy": "Conductor Wrapper",
  "restartable": true,
  "workflowStatusListenerEnabled": false,
  "timeoutSeconds": 300,
  "variables": {},
  "inputTemplate": {}
}
'
```

Again make sure you got `201 CREATED` status code.

## Implement worker to handle task invocation

Define your worker java class under the following package `backend/src/main/java/com/conductor/core/conductor/worker`

```java
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

@Log4j2
@Component
// TODO: You must extends WrappedWorker
public class CalculateCustomerTotalPaymentsWorker extends WrappedWorker {

    @Autowired private DatabaseActionService databaseActionService;

    public CalculateCustomerTotalPaymentsWorker(DatabaseActionService databaseActionService) {
        // TODO: Put here task name
        super("calculateTotalAmountOfCustomerInvoices");
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

        log.info("Calculate customer's invoices amounts");
        double totalAmount = 0;
        List<Map<String, Object>> resultS = queryResult.getResultSet();
        for (Map<String, Object> row : resultS){
            totalAmount += ((Number) row.get("total_amount")).doubleValue();
        }
        log.info("Total amount calculated successfully");

        result.setStatus(TaskResult.Status.COMPLETED);
        result.getOutputData().put("totalAmount", totalAmount);
        return result;
    }

}
```

## Result

Execute the following

```bash
curl --location 'http://localhost:8080/api/workflow' \
--header 'Content-Type: application/json' \
--data '{
    "name": "fetchCustomerTotalInvoices",
    "input": {
        "customer_id": 1
    },
    "priority": 0,
    "rateLimited": false,
    "workflowVersion": 2
}'
```

You must got the workflow `instance id` To be used in fetching flow status

Backend logs should be like this

```log
2025-09-21T13:49:46.168 | INFO  | traceId(84710fb/1758451786164)  | task(calculateTotalAmountOfCustomerInvoices) | com.conductor.core.conductor.worker.contract.WrappedWorker | ==================== [FETCHCUSTOMERTOTALINVOICES][START] ====================
2025-09-21T13:49:46.168 | INFO  | traceId(84710fb/1758451786164)  | task(calculateTotalAmountOfCustomerInvoices) | com.conductor.core.conductor.worker.contract.WrappedWorker | INPUT: {include_payments=true, customer_id=1}
2025-09-21T13:49:46.168 | INFO  | traceId(84710fb/1758451786164)  | task(calculateTotalAmountOfCustomerInvoices) | com.conductor.core.conductor.worker.CalculateCustomerTotalPaymentsWorker | Attempting to get user total invoices amounts
2025-09-21T13:49:46.169 | DEBUG | traceId(84710fb/1758451786164)  | task(calculateTotalAmountOfCustomerInvoices) | com.conductor.core.conductor.worker.CalculateCustomerTotalPaymentsWorker | Attempting to execute query=calculateAllInvoicesAmounts with parameters={customer_id=1}
2025-09-21T13:49:46.174 | DEBUG | traceId(84710fb/1758451786164)  | task(calculateTotalAmountOfCustomerInvoices) | com.conductor.core.action.db.service.DatabaseActionService | Attempting to fetch query from cache
2025-09-21T13:49:46.198 | INFO  | traceId(84710fb/1758451786164)  | task(calculateTotalAmountOfCustomerInvoices) | com.conductor.core.conductor.worker.CalculateCustomerTotalPaymentsWorker | Query executed successfully
2025-09-21T13:49:46.198 | DEBUG | traceId(84710fb/1758451786164)  | task(calculateTotalAmountOfCustomerInvoices) | com.conductor.core.conductor.worker.CalculateCustomerTotalPaymentsWorker | Query result = {queryId='calculateAllInvoicesAmounts', params={customer_id=1}, size=1, status=SUCCESS, resultSet=[{total_amount=21462.11}], affectedRowsCount=0}
2025-09-21T13:49:46.201 | INFO  | traceId(84710fb/1758451786164)  | task(calculateTotalAmountOfCustomerInvoices) | com.conductor.core.conductor.worker.contract.WrappedWorker | ==================== [FETCHCUSTOMERTOTALINVOICES][FINISH in 37ms] ====================
```

---

# Current implemented features

1. Support multiple data sources (MySQL, Postgres, and Oracle)
2. Database action to execute SQK queries.

# Future work

1. Implement HTTP action to support the following authentication methods:
   - OAuth
   - Basic Authentication
   - API Key
2. Integrate frontend with backend to allow workflow design using drag and drop.
3. Implement mediator action to support transformation of different payload formats.
4. Implement form creation mechanism.

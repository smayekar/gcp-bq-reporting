package io.ctl.cloudintegration.gcp.util;

import com.google.cloud.bigquery.*;
import io.ctl.cloudintegration.gcp.exception.FaultException;
import io.ctl.cloudintegration.gcp.service.GcpBqClient;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.internal.util.ExceptionUtils;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class GcpBqUtils {
    private static BigQuery bigQueryClient = null;

    public static final String GET_CONSOLIDATED_BILLING = "SELECT\n" +
            "  project.id,\n" +
            "  invoice.month,\n" +
            "  service.description as svc,\n" +
            "  sku.description as sku,\n" +
            "  SUM(CAST(cost * 1000000 AS int64)) / 1000000 AS totalCost,\n" +
            "  SUM(IFNULL((SELECT SUM(CAST(c.amount * 1000000 AS int64))\n" +
            "                  FROM UNNEST(credits) c), 0)) / 1000000\n" +
            "    AS totalCredit\n" +
            "FROM `${projectId}.${dataset}.billing`\n" +
            "WHERE invoice.month = @invoiceMonth \n" +
            "GROUP BY 1, 2, 3, 4\n";

    public static BigQuery getBigQueryClient(final GcpBqClient gcpBqClient) throws FaultException {
        if(bigQueryClient == null) {
            log.info("Creating BigQuery instance");
            bigQueryClient = gcpBqClient.getBigQueryClient();
        }

        return bigQueryClient;
    }

    public String getFieldStringValue(FieldValue fieldValue) {
        return fieldValue.getValue() == null ? null : fieldValue.getStringValue();
    }

    public Double getFieldDoubleValue(FieldValue fieldValue) {
        return fieldValue.getValue() == null ? null : fieldValue.getDoubleValue();
    }

    public QueryResult executeQuery(final GcpBqClient gcpBqClient, final QueryRequest queryRequest) throws FaultException {
        QueryResponse response = getQueryResponse(gcpBqClient, queryRequest);
        QueryResult result = response.getResult();
        log.info("Retrieved rows: {}", result.getTotalRows());

        return result;
    }

    private QueryResponse getQueryResponse(final GcpBqClient gcpBqClient, final QueryRequest queryRequest) throws FaultException {
        BigQuery bigquery = getBigQueryClient(gcpBqClient);
        // Execute the query
        log.info("STARTED getting the results for the following query: {}", queryRequest.getQuery());
        QueryResponse response = bigquery.query(queryRequest);
        int retryCount = 1;
        log.info("Attempt {} response status: {}", retryCount, response.jobCompleted());
        while (!response.jobCompleted() && retryCount < 5) {
            retryCount++;
            try {
                TimeUnit.MILLISECONDS.sleep(2000 * retryCount);
            } catch (InterruptedException e) {
                log.error("Error occurred while waiting for the job to complete. Will try again: {}", ExceptionUtils.exceptionStackTraceAsString(e));
            }
            try {
                response = bigquery.getQueryResults(response.getJobId());
            } catch (BigQueryException e) {
                log.error("Error occurred: {}", ExceptionUtils.exceptionStackTraceAsString(e));
            }
            log.info("Attempt {} response status: {}", retryCount, response.jobCompleted());
        }
        return response;
    }
}

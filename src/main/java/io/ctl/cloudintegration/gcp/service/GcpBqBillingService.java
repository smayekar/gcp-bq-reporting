package io.ctl.cloudintegration.gcp.service;

import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.QueryParameterValue;
import com.google.cloud.bigquery.QueryRequest;
import com.google.cloud.bigquery.QueryResult;
import io.ctl.cloudintegration.gcp.exception.FaultException;
import io.ctl.cloudintegration.gcp.models.GcpBqBillingResponse;
import io.ctl.cloudintegration.gcp.util.GcpBqUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StrSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GcpBqBillingService {
    @Autowired
    GcpBqClient gcpBqClient;

    @Autowired
    GcpBqUtils utils;

    public List<GcpBqBillingResponse> getConsolidatedBilling(final String reportDate) throws FaultException {
        QueryRequest queryRequest = QueryRequest.newBuilder(getBQQueryString(GcpBqUtils.GET_CONSOLIDATED_BILLING))
                .addNamedParameter("invoiceMonth", QueryParameterValue.string(reportDate))
                .setUseLegacySql(false).build();

        QueryResult result = utils.executeQuery(gcpBqClient, queryRequest);

        List<GcpBqBillingResponse> consolidatedBilling = new ArrayList<>();
        for (List<FieldValue> row : result.iterateAll()) {
            final GcpBqBillingResponse awsCurSummaryReportLineObject = GcpBqBillingResponse.builder()
                    .projectId(utils.getFieldStringValue(row.get(0)))
                    .invoiceMonth(utils.getFieldStringValue(row.get(1)))
                    .service(utils.getFieldStringValue(row.get(2)))
                    .sku(utils.getFieldStringValue(row.get(3)))
                    .totalCost(utils.getFieldDoubleValue(row.get(4)))
                    .totalCredit(utils.getFieldDoubleValue(row.get(5)))
                    .build();

            consolidatedBilling.add(awsCurSummaryReportLineObject);
        }
        log.info("******** ENDED getting results {} from Big Query from cache: {}", result.getTotalRows(), result.cacheHit());
        return consolidatedBilling;
    }

    private String getBQQueryString(final String queryTemplate) {
        Map<String, String> bqValueMap = new HashMap<>();
        bqValueMap.put("projectId", gcpBqClient.getBQProjectId());
        bqValueMap.put("dataset", gcpBqClient.getBQDataSetName());
        StrSubstitutor strSubstitutor = new StrSubstitutor(bqValueMap);
        return strSubstitutor.replace(queryTemplate);
    }
}

package io.ctl.cloudintegration.gcp.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import io.ctl.cloudintegration.gcp.exception.FaultException;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.internal.util.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Slf4j
@Service
public class GcpBqClient {

    @Value("${GCP_CREDENTIALS}")
    private String credential;
    @Value("${GCP_PROJECTID}")
    private String googleCloudProjectId;
    @Value("${GCP_DATASET}")
    private String googleCloudDataSet;

    public BigQuery getBigQueryClient() throws FaultException {
        return getBigQueryClient(credential, googleCloudProjectId);
    }

    private BigQuery getBigQueryClient(final String credential, final String googleCloudProjectId) throws FaultException {
        try (ByteArrayInputStream serviceAccountStream = new ByteArrayInputStream(credential.getBytes())) {
            GoogleCredentials credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);

            return BigQueryOptions.newBuilder().setProjectId(googleCloudProjectId).setCredentials(credentials).build().getService();
        } catch (Exception e) {
            log.error("Unable to authenticate to Google Cloud Platform {}", ExceptionUtils.exceptionStackTraceAsString(e));
            throw new FaultException("Unable to authenticate to Google Cloud Platform",
                    HttpStatus.UNAUTHORIZED.value());
        }
    }

    public String getBQDataSetName() {
        return googleCloudDataSet;
    }

    public String getBQProjectId() { return googleCloudProjectId; }
}

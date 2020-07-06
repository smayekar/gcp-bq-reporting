package io.ctl.cloudintegration.gcp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ctl.cloudintegration.gcp.exception.FaultException;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.internal.util.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class TeamsNotificationService {
    @Value("${spring.profiles.active}")
    private String env;

    @Value("${TEAMS_WEBHOOK}")
    private String endpoint;

    private static final String context = "https://schema.org/extensions";
    private static final String type = "MessageCard";
    private static final String color = "ff7f21";
    private RestTemplate restTemplate = new RestTemplate();

    public void notifyCloudIntegration(String body, boolean alert) throws FaultException {
        if (alert) {
            body = notifySquad() + body;
        }

        send(body, endpoint);
    }

    private String notifySquad() {
        if (!env.equals("prod")) {
            return "Testing in [" + env + "] - ";
        }
        else
            return  "";
    }

    private void send(String text, String endpoint) throws FaultException {
        String payloadJson = generatePayloadJson(text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(payloadJson, headers);

        try {
            restTemplate.postForObject(endpoint, entity, String.class);
        } catch (RestClientException e) {
            log.error("Teams notification could not be sent {}", ExceptionUtils.exceptionStackTraceAsString(e));
            throw new FaultException("Teams notification could not be sent", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private String generatePayloadJson(String text) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("@context", context);
        payload.put("@type", type);
        payload.put("themeColor", color);
        payload.put("text", text);

        try {
            return new ObjectMapper().writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            log.info("Error generating payload for Teams Alert", exception);
            throw new RuntimeException();
        }
    }
}

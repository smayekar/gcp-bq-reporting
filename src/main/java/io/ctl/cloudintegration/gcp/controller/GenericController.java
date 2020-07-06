package io.ctl.cloudintegration.gcp.controller;

import com.google.cloud.bigquery.BigQuery;
import io.ctl.cloudintegration.gcp.exception.FaultException;
import io.ctl.cloudintegration.gcp.service.TeamsNotificationService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Api(value = "Generic Operations")
public class GenericController {
    @Autowired
    TeamsNotificationService teamsNotificationService;

    @PostMapping("/alert")
    @ApiOperation(value = "Post an alert on Teams channel", response = BigQuery.class,
            notes = "Post an alert with the message body passed in the request on Teams channel")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Processed Successfully"),
            @ApiResponse(code = 400, message = "Bad Request")
    })
    public ResponseEntity postTeamsNotificationAlert(@RequestParam final String body,
                                                     @RequestParam(required = false)
                                                     @ApiParam(defaultValue = "true") final boolean shouldAlert){
        try{
            teamsNotificationService.notifyCloudIntegration(body, shouldAlert);
            return ResponseEntity.ok().build();
        } catch (FaultException e) {
            log.error("Error notifying alert to Teams {}", e.getMessage());
            return ResponseEntity.status(e.getCode()).body(e);
        }
    }
}

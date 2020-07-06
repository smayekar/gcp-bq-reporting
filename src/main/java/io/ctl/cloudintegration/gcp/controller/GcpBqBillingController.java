package io.ctl.cloudintegration.gcp.controller;

import io.ctl.cloudintegration.gcp.exception.FaultException;
import io.ctl.cloudintegration.gcp.models.GcpBqBillingResponse;
import io.ctl.cloudintegration.gcp.service.GcpBqBillingService;
import io.ctl.cloudintegration.gcp.service.TeamsNotificationService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.util.List;

@Slf4j
@RestController
@Api(value = "GCP Billing Operations")
@Validated
public class GcpBqBillingController {
    @Autowired
    GcpBqBillingService gcpBqBillingService;

    @Autowired
    TeamsNotificationService teamsNotificationService;

    @GetMapping("/gcp/billing/invoicedate/{invoiceMonth}")
    @ApiOperation(value = "Get consolidated billing", response = GcpBqBillingResponse.class, responseContainer = "List",
            notes = "Get consolidated billing")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Processed Successfully"),
            @ApiResponse(code = 400, message = "Bad Request")
    })
    public ResponseEntity getGcpBilling(@ApiParam(value = "date", required = true, example = "202002")
                                            @Valid @Pattern(regexp = "\\d{4}0?[1-9]|1[0-2]", message = "InvoiceMonth must be in the format <year><month>")
                                            @PathVariable(required = true) final String invoiceMonth){
        try {
            List<GcpBqBillingResponse> consolidatedBilling = gcpBqBillingService.getConsolidatedBilling(invoiceMonth);
            return ResponseEntity.ok(consolidatedBilling);
        } catch (FaultException e){
            log.error("Error notifying alert to Teams {}", e.getMessage());
            return ResponseEntity.status(e.getCode()).body(e);
        }
    }
}

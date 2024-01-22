package uk.gov.hmcts.juror.api.bureau.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauOfficerAllocatedResponses;
import uk.gov.hmcts.juror.api.bureau.service.BureauOfficerAllocatedRepliesService;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/bureau", produces = MediaType.APPLICATION_JSON_VALUE)
public class BureauOfficerAllocatedRepliesController {

    private final BureauOfficerAllocatedRepliesService bureauOfficerAllocatedRepliesService;


    @Autowired
    public BureauOfficerAllocatedRepliesController(
        final BureauOfficerAllocatedRepliesService bureauOfficerAllocatedRepliesService) {
        Assert.notNull(bureauOfficerAllocatedRepliesService, " BureauBacklogCountService cannot be null");
        this.bureauOfficerAllocatedRepliesService = bureauOfficerAllocatedRepliesService;
    }

    @GetMapping("/allocate/replies")
    @Operation(summary = "/bureau/allocate/replies",
        description = "Retrieve the count of backlog items")
    public ResponseEntity<BureauOfficerAllocatedResponses> getBureauOfficerAllocatedData(
        @Parameter(hidden = true) BureauJwtAuthentication principal) {
        BureauOfficerAllocatedResponses backLogData = bureauOfficerAllocatedRepliesService.getBackLogData();
        return ResponseEntity.ok().body(backLogData);
    }
}

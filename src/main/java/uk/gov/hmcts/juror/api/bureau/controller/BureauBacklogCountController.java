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
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauBacklogCountData;
import uk.gov.hmcts.juror.api.bureau.service.BureauBacklogCountService;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/bureau/backlog", produces = MediaType.APPLICATION_JSON_VALUE)
public class BureauBacklogCountController {

    private final BureauBacklogCountService backlogCountService;


    @Autowired
    public BureauBacklogCountController(final BureauBacklogCountService backlogCountService) {
        Assert.notNull(backlogCountService, " BureauBacklogCountService cannot be null");
        this.backlogCountService = backlogCountService;
    }

    @GetMapping("/count")
    @Operation(summary = "/backlog/count",
        description = "Retrieve the count of backlog items")
    public ResponseEntity<BureauBacklogCountData> getJurorResponseCount(
        @Parameter(hidden = true) BureauJwtAuthentication principal) {
        return ResponseEntity.ok().body(backlogCountService.getBacklogResponseCount());
    }

}

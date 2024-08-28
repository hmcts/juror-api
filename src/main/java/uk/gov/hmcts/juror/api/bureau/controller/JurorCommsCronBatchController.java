package uk.gov.hmcts.juror.api.bureau.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.bureau.scheduler.BureauBatchScheduler;

/**
 * API endpoints controller for Cron Batch.
*/
@RestController
@RequestMapping(value = "/api/v1/bureau/cron", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@Tag(name = "Cron API", description = "Bureau Cron API")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class JurorCommsCronBatchController {

    private final BureauBatchScheduler bureauBatchScheduler;

    @GetMapping
    @Operation(summary = "/bureau/cron",
               description = "BureauBatchScheduler is called")
    public ResponseEntity<Object> callBureauBatchScheduler(@RequestParam("types") String[] types,
                                                           @RequestHeader(value = "job_key", required = false)
                                                           String jobKey,
                                                           @RequestHeader(value = "task_id", required = false)
                                                           Long taskId) {

        log.info("Attempting to call BureauBatchScheduler");
        bureauBatchScheduler.processBatchJobServices(types,jobKey,taskId);
        return ResponseEntity.ok().build();
    }
}

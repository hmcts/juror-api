package uk.gov.hmcts.juror.api.moj.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolHistoryListDto;
import uk.gov.hmcts.juror.api.moj.service.PoolHistoryService;

@Slf4j
@RestController
@Validated
@RequestMapping(value = "/api/v1/moj/pool-history", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Pool Management")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PoolHistoryController {

    @NonNull
    private final PoolHistoryService poolHistoryService;

    @GetMapping("/{poolNumber}")
    @Operation(summary = "Retrieve all unique pool history events for a single pool request")
    public ResponseEntity<PoolHistoryListDto> getPoolHistory(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @PathVariable @Size(min = 9, max = 9)
        @Parameter(description = "Pool number", required = true)
        @Valid String poolNumber) {
        PoolHistoryListDto poolHistories = poolHistoryService.getPoolHistoryListData(payload, poolNumber);
        return ResponseEntity.ok().body(poolHistories);
    }


}

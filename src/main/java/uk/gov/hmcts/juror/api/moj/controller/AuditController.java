package uk.gov.hmcts.juror.api.moj.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.moj.service.audit.JurorAuditService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.time.LocalDate;
import java.util.List;

@RestController
@Validated
@RequestMapping(value = "/api/v1/moj/audit", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Audit")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@PreAuthorize(SecurityUtil.IS_COURT)
public class AuditController {

    private final JurorAuditService jurorAuditService;

    @GetMapping("/{date}/pool")
    @Operation(summary = "View all pool audits")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<String>> getAllPoolAuditsForDay(
        @PathVariable("date")
        @Parameter(description = "date", required = true)
        @JsonFormat(pattern = ValidationConstants.DATE_FORMAT)
        @Valid LocalDate date
    ) {
        return ResponseEntity.ok(jurorAuditService.getAllPoolAuditsForDay(date));
    }
}

package uk.gov.hmcts.juror.api.moj.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.juror.api.moj.audit.dto.TransportLimitAuditRecord;
import uk.gov.hmcts.juror.api.moj.controller.response.TransportLimitAuditListResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.TransportLimitAuditResponseDto;
import uk.gov.hmcts.juror.api.moj.service.audit.CourtLocationAuditService;
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
@Slf4j
public class AuditController {

    private final JurorAuditService jurorAuditService;
    private final CourtLocationAuditService courtLocationAuditService;

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

    @GetMapping("/court-location/{locCode}/transport-limits")
    @Operation(
        summary = "Get transport limit audit history",
        description = "Retrieve audit history for changes to public transport and taxi soft limits "
    )
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TransportLimitAuditListResponseDto> getTransportLimitAuditHistory(
        @PathVariable("locCode")
        @Parameter(description = "Court location code", example = "415", required = true)
        String locCode
    ) {
        log.info("Request received to get transport limit audit history for court location: {}", locCode);

        List<TransportLimitAuditRecord> records =
            courtLocationAuditService.getTransportLimitAuditHistory(locCode);

        if (records.isEmpty()) {
            log.info("No transport limit audit history found for court location: {}", locCode);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        TransportLimitAuditListResponseDto response = TransportLimitAuditListResponseDto.from(records);

        log.info("Returning {} audit records for court location: {}", records.size(), locCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/court-location/{locCode}/transport-limits/latest")
    @Operation(
        summary = "Get latest transport limit change",
        description = "Retrieve the most recent change to transport limits for a court location"
    )
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TransportLimitAuditResponseDto> getLatestTransportLimitChange(
        @PathVariable("locCode")
        @Parameter(description = "Court location code", example = "415", required = true)
        String locCode
    ) {
        log.info("Request received to get latest transport limit change for court location: {}", locCode);

        TransportLimitAuditRecord record =
            courtLocationAuditService.getLatestTransportLimitChange(locCode);

        if (record == null) {
            log.info("No transport limit changes found for court location: {}", locCode);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        TransportLimitAuditResponseDto response = TransportLimitAuditResponseDto.from(record);

        log.info("Returning latest audit record for court location: {}", locCode);
        return ResponseEntity.ok(response);
    }
}

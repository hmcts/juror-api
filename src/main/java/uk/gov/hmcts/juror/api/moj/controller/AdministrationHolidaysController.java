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
import org.springframework.security.core.parameters.P;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.moj.controller.response.administration.HolidayDate;
import uk.gov.hmcts.juror.api.moj.service.administration.AdministrationHolidaysService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;
import uk.gov.hmcts.juror.api.validation.CourtLocationCode;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@Validated
@RequestMapping(value = "/api/v1/moj/administration", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Administration - Holidays")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AdministrationHolidaysController {

    private final AdministrationHolidaysService administrationHolidaysService;

    @GetMapping("/bank-holidays")
    @Operation(summary = "View bank holidays")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(SecurityUtil.IS_ADMINISTRATOR + " or " + SecurityUtil.IS_MANAGER)
    public ResponseEntity<Map<Integer, List<HolidayDate>>> viewBankHolidays() {
        return ResponseEntity.ok(administrationHolidaysService.viewBankHolidays());
    }

    @GetMapping("/non-sitting-days/{loc_code}")
    @Operation(summary = "View non sitting days")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(SecurityUtil.LOC_CODE_AUTH + " && " + SecurityUtil.IS_MANAGER)
    public ResponseEntity<List<HolidayDate>> viewNonSittingDays(
        @P("loc_code")
        @PathVariable("loc_code")
        @CourtLocationCode
        @Parameter(description = "locCode", required = true)
        @Valid String locCode) {
        return ResponseEntity.ok(administrationHolidaysService.viewNonSittingDays(locCode));
    }

    @DeleteMapping("/non-sitting-days/{loc_code}/{date}")
    @Operation(summary = "Delete a non sitting days")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize(SecurityUtil.LOC_CODE_AUTH + " && " + SecurityUtil.IS_MANAGER)
    public ResponseEntity<Void> deleteNonSittingDays(
        @P("loc_code")
        @PathVariable("loc_code")
        @CourtLocationCode
        @Parameter(description = "locCode", required = true)
        @Valid String locCode,
        @PathVariable("date") @Parameter(description = "date", required = true) @Valid
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        administrationHolidaysService.deleteNonSittingDays(locCode, date);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/non-sitting-days/{loc_code}")
    @Operation(summary = "Add a non sitting days")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize(SecurityUtil.LOC_CODE_AUTH + " && " + SecurityUtil.IS_MANAGER)
    public ResponseEntity<Void> addNonSittingDays(
        @P("loc_code")
        @PathVariable("loc_code")
        @CourtLocationCode
        @Parameter(description = "locCode", required = true)
        @Valid String locCode,
        @RequestBody @Valid HolidayDate holidays) {
        administrationHolidaysService.addNonSittingDays(locCode, holidays);
        return ResponseEntity.accepted().build();
    }
}

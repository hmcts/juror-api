package uk.gov.hmcts.juror.api.moj.controller;


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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtRates;
import uk.gov.hmcts.juror.api.moj.controller.response.administration.CodeDescriptionResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.administration.CourtDetailsReduced;
import uk.gov.hmcts.juror.api.moj.domain.CodeType;
import uk.gov.hmcts.juror.api.moj.domain.CourtDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.ExpenseRatesDto;
import uk.gov.hmcts.juror.api.moj.domain.UpdateCourtDetailsDto;
import uk.gov.hmcts.juror.api.moj.service.AdministrationService;
import uk.gov.hmcts.juror.api.moj.service.expense.JurorExpenseService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;
import uk.gov.hmcts.juror.api.validation.CourtLocationCode;

import java.util.List;

@RestController
@Validated
@RequestMapping(value = "/api/v1/moj/administration", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Administration")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AdministrationController {

    private final AdministrationService administrationService;
    private final JurorExpenseService jurorExpenseService;

    @GetMapping("/codes/{code_type}")
    @Operation(summary = "View a list of codes and descriptions")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<List<CodeDescriptionResponse>> viewCodeAndDescriptions(
        @PathVariable("code_type")
        @Parameter(description = "CodeType", required = true)
        @Valid CodeType codeType
    ) {
        return ResponseEntity.ok(administrationService.viewCodeAndDescriptions(codeType));
    }

    @GetMapping("/courts/{loc_code}")
    @Operation(summary = "View court details")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(SecurityUtil.LOC_CODE_AUTH + " or " + SecurityUtil.IS_ADMINISTRATOR)
    public ResponseEntity<CourtDetailsDto> viewCourtDetails(
        @P("loc_code")
        @PathVariable("loc_code")
        @CourtLocationCode
        @Parameter(description = "locCode", required = true)
        @Valid String locCode
    ) {
        return ResponseEntity.ok(administrationService.viewCourt(locCode));
    }

    @PutMapping("/courts/{loc_code}")
    @Operation(summary = "Update court details")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("(" + SecurityUtil.IS_COURT + " and " + SecurityUtil.LOC_CODE_AUTH + ") or ("
        + SecurityUtil.IS_ADMINISTRATOR + ")")
    public ResponseEntity<Void> updateCourtDetails(
        @P("loc_code")
        @PathVariable("loc_code")
        @CourtLocationCode
        @Parameter(description = "loc_code", required = true)
        @Valid String locCode,
        @Valid @RequestBody UpdateCourtDetailsDto updateCourtDetailsDto
    ) {
        administrationService.updateCourt(locCode, updateCourtDetailsDto);
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/courts/{loc_code}/rates")
    @Operation(summary = "Update court details")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize(SecurityUtil.LOC_CODE_AUTH + " && " + SecurityUtil.IS_MANAGER)
    public ResponseEntity<Void> updateCourtRates(
        @P("loc_code")
        @PathVariable("loc_code")
        @CourtLocationCode
        @Parameter(description = "CourtCode", required = true)
        @Valid String courtCode,
        @RequestBody @Valid CourtRates courtRates
    ) {
        administrationService.updateCourtRates(courtCode, courtRates);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/courts")
    @Operation(summary = "View court details")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(SecurityUtil.IS_ADMINISTRATOR)
    public ResponseEntity<List<CourtDetailsReduced>> viewAllCourtsDetails() {
        return ResponseEntity.ok(administrationService.viewCourts());
    }

    @GetMapping("/expenses/rates")
    @Operation(summary = "View global expense rates")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(SecurityUtil.IS_ADMINISTRATOR + " or " + SecurityUtil.IS_COURT)
    public ResponseEntity<ExpenseRatesDto> viewExpenseDetails() {
        return ResponseEntity.ok(new ExpenseRatesDto(
            jurorExpenseService.getCurrentExpenseRates(SecurityUtil.isCourt())));
    }

    @PutMapping("/expenses/rates")
    @Operation(summary = "Update global expense rates")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize(SecurityUtil.IS_ADMINISTRATOR)
    public ResponseEntity<Void> updateExpenseDetails(
        @Valid @RequestBody ExpenseRatesDto expenseRatesDto
    ) {
        jurorExpenseService.updateExpenseRates(expenseRatesDto);
        return ResponseEntity.accepted().build();
    }
}

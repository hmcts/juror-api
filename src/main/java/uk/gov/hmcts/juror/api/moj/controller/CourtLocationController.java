package uk.gov.hmcts.juror.api.moj.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.jsonwebtoken.lang.Collections;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtLocationDataDto;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtLocationListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtRates;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.List;


@RestController
@Slf4j
@RequestMapping(value = "/api/v1/moj/court-location", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Pool Management")
@Validated
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CourtLocationController {


    @NonNull
    private final CourtLocationService courtLocationService;


    /**
     * Returns the full list of all court locations, regardless of current user's access/permissions. Only to be used
     * when the whole list of court locations should be returned, even for court users, for example when transferring to
     * another court. If the returned list should be filtered based on the current user's permissions,
     * please use {@link RequestPoolController (getCourtLocations() method)}
     *
     * @param auth JSON Web Token containing user authentication context
     * @return a simple DTO containing an unfiltered list of all court locations and a selection of related data
     */
    @GetMapping("/all-court-locations")
    @Operation(summary = "Retrieve a list of all court locations")
    public ResponseEntity<CourtLocationListDto> getAllCourtLocations(
        @Parameter(hidden = true) BureauJwtAuthentication auth) {
        CourtLocationListDto courtLocations = courtLocationService.buildAllCourtLocationDataResponse();
        return ResponseEntity.ok().body(courtLocations);
    }

    @GetMapping("/catchment-areas")
    @Operation(summary = "Retrieve a list of all court catchment areas for a given postcode")
    public ResponseEntity<List<CourtLocationDataDto>> getCourtCatchmentAreasByPostcode(
        @RequestParam(name = "postcode")
        @Length(max = 4)
        @Pattern(regexp = "[A-Z0-9]{1,4}") @Valid String postcode) {
        List<CourtLocationDataDto> catchmentAreas = courtLocationService.getCourtLocationsByPostcode(postcode);
        if (Collections.isEmpty(catchmentAreas)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(catchmentAreas);
    }

    @GetMapping("/{loc_code}/{date}/rates")
    @Operation(summary = "Retrieve a list of court rates / limits for a given day")
    @PreAuthorize(SecurityUtil.LOC_CODE_AUTH)
    public ResponseEntity<CourtRates> getCourtRates(
        @Parameter(description = "Valid location Code", required = true)
        @P("loc_code")
        @Size(min = 3, max = 3) @PathVariable("loc_code") @Valid String locCode,
        @Parameter(description = "Valid date yyyy-MM-dd", required = true)
        @JsonFormat(pattern = "yyyy-MM-dd")
        @PathVariable("date") @Valid LocalDate date) {

        return ResponseEntity.ok().body(courtLocationService.getCourtRates(locCode, date));
    }
}

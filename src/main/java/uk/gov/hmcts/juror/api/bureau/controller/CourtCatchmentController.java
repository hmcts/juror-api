package uk.gov.hmcts.juror.api.bureau.controller;

import io.jsonwebtoken.lang.Assert;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.bureau.controller.response.CourtCatchmentStatusDto;
import uk.gov.hmcts.juror.api.bureau.service.CourtCatchmentService;

import static uk.gov.hmcts.juror.api.bureau.controller.ResponseUpdateController.validateJurorNumberPathVariable;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/bureau/juror/court/catchment", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Bureau Catchment court API", description = "Bureau - check court catchment upon change of address")
public class CourtCatchmentController {

    private final CourtCatchmentService courtCatchmentService;

    @Autowired
    public CourtCatchmentController(final CourtCatchmentService courtCatchmentService) {
        Assert.notNull(courtCatchmentService, "courtCatchmentService cannot be null");
        this.courtCatchmentService = courtCatchmentService;
    }


    @GetMapping("/{jurorId}")
    @Operation(summary = "if court catchment changed",
        description = "by juror number")
    public ResponseEntity<CourtCatchmentStatusDto> courtCatchment(
        @Parameter(description = "Valid juror number", required = true) @PathVariable String jurorId) {
        validateJurorNumberPathVariable(jurorId);

        log.info("Attempting to check court catchment details, by juror {}", jurorId);
        try {
            return ResponseEntity.ok(courtCatchmentService.courtCatchmentFinder(jurorId));
        } catch (Exception e) {
            log.error("Failed to retrieve court catchment details: {}", e.getMessage());
            throw e;
        }
    }
}

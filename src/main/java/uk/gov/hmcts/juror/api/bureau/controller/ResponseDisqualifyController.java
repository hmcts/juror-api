package uk.gov.hmcts.juror.api.bureau.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jsonwebtoken.lang.Assert;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.bureau.domain.DisCode;
import uk.gov.hmcts.juror.api.bureau.exception.DisqualifyException;
import uk.gov.hmcts.juror.api.bureau.service.ResponseDisqualifyService;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.domain.DisqualifiedCode;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.juror.api.bureau.controller.ResponseUpdateController.assertJurorNumberPathVariable;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/bureau/juror/disqualify", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Bureau Juror-Disqualify API", description = "Bureau operations relating to manually disqualifying a "
    + "juror.")
public class ResponseDisqualifyController {
    private final ResponseDisqualifyService responseDisqualifyService;

    @Autowired
    public ResponseDisqualifyController(final ResponseDisqualifyService responseDisqualifyService) {
        Assert.notNull(responseDisqualifyService, "ResponseDisqualifyService cannot be null");
        this.responseDisqualifyService = responseDisqualifyService;
    }

    @GetMapping
    @Operation(summary = "list of potential disqualification reasons",
        description = "Retrieve list of potential disqualification reasons")
    public ResponseEntity<DisqualifyReasonsDto> getDisqualifyReasons() throws DisqualifyException {
        List<DisqualifyCodeDto> disqualifyReasons = responseDisqualifyService.getDisqualifyReasons();

        // JDB-1458: We need to remove "E - Electronic Police Check Failure" from the list
        disqualifyReasons.removeIf(disqualifyCode ->
            DisCode.ELECTRONIC_POLICE_CHECK_FAILURE.equals(disqualifyCode.getDisqualifyCode()));

        return ResponseEntity.ok().body(new DisqualifyReasonsDto(disqualifyReasons));
    }

    @PostMapping("/{jurorId}")
    @Operation(summary = "disqualification for a specific juror",
        description = "Mark a single juror with a certain disqualification code by their juror number")
    public ResponseEntity<Void> disqualifyJuror(
        @Parameter(description = "Valid juror number", required = true) @PathVariable String jurorId,
        BureauJwtAuthentication jwt,
        @Validated @RequestBody DisqualifyCodeDto disqualifyCodeDto) throws DisqualifyException {
        assertJurorNumberPathVariable(jurorId);
        final BureauJwtPayload jwtPayload = (BureauJwtPayload) jwt.getPrincipal();
        if (null == disqualifyCodeDto.getDisqualifyCode() || null == disqualifyCodeDto.getVersion()) {
            // there is either no body or no version present in the request
            throw new DisqualifyException.RequestIsMissingDetails(jurorId);
        }

        log.info(
            "Attempting to disqualify juror {} using code {}, by user {}",
            jurorId,
            disqualifyCodeDto.getDisqualifyCode(),
            jwtPayload.getLogin()
        );
        responseDisqualifyService.disqualifyJuror(jurorId, disqualifyCodeDto, jwtPayload.getLogin());
        return ResponseEntity.ok().build();
    }

    /**
     * Response DTO for disqualification reasons endpoint.
     *
     * @see ResponseDisqualifyController#getDisqualifyReasons()
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Schema(description = "List of Disqualification Reasons")
    public static class DisqualifyReasonsDto {
        @JsonProperty("data")
        @Schema(description = "List of valid disqualify codes", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<DisqualifyCodeDto> data;
    }

    /**
     * Response DTO for single disqualification reason.
     *
     * @see ResponseDisqualifyController#getDisqualifyReasons()
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Schema(description = "Disqualification Reason")
    public static class DisqualifyCodeDto {

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Min(0)
        @Max(Integer.MAX_VALUE)
        @Schema(description = "Optimistic locking version", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer version;

        @JsonProperty("disqualifyCode")
        @Schema(description = "Single-character disqualify code", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "A")
        private String disqualifyCode;

        @JsonProperty("description")
        @Schema(description = "Description of disqualification code", example = "On Bail")
        private String description;

        public DisqualifyCodeDto(DisqualifiedCode disqualifyCodeEntity) {
            if (!Objects.isNull(disqualifyCodeEntity)) {
                this.disqualifyCode = disqualifyCodeEntity.getDisqualifiedCode();
                this.description = disqualifyCodeEntity.getDescription();
            }
        }
    }
}

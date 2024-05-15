package uk.gov.hmcts.juror.api.bureau.controller;

import io.jsonwebtoken.lang.Assert;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.bureau.exception.ExcusalException;
import uk.gov.hmcts.juror.api.bureau.service.ResponseDeferralService;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.validation.ValidationHelper;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * API endpoints controller for manual deferral operations.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/bureau/juror/defer", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Bureau Deferral API", description = "Bureau operations relating to deferrals")
public class ResponseDeferralController {
    private final ResponseDeferralService responseDeferralService;

    @Autowired
    public ResponseDeferralController(final ResponseDeferralService responseDeferralService) {
        Assert.notNull(responseDeferralService, "ResponseDeferralService cannot be null.");
        this.responseDeferralService = responseDeferralService;
    }

    @PostMapping("/{jurorId}")
    @Operation(summary = "deferral for a specific juror",
        description = "Mark a single juror respose with a deferral decision")
    public ResponseEntity<Void> processJurorDeferral(
        @Parameter(description = "Valid juror number") @PathVariable String jurorId,
        @Parameter(hidden = true) BureauJwtAuthentication jwt,
        @Parameter(description = "Deferral update details") @Validated
        @RequestBody DeferralDto deferralDto) throws ExcusalException {
        ValidationHelper.validateJurorNumberPathVariable(jurorId);
        final BureauJwtPayload jwtPayload = (BureauJwtPayload) jwt.getPrincipal();

        log.info("Deferral of {} by officer {} using {}", jurorId, jwtPayload.getLogin(), deferralDto);
        responseDeferralService.processDeferralDecision(jurorId, jwtPayload.getLogin(), deferralDto);
        return ResponseEntity.noContent().build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Manual deferral dto")
    public static class DeferralDto implements Serializable {
        @NotNull
        @Schema(description = "Optimistic locking version", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer version;

        @NotNull
        @Schema(description = "Acceptance or denial.  true = accept", requiredMode = Schema.RequiredMode.REQUIRED)
        private Boolean acceptDeferral;

        @NotEmpty
        @Size(min = 1, max = 2)
        @Schema(description = "Excusal code reason.", requiredMode = Schema.RequiredMode.REQUIRED)
        private String deferralReason;//is an EXC_CODE char

        @Future
        @Schema(description = "Future date to defer to when accepting", requiredMode = Schema.RequiredMode.REQUIRED)
        private LocalDate deferralDate;
    }
}

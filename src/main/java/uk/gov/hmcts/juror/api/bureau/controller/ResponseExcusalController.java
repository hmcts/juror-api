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
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCodeEntity;
import uk.gov.hmcts.juror.api.bureau.exception.ExcusalException;
import uk.gov.hmcts.juror.api.bureau.service.ResponseExcusalService;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.domain.ExcusalCode;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.juror.api.bureau.controller.ResponseUpdateController.validateJurorNumberPathVariable;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/bureau/juror/excuse", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Bureau Juror-Excusal API", description = "Bureau operations relating to manually excusing a juror.")
public class ResponseExcusalController {
    private final ResponseExcusalService responseExcusalService;

    @Autowired
    public ResponseExcusalController(final ResponseExcusalService responseExcusalService) {
        Assert.notNull(responseExcusalService, "ResponseExcusalService cannot be null");
        this.responseExcusalService = responseExcusalService;
    }

    @GetMapping
    @Operation(summary = "list of potential excusal reasons",
        description = "Retrieve list of potential excusal reasons")
    public ResponseEntity<ExcusalReasonsDto> getExcusalReasons() throws ExcusalException {
        List<ExcusalCodeDto> excusalReasons = responseExcusalService.getExcusalReasons();

        return ResponseEntity.ok().body(new ExcusalReasonsDto(excusalReasons));
    }

    @PostMapping("/{jurorId}")
    @Operation(summary = "excusal for a specific juror",
        description = "Mark a single juror with a certain excusal code by their juror number")
    public ResponseEntity<Void> excuseJuror(
        @Parameter(description = "Valid juror number", required = true) @PathVariable String jurorId,
        BureauJwtAuthentication jwt,
        @Validated @RequestBody ExcusalCodeDto excusalCodeDto) throws ExcusalException {
        validateJurorNumberPathVariable(jurorId);
        final BureauJwtPayload jwtPayload = (BureauJwtPayload) jwt.getPrincipal();
        if (null == excusalCodeDto.getExcusalCode() || null == excusalCodeDto.getVersion()) {
            // there is either no body or no version present in the request
            throw new ExcusalException.RequestIsMissingDetails(jurorId);
        }
        log.info(
            "Attempting to excuse juror {} using code {}, by user {}",
            jurorId,
            excusalCodeDto.getExcusalCode(),
            jwtPayload.getLogin()
        );
        responseExcusalService.excuseJuror(jurorId, excusalCodeDto, jwtPayload.getLogin());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reject/{jurorId}")
    @Operation(summary = "excusal-rejection for a specific juror",
        description = "Reject a single jurors excusal request")
    public ResponseEntity<Void> rejectExcusalRequest(
        @Parameter(description = "Valid juror number", required = true) @PathVariable String jurorId,
        BureauJwtAuthentication jwt,
        @Validated @RequestBody ExcusalCodeDto excusalCodeDto) throws ExcusalException {
        validateJurorNumberPathVariable(jurorId);
        final BureauJwtPayload jwtPayload = (BureauJwtPayload) jwt.getPrincipal();
        if (null == excusalCodeDto.getExcusalCode() || null == excusalCodeDto.getVersion()) {
            // there is either no body or no version present in the request
            throw new ExcusalException.RequestIsMissingDetails(jurorId);
        }
        log.info(
            "Attempting to reject excusal request for juror {} using code {}, by user {}",
            jurorId,
            excusalCodeDto.getExcusalCode(),
            jwtPayload.getLogin()
        );
        responseExcusalService.rejectExcusalRequest(jurorId, excusalCodeDto, jwtPayload.getLogin());
        return ResponseEntity.ok().build();
    }

    /**
     * Response DTO for excusal reasons endpoint.
     *
     * @see ResponseExcusalController#getExcusalReasons()
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Schema(description = "List of Excusal Reasons")
    public static class ExcusalReasonsDto {
        @JsonProperty("data")
        @Schema(description = "List of valid excusal codes", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<ExcusalCodeDto> data;
    }

    /**
     * Response DTO for single excusal reason.
     *
     * @see ResponseExcusalController#getExcusalReasons()
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Schema(description = "Excusal Reason")
    public static class ExcusalCodeDto {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Min(0)
        @Max(Integer.MAX_VALUE)
        @Schema(description = "Optimistic locking version", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer version;

        @JsonProperty("excusalCode")
        @Schema(description = "Single-character excusal code", requiredMode = Schema.RequiredMode.REQUIRED, example =
            "B")
        private String excusalCode;

        @JsonProperty("description")
        @Schema(description = "Description of excusal code", example = "Student")
        private String description;

        @Deprecated(forRemoval = true)
        public ExcusalCodeDto(ExcusalCodeEntity excusalCodeEntity) {
            if (!Objects.isNull(excusalCodeEntity)) {
                this.excusalCode = excusalCodeEntity.getExcusalCode();
                this.description = excusalCodeEntity.getDescription();
            }
        }

        public ExcusalCodeDto(ExcusalCode excusalCodeEntity) {
            if (!Objects.isNull(excusalCodeEntity)) {
                this.excusalCode = excusalCodeEntity.getCode();
                this.description = excusalCodeEntity.getDescription();
            }
        }
    }
}

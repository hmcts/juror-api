package uk.gov.hmcts.juror.api.bureau.controller;


import io.jsonwebtoken.lang.Assert;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
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
import uk.gov.hmcts.juror.api.bureau.service.JurorCommsSuperUrgentSendToCourtService;
import uk.gov.hmcts.juror.api.bureau.service.ResponseSendToCourtService;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;

import java.io.Serializable;

import static uk.gov.hmcts.juror.api.bureau.controller.ResponseUpdateController.assertJurorNumberPathVariable;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/bureau/juror/tocourt", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Bureau Juror-Send to court API", description = "Bureau sending response to  court directly")

public class ResponseSendToCourtController {

    private final ResponseSendToCourtService sendToCourtServiceres;
    private final JurorCommsSuperUrgentSendToCourtService jurorCommsSuperUrgentSendToCourtService;

    @Autowired
    public ResponseSendToCourtController(
        final ResponseSendToCourtService sendToCourtService,
        final JurorCommsSuperUrgentSendToCourtService jurorCommsSuperUrgentSendToCourtService) {
        Assert.notNull(sendToCourtService, "sendToCourtService cannot be null");
        Assert.notNull(jurorCommsSuperUrgentSendToCourtService, "bureauProcessService cannot be null");
        this.sendToCourtServiceres = sendToCourtService;
        this.jurorCommsSuperUrgentSendToCourtService = jurorCommsSuperUrgentSendToCourtService;
    }


    @PostMapping("/{jurorId}")
    @Operation(summary = "send to court for a specific juror",
        description = "by juror number")
    public ResponseEntity<Void> responseToCourt(
        @Parameter(description = "Valid juror number", required = true) @PathVariable String jurorId,
        BureauJwtAuthentication jwt,
        @Validated @RequestBody SendToCourtDto sendToCourtDto) throws ExcusalException {
        assertJurorNumberPathVariable(jurorId);
        final BureauJwtPayload jwtPayload = (BureauJwtPayload) jwt.getPrincipal();

        log.info("Attempting to send juror {} using code {}, by user {}", jurorId, jwtPayload.getLogin());
        sendToCourtServiceres.sendResponseToCourt(jurorId, sendToCourtDto, jwtPayload.getLogin());

        //Added by Baharak Askarikeya - 02/07/19 - to send an email and a sms to super urgent send to court - JDB-3996
        jurorCommsSuperUrgentSendToCourtService.processSuperUrgent(jurorId);

        return ResponseEntity.ok().build();
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Sent to court dto")
    public static class SendToCourtDto implements Serializable {
        @NotNull
        @Schema(description = "Optimistic locking version", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer version;

    }

}

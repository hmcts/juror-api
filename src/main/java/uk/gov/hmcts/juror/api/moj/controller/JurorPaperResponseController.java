package uk.gov.hmcts.juror.api.moj.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.controller.request.CjsEmploymentDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.EligibilityDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPaperResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ReasonableAdjustmentDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ReplyTypeDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.SignatureDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorPaperResponseDetailDto;
import uk.gov.hmcts.juror.api.moj.controller.response.summonsmanagement.SaveJurorPaperReplyResponseDto;
import uk.gov.hmcts.juror.api.moj.service.JurorPaperResponseService;
import uk.gov.hmcts.juror.api.moj.service.StraightThroughProcessorService;
import uk.gov.hmcts.juror.api.moj.service.SummonsReplyStatusUpdateService;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

/**
 * API endpoints controller for Juror Paper Response Endpoints.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/moj/juror-paper-response", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Validated
@Tag(name = "Summons Management")
@SuppressWarnings("PMD.ExcessiveImports")
public class JurorPaperResponseController {

    @NonNull
    private final JurorPaperResponseService jurorPaperResponseService;
    @NonNull
    private final StraightThroughProcessorService straightThroughProcessorService;
    @NonNull
    private final SummonsReplyStatusUpdateService summonsReplyStatusUpdateService;

    /**
     * Return the existing juror paper response details.
     *
     * @param payload     JSON Web Token principal
     * @param jurorNumber The juror number to show the details of
     *
     * @return Juror Paper Response details
     */
    @GetMapping(path = "/juror/{jurorNumber}")
    @Operation(summary = "Find juror paper response details by Juror Number",
        description = "The Juror Paper response details")
    public ResponseEntity<JurorPaperResponseDetailDto> retrieveJurorById(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @Parameter(description = "Juror number",
            required = true)
        @PathVariable String jurorNumber) {
        final JurorPaperResponseDetailDto jurorPaperResponseDto = jurorPaperResponseService.getJurorPaperResponse(
            jurorNumber,
            payload
        );
        return ResponseEntity.ok(jurorPaperResponseDto);
    }

    /**
     * Process the Juror Paper Response from the Officer.
     *
     * @param payload          JSON Web Token principal
     * @param paperResponseDto Response information to persist
     *
     * @return Success message
     */
    @PostMapping(path = "/response")
    @Operation(summary = "Save a Juror paper response", description = "Process the Juror Paper Response from the "
        + "Officer and save")
    public ResponseEntity<SaveJurorPaperReplyResponseDto> respondToSummons(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @RequestBody @Valid JurorPaperResponseDto paperResponseDto) {
        jurorPaperResponseService.saveResponse(payload, paperResponseDto);

        SaveJurorPaperReplyResponseDto responseDto = new SaveJurorPaperReplyResponseDto();
        responseDto.setStraightThroughAcceptance(straightThroughProcessorService
            .isValidForStraightThroughAcceptance(paperResponseDto.getJurorNumber(), payload.getOwner(),
                BooleanUtils.toBoolean(paperResponseDto.getCanServeOnSummonsDate())));

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }


    /**
     * Update a Juror Paper Response CJS details for a given Juror.
     *
     * @param payload                 JSON Web Token principal
     * @param cjsEmploymentDetailsDto Response information to persist
     * @param jurorNumber             The juror number to update the CJS details for
     */
    @PatchMapping(path = "/juror/{jurorNumber}/details/cjs")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Update an existing Juror paper response CJS employment details")
    public void updatePaperSummonsCjsDetails(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @RequestBody @Valid CjsEmploymentDetailsDto cjsEmploymentDetailsDto,
        @Parameter(description = "Valid juror number", required = true)
        @Size(min = 9, max = 9) @PathVariable("jurorNumber")
        @Valid String jurorNumber) {
        jurorPaperResponseService.updateCjsDetails(payload, cjsEmploymentDetailsDto, jurorNumber);
    }

    /**
     * Update a Juror Paper Response Special needs details for a given Juror.
     *
     * @param payload                        JSON Web Token principal
     * @param reasonableAdjustmentDetailsDto Response information to persist
     * @param jurorNumber                    The juror number to update the special needs details for
     */
    @PatchMapping(path = "/juror/{jurorNumber}/details/special-needs")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Update an existing Juror paper response special needs details")
    public void updatePaperSpecialNeedsDetails(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @RequestBody @Valid ReasonableAdjustmentDetailsDto reasonableAdjustmentDetailsDto,
        @Parameter(description = "Valid juror number", required = true)
        @Size(min = 9, max = 9) @PathVariable("jurorNumber")
        @Valid String jurorNumber) {
        jurorPaperResponseService.updateReasonableAdjustmentsDetails(payload, reasonableAdjustmentDetailsDto,
            jurorNumber);
    }

    /**
     * Update a Juror Paper Response eligibility details for a given Juror.
     *
     * @param payload               JSON Web Token principal
     * @param eligibilityDetailsDto Response information to persist
     * @param jurorNumber           The juror number to update the eligibility details for
     */
    @PatchMapping(path = "/juror/{jurorNumber}/details/eligibility")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Update an existing Juror paper response eligibility details")
    public void updateJurorEligibilityDetails(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @RequestBody @Valid EligibilityDetailsDto eligibilityDetailsDto,
        @Parameter(description = "Valid juror number", required = true)
        @Size(min = 9, max = 9) @PathVariable("jurorNumber")
        @Valid String jurorNumber) {
        jurorPaperResponseService.updateJurorEligibilityDetails(payload, eligibilityDetailsDto, jurorNumber);
    }

    /**
     * Update a Juror Paper Response reply type details for a given Juror.
     *
     * @param payload             JSON Web Token principal
     * @param replyTypeDetailsDto Response information to persist
     * @param jurorNumber         The juror number to update the reply type details for
     */
    @PatchMapping(path = "/juror/{jurorNumber}/details/reply-type")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Update an existing Juror paper response reply type details")
    public void updateJurorReplyTypeDetails(@Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
                                            @RequestBody @Valid ReplyTypeDetailsDto replyTypeDetailsDto,
                                            @Parameter(description = "Valid juror number", required = true)
                                            @Size(min = 9, max = 9) @PathVariable("jurorNumber")
                                            @Valid String jurorNumber) {
        jurorPaperResponseService.updateJurorReplyTypeDetails(payload, replyTypeDetailsDto, jurorNumber);
    }

    /**
     * Update a Juror Paper Response signature for a given Juror.
     *
     * @param payload             JSON Web Token principal
     * @param signatureDetailsDto Response information to persist
     * @param jurorNumber         The juror number to update the signature details for
     */
    @PatchMapping(path = "/juror/{jurorNumber}/details/signature")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Update an existing Juror paper response signature details")
    public void updateJurorSignatureDetails(@Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
                                            @RequestBody @Valid SignatureDetailsDto signatureDetailsDto,
                                            @Parameter(description = "Valid juror number", required = true)
                                            @Size(min = 9, max = 9) @PathVariable("jurorNumber")
                                            @Valid String jurorNumber) {
        jurorPaperResponseService.updateJurorSignatureDetails(payload, signatureDetailsDto, jurorNumber);
    }

    @PutMapping("update-status/{jurorNumber}/{processingStatus}")
    @Operation(summary = "Update the processing status of a Juror Paper Response",
        description = "Juror records with a pending response can be uniquely identified by just the Juror Number")
    public ResponseEntity<Void> updateJurorPaperResponseStatus(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @Parameter(description = "Valid Juror Number",
            required = true)
        @Size(min = 9, max = 9) @PathVariable("jurorNumber")
        @JurorNumber @Valid String jurorNumber,
        @Parameter(description = "New Processing Status",
            required = true)
        @PathVariable("processingStatus")
        @Valid ProcessingStatus processingStatus) {
        summonsReplyStatusUpdateService.updateJurorResponseStatus(jurorNumber, processingStatus, payload);
        return ResponseEntity.accepted().build();
    }
}

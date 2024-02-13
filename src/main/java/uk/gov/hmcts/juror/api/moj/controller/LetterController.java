package uk.gov.hmcts.juror.api.moj.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.config.security.IsBureauUser;
import uk.gov.hmcts.juror.api.config.security.IsCourtUser;
import uk.gov.hmcts.juror.api.moj.controller.request.AdditionalInformationDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ReissueLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ReissueLetterRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.CourtLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.PrintLettersRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.ReissueLetterListResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.LetterListResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.PrintLetterDataResponseDto;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.ReissueLetterService;
import uk.gov.hmcts.juror.api.moj.service.letter.CourtLetterPrintService;
import uk.gov.hmcts.juror.api.moj.service.letter.CourtLetterService;
import uk.gov.hmcts.juror.api.moj.service.letter.RequestInformationLetterService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.List;

/**
 * API endpoints related to letters.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/moj/letter", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@Tag(name = "Summons Management")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@SuppressWarnings("PMD.ExcessiveImports")
public class LetterController {

    @NonNull
    private final RequestInformationLetterService requestInformationLetterService;
    @NonNull
    private final CourtLetterService courtLetterService;
    @NonNull
    private final ReissueLetterService reissueLetterService;
    @NonNull
    private final CourtLetterPrintService courtLetterPrintService;

    @PostMapping(path = "/request-information")
    @Operation(summary = "Request information",
        description = "Request information from the juror related to the juror response form")
    public ResponseEntity<String> requestInformation(
        @Parameter(hidden = true) BureauJwtAuthentication auth,
        @RequestBody @Valid AdditionalInformationDto additionalInformationDto) {
        final String jurorNumber = additionalInformationDto.getJurorNumber();
        log.trace("Process to queue the Request Letter started for juror {} ", jurorNumber);

        BureauJWTPayload payload = (BureauJWTPayload) auth.getPrincipal();
        if (!JurorDigitalApplication.JUROR_OWNER.equalsIgnoreCase(payload.getOwner())) {
            throw new MojException.Forbidden("Request additional information "
                + "letter is a Bureau only process", null);
        }

        requestInformationLetterService.requestInformation(payload, additionalInformationDto);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(String.format("Request Letter queued for juror number %s", jurorNumber));
    }

    @PostMapping(path = "/reissue-letter-list")
    @Operation(summary = "GET With Body - Reissue letter list", description = "Request a list of letters for reissue "
        + "to a juror")
    @IsBureauUser
    public ResponseEntity<ReissueLetterListResponseDto> reissueLetterList(
        @RequestBody @Valid @NotNull ReissueLetterListRequestDto reIssueLetterListRequestDto) {

        ReissueLetterListResponseDto reissueLetterListResponseDto = reissueLetterService.reissueLetterList(
            reIssueLetterListRequestDto);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(reissueLetterListResponseDto);
    }

    @PostMapping("/reissue-letter")
    @Operation(description = "Reissue letters to jurors as Bureau officer")
    @IsBureauUser
    public ResponseEntity<String> reissueLetter(
        @RequestBody @Valid @NotNull ReissueLetterRequestDto request) {

        reissueLetterService.reissueLetter(request);

        return ResponseEntity.ok("Letters reissued");
    }

    @DeleteMapping("/delete-pending-letter")
    @Operation(description = "Delete a pending letter to jurors as Bureau officer")
    @IsBureauUser
    public ResponseEntity<String> deletePendingLetter(
        @RequestBody @Valid @NotNull ReissueLetterRequestDto request) {

        reissueLetterService.deletePendingLetter(request);

        return ResponseEntity.ok("Letters reissued");
    }

    @PostMapping(path = "/court-letter-list")
    @Operation(summary = "GET With Body - Court letter list", description = "Request a list of jurors eligible "
        + "for court letters to be issued/re-issued.")
    @IsCourtUser
    public ResponseEntity<LetterListResponseDto> courtLetterList(
        @RequestBody @Valid CourtLetterListRequestDto courtLetterListRequestDto) {

        LetterListResponseDto courtLetterListResponseDto =
            courtLetterService.getEligibleList(courtLetterListRequestDto);

        return ResponseEntity.status(HttpStatus.OK).body(courtLetterListResponseDto);
    }

    @PostMapping(path = "/print-court-letter")
    @Operation(summary = "GET With Body - Get court letter print data",
        description = "Print/Reissue selected court letters")
    @IsCourtUser
    public ResponseEntity<List<PrintLetterDataResponseDto>> printCourtLetters(
        @RequestBody @Valid PrintLettersRequestDto lettersRequestDto) {
        List<PrintLetterDataResponseDto> dto = courtLetterPrintService.getPrintLettersData(lettersRequestDto,
            SecurityUtil.getActiveLogin());
        return ResponseEntity.ok().body(dto);
    }
}


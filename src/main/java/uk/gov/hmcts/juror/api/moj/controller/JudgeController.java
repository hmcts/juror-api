package uk.gov.hmcts.juror.api.moj.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.JudgeListDto;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.trial.JudgeService;

@RestController
@RequestMapping(value = "/api/v1/moj/trial/judge", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Judge")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class JudgeController {

    @NonNull
    private final JudgeService judgeService;

    private static final String BUREAU_USER = "400";

    /**
     * Retrieves a list of judges.
     * @param  payload - login information
     * @throws MojException.Forbidden - this endpoint is for court users only
     */
    @GetMapping("/list")
    @Operation(summary = "Retrieves a list of judges for court location(s)")
    public ResponseEntity<JudgeListDto> getJudgesForCourtLocations(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload) {
        if (BUREAU_USER.equals(payload.getOwner())) {
            throw new MojException.Forbidden("Bureau users are not allowed to use this service", null);
        }

        JudgeListDto dto = judgeService.getJudgeForCourtLocation(payload.getOwner());
        return ResponseEntity.ok().body(dto);
    }
}

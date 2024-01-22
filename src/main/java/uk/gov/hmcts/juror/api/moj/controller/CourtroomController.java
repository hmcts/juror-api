package uk.gov.hmcts.juror.api.moj.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.CourtroomsListDto;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.trial.CourtroomService;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/moj/trial/courtrooms", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Courtrooms")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CourtroomController {

    @NotNull
    private final CourtroomService courtroomService;

    private static final String BUREAU_USER = "400";

    /**
     * Gets a list of courtrooms for a court location(s).
     * @param payload - logged in user
     * @return ResponseEntity
     * @throws MojException.Forbidden - thrown if the user is a bureau user
     */

    @GetMapping("/list")
    public ResponseEntity<List<CourtroomsListDto>> getListOfCourtroomsForLocation(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload) {
        if (BUREAU_USER.equals(payload.getOwner())) {
            throw new MojException.Forbidden("This service is for court users only", null);
        }
        return ResponseEntity.ok().body(courtroomService.getCourtroomsForLocation(payload.getStaff().getCourts()));
    }
}

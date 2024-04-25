package uk.gov.hmcts.juror.api.moj.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.bureau.controller.request.StaffAssignmentRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffAssignmentResponseDto;
import uk.gov.hmcts.juror.api.bureau.exception.BureauOptimisticLockingException;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.config.security.IsBureauUser;
import uk.gov.hmcts.juror.api.moj.service.staff.StaffService;

@RestController
@RequestMapping(value = "/api/v1/moj/staff", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@Tag(name = "Staff Management")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@IsBureauUser
public class StaffController {

    private final StaffService staffService;

    /**
     * Update staff assignment to an unresolved Juror Response.
     *
     * @param requestDto Assignment changes
     * @param principal  Current user Authentication
     * @return Change summary
     * @throws BureauOptimisticLockingException Response data from the UI is outdated. Version mismatch with DB.
     */
    @PostMapping(path = "/assign")
    @Operation(summary = "Update staff assignment to juror response",
        description = "Update staff assignment to juror response")
    public ResponseEntity<StaffAssignmentResponseDto> changeStaffAssignment(
        @RequestBody @Validated StaffAssignmentRequestDto requestDto,
        @Parameter(hidden = true) BureauJwtAuthentication principal) throws BureauOptimisticLockingException {
        log.info("Processing assignment {} by '{}'", requestDto, principal);
        final BureauJwtPayload jwtPayload = (BureauJwtPayload) principal.getPrincipal();
        try {
            final StaffAssignmentResponseDto responseDto = staffService.changeAssignment(
                requestDto,
                jwtPayload.getLogin()
            );
            log.info("Assignment updated: {}", responseDto);
            return ResponseEntity.ok()
                .body(responseDto);
        } catch (OptimisticLockingFailureException olfe) {
            log.warn("Failed to change assignment: {}.  Updated by another user!", olfe.getMessage());
            throw new BureauOptimisticLockingException(olfe);
        }
    }

}

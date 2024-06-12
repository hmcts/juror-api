package uk.gov.hmcts.juror.api.bureau.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.bureau.controller.request.AssignmentsMultiRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.request.MultipleStaffAssignmentDto;
import uk.gov.hmcts.juror.api.bureau.controller.request.StaffAssignmentRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.AssignmentsListDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.OperationFailureListDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffAssignmentResponseDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffDetailDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffListDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffRosterResponseDto;
import uk.gov.hmcts.juror.api.bureau.exception.BureauOptimisticLockingException;
import uk.gov.hmcts.juror.api.bureau.service.UserService;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.stream.Collectors;

/**
 * API endpoints controller for staff-related operations.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/bureau/staff", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Bureau Staff API", description = "Bureau operations relating to staff members")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class BureauStaffController {

    private final UserService userService;


    @GetMapping
    @Operation(summary = "Staff list",
        description = "Retrieve a list of all bureau staff members")
    public ResponseEntity<StaffListDto> getAll(@Parameter(hidden = true) BureauJwtAuthentication principal) {
        if (!SecurityUtil.isBureauManager()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.ok(userService.getAll());
    }

    @GetMapping("/{login}")
    @Operation(summary = "Get details for one staff member",
        description = "Get details for one staff member")
    public ResponseEntity<StaffDetailDto> getOne(
        @Parameter(description = "Staff member login") @PathVariable String login,
        @Parameter(hidden = true) BureauJwtAuthentication principal) {
        if (!SecurityUtil.isBureauManager()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            return ResponseEntity.ok(userService.getOne(login));
        } catch (UserService.NoMatchForLoginException e) {
            log.info("Search request for login {} had no match: {}", login, e);
            return ResponseEntity.notFound().build();
        }

    }

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
        @Parameter(hidden = true) BureauJwtAuthentication principal) {
        log.info("Processing assignment {} by '{}'", requestDto, principal);
        final BureauJwtPayload jwtPayload = (BureauJwtPayload) principal.getPrincipal();
        try {
            final StaffAssignmentResponseDto responseDto = userService.changeAssignment(
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

    @GetMapping(path = "/roster")
    @Operation(summary = "List active staff members",
        description = "List active staff members")
    public ResponseEntity<StaffRosterResponseDto> activeStaffRoster() {
        return ResponseEntity.ok(userService.activeStaffRoster());
    }

    /**
     * Update multiple assignments to unresolved juror responses.
     *
     * @param principal Current user Authentication
     * @return {@link org.springframework.http.HttpStatus#NO_CONTENT} on successful update
     * @throws BureauOptimisticLockingException Response data from the UI is outdated. Version mismatch with DB.
     */
    @PostMapping(path = "/assign-multi")
    @Operation(summary = "/bureau/staff/assign-multi",
        description = "Assign multiple responses to a single staff member")
    public ResponseEntity<OperationFailureListDto> changeMultipleAssignments(
        @RequestBody @Validated MultipleStaffAssignmentDto requestDto,
        @Parameter(hidden = true) BureauJwtAuthentication principal) {
        final BureauJwtPayload jwtPayload = (BureauJwtPayload) principal.getPrincipal();

        try {
            log.info("Processing multiple assignments {} by '{}'", requestDto, principal);
            OperationFailureListDto failuresList = userService.multipleChangeAssignment(
                requestDto,
                jwtPayload.getLogin()
            );
            if (failuresList.getFailureDtos().isEmpty()) {
                log.info("Assigned {} responses to {}", requestDto.getResponses().size(), requestDto.getAssignTo());
            } else {
                int successes = requestDto.getResponses().size() - failuresList.getFailureDtos().size();
                log.info("Assigned {} responses to {}", successes, requestDto.getAssignTo());
                log.info(
                    "{} responses were unable to be assigned to {}",
                    failuresList.getFailureDtos().size(),
                    requestDto.getAssignTo()
                );
            }
            return ResponseEntity.accepted().body(failuresList);
        } catch (OptimisticLockingFailureException olfe) {
            log.warn("Failed to change assignment: {}.  Updated by another user!", olfe.getMessage());
            throw new BureauOptimisticLockingException(olfe);
        }
    }

    /**
     * Retrieve staff assigned-to details for a list of responses.
     *
     * @param principal Current user Authentication
     * @return TODO {@link AssignmentsListDto} List of responses and their staff assignments
     */
    @PostMapping(path = "/assignments-multi")
    @Operation(summary = "/bureau/staff/assignments-multi",
        description = "Retrieve staff assigned-to details for a list of responses")
    public ResponseEntity<AssignmentsListDto> getStaffAssignments(
        @RequestBody @Validated AssignmentsMultiRequestDto requestDto,
        @Parameter(hidden = true) BureauJwtAuthentication principal) {
        final BureauJwtPayload jwtPayload = (BureauJwtPayload) principal.getPrincipal();

        // filter out any juror numbers not nine characters in length
        requestDto.setJurorNumbers(requestDto.getJurorNumbers().stream()
            .filter(s -> !s.isEmpty() && s.length() <= 9)
            .collect(Collectors.toList())
        );
        log.debug("Retrieving staff assignments for responses: {}", requestDto);
        AssignmentsListDto assignmentsListDto = userService.getStaffAssignments(requestDto, jwtPayload.getLogin());
        return ResponseEntity.ok(assignmentsListDto);
    }
}

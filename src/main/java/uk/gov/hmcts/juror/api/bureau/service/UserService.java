package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.controller.request.AssignmentsMultiRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.request.MultipleStaffAssignmentDto;
import uk.gov.hmcts.juror.api.bureau.controller.request.ReassignResponsesDto;
import uk.gov.hmcts.juror.api.bureau.controller.request.StaffAssignmentRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.AssignmentsListDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.OperationFailureListDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffAssignmentResponseDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffDetailDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffListDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffRosterResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;

/**
 * Operations for modifying the staff assignment of juror responses.
 */
public interface UserService {
    /**
     * Change the staff assignment.
     *
     * @param staffAssignmentRequestDto Staff assignment request payload.
     * @param currentUser               The user carrying out this operation.
     * @return Assignment success
     * @throws StaffAssignmentException Failed to assign the staff member to the response.
     */
    StaffAssignmentResponseDto changeAssignment(StaffAssignmentRequestDto staffAssignmentRequestDto,
                                                String currentUser);

    /**
     * Return a complete list of active staff.
     *
     * @return All staff members dto
     */
    StaffRosterResponseDto activeStaffRoster();

    /**
     * Get all staff members.
     *
     * @return staff list
     */
    StaffListDto getAll();

    /**
     * Gets detail view for one staff member.
     *
     * @return detail view
     */
    StaffDetailDto getOne(String login) throws NoMatchForLoginException;

    /**
     * Assign an urgent juror response immediately.
     *
     * @param urgentJurorResponse Previously persisted juror response entity
     */
    void assignUrgentResponse(DigitalResponse urgentJurorResponse);

    User findByUsername(String activeLogin);


    class NoMatchForLoginException extends Exception {
        NoMatchForLoginException(String login) {
            super(String.format("No match found for login %s", login));
        }
    }

    /**
     * Get multiple staff assignments.
     *
     * @param responseListDto List of response numbers to retrieve information on
     * @param currentUser     The user carrying out this operation.
     */
    AssignmentsListDto getStaffAssignments(AssignmentsMultiRequestDto responseListDto,
                                           String currentUser);

    /**
     * Change multiple staff assignments.
     *
     * @param multipleStaffAssignmentDto Multiple staff assignment request payload.
     * @param currentUser                The user carrying out this operation.
     * @throws StaffAssignmentException Failed to assign the staff member to the response.
     */
    OperationFailureListDto multipleChangeAssignment(MultipleStaffAssignmentDto multipleStaffAssignmentDto,
                                                     String currentUser);

    /**
     * Reassign multiple responses depending on type, then deactivate staff member.
     *
     * @param reassignResponsesDto Reassignment request payload.
     * @param auditorUsername      The user carrying out this operation.
     * @throws uk.gov.hmcts.juror.api.bureau.exception.ReassignException.StaffMemberNotFound Failed to assign
     *                                                                                       responses to specified
     *                                                                                       staff member.
     */
    void reassignResponses(String auditorUsername, ReassignResponsesDto reassignResponsesDto);
}

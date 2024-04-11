package uk.gov.hmcts.juror.api.moj.service.staff;

import uk.gov.hmcts.juror.api.bureau.controller.request.StaffAssignmentRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffAssignmentResponseDto;

public interface StaffService {
    StaffAssignmentResponseDto changeAssignment(StaffAssignmentRequestDto staffAssignmentRequestDto,
                                                String currentUser);
}

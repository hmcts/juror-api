package uk.gov.hmcts.juror.api.moj.service.jurormanagement;

import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAppearanceDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorsToDismissRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.RetrieveAttendanceDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.UpdateAttendanceDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorAppearanceResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorsToDismissResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.jurormanagement.AttendanceDetailsResponse;

import java.time.LocalDate;

public interface JurorAppearanceService {

    JurorAppearanceResponseDto.JurorAppearanceResponseData processAppearance(
        BureauJWTPayload payload, JurorAppearanceDto jurorAppearanceDto);

    JurorAppearanceResponseDto getAppearanceRecords(String locCode, LocalDate date, BureauJWTPayload payload);

    boolean hasAppearances(String jurorNumber);

    AttendanceDetailsResponse retrieveAttendanceDetails(BureauJWTPayload payload, RetrieveAttendanceDetailsDto request);

    AttendanceDetailsResponse updateAttendance(BureauJWTPayload payload, UpdateAttendanceDto request);

    AttendanceDetailsResponse deleteAttendance(BureauJWTPayload payload, UpdateAttendanceDto request);

    JurorsToDismissResponseDto retrieveJurorsToDismiss(JurorsToDismissRequestDto request);
}

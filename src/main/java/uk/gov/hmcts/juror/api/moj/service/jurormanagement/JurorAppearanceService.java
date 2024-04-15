package uk.gov.hmcts.juror.api.moj.service.jurormanagement;

import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.AddAttendanceDayDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAppearanceDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorsToDismissRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.JurorNonAttendanceDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.ModifyConfirmedAttendanceDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.RetrieveAttendanceDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.UpdateAttendanceDateDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.UpdateAttendanceDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorAppearanceResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorsOnTrialResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorsToDismissResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.jurormanagement.AttendanceDetailsResponse;

import java.time.LocalDate;

@SuppressWarnings("PMD.TooManyMethods")
public interface JurorAppearanceService {

    void addAttendanceDay(BureauJwtPayload payload, AddAttendanceDayDto dto);

    JurorAppearanceResponseDto.JurorAppearanceResponseData processAppearance(
        BureauJwtPayload payload, JurorAppearanceDto jurorAppearanceDto);

    JurorAppearanceResponseDto getAppearanceRecords(String locCode, LocalDate date, BureauJwtPayload payload);

    boolean hasAppearances(String jurorNumber);

    AttendanceDetailsResponse retrieveAttendanceDetails(BureauJwtPayload payload, RetrieveAttendanceDetailsDto request);

    AttendanceDetailsResponse updateAttendance(BureauJwtPayload payload, UpdateAttendanceDto request);

    String updateAttendanceDate(UpdateAttendanceDateDto request);

    AttendanceDetailsResponse deleteAttendance(BureauJwtPayload payload, UpdateAttendanceDto request);

    JurorsToDismissResponseDto retrieveJurorsToDismiss(JurorsToDismissRequestDto request);

    void addNonAttendance(JurorNonAttendanceDto request);

    JurorsOnTrialResponseDto retrieveJurorsOnTrials(String locationCode, LocalDate attendanceDate);

    void confirmJuryAttendance(UpdateAttendanceDto request);

    void modifyConfirmedAttendance(ModifyConfirmedAttendanceDto request);
    
    boolean hasAttendances(String jurorNumber);
}

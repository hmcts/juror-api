package uk.gov.hmcts.juror.api.moj.service.jurormanagement;

import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.AddAttendanceDayDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorAppearanceDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorsToDismissRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.ConfirmAttendanceDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.JurorNonAttendanceDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.ModifyConfirmedAttendanceDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.RetrieveAttendanceDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.UpdateAttendanceDateDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.UpdateAttendanceDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorAppearanceResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorsOnTrialResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorsToDismissResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.jurormanagement.AttendanceDetailsResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.jurormanagement.UnconfirmedJurorResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.JurorStatusGroup;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("PMD.TooManyMethods")
public interface JurorAppearanceService {

    void addAttendanceDay(BureauJwtPayload payload, AddAttendanceDayDto dto);

    JurorAppearanceResponseDto.JurorAppearanceResponseData processAppearance(
        BureauJwtPayload payload, JurorAppearanceDto jurorAppearanceDto);

    JurorAppearanceResponseDto getAppearanceRecords(String locCode, LocalDate date, BureauJwtPayload payload,
                                                    JurorStatusGroup group);

    boolean hasAppearances(String jurorNumber);

    Optional<Appearance> getFirstAppearanceWithAuditNumber(String juryAuditNumber, Collection<String> locCodes);

    AttendanceDetailsResponse retrieveAttendanceDetails(BureauJwtPayload payload, RetrieveAttendanceDetailsDto request);

    AttendanceDetailsResponse updateAttendance(BureauJwtPayload payload, UpdateAttendanceDto request);

    String updateAttendanceDate(UpdateAttendanceDateDto request);

    void markJurorAsAbsent(BureauJwtPayload payload, UpdateAttendanceDto.CommonData request);

    JurorsToDismissResponseDto retrieveJurorsToDismiss(JurorsToDismissRequestDto request);

    void addNonAttendance(JurorNonAttendanceDto request);

    void addNonAttendanceBulk(List<JurorNonAttendanceDto> request);

    JurorsOnTrialResponseDto retrieveJurorsOnTrials(String locationCode, LocalDate attendanceDate);

    void confirmJuryAttendance(UpdateAttendanceDto request);

    void modifyConfirmedAttendance(ModifyConfirmedAttendanceDto request);

    boolean hasAttendances(String jurorNumber);

    Optional<Appearance> getAppearance(String jurorNumber,
                                       LocalDate appearanceDate,
                                       String locCode);

    void realignAttendanceType(Appearance appearance);

    void realignAttendanceType(String jurorNumber);

    UnconfirmedJurorResponseDto retrieveUnconfirmedJurors(String locationCode, LocalDate attendanceDate);

    void confirmAttendance(ConfirmAttendanceDto request);

    List<Appearance> getAppearancesSince(String jurorNumber, LocalDate appearanceDate, String locCode);

    Appearance saveAppearance(Appearance appearance);
}

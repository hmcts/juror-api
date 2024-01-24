package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolNumbersListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestActiveListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolsAtCourtLocationListDto;
import uk.gov.hmcts.juror.api.moj.domain.DayType;

import java.time.LocalDate;

public interface PoolRequestService {

    PoolRequestListDto getFilteredPoolRequests(BureauJWTPayload payload, String courtLocation,
                                               int offset, String sortBy, String sortOrder);

    void savePoolRequest(PoolRequestDto poolRequestDto, BureauJWTPayload payload);

    long getCourtDeferrals(String locationCode, LocalDate deferredTo);

    DayType checkAttendanceDate(LocalDate attendanceDate, String locationCode);

    PoolNumbersListDto getPoolNumbers(String poolNumberPrefix);

    PoolRequestActiveListDto getActivePoolRequests(BureauJWTPayload payload, String locCode,
                                                   String tab, int offset, String sortBy, String sortOrder);

    PoolsAtCourtLocationListDto getActivePoolsAtCourtLocation(String locCode);
}

package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.ActivePoolFilterQuery;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolRequestedFilterQuery;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolNumbersListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestActiveDataDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestDataDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolsAtCourtLocationListDto;
import uk.gov.hmcts.juror.api.moj.domain.DayType;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface PoolRequestService {

    PaginatedList<PoolRequestDataDto> getFilteredPoolRequests(PoolRequestedFilterQuery filterQuery);

    void savePoolRequest(PoolRequestDto poolRequestDto, BureauJwtPayload payload);

    long getCourtDeferrals(String locationCode, LocalDate deferredTo);

    DayType checkAttendanceDate(LocalDate attendanceDate, String locationCode);

    PoolNumbersListDto getPoolNumbers(String poolNumberPrefix);

    PaginatedList<PoolRequestActiveDataDto> getActivePoolRequests(ActivePoolFilterQuery filterQuery);

    PoolsAtCourtLocationListDto getActivePoolsAtCourtLocation(String locCode);

    /**
     * Retrieves the pool attendance time for a pool ID (if one is specified in the JUROR.UNIQUE_POOL table)
     *
     * @param poolId pool ID to retrieve attendance time for, not null
     * @return pool attendance time, nullable
     */
    LocalDateTime getPoolAttendanceTime(String poolId);

    PoolRequest getPoolRequest(String poolNumber);
}

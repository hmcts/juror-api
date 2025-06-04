package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.UnpaidExpenseSummaryRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.RetrieveAttendanceDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorAppearanceResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.UnpaidExpenseSummaryResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.JurorStatusGroup;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Custom Repository definition for the appearance entity.
 * Allowing for additional query functions to be explicitly declared
 */
public interface IAppearanceRepository {

    List<JurorAppearanceResponseDto.JurorAppearanceResponseData> getAppearanceRecords(
        String locCode, LocalDate date, String jurorNumber, JurorStatusGroup group);

    List<Tuple> retrieveAttendanceDetails(RetrieveAttendanceDetailsDto request);

    List<Tuple> retrieveNonAttendanceDetails(RetrieveAttendanceDetailsDto.CommonData commonData);

    List<Tuple> getAvailableJurors(String locCode);

    List<JurorPool> retrieveAllJurors(String locCode, LocalDate date);

    List<JurorPool> getJurorsInPools(String locCode, List<String> poolNumber, LocalDate date);

    JPAQuery<JurorPool> buildJurorPoolsCheckedInTodayQuery(String locCode, LocalDate date);

    long countPendingApproval(String locCode, boolean isCash);

    Optional<Appearance> findByJurorNumberAndLocCodeAndAttendanceDateAndVersion(
        String jurorNumber,
        String locCode,
        LocalDate attendanceDate,
        long appearanceVersion);

    List<Tuple> getTrialsWithAttendanceCount(String locationCode, LocalDate attendanceDate);

    PaginatedList<UnpaidExpenseSummaryResponseDto> findUnpaidExpenses(String locCode,
                                                                      UnpaidExpenseSummaryRequestDto search);

    List<Tuple> getUnconfirmedJurors(String locationCode, LocalDate attendanceDate);

    List<Tuple> getUnpaidAttendancesAtCourt(String locCode);
}

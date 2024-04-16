package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.RetrieveAttendanceDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorAppearanceResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;

import java.time.LocalDate;
import java.util.List;

/**
 * Custom Repository definition for the appearance entity.
 * Allowing for additional query functions to be explicitly declared
 */
public interface IAppearanceRepository {

    List<JurorAppearanceResponseDto.JurorAppearanceResponseData> getAppearanceRecords(
        String locCode, LocalDate date, String jurorNumber);

    List<Tuple> retrieveAttendanceDetails(RetrieveAttendanceDetailsDto request);

    List<Tuple> retrieveNonAttendanceDetails(RetrieveAttendanceDetailsDto.CommonData commonData);

    List<Tuple> getAvailableJurors(String locCode);

    List<JurorPool> retrieveAllJurors(String locCode, LocalDate date);

    List<JurorPool> getJurorsInPools(String locCode, List<String> poolNumber, LocalDate date);

    JPAQuery<JurorPool> buildJurorPoolsCheckedInTodayQuery(String locCode, LocalDate date);

    Integer countJurorExpenseForApproval(String jurorNumber, String poolNumber);

    long countPendingApproval(String locCode, boolean isCash);

    List<Tuple> getTrialsWithAttendanceCount(String locationCode, LocalDate attendanceDate);
}

package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.validation.constraints.NotNull;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPoolSearch;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolMemberFilterRequestQuery;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Custom Repository definition for the JurorPool entity.
 * Allowing for additional query functions to be explicitly declared
 */
public interface IJurorPoolRepository {
    StringPath ATTENDANCE = Expressions.stringPath("attendance");
    BooleanPath CHECKED_IN_TODAY = Expressions.booleanPath("checked_in_today");

    String findLatestPoolSequence(@NotNull String poolNumber);

    JurorPool findByJurorNumberAndIsActiveAndCourt(String jurorNumber, Boolean isActive, CourtLocation locCode);

    List<JurorPool> findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(List<String> jurorNumbers,
                                                                                   boolean isActive,
                                                                                   String poolNumber,
                                                                                   CourtLocation court,
                                                                                   List<Integer> status);

    List<JurorPool> findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(List<String> jurorNumbers, boolean isActive,
                                                                        String poolNumber, CourtLocation court);

    List<JurorPool> findJurorsOnCallAtCourtLocation(String locCode, List<String> poolNumbers);

    List<JurorPool> findJurorsInAttendanceAtCourtLocation(String locCode, List<String> poolNumbers);

    List<JurorPool> findJurorsNotInAttendanceAtCourtLocation(String locCode, List<String> poolNumbers);

    List<Tuple> getJurorsToDismiss(List<String> poolNumbers, List<String> jurorNumbers, String locCode);

    <T> PaginatedList<T> findJurorPoolsBySearch(JurorPoolSearch search, String owner,
                                                Consumer<JPQLQuery<JurorPool>> queryModifiers,
                                                Function<JurorPool, T> dataMapper,
                                                Long maxItems);

    JPAQuery<Tuple> fetchFilteredPoolMembers(PoolMemberFilterRequestQuery search, String owner);

    List<String> fetchThinPoolMembers(String poolNumber, String owner);

    boolean hasPoolWithLocCode(String jurorNumber, List<String> locCodes);

    int getCountJurorsDueToAttendCourt(String locCode, LocalDate startDate, LocalDate endDate,
                                       boolean reasonableAdjustmentRequired);

    List<Tuple> getIncompleteServiceCountsByCourt();

}

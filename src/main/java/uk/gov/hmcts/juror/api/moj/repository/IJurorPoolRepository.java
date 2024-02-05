package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPQLQuery;
import jakarta.validation.constraints.NotNull;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPoolSearch;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;

import java.util.List;
import java.util.function.Consumer;

/**
 * Custom Repository definition for the JurorPool entity.
 * Allowing for additional query functions to be explicitly declared
 */
public interface IJurorPoolRepository {

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

    List<JurorPool> findJurorPoolsBySearch(JurorPoolSearch search, String owner,
                                           Consumer<JPQLQuery<JurorPool>> queryModifiers);
}

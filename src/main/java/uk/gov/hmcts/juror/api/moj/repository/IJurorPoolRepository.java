package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import jakarta.validation.constraints.NotNull;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;

import java.util.List;

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
}

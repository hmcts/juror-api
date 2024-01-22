package uk.gov.hmcts.juror.api.moj.repository;

import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.Juror;

import java.time.LocalDate;
import java.util.List;

/**
 * Custom Repository definition for the Juror entity.
 * Allowing for additional query functions to be explicitly declared
 */
public interface IJurorRepository {

    Juror findByJurorNumberAndOwnerAndDeferralDate(String jurorNumber, String owner, LocalDate deferralDate);

    List<Juror> findByPoolNumberAndWasDeferredAndIsActive(String poolNumber, boolean wasDeferred, boolean isActive);

    List<Juror> findByPoolNumberAndIsActive(String poolNumber, boolean isActive);

    Juror findByJurorNumberAndPoolNumberAndIsActive(String jurorNumber, String poolNumber, boolean isActive);

    List<Juror> findByJurorNumberAndIsActive(String jurorNumber, boolean isActive);

    Juror findByJurorNumberAndIsActiveAndCourt(String jurorNumber, boolean isActive, CourtLocation locCode);

    List<Juror> findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(List<String> jurorNumbers,
                                                                               boolean isActive, String poolNumber,
                                                                               CourtLocation court,
                                                                               List<Integer> status);

    List<Juror> findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(List<String> jurorNumbers, boolean isActive,
                                                                    String poolNumber, CourtLocation court);

    Juror findByOwnerAndJurorNumberAndPoolNumber(String owner, String jurorNumber, String poolNumber);

    List<Juror> findByPoolNumberAndOwnerAndIsActive(String poolNumber, String owner, boolean isActive);

}

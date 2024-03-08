package uk.gov.hmcts.juror.api.moj.repository;

import uk.gov.hmcts.juror.api.moj.domain.ActivePoolsCourt;

import java.time.LocalDate;
import java.util.List;

/**
 * Custom Repository definition for the ActivePoolsCourt entity.
 * Allowing for additional query functions to be explicitly declared
 */
public interface IActivePoolsCourtRepository {

    List<ActivePoolsCourt> findActivePools(List<String> courts, LocalDate returnDate, String sortBy,
                                                  String order,
                                                  List<String> poolTypes);

}

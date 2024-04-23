package uk.gov.hmcts.juror.api.moj.repository.report;

import com.querydsl.core.Tuple;

import java.time.LocalDate;
import java.util.List;

/**
 * Custom Repository definition for the incomplete service report.
 * Allowing for additional query functions to be explicitly declared
 */
public interface IIncompleteServiceRepository {

    List<Tuple> getIncompleteServiceByLocationAndDate(String courtLocation, LocalDate cutOffDate);

}

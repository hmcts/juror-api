package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;

import java.time.LocalDate;
import java.util.List;

/**
 * Custom Repository definition for the PoolComment entity.
 * Allowing for additional query functions to be explicitly declared
 */

public interface IPoolCommentRepository {
    List<Tuple> findPoolCommentsForLocationsAndDates(List<String> locCodes, LocalDate dateFrom, LocalDate dateTo);
}

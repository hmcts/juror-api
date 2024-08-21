package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequestListAndCount;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Custom Repository definition for the PoolRequest entity.
 * Allowing for additional query functions to be explicitly declared
 */
public interface IPoolRequestRepository extends IPoolRequestSearchQueries {

    JPAQuery<PoolRequest> findBureauPoolRequestsList(List<String> poolTypes, String courtLocation);

    PoolRequestListAndCount findCourtsPoolRequestsList(List<String> courts, List<String> poolTypes,
                                                       String courtLocation, int offset, int pageSize,
                                                       OrderSpecifier<?> order);

    // This will get all numbers from a single court for a given prefix
    List<Tuple> findAllPoolNumbersByPoolNumberPrefix(String poolNumberPrefix);

    // This is similar to the above query but will get one pool request only
    PoolRequest findLatestPoolRequestByPoolNumberPrefix(String poolNumberPrefix);

    boolean isActive(String poolNumber);

    void deletePoolRequestByPoolNumber(String poolNumber);

    List<Tuple> findActivePoolsForDateRange(String owner, String locCode, LocalDate minDate, LocalDate maxDate,
                                            boolean isReassign);

    List<Tuple> findActivePoolsForDateRangeWithCourtCreatedRestriction(String owner, String locCode,
                                                                       LocalDate minDate,
                                                                       LocalDateTime courtCreationMinDate);
}

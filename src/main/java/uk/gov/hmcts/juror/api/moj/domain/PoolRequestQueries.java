package uk.gov.hmcts.juror.api.moj.domain;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@SuppressWarnings("PMD.LawOfDemeter")
public abstract class PoolRequestQueries {

    private static final QPoolRequest POOL_REQUEST = QPoolRequest.poolRequest;

    /**
     * Filter Pool Requests that match a given Pool Type.
     *
     * @param poolType Pool Type ID value
     *
     * @return Predicate
     */
    private static BooleanExpression filterByPoolType(final String poolType) {
        return POOL_REQUEST.poolType.poolType.eq(poolType);
    }

    /**
     * Filter Pool Requests that match a Pool Type from a given list of pool types.
     *
     * @param poolTypes List of Pool Type description values
     *                  return Predicate
     */
    private static BooleanExpression filterByPoolTypeIn(final List<String> poolTypes) {
        return POOL_REQUEST.poolType.description.in(poolTypes);
    }

    /**
     * Filter Pool Requests that match a given Court Location.
     *
     * @param locCode Court Location Code value
     *
     * @return Predicate
     */
    private static BooleanExpression filterByCourtLocation(final String locCode) {
        return POOL_REQUEST.courtLocation.locCode.eq(locCode);
    }

    /**
     * Filter Pool Requests that match a given list of Court Locations.
     *
     * @param locCodes List of acceptable Court Location Codes
     *
     * @return Predicate
     */
    public static BooleanExpression filterByCourtLocations(final List<String> locCodes) {
        return POOL_REQUEST.courtLocation.locCode.in(locCodes);
    }

    /**
     * Filter Pool Requests by Pool Type and also optionally by Court Location.
     *
     * @param poolTypes    List of Pool Type description values
     * @param courtLocCode Court Location Code value
     *
     * @return Predicate
     */
    public static BooleanExpression filterByPoolTypeAndLocation(@NotNull final List<String> poolTypes,
                                                                final String courtLocCode) {
        BooleanExpression predicate = filterByPoolTypeIn(poolTypes);

        if (courtLocCode != null && !courtLocCode.isEmpty()) {
            predicate = predicate.and(filterByCourtLocation(courtLocCode));
        }

        return predicate;
    }

    private static NumberExpression<Integer> calculateActiveFlag(QPoolRequest poolRequest) {
        final Character requestCreatedStatus = 'N';
        return poolRequest.newRequest.when(requestCreatedStatus).then(1).otherwise(0).sum();
    }

    /**
     * Use a WHERE-IN clause to filter Pool Requests based on the NEW_REQUEST value of associated records in a
     * sub-query.
     *
     * @param poolRequest    QPoolRequest model used for the root query object
     * @param subPoolRequest QPoolRequest model used to the sub-query object
     * @param status         PoolRequestStatus enum used to dynamically determine what to evaluate the
     *                       aggregated sub-query value against
     *
     * @return Predicate
     */
    public static BooleanExpression filterByActiveFlag(QPoolRequest poolRequest, QPoolRequest subPoolRequest,
                                                       PoolRequestStatus status) {
        return poolRequest.poolNumber.in(
            evaluateActiveStatus(subPoolRequest, status));
    }

    private static JPQLQuery<String> evaluateActiveStatus(QPoolRequest subPoolRequest, PoolRequestStatus status) {
        JPQLQuery<String> query = JPAExpressions.select(subPoolRequest.poolNumber).from(subPoolRequest)
            .groupBy(subPoolRequest.poolNumber);
        if (status == PoolRequestStatus.REQUESTED) {
            query.having(calculateActiveFlag(subPoolRequest).eq(0));
        } else {
            query.having(calculateActiveFlag(subPoolRequest).gt(0));
        }
        return query;
    }
}

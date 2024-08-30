package uk.gov.hmcts.juror.api.moj.domain;

import com.querydsl.core.types.dsl.BooleanExpression;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public abstract class PoolRequestQueries {

    private static final QPoolRequest POOL_REQUEST = QPoolRequest.poolRequest;

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
     * @return Predicate
     */
    private static BooleanExpression filterByCourtLocation(final String locCode) {
        return POOL_REQUEST.courtLocation.locCode.eq(locCode);
    }

    /**
     * Filter Pool Requests that match a given list of Court Locations.
     *
     * @param locCodes List of acceptable Court Location Codes
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
     * @return Predicate
     */
    public static BooleanExpression filterByPoolTypeAndLocation(@NotNull final List<String> poolTypes,
                                                                final String courtLocCode) {
        BooleanExpression predicate = filterByPoolTypeIn(poolTypes);

        if (StringUtils.isNotBlank(courtLocCode)) {
            predicate = predicate.and(filterByCourtLocation(courtLocCode));
        }

        return predicate;
    }
}

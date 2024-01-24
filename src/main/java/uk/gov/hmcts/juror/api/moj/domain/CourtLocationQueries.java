package uk.gov.hmcts.juror.api.moj.domain;

import com.querydsl.core.types.dsl.BooleanExpression;
import uk.gov.hmcts.juror.api.juror.domain.QCourtLocation;

import java.util.List;

/**
 * Define QueryDSL predicates for Court Location Repository operations.
 */
public abstract class CourtLocationQueries {

    private static final QCourtLocation COURT_LOCATION = QCourtLocation.courtLocation;

    /**
     * Filter Court Location records that have location codes which exist in a given list of location codes.
     *
     * @param locCodes A list of 3 digit Court Location codes (unique identifier for each court location)
     *
     * @return Predicate
     */
    public static BooleanExpression filterByLocCodes(final List<String> locCodes) {
        return COURT_LOCATION.locCode.in(locCodes);
    }

}

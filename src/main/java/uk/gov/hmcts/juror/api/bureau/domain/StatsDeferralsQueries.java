package uk.gov.hmcts.juror.api.bureau.domain;


import com.querydsl.core.types.dsl.BooleanExpression;

/**
 * QueryDSL queries for {@Link StatsDeferrals}.
 */

@Deprecated(forRemoval = true)
public class StatsDeferralsQueries {
    private static final String bureauSelection = "Bureau";
    private static final String courtSelection = "Court";

    private StatsDeferralsQueries() {
    }

    private static final QStatsDeferrals statsDeferralsDetail = QStatsDeferrals.statsDeferrals;

    /**
     * Query to match all deferral records for BUREAU_OR_COURT where Bureau is selected.
     */

    public static BooleanExpression isBureau() {
        return statsDeferralsDetail.bureauOrCourt.eq(bureauSelection);

    }

    /**
     * Query to match all STATSDEFERRALS records for BUREAU_OR_COURT where Court is selected.
     */

    public static BooleanExpression isCourt() {
        return statsDeferralsDetail.bureauOrCourt.eq(courtSelection);

    }

    /**
     * Query to match all deferral records from STATSDEFERRALS table in between week parameters.
     */

    public static BooleanExpression deferralRecordsBetween(String startYearWeek, String endYearWeek) {
        return statsDeferralsDetail.week.between(startYearWeek, endYearWeek);
    }

    /**
     * Query to match deferral court records where week is between week parameters.
     */
    public static BooleanExpression deferralCourtRecordsBetween(String startYearWeek, String endYearWeek) {
        return statsDeferralsDetail.week.between(startYearWeek, endYearWeek).and(isCourt());
    }

    public static BooleanExpression deferralBureauRecordsBetween(String startYearWeek, String endYearWeek) {
        return statsDeferralsDetail.week.between(startYearWeek, endYearWeek).and(isBureau());
    }


}


package uk.gov.hmcts.juror.api.bureau.domain;


import com.querydsl.core.types.dsl.BooleanExpression;

/**
 * QueryDSL queries for {@Link StatsDeferrals}.
 */

@Deprecated(forRemoval = true)
public final class StatsDeferralsQueries {
    private static final String BUREAU_SELECTION = "Bureau";
    private static final String COURT_SELECTION = "Court";

    private static final QStatsDeferrals STATS_DEFERRALS_DETAIL = QStatsDeferrals.statsDeferrals;

    private StatsDeferralsQueries() {
    }

    /**
     * Query to match all deferral records for BUREAU_OR_COURT where Bureau is selected.
     */

    @SuppressWarnings({"PMD.LinguisticNaming"})
    public static BooleanExpression isBureau() {
        return STATS_DEFERRALS_DETAIL.bureauOrCourt.eq(BUREAU_SELECTION);

    }

    /**
     * Query to match all STATSDEFERRALS records for BUREAU_OR_COURT where Court is selected.
     */
    @SuppressWarnings({"PMD.LinguisticNaming"})
    public static BooleanExpression isCourt() {
        return STATS_DEFERRALS_DETAIL.bureauOrCourt.eq(COURT_SELECTION);

    }

    /**
     * Query to match all deferral records from STATSDEFERRALS table in between week parameters.
     */

    public static BooleanExpression deferralRecordsBetween(String startYearWeek, String endYearWeek) {
        return STATS_DEFERRALS_DETAIL.week.between(startYearWeek, endYearWeek);
    }

    /**
     * Query to match deferral court records where week is between week parameters.
     */
    public static BooleanExpression deferralCourtRecordsBetween(String startYearWeek, String endYearWeek) {
        return STATS_DEFERRALS_DETAIL.week.between(startYearWeek, endYearWeek).and(isCourt());
    }

    public static BooleanExpression deferralBureauRecordsBetween(String startYearWeek, String endYearWeek) {
        return STATS_DEFERRALS_DETAIL.week.between(startYearWeek, endYearWeek).and(isBureau());
    }


}


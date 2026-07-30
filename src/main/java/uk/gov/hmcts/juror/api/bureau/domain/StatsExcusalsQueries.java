package uk.gov.hmcts.juror.api.bureau.domain;

import com.querydsl.core.types.dsl.BooleanExpression;

/**
 * QueryDSL queries for {@Link StatsExcusals}.
 */

@Deprecated(forRemoval = true)
public final class StatsExcusalsQueries {
    private static final String BUREAU_SELECTION = "Bureau";
    private static final String COURT_SELECTION = "Court";
    private static final QStatsExcusals STATS_EXCUSALS_DETAIL = QStatsExcusals.statsExcusals;

    private StatsExcusalsQueries() {
    }

    /**
     * Query to match all excusal records for BUREAU_OR_COURT where Bureau is selected.
     */
    @SuppressWarnings({"PMD.LinguisticNaming"})
    public static BooleanExpression isBureau() {
        return STATS_EXCUSALS_DETAIL.bureauOrCourt.eq(BUREAU_SELECTION);

    }

    /**
     * Query to match all excusal  records for BUREAU_OR_COURT where Court is selected.
     */
    @SuppressWarnings({"PMD.LinguisticNaming"})
    public static BooleanExpression isCourt() {
        return STATS_EXCUSALS_DETAIL.bureauOrCourt.eq(COURT_SELECTION);

    }

    /**
     * Query to match all excusal records from STATSEXCUSALS table in between week parameters.
     */

    public static BooleanExpression excusalRecordsBetween(String startYearWeek, String endYearWeek) {
        return STATS_EXCUSALS_DETAIL.week.between(startYearWeek, endYearWeek);
    }

    /**
     * Query to match excusal court records where week is between  week parameters.
     */
    public static BooleanExpression excusalsCourtRecordsBetween(String startYearWeek, String endYearWeek) {
        return STATS_EXCUSALS_DETAIL.week.between(startYearWeek, endYearWeek).and(isCourt());
    }

    public static BooleanExpression excusalBureauRecordsBetween(String startYearWeek, String endYearWeek) {
        return STATS_EXCUSALS_DETAIL.week.between(startYearWeek, endYearWeek).and(isBureau());
    }


}


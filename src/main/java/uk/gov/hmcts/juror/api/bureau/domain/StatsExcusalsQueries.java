package uk.gov.hmcts.juror.api.bureau.domain;

import com.querydsl.core.types.dsl.BooleanExpression;

/**
 * QueryDSL queries for {@Link StatsExcusals}.
 */

public class StatsExcusalsQueries {
    private static final String bureauSelection = "Bureau";
    private static final String courtSelection = "Court";


    private StatsExcusalsQueries() {
    }

    private static final QStatsExcusals statsExcusalsDetail = QStatsExcusals.statsExcusals;

    /**
     * Query to match all excusal records for BUREAU_OR_COURT where Bureau is selected.
     *
     * @return
     */

    public static BooleanExpression isBureau() {
        return statsExcusalsDetail.bureauOrCourt.eq(bureauSelection);

    }

    /**
     * Query to match all excusal  records for BUREAU_OR_COURT where Court is selected.
     *
     * @return
     */

    public static BooleanExpression isCourt() {
        return statsExcusalsDetail.bureauOrCourt.eq(courtSelection);

    }

    /**
     * Query to match all excusal records from STATSEXCUSALS table in between week parameters.
     *
     * @param startYearWeek
     * @param endYearWeek
     * @return
     */

    public static BooleanExpression excusalRecordsBetween(String startYearWeek, String endYearWeek) {
        return statsExcusalsDetail.week.between(startYearWeek, endYearWeek);
    }

    /**
     * Query to match excusal court records where week is between  week parameters.
     *
     * @param startYearWeek
     * @param endYearWeek
     * @return
     */
    public static BooleanExpression excusalsCourtRecordsBetween(String startYearWeek, String endYearWeek) {
        return statsExcusalsDetail.week.between(startYearWeek, endYearWeek).and(isCourt());
    }

    /**
     * @param startYearWeek
     * @param endYearWeek
     * @return
     */
    public static BooleanExpression excusalBureauRecordsBetween(String startYearWeek, String endYearWeek) {
        return statsExcusalsDetail.week.between(startYearWeek, endYearWeek).and(isBureau());
    }


}


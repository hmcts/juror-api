package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.domain.StatsDeferrals;
import uk.gov.hmcts.juror.api.bureau.domain.StatsExcusals;

import java.util.List;


public interface DashboardDeferralExcusalDataService {

    /**
     * Get all Deferral records from the StatsDeferrals table between given parameters.
     *
     * @param startYearWeek
     * @param endYearWeek
     * @return List
     */

    List<StatsDeferrals> getStatsDeferrals(String startYearWeek, String endYearWeek);


    /**
     * Get all Deferral Court records from the StatsDeferrals table between given parameters.
     *
     * @param startYearWeek
     * @param endYearWeek
     * @return List
     */

    List<StatsDeferrals> getStatsCourtDeferrals(String startYearWeek, String endYearWeek);

    /**
     * Get all Deferral Bureau records from the StatsDeferrals table between given parameters.
     *
     * @param startYearWeek
     * @param endYearWeek
     * @return List
     */

    List<StatsDeferrals> getStatsBureauDeferrals(String startYearWeek, String endYearWeek);


    /**
     * Get all Excusal records from the StatsExcusal table between given parameters.
     *
     * @param startYearWeek
     * @param endYearWeek
     * @return List
     */
    List<StatsExcusals> getStatsExcusals(String startYearWeek, String endYearWeek);


    /**
     * Get all Excusal Court records from the StatsExcusal table between given parameters.
     *
     * @param startYearWeek
     * @param endYearWeek
     * @return List
     */
    List<StatsExcusals> getStatsCourtExcusals(String startYearWeek, String endYearWeek);

    /**
     * Get all Excusal Bureau records from the StatsExcusal table between given parameters.
     *
     * @param startYearWeek
     * @param endYearWeek
     * @return List
     */
    List<StatsExcusals> getStatsBureauExcusals(String startYearWeek, String endYearWeek);


}


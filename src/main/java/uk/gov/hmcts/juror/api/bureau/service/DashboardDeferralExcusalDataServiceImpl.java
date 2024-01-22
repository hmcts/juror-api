package uk.gov.hmcts.juror.api.bureau.service;

import com.google.common.collect.Lists;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import uk.gov.hmcts.juror.api.bureau.domain.StatsDeferrals;
import uk.gov.hmcts.juror.api.bureau.domain.StatsDeferralsQueries;
import uk.gov.hmcts.juror.api.bureau.domain.StatsDeferralsRepository;
import uk.gov.hmcts.juror.api.bureau.domain.StatsExcusals;
import uk.gov.hmcts.juror.api.bureau.domain.StatsExcusalsQueries;
import uk.gov.hmcts.juror.api.bureau.domain.StatsExcusalsRepository;

import java.util.List;


@Slf4j
@Service
public class DashboardDeferralExcusalDataServiceImpl implements DashboardDeferralExcusalDataService {

    private final StatsDeferralsRepository statsDeferralsRepository;
    private final StatsExcusalsRepository statsExcusalsRepository;

    @Autowired
    public DashboardDeferralExcusalDataServiceImpl(
        final StatsDeferralsRepository statsDeferralsRepository,
        final StatsExcusalsRepository statsExcusalsRepository) {

        Assert.notNull(statsDeferralsRepository, "StatsDeferralsRepository");
        Assert.notNull(statsExcusalsRepository, "StatsExcusalsRepository");
        this.statsExcusalsRepository = statsExcusalsRepository;
        this.statsDeferralsRepository = statsDeferralsRepository;
    }

    /**
     * Get all Deferral records from the StatsDeferrals table between given parameters.
     *
     * @param startYearWeek
     * @param endYearWeek
     * @return statsDeferrals
     */
    @Override
    @Transactional
    public List<StatsDeferrals> getStatsDeferrals(String startYearWeek, String endYearWeek) {
        log.info("Called Service : DashboardDeferralExcusalDataServiceImpl.getStatsDeferrals()....");
        log.info("Passed parameters startYearWeek: {}", startYearWeek);
        log.info("Passed parameters endYearWeek: {}", endYearWeek);

        BooleanExpression deferralsRecordsBetween = StatsDeferralsQueries.deferralRecordsBetween(
            startYearWeek,
            endYearWeek
        );

        final List<StatsDeferrals> statsDeferralsList = Lists.newLinkedList(statsDeferralsRepository.findAll(
            deferralsRecordsBetween));

        log.info("****STATS DEFERRALS RECORDS**** ");
        log.info("The count of StatsDeferrals records to be returned:{} ", statsDeferralsList.size());
        log.debug("All StatsDeferrals:{} ", statsDeferralsList);

        return statsDeferralsList;
    }

    /**
     * Get all Deferral Court records from the StatsDeferrals table between given parameters.
     *
     * @param startYearWeek
     * @param endYearWeek
     * @return statsDeferralsCourt
     */

    @Override
    public List<StatsDeferrals> getStatsCourtDeferrals(String startYearWeek, String endYearWeek) {
        log.info("Called Service : DashboardDeferralExcusalDataServiceImpl.getStatsCourtDeferrals()....");

        BooleanExpression deferralsCourtRecordsBetween = StatsDeferralsQueries.deferralCourtRecordsBetween(
            startYearWeek,
            endYearWeek
        );

        List<StatsDeferrals> statsDeferralsCourt = Lists.newLinkedList(statsDeferralsRepository.findAll(
            deferralsCourtRecordsBetween));
        log.info("****COURT STATS DEFERRALS RECORDS****");
        log.info("The count of Stats Court Deferrals records to be returned:{} ", statsDeferralsCourt.size());
        log.debug("All CourtStatsDeferrals:{} ", statsDeferralsCourt);

        return statsDeferralsCourt;
    }

    /**
     * Get all Deferral Bureau records from the StatsDeferrals table between given parameters.
     *
     * @param startYearWeek
     * @param endYearWeek
     * @return statsDeferralsBureau
     */

    @Override
    public List<StatsDeferrals> getStatsBureauDeferrals(String startYearWeek, String endYearWeek) {
        log.info("Called Service : DashboardDeferralExcusalDataServiceImpl.getStatsCourtDeferrals()....");

        BooleanExpression deferralsBureauRecordsBetween = StatsDeferralsQueries.deferralBureauRecordsBetween(
            startYearWeek,
            endYearWeek
        );

        List<StatsDeferrals> statsDeferralsBureau = Lists.newLinkedList(statsDeferralsRepository.findAll(
            deferralsBureauRecordsBetween));

        log.info("****BUREAU STATS DEFERRALS RECORDS****");
        log.info("The count of Stats Bureau Deferrals records to be returned:{} ", statsDeferralsBureau.size());
        log.debug("All BureauStatsDeferrals:{} ", statsDeferralsBureau);

        return statsDeferralsBureau;
    }

    /**
     * Get all Excusal records from the StatsExcusal table between given parameters.
     *
     * @param startYearWeek
     * @param endYearWeek
     * @return statsExcusals
     */

    @Override
    public List<StatsExcusals> getStatsExcusals(String startYearWeek, String endYearWeek) {

        log.info("Called Service : DashboardDeferralExcusalDataServiceImpl.getStatsExcusals()....");

        BooleanExpression excusalsRecordsBetween = StatsExcusalsQueries.excusalRecordsBetween(
            startYearWeek,
            endYearWeek
        );

        List<StatsExcusals> statsExcusals =
            Lists.newLinkedList(statsExcusalsRepository.findAll(excusalsRecordsBetween));
        log.info("****STATS EXCUSALS RECORDS****");
        log.info("The count of StatsExcusals records to be returned: {}", statsExcusals.size());
        log.debug("All StatsExcusals: {}", statsExcusals);

        return statsExcusals;
    }

    /**
     * Get all Excusal Court records from the StatsExcusal table between given parameters.
     *
     * @param startYearWeek
     * @param endYearWeek
     * @return statsExcusalsCourt
     */

    @Override
    public List<StatsExcusals> getStatsCourtExcusals(String startYearWeek, String endYearWeek) {
        log.info("Called Service : DashboardDeferralExcusalDataServiceImpl.getStatsCourtExcusals()....");

        BooleanExpression excusalsCourtRecordsBetween = StatsExcusalsQueries.excusalsCourtRecordsBetween(
            startYearWeek,
            endYearWeek
        );

        List<StatsExcusals> statsExcusalsCourt = Lists.newLinkedList(statsExcusalsRepository.findAll(
            excusalsCourtRecordsBetween));
        log.info("****COURT STATS EXCUSALS RECORDS****");
        log.info("The count of count Court StatsExcusals records to be returned: {}", statsExcusalsCourt.size());
        log.debug("All Court StatsExcusals: {}", statsExcusalsCourt);

        return statsExcusalsCourt;
    }


    /**
     * Get all Excusal Bureau records from the StatsExcusal table between given parameters.
     *
     * @param startYearWeek
     * @param endYearWeek
     * @return statsExcusalsBureau
     */

    @Override
    public List<StatsExcusals> getStatsBureauExcusals(String startYearWeek, String endYearWeek) {
        log.info("Called Service : DashboardDeferralBureauExcusalDataServiceImpl.getStatsExcusals()....");

        BooleanExpression excusalBureauRecordsBetween = StatsExcusalsQueries.excusalBureauRecordsBetween(
            startYearWeek,
            endYearWeek
        );

        List<StatsExcusals> statsExcusalsBureau = Lists.newLinkedList(statsExcusalsRepository.findAll(
            excusalBureauRecordsBetween));
        log.info("****BUREAU STATS EXCUSALS RECORDS****");
        log.info("The count of StatsBureau Excusals records to be returned: {}", statsExcusalsBureau.size());
        log.debug("All StatsBureauExcusals: {}", statsExcusalsBureau);

        return statsExcusalsBureau;
    }

}

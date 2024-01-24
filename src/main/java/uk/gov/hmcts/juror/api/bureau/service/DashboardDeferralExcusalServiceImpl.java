package uk.gov.hmcts.juror.api.bureau.service;

import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.bureau.controller.request.DashboardDeferralExcusalRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.DashboardDeferralExcusalResponseDto;
import uk.gov.hmcts.juror.api.bureau.domain.StatsDeferrals;
import uk.gov.hmcts.juror.api.bureau.domain.StatsExcusals;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DashboardDeferralExcusalServiceImpl implements DashboardDeferralExcusalService {


    private final DashboardDeferralExcusalDataService dashboardDeferralExcusalDataService;

    @Autowired
    public DashboardDeferralExcusalServiceImpl(
        final DashboardDeferralExcusalDataService dashboardDeferralExcusalDataService) {
        Assert.notNull(dashboardDeferralExcusalDataService, "DashboardDeferralExcusalDataService cannot be null");
        this.dashboardDeferralExcusalDataService = dashboardDeferralExcusalDataService;
    }

    @Override
    public DashboardDeferralExcusalResponseDto.DeferralExcusalValues getDeferralExcusalValues(
        DashboardDeferralExcusalRequestDto requestDto) {
        log.info("Called Service : DashboardDeferralExcusalServiceImpl.getDeferralExcusalValues() ");
        String startYearWeek = requestDto.getStartYearWeek();
        String endYearWeek = requestDto.getEndYearWeek();
        String deferralSelection = requestDto.getDeferral();
        String excusalSelection = requestDto.getExcusal();
        String bureauSelection = requestDto.getBureau();
        String courtSelection = requestDto.getCourt();
        String excusalBureauSelection = requestDto.getBureau();
        String excusalCourtSelection = requestDto.getCourt();


        log.info("Start Year Week  {}: ", startYearWeek);
        log.info("End Year Week  {}: ", endYearWeek);
        log.info("Excusal selection {}: ", excusalSelection);
        log.info("Deferral Selection {}: ", deferralSelection);
        log.info("Bureau Selection {}: ", bureauSelection);
        log.info("Court Selection {}: ", courtSelection);


        DashboardDeferralExcusalResponseDto.DeferralExcusalValues deferralExcusalValues =
            new DashboardDeferralExcusalResponseDto.DeferralExcusalValues();


        if (deferralSelection.equals("Y")) {

            /**
             * Gets all records from juror.digital.STATS_DEFERRALS
             */
            final List<StatsDeferrals> statsDeferrals = new ArrayList<>();

            if ("Y".equals(bureauSelection) & courtSelection.equals("Y")) {
                statsDeferrals.addAll(dashboardDeferralExcusalDataService.getStatsDeferrals(
                    startYearWeek,
                    endYearWeek
                ));
                deferralExcusalValues.setDeferralStats(statsDeferrals);
                /**
                 * Gets all COURT records from juror.digital.STATS_DEFERRALS
                 */
            } else if (courtSelection.equals("Y")) {
                statsDeferrals.addAll(dashboardDeferralExcusalDataService.getStatsCourtDeferrals(
                    startYearWeek,
                    endYearWeek
                ));
                deferralExcusalValues.setDeferralStats(statsDeferrals);
                /**
                 * Gets all BUREAU records from juror.digital.STATS_DEFERRALS
                 */
            } else if
            (bureauSelection.equals("Y")) {
                statsDeferrals.addAll(dashboardDeferralExcusalDataService.getStatsBureauDeferrals(
                    startYearWeek,
                    endYearWeek
                ));
                deferralExcusalValues.setDeferralStats(statsDeferrals);
            }
        }


        if (excusalSelection.equals("Y")) {
            /**
             * Gets all records from juror.digital.STATS_EXCUSALS
             */
            final List<StatsExcusals> statsExcusals = new ArrayList<>();


            if (excusalBureauSelection.equals("Y") & excusalCourtSelection.equals("Y")) {
                statsExcusals.addAll(dashboardDeferralExcusalDataService.getStatsExcusals(startYearWeek, endYearWeek));
                deferralExcusalValues.setExcusalStats(statsExcusals);
                /**
                 * Gets all COURT records from juror.digital.STATS_EXCUSALS
                 */
            } else if
            (courtSelection.equals("Y")) {
                statsExcusals.addAll(dashboardDeferralExcusalDataService.getStatsCourtExcusals(
                    startYearWeek,
                    endYearWeek
                ));
                deferralExcusalValues.setExcusalStats(statsExcusals);
                /**
                 * Gets all BUREAU records from juror.digital.STATS_EXCUSALS
                 */
            } else if
            (bureauSelection.equals("Y")) {
                statsExcusals.addAll(dashboardDeferralExcusalDataService.getStatsBureauExcusals(
                    startYearWeek,
                    endYearWeek
                ));
                deferralExcusalValues.setExcusalStats(statsExcusals);
            }

        }

        return deferralExcusalValues;
    }


}




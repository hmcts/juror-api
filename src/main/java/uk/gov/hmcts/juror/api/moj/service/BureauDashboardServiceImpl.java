package uk.gov.hmcts.juror.api.moj.service;

import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.controller.bureaudashboard.BureauNotificationManagementInfoDto;
import uk.gov.hmcts.juror.api.moj.controller.bureaudashboard.BureauPoolManagementInfoDto;
import uk.gov.hmcts.juror.api.moj.controller.bureaudashboard.BureauPoolsUnderRespondedInfoDto;
import uk.gov.hmcts.juror.api.moj.controller.bureaudashboard.BureauSummonsManagementInfoDto;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorCommonResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.summonsmanagement.JurorResponseService;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class BureauDashboardServiceImpl implements BureauDashboardService {

    private final JurorCommonResponseRepositoryMod jurorCommonResponseRepositoryMod;
    private final JurorResponseService jurorResponseService;

    @Override
    public BureauPoolsUnderRespondedInfoDto getBureauPoolsUnderRespondedInfo() {
        // TODO: Implement actual business logic

        return null;
    }

    @Override
    public BureauPoolManagementInfoDto getBureauPoolManagementInfo(String locCode) {
        BureauPoolManagementInfoDto bureauPoolManagementInfoDto = BureauPoolManagementInfoDto.builder()
            .deferredJurorsWithStartDateNextWeek(jurorResponseService.getDeferredJurorsStartDateNextWeekCount(locCode))
            .poolsNotYetSummoned(jurorResponseService.getPoolsNotYetSummonedCount(locCode))
            .poolsTransferringNextWeek(jurorResponseService.getPoolsTransferringNextWeekCount(locCode))
            .build();
        return bureauPoolManagementInfoDto;
    }


    @Override
    public BureauSummonsManagementInfoDto getBureauSummonsManagementInfo(String locCode) {
        log.info("Retrieving all bureau summons management info for location: {}", locCode);
        Tuple result = jurorCommonResponseRepositoryMod.getAllSummonsCountsTuple(locCode);

        // Extract values from the Tuple result
        Long total = result.get(0, Long.class);
        Integer standard = result.get(1, Integer.class);
        Integer overdue = result.get(2, Integer.class);
        Integer fourWeeks = result.get(3, Integer.class);
        Integer assigned = result.get(4, Integer.class);
        Integer unassigned = result.get(5, Integer.class);

        return BureauSummonsManagementInfoDto.builder()
            .total(total.intValue())
            .summonsToProcess(total.intValue()) // Same as total based on your current logic
            .standard(standard)
            .overdue(overdue)
            .fourWeeks(fourWeeks)
            .assigned(assigned)
            .unassigned(unassigned)
            .build();

    }

    @Override
    public BureauNotificationManagementInfoDto getBureauNotificationManagementInfo(String locCode) {

        BureauNotificationManagementInfoDto bureauNotificationManagementInfoDto =
            BureauNotificationManagementInfoDto.builder()
                .fourWeeksSummonsReplies(jurorResponseService.getOpenSummonsRepliesFourWeeksCount(locCode))
                .build();
        return bureauNotificationManagementInfoDto;
    }
}

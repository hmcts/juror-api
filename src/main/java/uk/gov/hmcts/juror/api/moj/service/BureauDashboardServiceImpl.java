package uk.gov.hmcts.juror.api.moj.service;

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
        BureauSummonsManagementInfoDto bureauSummonsManagementInfoDto = BureauSummonsManagementInfoDto.builder()
            .total(jurorResponseService.getSummonsRepliesCount(locCode))
            .summonsToProcess(jurorResponseService.getSummonsRepliesCount(locCode))
            .overdue(jurorResponseService.getOpenSummonsRepliesOverdueCount(locCode))
            .standard(jurorResponseService.getOpenSummonsRepliesStandardCount(locCode))
            .fourWeeks(jurorResponseService.getOpenSummonsRepliesFourWeeksCount(locCode))
            .assigned(jurorResponseService.getOpenSummonsRepliesAssignedCount(locCode))
            .unassigned(jurorResponseService.getOpenSummonsRepliesUnassignedCount(locCode))
            .build();
        return bureauSummonsManagementInfoDto;
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

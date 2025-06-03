package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.controller.courtdashboard.CourtNotificationListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PendingJurorsResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.PendingJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.enumeration.PendingJurorStatusEnum;
import uk.gov.hmcts.juror.api.moj.repository.PendingJurorRepository;
import uk.gov.hmcts.juror.api.moj.service.summonsmanagement.JurorResponseService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CourtDashboardServiceImpl implements CourtDashboardService {

    private final PendingJurorRepository pendingJurorRepository;

    private final JurorResponseService jurorResponseService;

    @Override
    public CourtNotificationListDto getCourtNotifications(String locCode) {

        log.info("Retrieving court notifications for location code: {}", locCode);

        // get the number of open summons replies for the court
        CourtNotificationListDto courtNotificationListDto = CourtNotificationListDto.builder()
            .openSummonsReplies(jurorResponseService.getOpenSummonsRepliesCount(locCode))
            .build();

        if (SecurityUtil.hasRole(Role.SENIOR_JUROR_OFFICER)) {
            // get the number of pending jurors awaiting approval for the court
            PendingJurorStatus pendingJurorStatus = PendingJurorStatus.builder()
                .code(PendingJurorStatusEnum.QUEUED.getCode())
                .build();
            log.info("Retrieving pending jurors for location code: {} with status: {}",
                locCode, pendingJurorStatus.getCode());
            List<PendingJurorsResponseDto.PendingJurorsResponseData> pendingJurorsResponseData =
                pendingJurorRepository.findPendingJurorsForCourt(locCode, pendingJurorStatus);

            courtNotificationListDto.setPendingJurors(pendingJurorsResponseData.size());
        }

        return courtNotificationListDto;
    }

}

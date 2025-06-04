package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.controller.courtdashboard.CourtAdminInfoDto;
import uk.gov.hmcts.juror.api.moj.controller.courtdashboard.CourtNotificationInfoDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PendingJurorsResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.PendingJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UtilisationStats;
import uk.gov.hmcts.juror.api.moj.enumeration.PendingJurorStatusEnum;
import uk.gov.hmcts.juror.api.moj.repository.PendingJurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.UtilisationStatsRepository;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAppearanceService;
import uk.gov.hmcts.juror.api.moj.service.summonsmanagement.JurorResponseService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CourtDashboardServiceImpl implements CourtDashboardService {

    private final PendingJurorRepository pendingJurorRepository;

    private final JurorResponseService jurorResponseService;

    private final JurorAppearanceService appearanceService;

    private final UtilisationStatsRepository utilisationStatsRepository;

    @Override
    public CourtNotificationInfoDto getCourtNotifications(String locCode) {

        log.info("Retrieving court notifications for location code: {}", locCode);

        // get the number of open summons replies for the court
        CourtNotificationInfoDto courtNotificationInfoDto = CourtNotificationInfoDto.builder()
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

            courtNotificationInfoDto.setPendingJurors(pendingJurorsResponseData.size());
        }

        return courtNotificationInfoDto;
    }

    @Override
    public CourtAdminInfoDto getCourtAdminInfo(String locCode) {

        log.info("Retrieving court admin info for location code: {}", locCode);
        List<Appearance> unpaidAttendances = appearanceService.getUnpaidAttendancesAtCourt(locCode);

        CourtAdminInfoDto courtAdminInfoDto = CourtAdminInfoDto.builder()
            .unpaidAttendances(unpaidAttendances.size()).build();

        if (!unpaidAttendances.isEmpty()) {

            // find the oldest attendance date using natural ordering
            unpaidAttendances.stream()
                .map(Appearance::getAttendanceDate)
                .min(LocalDate::compareTo)
                .ifPresent(date -> {
                    log.info("Earliest unpaid attendance date found: {}", date);
                    courtAdminInfoDto.setOldestUnpaidAttendanceDate(date);

                    long daysSinceOldest = LocalDate.now().toEpochDay() - date.toEpochDay();
                    log.info("Days since oldest unpaid attendance: {}", daysSinceOldest);
                    courtAdminInfoDto.setOldestUnpaidAttendanceDays(daysSinceOldest);

                    // find the juror number associated with the oldest unpaid attendance
                    unpaidAttendances.stream()
                        .filter(a -> a.getAttendanceDate().equals(date))
                        .findFirst()
                        .ifPresent(appearance -> {
                            log.info("Oldest unpaid juror number: {}", appearance.getJurorNumber());
                            courtAdminInfoDto.setOldestUnpaidJurorNumber(appearance.getJurorNumber());
                        });
                });

        }

        // get the last run utilisation report for the court
        List<UtilisationStats> utilisationStats = utilisationStatsRepository
            .findTop12ByLocCodeOrderByMonthStartDesc(locCode);

        if (!utilisationStats.isEmpty()) {
            UtilisationStats lastUtilisationStats = utilisationStats.get(0);
            log.info("Last utilisation stats found for month: {}", lastUtilisationStats.getMonthStart());
            courtAdminInfoDto.setUtilisationReportDate(lastUtilisationStats.getLastUpdate());
        } else {
            log.info("No utilisation stats found for location code: {}", locCode);
        }

        return courtAdminInfoDto;
    }

}

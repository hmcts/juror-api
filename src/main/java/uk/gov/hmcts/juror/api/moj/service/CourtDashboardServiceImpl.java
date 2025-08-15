package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.controller.courtdashboard.CourtAdminInfoDto;
import uk.gov.hmcts.juror.api.moj.controller.courtdashboard.CourtAttendanceInfoDto;
import uk.gov.hmcts.juror.api.moj.controller.courtdashboard.CourtNotificationInfoDto;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.DailyUtilisationReportJurorsResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.DailyUtilisationReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.PendingJurorsResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.PendingJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UtilisationStats;
import uk.gov.hmcts.juror.api.moj.enumeration.PendingJurorStatusEnum;
import uk.gov.hmcts.juror.api.moj.repository.PendingJurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.UtilisationStatsRepository;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAppearanceService;
import uk.gov.hmcts.juror.api.moj.service.report.UtilisationReportService;
import uk.gov.hmcts.juror.api.moj.service.summonsmanagement.JurorResponseService;
import uk.gov.hmcts.juror.api.moj.service.trial.PanelService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CourtDashboardServiceImpl implements CourtDashboardService {

    private final PendingJurorRepository pendingJurorRepository;

    private final JurorResponseService jurorResponseService;

    private final JurorPoolService jurorPoolService;

    private final JurorAppearanceService appearanceService;

    private final UtilisationStatsRepository utilisationStatsRepository;

    private final UtilisationReportService utilisationReportService;

    private final PanelService panelService;

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

        CourtAdminInfoDto courtAdminInfoDto = getUnpaidAttendancesInfo(locCode);

        getUtilisationInfo(locCode, courtAdminInfoDto);

        return courtAdminInfoDto;
    }

    private CourtAdminInfoDto getUnpaidAttendancesInfo(String locCode) {
        log.info("Retrieving unpaid attendances for location code: {}", locCode);
        List<Appearance> unpaidAttendances = appearanceService.getUnpaidAttendancesAtCourt(locCode);

        CourtAdminInfoDto courtAdminInfoDto = CourtAdminInfoDto.builder()
            .unpaidAttendances(unpaidAttendances.size()).build();

        if (!unpaidAttendances.isEmpty()) {

            // find the oldest attendance date using natural ordering
            unpaidAttendances.stream()
                .map(Appearance::getAttendanceDate)
                .min(LocalDate::compareTo)
                .ifPresent(date -> {
                    courtAdminInfoDto.setOldestUnpaidAttendanceDate(date);

                    long daysSinceOldest = LocalDate.now().toEpochDay() - date.toEpochDay();
                    courtAdminInfoDto.setOldestUnpaidAttendanceDays(daysSinceOldest);

                    // find the juror number associated with the oldest unpaid attendance
                    unpaidAttendances.stream()
                        .filter(a -> a.getAttendanceDate().equals(date))
                        .findFirst()
                        .ifPresent(appearance -> {
                            courtAdminInfoDto.setOldestUnpaidJurorNumber(appearance.getJurorNumber());
                        });
                });

        }
        return courtAdminInfoDto;
    }

    @Override
    public CourtAttendanceInfoDto getCourtAttendanceInfo(String locCode) {
        log.info("Retrieving court attendance info for location code: {}", locCode);

        // get the expected attendance for the court in next week
        CourtAttendanceInfoDto courtAttendanceInfoDto = CourtAttendanceInfoDto.builder()
            .totalDueToAttend(jurorPoolService.getCountJurorsDueToAttendCourtNextWeek(locCode, false))
            .build();

        // get the expected attendance for the court in next week with reasonable adjustments
        courtAttendanceInfoDto.setReasonableAdjustments(jurorPoolService.getCountJurorsDueToAttendCourtNextWeek(
            locCode,
            true));

        courtAttendanceInfoDto.setUnconfirmedAttendances(appearanceService
                                                             .getUnconfirmedAttendanceCountAtCourt(locCode));

        // use the utilisation report for 7 days stats
        DailyUtilisationReportResponse dailyUtilisation =
            utilisationReportService.viewDailyUtilisationReport(locCode,LocalDate.now().minusDays(7),
                LocalDate.now());

        if (dailyUtilisation == null) {
            log.info("No daily utilisation report found for location code: {}", locCode);
            return courtAttendanceInfoDto;
        }

        List<DailyUtilisationReportResponse.TableData.Week.Day> dailyUtilisationDays = new ArrayList<>(
            dailyUtilisation.getTableData().getWeeks().stream()
                .flatMap(week -> week.getDays().stream())
                .toList());

        // sort the days in reverse date order
        dailyUtilisationDays.sort((d1, d2) -> d2.getDate().compareTo(d1.getDate()));

        int expected = 0;
        int attended = 0;
        int onTrials = 0;
        int onTrialsToday = 0;

        // expected today is those with the next sitting date today
        List<String> expectedJurorsAtCourt = appearanceService.getExpectedJurorsAtCourt(locCode, LocalDate.now());

        // get the jurors on trials today as well as those who are expected to attend today with existing attendance
        DailyUtilisationReportJurorsResponse utilisationReportJurorsResponse =
            utilisationReportService.viewDailyUtilisationJurors(locCode, LocalDate.now());

        // need to read all the juror numbers from utilisation report
        List<String> utilisationJurors = utilisationReportJurorsResponse.getTableData().getJurors().stream()
            .map(DailyUtilisationReportJurorsResponse.TableData.Juror::getJuror)
            .toList();

        expectedJurorsAtCourt.addAll(utilisationJurors);

        // find jurors who have an appearance record and completed today
        List<String> jurorsWithCompletedToday =
            appearanceService.getCompletedJurorsAtCourt(locCode, LocalDate.now());

        expectedJurorsAtCourt.addAll(jurorsWithCompletedToday);

        final int expectedToday = expectedJurorsAtCourt.stream().distinct().toList().size();

        boolean skip = true;

        for (DailyUtilisationReportResponse.TableData.Week.Day day : dailyUtilisationDays) {

            // skip the first day as it is today's stats
            if (skip) {
                onTrialsToday = day.getSittingDays();
                skip = false;
                continue;
            }

            expected += day.getJurorWorkingDays();
            attended += day.getAttendanceDays();
            onTrials += day.getSittingDays();
        }

        // also need to add those jurors who are panelled to be part of on trials today
        onTrialsToday += panelService.getCountPanelledJurors(locCode);

        // set the last 7 days stats
        CourtAttendanceInfoDto.AttendanceStatsLastSevenDays attendanceStatsLastSevenDays =
            CourtAttendanceInfoDto.AttendanceStatsLastSevenDays.builder()
                .expected(expected)
                .attended(attended)
                .onTrials(onTrials)
                .build();

        // get the jurors absent in the last 7 days, those with no shows
        attendanceStatsLastSevenDays.setAbsent(
            appearanceService.getAbsentCountAtCourt(locCode, LocalDate.now().minusDays(7),
                LocalDate.now().minusDays(1)));

        courtAttendanceInfoDto.setAttendanceStatsLastSevenDays(attendanceStatsLastSevenDays);

        CourtAttendanceInfoDto.AttendanceStatsToday attendanceStatsToday =
            CourtAttendanceInfoDto.AttendanceStatsToday.builder()
                .expected(expectedToday)
                .onTrials(onTrialsToday)
                .build();

        // get jurors checked in today
        attendanceStatsToday.setCheckedIn(appearanceService.getCountCheckedInJurors(locCode, LocalDate.now()));

        // get jurors checked out today
        attendanceStatsToday.setCheckedOut(appearanceService.getCountCheckedOutJurors(locCode, LocalDate.now()));

        // get jurors with confirmed attendance today, this does not include jurors on trials
        // but does include non-attendance
        final int confirmedAttendances = appearanceService.getConfirmedAttendanceCountAtCourt(locCode, LocalDate.now(),
                                                                                        true, false);

        // get the absent jurors today, those who have not checked in or checked out
        final int absentToday = appearanceService.getAbsentCountAtCourt(locCode, LocalDate.now(), LocalDate.now());

        int notCheckedIn = expectedToday - (confirmedAttendances
            + absentToday
            + attendanceStatsToday.getCheckedIn()
            + attendanceStatsToday.getCheckedOut()
            + attendanceStatsToday.getOnTrials());

        if (notCheckedIn < 0) {
            // log the values of the calculations
            log.info("Not checked in count is negative for court {}, resetting to 0. Current value: {}\n"
                         + "Expected: {}, Confirmed: {}, Absent: {}, Checked In: {}, Checked Out: {}, On Trials: {}",
                     locCode, notCheckedIn, expectedToday, confirmedAttendances,
                     absentToday, attendanceStatsToday.getCheckedIn(),
                     attendanceStatsToday.getCheckedOut(), attendanceStatsToday.getOnTrials());
            notCheckedIn = 0; // ensure not negative

        }

        attendanceStatsToday.setNotCheckedIn(notCheckedIn);

        courtAttendanceInfoDto.setAttendanceStatsToday(attendanceStatsToday);

        return courtAttendanceInfoDto;
    }

    private void getUtilisationInfo(String locCode, CourtAdminInfoDto courtAdminInfoDto) {
        log.info("Retrieving utilisation info for location code: {}", locCode);
        // get the last run utilisation report for the court
        List<UtilisationStats> utilisationStats = utilisationStatsRepository
            .findTop12ByLocCodeOrderByMonthStartDesc(locCode);

        if (!utilisationStats.isEmpty()) {
            UtilisationStats lastUtilisationStats = utilisationStats.get(0);
            courtAdminInfoDto.setUtilisationReportDate(lastUtilisationStats.getLastUpdate());

            // calculate the overall utilisation
            double overallUtilisation = lastUtilisationStats.getAvailableDays() == 0
                ? 0.0
                : (double)  lastUtilisationStats.getSittingDays() / lastUtilisationStats.getAvailableDays() * 100;

            courtAdminInfoDto.setUtilisationPercentage(overallUtilisation);

        } else {
            log.info("No utilisation stats found for location code: {}", locCode);
        }
    }

}

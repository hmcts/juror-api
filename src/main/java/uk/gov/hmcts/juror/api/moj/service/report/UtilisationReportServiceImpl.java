package uk.gov.hmcts.juror.api.moj.service.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.DailyUtilisationReportJurorsResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.DailyUtilisationReportResponse;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.utils.CourtLocationUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@SuppressWarnings("PMD.LawOfDemeter")
public class UtilisationReportServiceImpl implements UtilisationReportService {
    private final CourtLocationRepository courtLocationRepository;
    private final JurorRepository jurorRepository;

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings({"PMD.CognitiveComplexity", "PMD.CyclomaticComplexity"})
    public DailyUtilisationReportResponse viewDailyUtilisationReport(String locCode, LocalDate reportFromDate,
                                                                     LocalDate reportToDate) {
        log.info("Fetching daily utilisation stats for location: {} from: {} to: {}", locCode, reportFromDate,
            reportToDate);

        if (reportFromDate.isAfter(reportToDate)) {
            throw new MojException.BadRequest("Report from date cannot be after report to date", null);
        }

        // check the user has permission to view the location
        CourtLocation courtLocation = CourtLocationUtils.validateAccessToCourtLocation(locCode,
            SecurityUtil.getActiveOwner(),
            courtLocationRepository);

        Map<String, AbstractReportResponse.DataTypeValue> reportHeadings
            = getDailyUtilReportHeaders(reportFromDate, reportToDate, courtLocation.getName());

        DailyUtilisationReportResponse response = new DailyUtilisationReportResponse(reportHeadings);

        try {
            List<String> results = jurorRepository.callDailyUtilStats(locCode, reportFromDate, reportToDate);

            if (results != null && !results.isEmpty()) {

                DailyUtilisationReportResponse.TableData tableData = response.getTableData();

                boolean firstPass = true;
                DailyUtilisationReportResponse.TableData.Week week = null;

                for (String result : results) {

                    List<String> res = List.of(result.split(","));
                    LocalDate date = LocalDate.parse(res.get(0), DateTimeFormatter.ISO_LOCAL_DATE);
                    Double utilisation = Double.parseDouble(res.get(5));

                    int workingDays = Integer.parseInt(res.get(1));
                    if (workingDays == 0 && (date.getDayOfWeek().equals(DayOfWeek.SATURDAY)
                        || date.getDayOfWeek().equals(DayOfWeek.SUNDAY))) {
                        // ignore weekends with no working days
                        continue;
                    }

                    if (date.getDayOfWeek().equals(DayOfWeek.MONDAY) || firstPass) {
                        week = new DailyUtilisationReportResponse.TableData.Week();
                        initialiseWeek(tableData, week);
                        firstPass = false;
                    }

                    int sittingDays = Integer.parseInt(res.get(2));
                    int attendanceDays = Integer.parseInt(res.get(3));
                    int nonAttendanceDays = Integer.parseInt(res.get(4));

                    DailyUtilisationReportResponse.TableData.Week.Day day =
                        DailyUtilisationReportResponse.TableData.Week.Day.builder()
                            .date(date)
                            .jurorWorkingDays(workingDays)
                            .sittingDays(sittingDays)
                            .attendanceDays(attendanceDays)
                            .nonAttendanceDays(nonAttendanceDays)
                            .utilisation(utilisation * 100)
                            .build();

                    updateWeek(week, workingDays, sittingDays, attendanceDays, nonAttendanceDays, day);

                    updateDailyUtilOverallTotals(tableData, workingDays, sittingDays, attendanceDays,
                        nonAttendanceDays);

                }

                calculateDailyOverallUtilisation(tableData);

            }
        } catch (SQLException exc) {
            log.error("Error while fetching daily utilisation stats", exc.getMessage());
            throw new MojException.InternalServerError("Error while fetching daily utilisation stats", exc);
        }

        log.info("Fetched daily utilisation stats for location: {} from: {} to: {}", locCode, reportFromDate,
            reportToDate);

        return response;
    }


    @Override
    @Transactional(readOnly = true)
    public DailyUtilisationReportJurorsResponse viewDailyUtilisationJurors(String locCode, LocalDate reportDate) {
        log.info("Fetching daily utilisation jurors stats for location: {} on: {}", locCode, reportDate);

        // check the user has permission to view the location
        CourtLocation courtLocation = CourtLocationUtils.validateAccessToCourtLocation(locCode,
            SecurityUtil.getActiveOwner(),
            courtLocationRepository);

        Map<String, AbstractReportResponse.DataTypeValue> reportHeadings
            = getDailyUtilJurorsReportHeaders(reportDate, courtLocation.getName());

        DailyUtilisationReportJurorsResponse response = new DailyUtilisationReportJurorsResponse(reportHeadings);

        try {
            List<String> results = jurorRepository.callDailyUtilJurorsStats(locCode, reportDate);

            if (results != null && !results.isEmpty()) {

                DailyUtilisationReportJurorsResponse.TableData tableData = response.getTableData();

                for (String result : results) {

                    List<String> res = List.of(result.split(","));
                    DailyUtilisationReportJurorsResponse.TableData.Juror jurors =
                        DailyUtilisationReportJurorsResponse.TableData.Juror.builder()
                            .juror(res.get(1))
                            .jurorWorkingDay(Integer.parseInt(res.get(2)))
                            .sittingDay(Integer.parseInt(res.get(3)))
                            .attendanceDay(Integer.parseInt(res.get(4)))
                            .nonAttendanceDay(Integer.parseInt(res.get(5)))
                            .build();

                    tableData.getJurors().add(jurors);

                    //update the totals
                    tableData.setTotalJurorWorkingDays(tableData.getTotalJurorWorkingDays()
                        + jurors.getJurorWorkingDay());
                    tableData.setTotalSittingDays(tableData.getTotalSittingDays() + jurors.getSittingDay());
                    tableData.setTotalAttendanceDays(tableData.getTotalAttendanceDays() + jurors.getAttendanceDay());
                    tableData.setTotalNonAttendanceDays(tableData.getTotalNonAttendanceDays()
                        + jurors.getNonAttendanceDay());

                }

            }
        } catch (SQLException exc) {
            log.error("Error while fetching daily utilisation jurors stats", exc.getMessage());
            throw new MojException.InternalServerError("Error while fetching daily utilisation jurors stats", exc);
        }

        log.info("Fetched daily utilisation jurors stats for location: {} on: {}", locCode, reportDate);

        return response;

    }

    private Map<String, AbstractReportResponse.DataTypeValue>
        getDailyUtilJurorsReportHeaders(LocalDate reportDate, String name) {
        return Map.of(
            "date", AbstractReportResponse.DataTypeValue.builder()
                .displayName(DailyUtilisationReportJurorsResponse.ReportHeading.REPORT_DATE.getDisplayName())
                .dataType(DailyUtilisationReportJurorsResponse.ReportHeading.REPORT_DATE.getDataType())
                .value(reportDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build(),
            "report_created", AbstractReportResponse.DataTypeValue.builder()
                .displayName(DailyUtilisationReportJurorsResponse.ReportHeading.REPORT_CREATED.getDisplayName())
                .dataType(DailyUtilisationReportJurorsResponse.ReportHeading.REPORT_CREATED.getDataType())
                .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build(),
            "time_created", AbstractReportResponse.DataTypeValue.builder()
                .displayName(DailyUtilisationReportJurorsResponse.ReportHeading.TIME_CREATED.getDisplayName())
                .dataType(DailyUtilisationReportJurorsResponse.ReportHeading.TIME_CREATED.getDataType())
                .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build(),
            "court_name", AbstractReportResponse.DataTypeValue.builder()
                .displayName(DailyUtilisationReportJurorsResponse.ReportHeading.COURT_NAME.getDisplayName())
                .dataType(DailyUtilisationReportJurorsResponse.ReportHeading.COURT_NAME.getDataType())
                .value(name)
                .build()
        );
    }


    private void initialiseWeek(DailyUtilisationReportResponse.TableData tableData,
                                DailyUtilisationReportResponse.TableData.Week week) {
        tableData.getWeeks().add(week);
        week.setDays(new ArrayList<>());
    }

    private void calculateDailyOverallUtilisation(DailyUtilisationReportResponse.TableData tableData) {
        Double overallUtilisation = tableData.getOverallTotalJurorWorkingDays() == 0
            ? 0.0
            : (double) tableData.getOverallTotalSittingDays()
                / tableData.getOverallTotalJurorWorkingDays() * 100;
        tableData.setOverallTotalUtilisation(overallUtilisation);
    }

    private void updateDailyUtilOverallTotals(DailyUtilisationReportResponse.TableData tableData, int workingDays,
                                              int sittingDays, int attendanceDays, int nonAttendanceDays) {
        tableData.setOverallTotalJurorWorkingDays(tableData.getOverallTotalJurorWorkingDays()
            + workingDays);
        tableData.setOverallTotalSittingDays(tableData.getOverallTotalSittingDays() + sittingDays);
        tableData.setOverallTotalAttendanceDays(tableData.getOverallTotalAttendanceDays()
            + attendanceDays);
        tableData.setOverallTotalNonAttendanceDays(tableData.getOverallTotalNonAttendanceDays()
            + nonAttendanceDays);
    }

    private void updateWeek(DailyUtilisationReportResponse.TableData.Week week, int workingDays, int sittingDays,
                            int attendanceDays, int nonAttendanceDays,
                            DailyUtilisationReportResponse.TableData.Week.Day day) {
        week.getDays().add(day);
        week.setWeeklyTotalJurorWorkingDays(week.getWeeklyTotalJurorWorkingDays() + workingDays);
        week.setWeeklyTotalSittingDays(week.getWeeklyTotalSittingDays() + sittingDays);
        week.setWeeklyTotalAttendanceDays(week.getWeeklyTotalAttendanceDays() + attendanceDays);
        week.setWeeklyTotalNonAttendanceDays(week.getWeeklyTotalNonAttendanceDays() + nonAttendanceDays);

        Double weekUtilisation = week.getWeeklyTotalJurorWorkingDays() == 0
            ? 0.0
            :
                (double) week.getWeeklyTotalSittingDays() / week.getWeeklyTotalJurorWorkingDays() * 100;
        week.setWeeklyTotalUtilisation(weekUtilisation);
    }

    private Map<String, AbstractReportResponse.DataTypeValue>
        getDailyUtilReportHeaders(LocalDate reportFromDate, LocalDate reportToDate, String courtName) {
        return Map.of(
            "date_from", AbstractReportResponse.DataTypeValue.builder()
                .displayName(DailyUtilisationReportResponse.ReportHeading.DATE_FROM.getDisplayName())
                .dataType(DailyUtilisationReportResponse.ReportHeading.DATE_FROM.getDataType())
                .value(reportFromDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build(),
            "date_to", AbstractReportResponse.DataTypeValue.builder()
                .displayName(DailyUtilisationReportResponse.ReportHeading.DATE_TO.getDisplayName())
                .dataType(DailyUtilisationReportResponse.ReportHeading.DATE_TO.getDataType())
                .value(reportToDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build(),
            "report_created", AbstractReportResponse.DataTypeValue.builder()
                .displayName(DailyUtilisationReportResponse.ReportHeading.REPORT_CREATED.getDisplayName())
                .dataType(DailyUtilisationReportResponse.ReportHeading.REPORT_CREATED.getDataType())
                .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build(),
            "time_created", AbstractReportResponse.DataTypeValue.builder()
                .displayName(DailyUtilisationReportResponse.ReportHeading.TIME_CREATED.getDisplayName())
                .dataType(DailyUtilisationReportResponse.ReportHeading.TIME_CREATED.getDataType())
                .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build(),
            "court_name", AbstractReportResponse.DataTypeValue.builder()
                .displayName(DailyUtilisationReportResponse.ReportHeading.COURT_NAME.getDisplayName())
                .dataType(DailyUtilisationReportResponse.ReportHeading.COURT_NAME.getDataType())
                .value(courtName)
                .build()
        );
    }

}

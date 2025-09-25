package uk.gov.hmcts.juror.api.moj.service.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.CourtUtilisationStatsReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.CourtUtilisationStatsReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.DailyUtilisationReportJurorsResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.DailyUtilisationReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.MonthlyUtilisationReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.UtilisationStats;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.UtilisationStatsRepository;
import uk.gov.hmcts.juror.api.moj.utils.CourtLocationUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UtilisationReportServiceImpl implements UtilisationReportService {
    private final CourtLocationRepository courtLocationRepository;
    private final JurorRepository jurorRepository;

    private final UtilisationStatsRepository utilisationStatsRepository;

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings({"PMD.CognitiveComplexity", "PMD.CyclomaticComplexity"})
    public DailyUtilisationReportResponse viewDailyUtilisationReport(String locCode, LocalDate reportFromDate,
                                                                     LocalDate reportToDate) {
        log.info("Fetching daily utilisation stats for location: {} from: {} to: {}", locCode, reportFromDate,
            reportToDate);

        validateDates(reportFromDate, reportToDate);

        // check the user has permission to view the location
        CourtLocation courtLocation = CourtLocationUtils.validateAccessToCourtLocation(locCode,
            SecurityUtil.getActiveOwner(),
            courtLocationRepository);

        Map<String, AbstractReportResponse.DataTypeValue> reportHeadings
            = getUtilReportHeaders(reportFromDate, reportToDate, courtLocation.getName());

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
                    Double utilisation = Double.parseDouble(res.get(5));

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

    private void validateDates(LocalDate reportFromDate, LocalDate reportToDate) {
        if (reportFromDate.isAfter(reportToDate)) {
            throw new MojException.BadRequest("Report from date cannot be after report to date", null);
        }

        // check the difference between the report from and report to dates is less than or equal to 31 days
        if (reportFromDate.plusDays(31).isBefore(reportToDate)) {
            throw new MojException.BadRequest("Report date range cannot be more than 31 days", null);
        }
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

                    if (res.get(1).equals("null")) {
                        continue;
                    }

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
                    updateJurorTotals(tableData, jurors);

                }

            }
        } catch (SQLException exc) {
            log.error("Error while fetching daily utilisation jurors stats", exc.getMessage());
            throw new MojException.InternalServerError("Error while fetching daily utilisation jurors stats", exc);
        }

        log.info("Fetched daily utilisation jurors stats for location: {} on: {}", locCode, reportDate);

        return response;

    }

    @Override
    @Transactional
    public MonthlyUtilisationReportResponse generateMonthlyUtilisationReport(String locCode, LocalDate reportDate) {

        // extract the month and year from the report date
        int month = reportDate.getMonthValue();
        int year = reportDate.getYear();

        log.info("Fetching monthly utilisation jurors stats for location: {} month: {} year: {}",
            locCode, month, year);

        // check the user has permission to view the location
        CourtLocation courtLocation = CourtLocationUtils.validateAccessToCourtLocation(locCode,
            SecurityUtil.getActiveOwner(),
            courtLocationRepository);

        LocalDate reportFromDate = LocalDate.of(year, month, 1);
        LocalDate reportToDate = reportFromDate.withDayOfMonth(reportFromDate.lengthOfMonth());

        // The monthly utilisation report uses the exact same headers as the daily utilisation report
        Map<String, AbstractReportResponse.DataTypeValue> reportHeadings
            = getUtilReportHeaders(reportFromDate, reportToDate, courtLocation.getName());

        MonthlyUtilisationReportResponse response = new MonthlyUtilisationReportResponse(reportHeadings);

        try {
            List<String> results = jurorRepository.callDailyUtilStats(locCode, reportFromDate, reportToDate);

            if (results != null && !results.isEmpty()) {

                MonthlyUtilisationReportResponse.TableData tableData = response.getTableData();

                int workingDays = 0;
                int sittingDays = 0;
                int attendanceDays = 0;
                int nonAttendanceDays = 0;

                for (String result : results) {

                    List<String> res = List.of(result.split(","));

                    workingDays = workingDays + Integer.parseInt(res.get(1));
                    sittingDays = sittingDays + Integer.parseInt(res.get(2));
                    attendanceDays = attendanceDays + Integer.parseInt(res.get(3));
                    nonAttendanceDays = nonAttendanceDays + Integer.parseInt(res.get(4));
                }

                Double utilisation = workingDays == 0 ? 0.0 : (double) sittingDays / workingDays * 100;

                MonthlyUtilisationReportResponse.TableData.Month monthData =
                    MonthlyUtilisationReportResponse.TableData.Month.builder()
                        .month(reportDate.getMonth().getDisplayName(TextStyle.FULL, Locale.UK) + " "
                            + reportDate.getYear())
                        .jurorWorkingDays(workingDays)
                        .sittingDays(sittingDays)
                        .attendanceDays(attendanceDays)
                        .nonAttendanceDays(nonAttendanceDays)
                        .utilisation(utilisation)
                        .build();

                tableData.getMonths().add(monthData);

                //update the totals
                tableData.setTotalJurorWorkingDays(workingDays);
                tableData.setTotalSittingDays(sittingDays);
                tableData.setTotalAttendanceDays(attendanceDays);
                tableData.setTotalNonAttendanceDays(nonAttendanceDays);
                tableData.setTotalUtilisation(utilisation);

                // save the monthly utilisation report to the database
                utilisationStatsRepository.save(new UtilisationStats(reportFromDate,
                    courtLocation.getLocCode(), workingDays, attendanceDays, sittingDays, LocalDateTime.now()));

            }
        } catch (SQLException exc) {
            log.error("Error while fetching monthly utilisation jurors stats", exc.getMessage());
            throw new MojException.InternalServerError("Error while fetching monthly utilisation jurors stats", exc);
        }

        log.info("Fetched monthly utilisation jurors stats for location: {} month: {} year: {}",
            locCode, month, year);

        return response;
    }

    @Override
    public MonthlyUtilisationReportResponse viewMonthlyUtilisationReport(String locCode, LocalDate reportDate,
                                                                         boolean previousMonths) {
        // extract the month and year from the report date
        int month = reportDate.getMonthValue();
        int year = reportDate.getYear();

        log.info("Fetching monthly utilisation jurors stats for location: {} month: {} year: {}",
            locCode, month, year);

        // check the user has permission to view the location
        CourtLocation courtLocation = CourtLocationUtils.validateAccessToCourtLocation(locCode,
            SecurityUtil.getActiveOwner(),
            courtLocationRepository);

        LocalDate reportToDate = LocalDate.of(year, month, 1);
        LocalDate reportFromDate = reportToDate;

        if (previousMonths) {
            reportFromDate = reportFromDate.minusMonths(2);
        }

        // read the monthly utilisation report from the database for given date range
        List<UtilisationStats> utilisationStats = utilisationStatsRepository
            .findByMonthStartBetweenAndLocCode(reportFromDate, reportToDate, locCode);

        Map<String, AbstractReportResponse.DataTypeValue> reportHeadings
            = getViewMonthlyUtilReportHeaders(courtLocation.getName());

        MonthlyUtilisationReportResponse response = new MonthlyUtilisationReportResponse(reportHeadings);

        if (utilisationStats != null && !utilisationStats.isEmpty()) {
            MonthlyUtilisationReportResponse.TableData tableData = response.getTableData();

            for (UtilisationStats stats : utilisationStats) {
                Double utilisation = stats.getAvailableDays() == 0 ? 0.0
                    : (double) stats.getSittingDays() / stats.getAvailableDays() * 100;

                MonthlyUtilisationReportResponse.TableData.Month monthData =
                    MonthlyUtilisationReportResponse.TableData.Month.builder()
                        .month(stats.getMonthStart().getMonth().getDisplayName(TextStyle.FULL, Locale.UK) + " "
                            + stats.getMonthStart().getYear())
                        .jurorWorkingDays(stats.getAvailableDays())
                        .sittingDays(stats.getSittingDays())
                        .attendanceDays(stats.getAttendanceDays())
                        .nonAttendanceDays(stats.getAvailableDays() - stats.getAttendanceDays())
                        .utilisation(utilisation)
                        .build();

                tableData.getMonths().add(monthData);

                //update the totals
                updateTotalStats(tableData, stats);
            }

            // calculate the overall utilisation
            Double overallUtilisation = tableData.getTotalJurorWorkingDays() == 0
                ? 0.0
                : (double) tableData.getTotalSittingDays() / tableData.getTotalJurorWorkingDays() * 100;

            tableData.setTotalUtilisation(overallUtilisation);
        }

        return response;
    }

    @Override
    public String getMonthlyUtilisationReports(String locCode) {
        log.info("Fetching monthly utilisation reports for location: {}", locCode);

        // check the user has permission to view the location
        CourtLocationUtils.validateAccessToCourtLocation(locCode,
            SecurityUtil.getActiveOwner(),
            courtLocationRepository);

        List<UtilisationStats> utilisationStats = utilisationStatsRepository
            .findTop12ByLocCodeOrderByMonthStartDesc(locCode);

        StringBuilder response = new StringBuilder();

        if (utilisationStats != null && !utilisationStats.isEmpty()) {
            for (UtilisationStats stats : utilisationStats) {
                response.append(stats.getMonthStart().getMonth().getDisplayName(TextStyle.FULL, Locale.UK))
                    .append(' ')
                    .append(stats.getMonthStart().getYear())
                    .append(',');
            }
        }

        log.info("Fetched monthly utilisation reports for location: {}", locCode);

        return response.toString();

    }

    @Override
    public CourtUtilisationStatsReportResponse courtUtilisationStatsReport(
                                                        CourtUtilisationStatsReportRequest request) {

        String courtNames;
        if (request.isAllCourts()) {
            log.info("Fetching court utilisation jurors stats for all locations");
            courtNames = "All Courts";
            // if all courts are requested, we don't need to filter by location codes
            request.setCourtLocCodes(new ArrayList<>());
        } else {
            if (request.getCourtLocCodes().isEmpty()) {
                throw new MojException.BadRequest("Court location codes cannot be empty", null);
            }
            log.info("Fetching court utilisation jurors stats for locations: {}", request.getCourtLocCodes());

            List<CourtLocation> courtLocations = courtLocationRepository
                .findByLocCodeIn(request.getCourtLocCodes());
            if (courtLocations.isEmpty()) {
                throw new MojException.BadRequest("No court locations found for the provided codes", null);
            }
            // create a comma separated list of court names and location codes
            courtNames = courtLocations.stream()
                .map(cl -> cl.getName() + " (" + cl.getLocCode() + ")")
                .reduce((a, b) -> a + ", " + b)
                .orElse("No Court Locations Found");
        }

        Map<String, AbstractReportResponse.DataTypeValue> reportHeadings = getCourtUtilStatsReportHeaders(courtNames);
        CourtUtilisationStatsReportResponse response = new CourtUtilisationStatsReportResponse(reportHeadings);

        List<String> utilisationStats = utilisationStatsRepository.getCourtUtilisationStats();

        if (utilisationStats != null && !utilisationStats.isEmpty()) {
            getCourtUtilisationStats(utilisationStats, response.getTableData(), request.getCourtLocCodes());
        }

        return response;
    }

    private void getCourtUtilisationStats(List<String> utilisationStats,
                                          CourtUtilisationStatsReportResponse.TableData tableData,
                                          List<String> courtLocCodes) {
        for (String line : utilisationStats) {

            List<String> stats = List.of(line.split(","));

            if (stats.size() < 6) {
                log.warn("Invalid utilisation stats line: {}", line);
                throw new MojException.InternalServerError("Invalid utilisation stats line: " + line, null);
            }

            stats = adjustedStatsForCommas(stats);

            try {
                String locCode = stats.get(0);
                String locName = stats.get(1);

                if (!courtLocCodes.isEmpty() && !courtLocCodes.contains(locCode)) {
                    // skip this record if the location code is not in the requested list
                    continue;
                }

                LocalDate monthStart = LocalDate.parse(stats.get(2), DateTimeFormatter.ISO_LOCAL_DATE);
                int availableDays = Integer.parseInt(stats.get(3));
                int sittingDays = Integer.parseInt(stats.get(4));
                LocalDateTime lastUpdate = LocalDateTime.parse(
                    stats.get(5).substring(0, 19),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                );

                double utilisation = availableDays == 0 ? 0.0 : (double) sittingDays / availableDays * 100;

                CourtUtilisationStatsReportResponse.UtilisationStats utilisationStat =
                    CourtUtilisationStatsReportResponse.UtilisationStats.builder()
                        .courtName(locName + " (" + locCode + ")")
                        .utilisation(utilisation)
                        .month(monthStart.getMonth().getDisplayName(TextStyle.FULL, Locale.UK) + " "
                            + monthStart.getYear())
                        .dateLastRun(lastUpdate)
                        .build();

                tableData.getData().add(utilisationStat);
            } catch (Exception e) {
                log.warn("Error parsing utilisation stats line: {}", line, e);
            }

        }
    }

    private List<String> adjustedStatsForCommas(List<String> stats) {
        if (stats.size() > 6) {
            String locName = String.join(",", stats.subList(1, stats.size() - 4));
            List<String> adjustedStats = new ArrayList<>();
            adjustedStats.add(stats.get(0)); // locCode
            adjustedStats.add(locName); // locName
            adjustedStats.addAll(stats.subList(stats.size() - 4, stats.size())); // Remaining stats
            return adjustedStats;
        }
        return stats;
    }

    private void updateTotalStats(MonthlyUtilisationReportResponse.TableData tableData, UtilisationStats stats) {
        tableData.setTotalJurorWorkingDays(tableData.getTotalJurorWorkingDays() + stats.getAvailableDays());
        tableData.setTotalSittingDays(tableData.getTotalSittingDays() + stats.getSittingDays());
        tableData.setTotalAttendanceDays(tableData.getTotalAttendanceDays() + stats.getAttendanceDays());
        tableData.setTotalNonAttendanceDays(tableData.getTotalNonAttendanceDays()
            + (stats.getAvailableDays() - stats.getAttendanceDays()));
    }

    private void updateJurorTotals(DailyUtilisationReportJurorsResponse.TableData tableData,
                                  DailyUtilisationReportJurorsResponse.TableData.Juror jurors) {
        tableData.setTotalJurorWorkingDays(tableData.getTotalJurorWorkingDays()
            + jurors.getJurorWorkingDay());
        tableData.setTotalSittingDays(tableData.getTotalSittingDays() + jurors.getSittingDay());
        tableData.setTotalAttendanceDays(tableData.getTotalAttendanceDays() + jurors.getAttendanceDay());
        tableData.setTotalNonAttendanceDays(tableData.getTotalNonAttendanceDays()
            + jurors.getNonAttendanceDay());
    }


    private Map<String, AbstractReportResponse.DataTypeValue>
        getDailyUtilJurorsReportHeaders(LocalDate reportDate, String name) {
        return Map.of(
            "date", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.REPORT_DATE.getDisplayName())
                .dataType(ReportHeading.REPORT_DATE.getDataType())
                .value(reportDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build(),
            "report_created", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.REPORT_CREATED.getDisplayName())
                .dataType(ReportHeading.REPORT_CREATED.getDataType())
                .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build(),
            "time_created", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.TIME_CREATED.getDisplayName())
                .dataType(ReportHeading.TIME_CREATED.getDataType())
                .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build(),
            "court_name", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.COURT_NAME.getDisplayName())
                .dataType(ReportHeading.COURT_NAME.getDataType())
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
        getUtilReportHeaders(LocalDate reportFromDate, LocalDate reportToDate, String courtName) {
        return Map.of(
            "date_from", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.DATE_FROM.getDisplayName())
                .dataType(ReportHeading.DATE_FROM.getDataType())
                .value(reportFromDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build(),
            "date_to", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.DATE_TO.getDisplayName())
                .dataType(ReportHeading.DATE_TO.getDataType())
                .value(reportToDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build(),
            "report_created", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.REPORT_CREATED.getDisplayName())
                .dataType(ReportHeading.REPORT_CREATED.getDataType())
                .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build(),
            "time_created", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.TIME_CREATED.getDisplayName())
                .dataType(ReportHeading.TIME_CREATED.getDataType())
                .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build(),
            "court_name", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.COURT_NAME.getDisplayName())
                .dataType(ReportHeading.COURT_NAME.getDataType())
                .value(courtName)
                .build()
        );
    }


    private Map<String, AbstractReportResponse.DataTypeValue>
        getViewMonthlyUtilReportHeaders(String courtName) {
        return Map.of(
            "report_created", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.REPORT_CREATED.getDisplayName())
                .dataType(ReportHeading.REPORT_CREATED.getDataType())
                .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build(),
            "time_created", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.TIME_CREATED.getDisplayName())
                .dataType(ReportHeading.TIME_CREATED.getDataType())
                .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build(),
            "court_name", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.COURT_NAME.getDisplayName())
                .dataType(ReportHeading.COURT_NAME.getDataType())
                .value(courtName)
                .build()
        );
    }

    private Map<String, AbstractReportResponse.DataTypeValue> getCourtUtilStatsReportHeaders(String courtName) {
        // uses the same headers as the monthly utilisation report
        return getViewMonthlyUtilReportHeaders(courtName);
    }

    public enum ReportHeading {
        REPORT_DATE("Report date", LocalDate.class.getSimpleName()),
        DATE_FROM("Date from", LocalDate.class.getSimpleName()),
        DATE_TO("Date to", LocalDate.class.getSimpleName()),
        REPORT_CREATED("Report created", LocalDate.class.getSimpleName()),
        TIME_CREATED("Time created", LocalDateTime.class.getSimpleName()),
        COURT_NAME("Court name", String.class.getSimpleName());

        private String displayName;

        private String dataType;

        ReportHeading(String displayName, String dataType) {
            this.displayName = displayName;
            this.dataType = dataType;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDataType() {
            return dataType;
        }
    }
}

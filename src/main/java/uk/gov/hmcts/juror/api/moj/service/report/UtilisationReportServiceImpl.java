package uk.gov.hmcts.juror.api.moj.service.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
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
    public DailyUtilisationReportResponse viewDailyUtilisationReport(String locCode, LocalDate reportFromDate,
                                                                     LocalDate reportToDate) {
        log.info("Fetching daily utilisation stats for location: {} from: {} to: {}", locCode, reportFromDate,
            reportToDate);
        // check the user has permission to view the location
        CourtLocation courtLocation = CourtLocationUtils.validateAccessToCourtLocation(locCode,
            SecurityUtil.getActiveOwner(),
            courtLocationRepository);

        Map<String, AbstractReportResponse.DataTypeValue> reportHeadings
            = getReportHeaders(reportFromDate, reportToDate, courtLocation.getName());

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

                    if (date.getDayOfWeek().equals(DayOfWeek.MONDAY) || firstPass) {
                        week = new DailyUtilisationReportResponse.TableData.Week();
                        tableData.getWeeks().add(week);
                        week.setDays(new ArrayList<>());
                        firstPass = false;
                    }

                    int workingDays = Integer.parseInt(res.get(1));
                    if (workingDays == 0 && (date.getDayOfWeek().equals(DayOfWeek.SATURDAY)
                        || date.getDayOfWeek().equals(DayOfWeek.SUNDAY))) {
                        // ignore weekends with no working days
                        continue;
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
                        .utilisation(Math.round(utilisation * 100.0))
                        .build();
                    week.getDays().add(day);
                    week.setWeeklyTotalJurorWorkingDays(week.getWeeklyTotalJurorWorkingDays() + workingDays);
                    week.setWeeklyTotalSittingDays(week.getWeeklyTotalSittingDays() + sittingDays);
                    week.setWeeklyTotalAttendanceDays(week.getWeeklyTotalAttendanceDays() + attendanceDays);
                    week.setWeeklyTotalNonAttendanceDays(week.getWeeklyTotalNonAttendanceDays() + nonAttendanceDays);

                    Double weekUtilisation = week.getWeeklyTotalJurorWorkingDays() == 0 ? 0.0 :
                        (double) week.getWeeklyTotalSittingDays() / week.getWeeklyTotalJurorWorkingDays() * 100;
                    week.setWeeklyTotalUtilisation(weekUtilisation);

                    tableData.setOverallTotalJurorWorkingDays(tableData.getOverallTotalJurorWorkingDays()
                        + workingDays);
                    tableData.setOverallTotalSittingDays(tableData.getOverallTotalSittingDays() + sittingDays);
                    tableData.setOverallTotalAttendanceDays(tableData.getOverallTotalAttendanceDays()
                        + attendanceDays);
                    tableData.setOverallTotalNonAttendanceDays(tableData.getOverallTotalNonAttendanceDays()
                        + nonAttendanceDays);

                    Double overallUtilisation = tableData.getOverallTotalJurorWorkingDays() == 0
                        ? 0.0 : (double) tableData.getOverallTotalSittingDays()
                        / tableData.getOverallTotalJurorWorkingDays() * 100;
                    tableData.setOverallTotalUtilisation(overallUtilisation);
                }
            }
        } catch (SQLException exc) {
            log.error("Error while fetching daily utilisation stats", exc.getMessage());
            throw new MojException.InternalServerError("Error while fetching daily utilisation stats", exc);
        }
        return response;
    }

    private Map<String, AbstractReportResponse.DataTypeValue>
        getReportHeaders(LocalDate reportFromDate, LocalDate reportToDate, String courtName) {
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
                .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME))
                .build(),
            "court_name", AbstractReportResponse.DataTypeValue.builder()
                .displayName(DailyUtilisationReportResponse.ReportHeading.COURT_NAME.getDisplayName())
                .dataType(DailyUtilisationReportResponse.ReportHeading.COURT_NAME.getDataType())
                .value(courtName)
                .build()
            );
    }

}

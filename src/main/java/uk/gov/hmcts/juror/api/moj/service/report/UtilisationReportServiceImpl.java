package uk.gov.hmcts.juror.api.moj.service.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
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

        // check the user has permission to view the location
        CourtLocation courtLocation = CourtLocationUtils.validateAccessToCourtLocation(locCode,
            SecurityUtil.getActiveOwner(),
            courtLocationRepository);

        List<DailyUtilisationReportResponse.Heading> reportHeadings = getReportHeaders(reportFromDate,
            reportToDate, courtLocation.getName());

        DailyUtilisationReportResponse response = new DailyUtilisationReportResponse(reportHeadings);

        try {
            List<String> results = jurorRepository.callDailyUtilStats(locCode, reportFromDate, reportToDate);

            //TODO: Complete the implementation of this method
            // sum up the weekly totals and the overall totals

            if (results != null && !results.isEmpty()) {

                DailyUtilisationReportResponse.TableData tableData = response.getTableData();
                tableData.setWeeks(new ArrayList<>());

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
                    if (workingDays == 0 && (date.getDayOfWeek().equals(DayOfWeek.SATURDAY) || date.getDayOfWeek().equals(DayOfWeek.SUNDAY))) {
                        continue;
                    }

                    int sittingDays = Integer.parseInt(res.get(2));
                    int attendanceDays = Integer.parseInt(res.get(3));
                    int nonAttendanceDays = Integer.parseInt(res.get(4));

                    DailyUtilisationReportResponse.TableData.Week.Day day = DailyUtilisationReportResponse.TableData.Week.Day.builder()
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

                }
            }

        } catch (SQLException e) {
            log.error("Error while fetching daily utilisation stats", e);
            throw new MojException.InternalServerError("Error while fetching daily utilisation stats", null);
        }

        return response;
    }


    private List<DailyUtilisationReportResponse.Heading> getReportHeaders(LocalDate reportFromDate,
                                                                          LocalDate reportToDate, String courtName) {
             return List.of(
            DailyUtilisationReportResponse.Heading.builder()
            .name(DailyUtilisationReportResponse.ReportHeading.DATE_FROM)
            .displayName(DailyUtilisationReportResponse.ReportHeading.DATE_FROM.getDisplayName())
                .value(reportFromDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .dataType(DailyUtilisationReportResponse.ReportHeading.DATE_FROM.getDataType()).build(),
            DailyUtilisationReportResponse.Heading.builder()
                .name(DailyUtilisationReportResponse.ReportHeading.DATE_TO)
                .displayName(DailyUtilisationReportResponse.ReportHeading.DATE_TO.getDisplayName())
                .value(reportToDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .dataType(DailyUtilisationReportResponse.ReportHeading.DATE_TO.getDataType()).build(),
            DailyUtilisationReportResponse.Heading.builder()
                .name(DailyUtilisationReportResponse.ReportHeading.REPORT_CREATED)
                .displayName(DailyUtilisationReportResponse.ReportHeading.REPORT_CREATED.getDisplayName())
                .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .dataType(DailyUtilisationReportResponse.ReportHeading.REPORT_CREATED.getDataType()).build(),
            DailyUtilisationReportResponse.Heading.builder()
                .name(DailyUtilisationReportResponse.ReportHeading.TIME_CREATED)
                .displayName(DailyUtilisationReportResponse.ReportHeading.TIME_CREATED.getDisplayName())
                .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME))
                .dataType(DailyUtilisationReportResponse.ReportHeading.TIME_CREATED.getDataType()).build(),
            DailyUtilisationReportResponse.Heading.builder()
                .name(DailyUtilisationReportResponse.ReportHeading.COURT_NAME)
                .displayName(DailyUtilisationReportResponse.ReportHeading.COURT_NAME.getDisplayName())
                .dataType(DailyUtilisationReportResponse.ReportHeading.COURT_NAME.getDataType())
                .value(courtName).build());
    }



}

package uk.gov.hmcts.juror.api.moj.service;

import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.IncompleteServiceReportResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.OverdueUtilisationReportResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.WeekendAttendanceReportResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.WeekendAttendanceReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.Permission;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.UtilisationStatsRepository;
import uk.gov.hmcts.juror.api.moj.service.report.AttendanceReportService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManagementDashboardServiceImpl implements ManagementDashboardService {

    private final UtilisationStatsRepository utilisationStatsRepository;
    private final JurorPoolRepository jurorPoolRepository;
    private final AttendanceReportService attendanceReportService;

    @Override
    public OverdueUtilisationReportResponseDto getOverdueUtilisationReport(boolean top10) {
        log.info("Generating overdue utilisation table for user {}", SecurityUtil.getActiveLogin());

        checkSuperUserPermission();

        List<String> utilisationStats = utilisationStatsRepository.getCourtUtilisationStats();

        return getCourtUtilisationStats(utilisationStats, top10);
    }

    @Override
    public IncompleteServiceReportResponseDto getIncompleteServiceReport() {

        checkSuperUserPermission();

        List<Tuple> incompleteServiceStats =
                jurorPoolRepository.getIncompleteServiceCountsByCourt();

        List<IncompleteServiceReportResponseDto.IncompleteServiceRecord> records = new ArrayList<>();

        for (Tuple tuple : incompleteServiceStats) {
            // populate the response DTO
            IncompleteServiceReportResponseDto.IncompleteServiceRecord record =
                    IncompleteServiceReportResponseDto.IncompleteServiceRecord.builder()
                            .court(tuple.get(0, String.class) + " (" + tuple.get(1, String.class) + ")")
                            .numberOfIncompleteServices(tuple.get(2, Long.class).intValue())
                            .build();
            records.add(record);
        }

        IncompleteServiceReportResponseDto responseDto = new IncompleteServiceReportResponseDto();
        responseDto.setRecords(records);
        return responseDto;
    }

    @Override
    public WeekendAttendanceReportResponseDto getWeekendAttendanceReport() {

        checkSuperUserPermission();

        // use the weekend attendance stats repository method to get the data
        WeekendAttendanceReportResponse attendanceReportResponse = attendanceReportService.getWeekendAttendanceReport();

        if (attendanceReportResponse == null) {
            log.info("Weekend attendance report data is null or empty");
            return new WeekendAttendanceReportResponseDto();
        }

        // need to sort the data by total attendances descending
        List<WeekendAttendanceReportResponse.TableData.DataRow> top10records =
                attendanceReportResponse.getTableData().getData().stream()
                        .sorted((r1, r2) -> r2.getTotalWeekendDays().compareTo(r1.getTotalWeekendDays()))
                .limit(10)
                .toList();

        // build the response DTO
        WeekendAttendanceReportResponseDto responseDto = new WeekendAttendanceReportResponseDto();

        // iterate through the top 10 records and map them to the DTO
        top10records.forEach(dataRow -> {
            WeekendAttendanceReportResponseDto.WeekendAttendanceRecord attendanceRecord =
                    WeekendAttendanceReportResponseDto.WeekendAttendanceRecord.builder()
                            .courtLocationNameAndCode(dataRow.getCourtLocationNameAndCode())
                            .saturdayTotal(dataRow.getSaturdayTotal())
                            .sundayTotal(dataRow.getSundayTotal())
                            .holidayTotal(dataRow.getHolidayTotal())
                            .totalPaid(dataRow.getTotalPaid())
                            .build();
            responseDto.getRecords().add(attendanceRecord);
        });

        return responseDto;
    }

    private void checkSuperUserPermission() {
        // check user has superuser permissions
        if (!SecurityUtil.hasPermission(Permission.SUPER_USER)) {
            log.info("User {} attempted to access overdue utilisation report without sufficient permissions",
                    SecurityUtil.getActiveLogin());
            throw new MojException.Forbidden("Insufficient permissions to access overdue utilisation report", null);
        }
    }


    private OverdueUtilisationReportResponseDto getCourtUtilisationStats(List<String> utilisationStats, boolean top10) {

        List<String> skippedLocCodes = List.of("127", "428", "462", "750", "751", "768", "795"); // To be confirmed
        List<OverdueUtilisationReportResponseDto.OverdueUtilisationRecord> records = new ArrayList<>();
        for (String line : utilisationStats) {

            List<String> stats = List.of(line.split(","));

            if (stats.size() < 6) {
                log.warn("Invalid utilisation stats line: {}", line);
                throw new MojException.InternalServerError("Invalid utilisation stats line: " + line, null);
            }

            stats = adjustedStatsForCommas(stats);

            try {
                String locCode = stats.get(0);
                // some courts don't have any recent utilisation stats so skip them
                if (skippedLocCodes.contains(locCode)) {
                    continue;
                }

                String locName = stats.get(1);
                int availableDays = Integer.parseInt(stats.get(3));
                int sittingDays = Integer.parseInt(stats.get(4));
                LocalDateTime lastUpdateTime = LocalDateTime.parse(stats.get(5).substring(0, 19),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                );

                LocalDate lastUpdateDate = lastUpdateTime.toLocalDate();
                int daysElapsed = (int) java.time.Duration.between(lastUpdateTime, LocalDateTime.now()).toDays();

                if (daysElapsed <= 30) {
                    // only include courts with overdue utilisation (last updated more than 30 days ago)
                    continue;
                }

                double utilisation = availableDays == 0 ? 0.0 : (double) sittingDays / availableDays * 100;

                OverdueUtilisationReportResponseDto.OverdueUtilisationRecord overdueUtilisationRecord =
                        OverdueUtilisationReportResponseDto.OverdueUtilisationRecord.builder()
                                .court(locName + " (" + locCode + ")")
                                .utilisation(utilisation)
                                .daysElapsed(daysElapsed)
                                .reportLastRun(lastUpdateDate)
                                .build();

                records.add(overdueUtilisationRecord);
            } catch (Exception e) {
                log.warn("Error parsing overdue utilisation stats line: {}", line, e);
            }

        }
        OverdueUtilisationReportResponseDto responseDto = new OverdueUtilisationReportResponseDto();

        if (!top10) {
            // sort the records by daysElapsed descending
            records.sort((r1, r2) -> r2.getDaysElapsed().compareTo(r1.getDaysElapsed()));
            responseDto.setRecords(records);
        } else {
            // Sort records by daysElapsed descending and get only 10 records
            List<OverdueUtilisationReportResponseDto.OverdueUtilisationRecord> top10Records = records.stream()
                .sorted((r1, r2) -> r2.getDaysElapsed().compareTo(r1.getDaysElapsed()))
                .limit(10)
                .toList();

            responseDto.setRecords(top10Records);
        }

        return responseDto;
    }

    @Override
    public List<String> adjustedStatsForCommas(List<String> stats) {
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

}

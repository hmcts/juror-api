package uk.gov.hmcts.juror.api.moj.service;

import com.querydsl.core.Tuple;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.ExpenseLimitsReportResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.IncompleteServiceReportResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.OverdueUtilisationReportResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.SmsMessagesReportResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.WeekendAttendanceReportResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.WeekendAttendanceReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.Permission;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.MessageRepository;
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
    private final CourtLocationRepository courtLocationRepository;
    private final MessageRepository messageRepository;

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

    @Override
    public ExpenseLimitsReportResponseDto getExpenseLimitsReport() {
        checkSuperUserPermission();

        List<String> recentlyUpdatedCourts = courtLocationRepository.getRecentlyUpdatedRecords();

        if (recentlyUpdatedCourts.isEmpty()) {
            log.info("No recently updated court location records found for expense limits report");
            return new ExpenseLimitsReportResponseDto();
        }

        List<String> codes = new ArrayList<>();
        for (String line : recentlyUpdatedCourts) {
            List<String> stats = List.of(line.split(","));
            String locCode = stats.get(2);
            codes.add(locCode);
        }

        // get distinct loc codes and limit to 10
        codes = codes.stream().distinct().limit(10).toList();

        List<String> courtRevisions = courtLocationRepository.getCourtRevisionsByLocCodes(codes);

        List<CourtLocationAuditRecord> auditRecords = new ArrayList<>();
        for (String line : courtRevisions) {
            List<String> stats = List.of(line.split(","));
            try {
                final String locCode = stats.get(0);
                final Double publicTransportSoftLimit = Double.parseDouble(stats.get(1));
                final Double taxiSoftLimit = Double.parseDouble(stats.get(2));
                final String changedBy = stats.get(3);
                String courtName = stats.get(5);
                if (stats.size() > 6) {
                    // court name has comma(s)
                    courtName = String.join(",", stats.subList(5, stats.size() - 1));
                }
                CourtLocationAuditRecord courtLocationAuditRecord = new CourtLocationAuditRecord();
                courtLocationAuditRecord.setLocCode(locCode);
                courtLocationAuditRecord.setCourtName(courtName);
                courtLocationAuditRecord.setPublicTransportSoftLimit(publicTransportSoftLimit);
                courtLocationAuditRecord.setTaxiSoftLimit(taxiSoftLimit);
                courtLocationAuditRecord.setChangedBy(changedBy);
                auditRecords.add(courtLocationAuditRecord);
            } catch (Exception e) {
                log.warn("Error parsing court location audit record line: {}", line, e);
            }
        }

        List<ExpenseLimitsReportResponseDto.ExpenseLimitsRecord> expenseLimitsRecords = new ArrayList<>();

        // use court revisions to build the expense limits report response DTO
        // the revision contains data in pairs - the latest change and the previous value
        for (int i = 0; i < auditRecords.size(); i += 2) {
            final CourtLocationAuditRecord latestRecord = auditRecords.get(i);
            final CourtLocationAuditRecord previousRecord = auditRecords.get(i + 1);

            double oldValue;
            double newValue;
            String expenseTypeChanged;

            double publicTransportDifference = Math.abs(latestRecord.getPublicTransportSoftLimit()
                                                     - previousRecord.getPublicTransportSoftLimit());
            double taxiDifference = Math.abs(latestRecord.getTaxiSoftLimit() - previousRecord.getTaxiSoftLimit());

            if (publicTransportDifference > taxiDifference) {
                oldValue = previousRecord.getPublicTransportSoftLimit();
                newValue = latestRecord.getPublicTransportSoftLimit();
                expenseTypeChanged = "Public Transport";
            } else {
                oldValue = previousRecord.getTaxiSoftLimit();
                newValue = latestRecord.getTaxiSoftLimit();
                expenseTypeChanged = "Taxi";
            }

            // put the largestRecord and differences into the response DTO as needed
            ExpenseLimitsReportResponseDto.ExpenseLimitsRecord expenseLimitsRecord =
                    ExpenseLimitsReportResponseDto.ExpenseLimitsRecord.builder()
                        .courtLocationNameAndCode(latestRecord.getCourtName() + " (" + latestRecord.getLocCode() + ")")
                        .type(expenseTypeChanged)
                        .oldLimit(oldValue)
                        .newLimit(newValue)
                        .changedBy(latestRecord.getChangedBy())
                        .build();
            expenseLimitsRecords.add(expenseLimitsRecord);
        }

        return new ExpenseLimitsReportResponseDto(expenseLimitsRecords);
    }

    @Override
    public SmsMessagesReportResponseDto getSmsMessagesReport() {

        checkSuperUserPermission();

        SmsMessagesReportResponseDto returnDto = new SmsMessagesReportResponseDto();

        List<SmsMessagesReportResponseDto.SmsMessagesRecord> records = messageRepository.getSmsMessageCounts();

        // limit to top 10 records
        returnDto.setRecords(records.stream().limit(10).toList());

        long totalMessages = records.stream()
            .mapToLong(SmsMessagesReportResponseDto.SmsMessagesRecord::getMessagesSent)
            .sum();

        returnDto.setTotalMessagesSent(totalMessages);

        return returnDto;
    }

    private void checkSuperUserPermission() {
        // check user has superuser permissions
        if (!SecurityUtil.hasPermission(Permission.SUPER_USER)) {
            log.info("User {} attempted to access overdue utilisation report without sufficient permissions",
                    SecurityUtil.getActiveLogin());
            throw new MojException.Forbidden("Insufficient permissions to access overdue utilisation report", null);
        }
    }


    private OverdueUtilisationReportResponseDto getCourtUtilisationStats(List<String> utilisationStats,
                                                                         boolean top10) {

        // List of courts to be confirmed
        List<String> skippedLocCodes = List.of("000", "127", "428", "462", "750", "751", "768", "795");
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


    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    class CourtLocationAuditRecord {
        String locCode;
        String courtName;
        Double publicTransportSoftLimit;
        Double taxiSoftLimit;
        String changedBy;
    }

}

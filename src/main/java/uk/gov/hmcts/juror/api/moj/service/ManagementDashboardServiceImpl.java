package uk.gov.hmcts.juror.api.moj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.OverdueUtilisationReportResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Permission;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.UtilisationStatsRepository;
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

    @Override
    public OverdueUtilisationReportResponseDto getOverdueUtilisationReport() {
        log.info("Generating overdue utilisation table for user {}", SecurityUtil.getActiveLogin());

        // check user has superuser permissions
        if (!SecurityUtil.hasPermission(Permission.SUPER_USER)) {
            log.info("User {} attempted to access overdue utilisation report without sufficient permissions",
                SecurityUtil.getActiveLogin());
            throw new MojException.Forbidden("Insufficient permissions to access overdue utilisation report", null);
        }

        List<String> utilisationStats = utilisationStatsRepository.getCourtUtilisationStats();

        return getCourtUtilisationStats(utilisationStats);
    }

    private OverdueUtilisationReportResponseDto getCourtUtilisationStats(List<String> utilisationStats) {

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

        // Sort records by daysElapsed descending and get only 10 records
        List<OverdueUtilisationReportResponseDto.OverdueUtilisationRecord> top10Records = records.stream()
            .sorted((r1, r2) -> r2.getDaysElapsed().compareTo(r1.getDaysElapsed()))
            .limit(10)
            .toList();
        responseDto.setRecords(top10Records);

        return responseDto;
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
}

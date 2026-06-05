package uk.gov.hmcts.juror.api.moj.service.report;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.CourtsAndDatesReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.SittingDaysStatsReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.Permission;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.StatsSittingDaysRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SittingDaysReportServiceImpl implements SittingDaysReportService {

    private final StatsSittingDaysRepository statsSittingDaysRepository;

    @Override
    @Transactional(readOnly = true)
    public SittingDaysStatsReportResponse getSittingDaysStats(CourtsAndDatesReportRequest request) {
        if (!SecurityUtil.hasPermission(Permission.SUPER_USER)) {
            throw new MojException.Forbidden("User not allowed to access this report", null);
        }

        validateRequest(request);

        List<StatsSittingDaysRepository.SittingDaysStatsData> stats = request.isAllCourts()
            ? statsSittingDaysRepository.findStatsByMonthRange(toServiceMonth(request.getFromDate()),
                toServiceMonth(request.getToDate()))
            : statsSittingDaysRepository.findStatsByMonthRangeAndCourtCodes(toServiceMonth(request.getFromDate()),
                toServiceMonth(request.getToDate()), request.getCourtLocCodes());

        List<SittingDaysStatsReportResponse.TableData.DataRow> dataRows = toDataRows(stats);
        SittingDaysStatsReportResponse response = new SittingDaysStatsReportResponse(getReportHeadings(
            request.getFromDate(), request.getToDate(), getTotalJurors(dataRows), getTotalSittingDays(dataRows)));
        response.getTableData().setData(dataRows);

        return response;
    }

    private void validateRequest(CourtsAndDatesReportRequest request) {
        if (request.getToDate().isBefore(request.getFromDate())) {
            throw new MojException.BadRequest("To date must be on or after from date", null);
        }
        if (!request.isAllCourts() && (request.getCourtLocCodes() == null || request.getCourtLocCodes().isEmpty())) {
            throw new MojException.BadRequest("No court locations provided", null);
        }
    }

    private List<SittingDaysStatsReportResponse.TableData.DataRow> toDataRows(
        List<StatsSittingDaysRepository.SittingDaysStatsData> stats) {
        Map<String, SittingDaysStatsReportResponse.TableData.DataRow> rowsByCourt = new ConcurrentHashMap<>();

        for (StatsSittingDaysRepository.SittingDaysStatsData data : stats) {
            SittingDaysStatsReportResponse.TableData.DataRow row = rowsByCourt.computeIfAbsent(data.getCourtCode(),
                courtCode -> SittingDaysStatsReportResponse.TableData.DataRow.builder()
                    .courtLocationNameAndCode(formatCourt(data.getCourtName(), courtCode))
                    .build());
            applyCategory(row, data.getSittingDaysCategory(), data.getNumberOfSittingDays());
            row.setTotalJurors(row.getTotalJurors() + defaultValue(data.getNumberOfJurors()));
            row.setTotalSittingDays(row.getTotalSittingDays() + defaultValue(data.getNumberOfSittingDays()));
        }

        return rowsByCourt.values().stream().toList();
    }

    private void applyCategory(SittingDaysStatsReportResponse.TableData.DataRow row, String category, Integer value) {
        int count = defaultValue(value);
        switch (category) {
            case "0" -> row.setZeroSittingDays(count);
            case "1" -> row.setOneSittingDay(count);
            case "2" -> row.setTwoSittingDays(count);
            case "3" -> row.setThreeSittingDays(count);
            case "4" -> row.setFourSittingDays(count);
            case "5" -> row.setFiveSittingDays(count);
            case "6" -> row.setSixSittingDays(count);
            case "7" -> row.setSevenSittingDays(count);
            case "8" -> row.setEightSittingDays(count);
            case "9" -> row.setNineSittingDays(count);
            case "10" -> row.setTenSittingDays(count);
            case "11 or more" -> row.setElevenOrMoreSittingDays(count);
            default -> log.warn("Ignoring unsupported sitting days category: {}", category);
        }
    }

    private int getTotalJurors(List<SittingDaysStatsReportResponse.TableData.DataRow> dataRows) {
        return dataRows.stream()
            .mapToInt(SittingDaysStatsReportResponse.TableData.DataRow::getTotalJurors)
            .sum();
    }

    private int getTotalSittingDays(List<SittingDaysStatsReportResponse.TableData.DataRow> dataRows) {
        return dataRows.stream()
            .mapToInt(SittingDaysStatsReportResponse.TableData.DataRow::getTotalSittingDays)
            .sum();
    }

    private int defaultValue(Integer value) {
        return value == null ? 0 : value;
    }

    private String formatCourt(String courtName, String courtCode) {
        return (courtName == null ? "" : courtName) + " (" + courtCode + ")";
    }

    private String toServiceMonth(LocalDate date) {
        return YearMonth.from(date).toString();
    }

    private Map<String, AbstractReportResponse.DataTypeValue> getReportHeadings(LocalDate reportFrom,
                                                                                 LocalDate reportTo,
                                                                                 Integer totalJurors,
                                                                                 Integer totalSittingDays) {
        return Map.of(
            "date_from", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.DATE_FROM.getDisplayName())
                .dataType(ReportHeading.DATE_FROM.getDataType())
                .value(reportFrom.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build(),
            "date_to", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.DATE_TO.getDisplayName())
                .dataType(ReportHeading.DATE_TO.getDataType())
                .value(reportTo.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build(),
            "total_number_of_jurors", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.TOTAL_JURORS_DAYS.getDisplayName())
                .dataType(ReportHeading.TOTAL_JURORS_DAYS.getDataType())
                .value(totalJurors)
                .build(),
            "total_sitting_days", AbstractReportResponse.DataTypeValue.builder()
                .displayName(ReportHeading.TOTAL_SITTING_DAYS.getDisplayName())
                .dataType(ReportHeading.TOTAL_SITTING_DAYS.getDataType())
                .value(totalSittingDays)
                .build(),
            "report_created", AbstractReportResponse.DataTypeValue.builder()
                .dataType(ReportHeading.REPORT_CREATED.getDataType())
                .value(DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now()))
                .build()
        );
    }

    @Getter
    public enum ReportHeading {
        DATE_FROM("Date from", LocalDate.class.getSimpleName()),
        DATE_TO("Date to", LocalDate.class.getSimpleName()),
        TOTAL_JURORS_DAYS("Total jurors", Integer.class.getSimpleName()),
        TOTAL_SITTING_DAYS("Total sitting days", Integer.class.getSimpleName()),
        REPORT_CREATED("Report created", LocalDateTime.class.getSimpleName());

        private final String displayName;
        private final String dataType;

        ReportHeading(String displayName, String dataType) {
            this.displayName = displayName;
            this.dataType = dataType;
        }
    }
}

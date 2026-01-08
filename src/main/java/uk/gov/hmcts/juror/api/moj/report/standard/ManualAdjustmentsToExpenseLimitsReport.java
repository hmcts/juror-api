package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Report showing manual adjustments to transport expense limits across all courts.
 *
 * This report:
 * - Shows all transport limit changes (Public Transport and Taxi) in the last 12 months
 * - Uses native SQL queries via CourtLocationRepository
 * - Displays old vs new values for each changed limit
 * - Groups changes by court and transport type
 *
 * Access: Bureau Super Users only
 */
@Slf4j
@Component
public class ManualAdjustmentsToExpenseLimitsReport extends AbstractStandardReport {

    private final CourtLocationRepository courtLocationRepository;

    @Autowired
    public ManualAdjustmentsToExpenseLimitsReport(PoolRequestRepository poolRequestRepository,
                                                  CourtLocationRepository courtLocationRepository) {
        super(poolRequestRepository,
                QAppearance.appearance,
                DataType.COURT_LOCATION_NAME_AND_CODE_EP,
                DataType.TRANSPORT_TYPE,
                DataType.OLD_LIMIT,
                DataType.NEW_LIMIT,
                DataType.CHANGED_BY,
                DataType.CHANGE_DATE);
        this.courtLocationRepository = courtLocationRepository;

        // Only super users can access this report
        isSuperUserOnly();
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        // This report doesn't use QueryDSL - it overrides getStandardReportResponse instead
        // This method won't be called, but must be implemented for AbstractStandardReport
    }

    @Override
    public Class<? extends Validators.AbstractRequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    @Override
    public StandardReportResponse getStandardReportResponse(StandardReportRequest request) {
        log.info("Generating Manual Adjustments to Expense Limits Report");

        // Get recently updated court location codes (last 12 months)
        List<String> recentlyUpdatedCourts = courtLocationRepository.getRecentlyUpdatedRecordsLastYear();

        if (recentlyUpdatedCourts.isEmpty()) {
            log.info("No recently updated court location records found in the last 12 months");
            return createEmptyResponse(request);
        }

        // Get distinct court codes
        List<String> codes = recentlyUpdatedCourts.stream().distinct().toList();

        // Get audit records for these courts
        List<String> courtRevisions = courtLocationRepository.getCourtRevisionsByLocCodesLastYear(codes);

        // Parse audit records
        List<CourtLocationAuditRecord> auditRecords = parseAuditRecords(courtRevisions);

        // Build expense limit records
        List<ExpenseLimitChange> expenseLimitChanges = buildExpenseLimitChanges(auditRecords);

        // Sort by change date descending (most recent first)
        expenseLimitChanges.sort((r1, r2) -> r2.getRevisionNumber().compareTo(r1.getRevisionNumber()));

        // Build table data
        StandardTableData tableData = buildTableData(expenseLimitChanges);

        // Build table data wrapper
        AbstractReportResponse.TableData<StandardTableData> tableDataWrapper =
                AbstractReportResponse.TableData.<StandardTableData>builder()
                        .headings(getColumnHeadings())
                        .data(tableData)
                        .build();

        // Build response with headings
        Map<String, StandardReportResponse.DataTypeValue> headings = getHeadings(request, tableDataWrapper);

        return StandardReportResponse.builder()
                .headings(headings)
                .tableData(tableDataWrapper)
                .build();
    }

    /**
     * Get column headings for the table
     */
    private List<StandardReportResponse.TableData.Heading> getColumnHeadings() {
        return Arrays.asList(
                StandardReportResponse.TableData.Heading.builder()
                        .id("court_location_name_and_code")
                        .name("Court")
                        .dataType(String.class.getSimpleName())
                        .build(),
                StandardReportResponse.TableData.Heading.builder()
                        .id("transport_type")
                        .name("Transport Type")
                        .dataType(String.class.getSimpleName())
                        .build(),
                StandardReportResponse.TableData.Heading.builder()
                        .id("old_limit")
                        .name("Old Limit")
                        .dataType(String.class.getSimpleName())
                        .build(),
                StandardReportResponse.TableData.Heading.builder()
                        .id("new_limit")
                        .name("New Limit")
                        .dataType(String.class.getSimpleName())
                        .build(),
                StandardReportResponse.TableData.Heading.builder()
                        .id("changed_by")
                        .name("Changed By")
                        .dataType(String.class.getSimpleName())
                        .build(),
                StandardReportResponse.TableData.Heading.builder()
                        .id("change_date")
                        .name("Change Date")
                        .dataType(LocalDate.class.getSimpleName())
                        .build()
        );
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> getHeadings(
            StandardReportRequest request,
            AbstractReportResponse.TableData<StandardTableData> tableData) {

        Map<String, StandardReportResponse.DataTypeValue> map = new ConcurrentHashMap<>();

        // Date range: last 12 months
        LocalDate dateFrom = LocalDate.now().minusMonths(12);
        LocalDate dateTo = LocalDate.now();

        map.put("manual_adjustments_title", StandardReportResponse.DataTypeValue.builder()
                .displayName("Manual Adjustments to Expense Limits")
                .dataType(String.class.getSimpleName())
                .value("Manual Adjustments to Expense Limits")
                .build());

        map.put("date_from", StandardReportResponse.DataTypeValue.builder()
                .displayName("Date from")
                .dataType(LocalDate.class.getSimpleName())
                .value(DateTimeFormatter.ISO_DATE.format(dateFrom))
                .build());

        map.put("date_to", StandardReportResponse.DataTypeValue.builder()
                .displayName("Date to")
                .dataType(LocalDate.class.getSimpleName())
                .value(DateTimeFormatter.ISO_DATE.format(dateTo))
                .build());

        map.put("report_created", StandardReportResponse.DataTypeValue.builder()
                .dataType(LocalDate.class.getSimpleName())
                .value(DateTimeFormatter.ISO_DATE.format(LocalDate.now()))
                .build());

        map.put("report_generated", StandardReportResponse.DataTypeValue.builder()
                .displayName("Report Generated")
                .dataType(LocalDate.class.getSimpleName())
                .value(DateTimeFormatter.ISO_DATE.format(LocalDate.now()))
                .build());

        return map;
    }

    /**
     * Create empty response when no data found
     */
    private StandardReportResponse createEmptyResponse(StandardReportRequest request) {
        StandardTableData emptyData = new StandardTableData();

        AbstractReportResponse.TableData<StandardTableData> tableDataWrapper =
                AbstractReportResponse.TableData.<StandardTableData>builder()
                        .headings(getColumnHeadings())
                        .data(emptyData)
                        .build();

        Map<String, StandardReportResponse.DataTypeValue> headings = getHeadings(request, tableDataWrapper);

        return StandardReportResponse.builder()
                .headings(headings)
                .tableData(tableDataWrapper)
                .build();
    }

    /**
     * Parse raw audit record strings into structured objects
     */
    private List<CourtLocationAuditRecord> parseAuditRecords(List<String> courtRevisions) {
        List<CourtLocationAuditRecord> auditRecords = new ArrayList<>();

        for (String line : courtRevisions) {
            try {
                List<String> stats = List.of(line.split(","));

                if (stats.size() < 7) {
                    log.warn("Invalid audit record line (too few fields): {}", line);
                    continue;
                }

                String locCode = stats.get(0);
                Double publicTransportSoftLimit = parseDoubleOrNull(stats.get(1));
                Double taxiSoftLimit = parseDoubleOrNull(stats.get(2));
                String changedBy = stats.get(3);
                Long revisionNumber = parseLongOrNull(stats.get(4));
                Long revisionTimestamp = parseLongOrNull(stats.get(5));  // New field

                // Court name may contain commas, so join remaining fields (starting from index 6)
                String courtName = stats.get(6);
                if (stats.size() > 7) {
                    courtName = String.join(",", stats.subList(6, stats.size()));
                }

                if (revisionNumber == null) {
                    log.warn("Invalid revision number in line: {}", line);
                    continue;
                }

                if (revisionTimestamp == null) {
                    log.warn("Invalid revision timestamp in line: {}", line);
                    continue;
                }

                CourtLocationAuditRecord record = CourtLocationAuditRecord.builder()
                        .locCode(locCode)
                        .courtName(courtName)
                        .publicTransportSoftLimit(publicTransportSoftLimit)
                        .taxiSoftLimit(taxiSoftLimit)
                        .changedBy(changedBy)
                        .revisionNumber(revisionNumber)
                        .revisionTimestamp(revisionTimestamp)
                        .build();

                auditRecords.add(record);

            } catch (Exception e) {
                log.warn("Error parsing court location audit record line: {}", line, e);
            }
        }

        return auditRecords;
    }

    /**
     * Build expense limit changes by comparing consecutive audit records
     */
    private List<ExpenseLimitChange> buildExpenseLimitChanges(List<CourtLocationAuditRecord> auditRecords) {
        List<ExpenseLimitChange> expenseLimitChanges = new ArrayList<>();

        if (auditRecords.isEmpty()) {
            return expenseLimitChanges;
        }

        CourtLocationAuditRecord latestRecord = auditRecords.get(0);

        for (int i = 1; i < auditRecords.size(); i++) {
            CourtLocationAuditRecord previousRecord = auditRecords.get(i);

            // If we've moved to a different court, update the latest record
            if (!latestRecord.getLocCode().equals(previousRecord.getLocCode())) {
                latestRecord = previousRecord;
                continue;
            }

            // Convert revisionTimestamp (milliseconds) to LocalDate
            LocalDate changeDate = convertTimestampToLocalDate(latestRecord.getRevisionTimestamp());

            // Check for Public Transport changes
            if (latestRecord.getPublicTransportSoftLimit() != null
                    && previousRecord.getPublicTransportSoftLimit() != null) {

                double difference = Math.abs(latestRecord.getPublicTransportSoftLimit()
                        - previousRecord.getPublicTransportSoftLimit());

                if (difference > 0.001) { // Use small epsilon for floating point comparison
                    ExpenseLimitChange change = ExpenseLimitChange.builder()
                            .courtLocationNameAndCode(latestRecord.getCourtName()
                                    + " (" + latestRecord.getLocCode() + ")")
                            .transportType("Public Transport")
                            .oldLimit(previousRecord.getPublicTransportSoftLimit())
                            .newLimit(latestRecord.getPublicTransportSoftLimit())
                            .changedBy(latestRecord.getChangedBy())
                            .revisionNumber(latestRecord.getRevisionNumber())
                            .changeDate(changeDate)
                            .build();
                    expenseLimitChanges.add(change);
                }
            }

            // Check for Taxi changes
            if (latestRecord.getTaxiSoftLimit() != null
                    && previousRecord.getTaxiSoftLimit() != null) {

                double difference = Math.abs(latestRecord.getTaxiSoftLimit()
                        - previousRecord.getTaxiSoftLimit());

                if (difference > 0.001) { // Use small epsilon for floating point comparison
                    ExpenseLimitChange change = ExpenseLimitChange.builder()
                            .courtLocationNameAndCode(latestRecord.getCourtName()
                                    + " (" + latestRecord.getLocCode() + ")")
                            .transportType("Taxi")
                            .oldLimit(previousRecord.getTaxiSoftLimit())
                            .newLimit(latestRecord.getTaxiSoftLimit())
                            .changedBy(latestRecord.getChangedBy())
                            .revisionNumber(latestRecord.getRevisionNumber())
                            .changeDate(changeDate)
                            .build();
                    expenseLimitChanges.add(change);
                }
            }

            latestRecord = previousRecord;
        }

        return expenseLimitChanges;
    }

    /**
     * Convert revision timestamp (milliseconds since epoch) to LocalDate
     */
    private LocalDate convertTimestampToLocalDate(Long timestampMillis) {
        if (timestampMillis == null) {
            return LocalDate.now(); // Fallback
        }
        return java.time.Instant.ofEpochMilli(timestampMillis)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
    }

    /**
     * Build table data from expense limit changes
     */
    private StandardTableData buildTableData(List<ExpenseLimitChange> changes) {
        StandardTableData tableData = new StandardTableData();

        for (ExpenseLimitChange change : changes) {
            LinkedHashMap<String, Object> row = new LinkedHashMap<>();
            row.put("court_location_name_and_code", change.getCourtLocationNameAndCode());
            row.put("transport_type", change.getTransportType());
            row.put("old_limit", String.format("£%.2f", change.getOldLimit()));
            row.put("new_limit", String.format("£%.2f", change.getNewLimit()));
            row.put("changed_by", change.getChangedBy());
            row.put("change_date", change.getChangeDate());  // Actual date from revision_timestamp
            row.put("revision_number", change.getRevisionNumber());  // Hidden field for drill-down

            tableData.add(row);
        }

        return tableData;
    }

    // Helper methods
    private Double parseDoubleOrNull(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long parseLongOrNull(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Inner class to represent an audit record
     */
    @lombok.Builder
    @lombok.Getter
    private static class CourtLocationAuditRecord {
        private String locCode;
        private String courtName;
        private Double publicTransportSoftLimit;
        private Double taxiSoftLimit;
        private String changedBy;
        private Long revisionNumber;
        private Long revisionTimestamp;  // Milliseconds since epoch
    }

    /**
     * Inner class to represent an expense limit change
     */
    @lombok.Builder
    @lombok.Getter
    private static class ExpenseLimitChange {
        private String courtLocationNameAndCode;
        private String transportType;
        private Double oldLimit;
        private Double newLimit;
        private String changedBy;
        private Long revisionNumber;
        private LocalDate changeDate;  // Actual change date from revision_timestamp
    }

    /**
     * Validator - no parameters required for this report
     */
    public interface RequestValidator extends Validators.AbstractRequestValidator {
        // No additional validation required
    }
}

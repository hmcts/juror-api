package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.juror.domain.QCourtLocation;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.service.audit.CourtLocationAuditService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Summary report showing all manual adjustments to transport expense limits across courts
 * for the previous 12 months.
 * This report uses the CourtLocationAuditService instead of standard QueryDSL queries.
 */
@Component
public class ManualAdjustmentsToExpenseLimitsReport extends AbstractReport<StandardTableData> {

    private final CourtLocationAuditService courtLocationAuditService;

    @Autowired
    public ManualAdjustmentsToExpenseLimitsReport(CourtLocationAuditService courtLocationAuditService) {
        super(
                QCourtLocation.courtLocation,
                DataType.COURT_LOCATION_NAME_AND_CODE_EP,
                DataType.TRANSPORT_TYPE,
                DataType.OLD_LIMIT,
                DataType.NEW_LIMIT,
                DataType.CHANGED_BY,
                DataType.CHANGE_DATE
        );
        this.courtLocationAuditService = courtLocationAuditService;
       // isCourtUserOnly();
    }

    @Override
    public Class<? extends Validators.AbstractRequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    @Override
    protected StandardReportResponse createBlankResponse() {
        return new StandardReportResponse();
    }

    @Override
    protected StandardTableData getTableData(List<Tuple> data) {
        // This method is called by the parent but we override the entire flow
        // So we return empty data here and populate it in getStandardReportResponse
        return new StandardTableData();
    }

    @Override
    public StandardReportResponse getStandardReportResponse(StandardReportRequest request) {
        // Override to use audit service instead of QueryDSL
  //      authenticationConsumers.forEach(consumer -> consumer.accept(request));

     //   String locCode = SecurityUtil.getLocCode();
        LocalDateTime toDateTime = LocalDateTime.now();
        LocalDateTime fromDateTime = toDateTime.minusMonths(12);

        // Get audit records from the service
        var auditRecords = courtLocationAuditService.getAllTransportLimitAuditHistory();

        StandardTableData tableData = new StandardTableData();

        // Filter and convert audit records to report rows (only last 12 months)
        for (var record : auditRecords) {
            // Filter by date range - only include changes from last 12 months
            if (record.getChangeDateTime().isBefore(fromDateTime)) {
                continue;
            }

            // Add public transport changes
            if (record.hasPublicTransportChanged()) {
                LinkedHashMap<String, Object> row = new LinkedHashMap<>();

                // Format court name as "Court Name (LocCode)" to match COURT_LOCATION_NAME_AND_CODE_EP format
                String courtNameWithCode = String.format("%s (%s)",
                                                         record.getCourtName(),
                                                         record.getLocCode());

                row.put("court_name", courtNameWithCode);
                row.put("transport_type", "Public Transport");
                row.put("old_limit", formatLimit(record.getPublicTransportPreviousValue()));
                row.put("new_limit", formatLimit(record.getPublicTransportCurrentValue()));
                row.put("changed_by", record.getChangedBy());
                row.put("change_date", record.getChangeDateTime());

                tableData.add(row);
            }

            // Add taxi changes
            if (record.hasTaxiChanged()) {
                LinkedHashMap<String, Object> row = new LinkedHashMap<>();

              //  row.put("court_name", record.getCourtName());
                // Structure court_name as a complex object with name and locCode for hyperlinking
                // Format court name as "Court Name (LocCode)" to match COURT_LOCATION_NAME_AND_CODE_EP format
                String courtNameWithCode = String.format("%s (%s)",
                                                         record.getCourtName(),
                                                         record.getLocCode());

                row.put("court_name", courtNameWithCode);
                row.put("transport_type", "Taxi");
                row.put("old_limit", formatLimit(record.getTaxiPreviousValue()));
                row.put("new_limit", formatLimit(record.getTaxiCurrentValue()));
                row.put("changed_by", record.getChangedBy());
                row.put("change_date", record.getChangeDateTime());

                tableData.add(row);
            }
        }

        // Build the table data structure
        StandardReportResponse.TableData<StandardTableData> responseTableData =
                StandardReportResponse.TableData.<StandardTableData>builder()
                        .headings(getColumnHeadings())
                        .data(tableData)
                        .build();

        // Build the complete response
        StandardReportResponse report = new StandardReportResponse();

        Map<String, StandardReportResponse.DataTypeValue> headings =
                new ConcurrentHashMap<>(getHeadings(request, responseTableData));

        headings.put("report_created", StandardReportResponse.DataTypeValue.builder()
                .value(DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now()))
                .dataType(LocalDateTime.class.getSimpleName())
                .build());

        report.setHeadings(headings);
        report.setTableData(responseTableData);

        return report;
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        // Not used since we override getStandardReportResponse
        // But required by abstract class
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> getHeadings(
            StandardReportRequest request,
            AbstractReportResponse.TableData<StandardTableData> tableData) {

        LocalDateTime now = LocalDateTime.now();
        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusMonths(12);

        Map<String, StandardReportResponse.DataTypeValue> map = new ConcurrentHashMap<>();

        map.put("manual_adjustments_title", StandardReportResponse.DataTypeValue.builder()
                .displayName("Manual Adjustments to Expense Limits")
                .dataType(String.class.getSimpleName())
                .value("Manual Adjustments to Expense Limits")
                .build());

        map.put("date_from", StandardReportResponse.DataTypeValue.builder()
                .displayName("Date from")
                .dataType(LocalDate.class.getSimpleName())
                .value(DateTimeFormatter.ISO_DATE.format(fromDate))
                .build());

        map.put("date_to", StandardReportResponse.DataTypeValue.builder()
                .displayName("Date to")
                .dataType(LocalDate.class.getSimpleName())
                .value(DateTimeFormatter.ISO_DATE.format(toDate))
                .build());

        map.put("report_generated", StandardReportResponse.DataTypeValue.builder()
                .displayName("Report Generated")
                .dataType(LocalDateTime.class.getSimpleName())
                .value(DateTimeFormatter.ISO_DATE_TIME.format(now))
                .build());

        return map;
    }

    /**
     * Build the column headings for the table.
     */
    private List<StandardReportResponse.TableData.Heading> getColumnHeadings() {
        return Arrays.asList(
                StandardReportResponse.TableData.Heading.builder()
                        .id("court_name")
                        .name("Court")
                        .dataType(String.class.getSimpleName())
                        .build(),
                StandardReportResponse.TableData.Heading.builder()
                        .id("transport_type")
                        .name("Type")
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
                        .dataType(LocalDateTime.class.getSimpleName())
                        .build()
        );
    }

    private String formatLimit(java.math.BigDecimal value) {
        if (value == null) {
            return "Not Set";
        }
        return String.format("£%.2f", value);
    }

    /**
     * Request validator for the manual adjustments report.
     * No date range required - automatically uses previous 12 months.
     */
    public interface RequestValidator extends
            AbstractReport.Validators.AbstractRequestValidator {
        // No additional validation required for this report
    }
}

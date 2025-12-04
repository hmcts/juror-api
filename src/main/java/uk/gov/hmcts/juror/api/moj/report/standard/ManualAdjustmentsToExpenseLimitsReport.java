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
import uk.gov.hmcts.juror.api.moj.report.ReportHashMap;
import uk.gov.hmcts.juror.api.moj.report.ReportLinkedMap;
import uk.gov.hmcts.juror.api.moj.service.audit.CourtLocationAuditService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Summary report showing all manual adjustments to transport expense limits across courts.
 * This is the top-level report that provides links to detailed court-specific reports.
 */
@Component
public class ManualAdjustmentsToExpenseLimitsReport extends AbstractReport {

    private final CourtLocationAuditService courtLocationAuditService;

    @Autowired
    public ManualAdjustmentsToExpenseLimitsReport(CourtLocationAuditService courtLocationAuditService) {
        super(
            QCourtLocation.courtLocation,
            DataType.COURT_NAME,
            DataType.TRANSPORT_TYPE,
            DataType.OLD_LIMIT,
            DataType.NEW_LIMIT,
            DataType.CHANGED_BY,
            DataType.CHANGE_DATE
        );
    //    this.courtLocationAuditService = courtLocationAuditService;
    //    isCourtUserOnly();
    }

    @Override
    public Class<? extends Validators.AbstractRequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        StandardReportResponse.TableData<List<LinkedHashMap<String, Object>>> tableData) {

        Map<String, StandardReportResponse.DataTypeValue> headings = new LinkedHashMap<>();

        // Add report title
        headings.put("report_title",
                     StandardReportResponse.DataTypeValue.builder()
                         .displayName("Report Title")
                         .dataType("String")
                         .value("Manual Adjustments to Expense Limits")
                         .build());

        // Add date range or generation date if needed
        headings.put("report_generated",
                     StandardReportResponse.DataTypeValue.builder()
                         .displayName("Report Generated")
                         .dataType("LocalDateTime")
                         .value(java.time.LocalDateTime.now())
                         .build());

        return headings;
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        // Get the user's court location
        String locCode = SecurityUtil.getLocCode();

        // Filter to only show audit records for the user's court
        query.where(QCourtLocation.courtLocation.locCode.eq(locCode));
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        AbstractReportResponse.TableData<List<LinkedHashMap<String, Object>>> tableData) {

        return getHeadings(request, (StandardReportResponse.TableData<List<LinkedHashMap<String, Object>>>) tableData);
    }

    /**
     * Custom data retrieval that gets audit data from CourtLocationAuditService
     * instead of using standard QueryDSL queries.
     */
    @Override
    public StandardTableData getTableData(StandardReportRequest request) {
        String locCode = SecurityUtil.getLocCode();

        // Get audit records from the service
        var auditRecords = courtLocationAuditService.getTransportLimitAuditHistory(locCode);

        StandardTableData tableData = new StandardTableData();

        // Convert audit records to report rows
        for (var record : auditRecords) {
            // Add public transport changes
            if (record.hasPublicTransportChanged()) {
                ReportLinkedMap<String, Object> row = new ReportLinkedMap<>();

                row.put("court_name",
                        ReportHashMap.builder()
                            .add("court_name", record.getCourtName())
                            .add("loc_code", record.getLocCode())
                            .build());

                row.put("transport_type", "Public Transport");
                row.put("old_limit", formatLimit(record.getPublicTransportPreviousValue()));
                row.put("new_limit", formatLimit(record.getPublicTransportCurrentValue()));
                row.put("changed_by", record.getChangedBy());
                row.put("change_date", record.getChangeDateTime());

                tableData.add(row);
            }

            // Add taxi changes
            if (record.hasTaxiChanged()) {
                ReportLinkedMap<String, Object> row = new ReportLinkedMap<>();

                row.put("court_name",
                        ReportHashMap.builder()
                            .add("court_name", record.getCourtName())
                            .add("loc_code", record.getLocCode())
                            .build());

                row.put("transport_type", "Taxi");
                row.put("old_limit", formatLimit(record.getTaxiPreviousValue()));
                row.put("new_limit", formatLimit(record.getTaxiCurrentValue()));
                row.put("changed_by", record.getChangedBy());
                row.put("change_date", record.getChangeDateTime());

                tableData.add(row);
            }
        }

        return tableData;
    }

    private String formatLimit(java.math.BigDecimal value) {
        if (value == null) {
            return "Not Set";
        }
        return String.format("£%.2f", value);
    }

    /**
     * Request validator for the manual adjustments report.
     */
    public interface RequestValidator extends
        Validators.AbstractRequestValidator {
        // No additional validation required for this report
    }
}

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
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.service.audit.CourtLocationAuditService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class ManualAdjustmentsToExpenseLimitsReport extends AbstractStandardReport {

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
        this.courtLocationAuditService = courtLocationAuditService;
      //  isCourtUserOnly();
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        // This method won't be used since we override getTableData()
        // But we need to implement it for the abstract class
        String locCode = SecurityUtil.getLocCode();
        query.where(QCourtLocation.courtLocation.locCode.eq(locCode));
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> getHeadings(
            StandardReportRequest request,
            AbstractReportResponse.TableData<StandardTableData> tableData) {

        LocalDateTime now = LocalDateTime.now();

        Map<String, StandardReportResponse.DataTypeValue> map = new ConcurrentHashMap<>();

        map.put("manual_adjustments_title", StandardReportResponse.DataTypeValue.builder()
                .displayName("Manual Adjustments to Expense Limits")
                .dataType(String.class.getSimpleName())
                .value("Manual Adjustments to Expense Limits")
                .build());

        map.put("report_generated", StandardReportResponse.DataTypeValue.builder()
                .displayName("Report Generated")
                .dataType(LocalDateTime.class.getSimpleName())
                .value(DateTimeFormatter.ISO_DATE_TIME.format(now))
                .build());

        return map;
    }

    /**
     * Override the standard table data retrieval to use audit service instead of QueryDSL.
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
                LinkedHashMap<String, Object> row = new LinkedHashMap<>();

                row.put("court_name", record.getCourtName());
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

                row.put("court_name", record.getCourtName());
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

    @Override
    public Class<RequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    /**
     * Request validator for the manual adjustments report.
     */
    public interface RequestValidator extends
            AbstractReport.Validators.AbstractRequestValidator {
        // No additional validation required for this report
    }
}

package uk.gov.hmcts.juror.api.moj.report.standard;


import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.service.audit.CourtLocationAuditService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ExpensePaymentsUsingAdjustedLimitsReport extends AbstractStandardReport {

    private final CourtLocationRepository courtLocationRepository;
    private final CourtLocationAuditService courtLocationAuditService;

    @Autowired
    public ExpensePaymentsUsingAdjustedLimitsReport(CourtLocationRepository courtLocationRepository,
                                                  CourtLocationAuditService courtLocationAuditService ) {
        super(
            QAppearance.appearance,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.POOL_NUMBER_JP,
            DataType.TRIAL_NUMBER,
            DataType.PUBLIC_TRANSPORT_TOTAL_DUE,
            DataType.PUBLIC_TRANSPORT_PAID_OVER_OLD_LIMIT,
            DataType.TAXI_TOTAL_DUE,
            DataType.TAXI_PAID_OVER_OLD_LIMIT,
            DataType.ATTENDANCE_DATE
        );
        this.courtLocationRepository = courtLocationRepository;
        this.courtLocationAuditService = courtLocationAuditService;


    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        String locCode = SecurityUtil.getLocCode();

        // Join to get juror details
      //  query.join(QAppearance.appearance.jurorPool, QJurorPool.jurorPool);
      //  query.join(QJurorPool.jurorPool.juror, QJuror.juror);

        query.join(QJurorPool.jurorPool)
            .on(QAppearance.appearance.jurorNumber.eq(QJurorPool.jurorPool.juror.jurorNumber));
        query.join(QJuror.juror)
            .on(QJurorPool.jurorPool.juror.jurorNumber.eq(QJuror.juror.jurorNumber));

        // Filter by court location
        query.where(QAppearance.appearance.locCode.eq(locCode));

        // Filter by date range
        query.where(QAppearance.appearance.attendanceDate.between(
            request.getFromDate(), request.getToDate()));

        // Only include appearances with transport expenses
        query.where(
            QAppearance.appearance.publicTransportDue.isNotNull()
                .or(QAppearance.appearance.hiredVehicleDue.isNotNull())
        );

        // Only include processed expenses
        query.where(QAppearance.appearance.isDraftExpense.isFalse());
        query.where(QAppearance.appearance.appearanceStage.in(
            AppearanceStage.EXPENSE_ENTERED, AppearanceStage.EXPENSE_EDITED));

        // Order by attendance date descending
        query.orderBy(QAppearance.appearance.attendanceDate.desc());
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData) {

        Map<String, StandardReportResponse.DataTypeValue> map = new ConcurrentHashMap<>();

        String locCode = SecurityUtil.getLocCode();
        var courtLocation = courtLocationRepository.findById(locCode).orElse(null);

        // Add court name
        map.put("court_name", StandardReportResponse.DataTypeValue.builder()
            .displayName("Court Name")
            .dataType(String.class.getSimpleName())
            .value(courtLocation != null ? courtLocation.getName() : locCode)
            .build());

        // Add date range
        map.put("date_from", StandardReportResponse.DataTypeValue.builder()
            .displayName("Date from")
            .dataType(LocalDate.class.getSimpleName())
            .value(DateTimeFormatter.ISO_DATE.format(request.getFromDate()))
            .build());

        map.put("date_to", StandardReportResponse.DataTypeValue.builder()
            .displayName("Date to")
            .dataType(LocalDate.class.getSimpleName())
            .value(DateTimeFormatter.ISO_DATE.format(request.getToDate()))
            .build());

        // Add report creation date
        map.put("report_created", StandardReportResponse.DataTypeValue.builder()
            .displayName("Report Created")
            .dataType(LocalDate.class.getSimpleName())
            .value(DateTimeFormatter.ISO_DATE.format(LocalDate.now()))
            .build());

        return map;
    }

    @Override
    public Class<RequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    /**
     * Request validator for the detail report.
     */
    public interface RequestValidator extends
        AbstractReport.Validators.AbstractRequestValidator,
        AbstractReport.Validators.RequireFromDate,
        AbstractReport.Validators.RequireToDate {
        // Requires date range to filter juror expenses
    }
}

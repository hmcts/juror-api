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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ExpensePaymentsUsingAdjustedLimitsReport extends AbstractStandardReport {

    private final CourtLocationRepository courtLocationRepository;
    private final CourtLocationAuditService courtLocationAuditService;

    @Autowired
    public ExpensePaymentsUsingAdjustedLimitsReport
        (CourtLocationRepository courtLocationRepository,
          CourtLocationAuditService courtLocationAuditService ) {
        super(
            QAppearance.appearance,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.POOL_NUMBER_JP,
            DataType.TRIAL_NUMBER,
            DataType.PUBLIC_TRANSPORT_TOTAL_DUE,
          //  DataType.PUBLIC_TRANSPORT_PAID_OVER_OLD_LIMIT,
            DataType.TAXI_TOTAL_DUE,
         //   DataType.TAXI_PAID_OVER_OLD_LIMIT,
            DataType.ATTENDANCE_DATE
        );
        this.courtLocationRepository = courtLocationRepository;
        this.courtLocationAuditService = courtLocationAuditService;


    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        String locCode ;
        if (request.getCourts() != null && !request.getCourts().isEmpty()) {
            // locCode provided in request (from hyperlink)
            locCode = request.getCourts().get(0);
            // Validate that the user has access to this court
            checkOwnership(locCode, false);
        } else {
            // No locCode provided, use user's primary court
            locCode = SecurityUtil.getLocCode();
        }

        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusMonths(12);

        // Join to get juror details
      //  query.join(QAppearance.appearance.jurorPool, QJurorPool.jurorPool);
      //  query.join(QJurorPool.jurorPool.juror, QJuror.juror);

        var auditRecords = courtLocationAuditService.getTransportLimitAuditHistory(locCode);

        if(auditRecords.isEmpty()) {
            query.where(QAppearance.appearance.locCode.isNull());
            return;
        }
        var latestAudit = auditRecords.get(auditRecords.size() - 1);

        BigDecimal oldPublicTransportLimit = latestAudit.getPublicTransportPreviousValue();
        BigDecimal newPublicTransportLimit = latestAudit.getPublicTransportCurrentValue();
        BigDecimal oldTaxiLimit = latestAudit.getTaxiPreviousValue();
        BigDecimal newTaxiLimit = latestAudit.getTaxiCurrentValue();


        query.join(QJurorPool.jurorPool)
            .on(QAppearance.appearance.jurorNumber.eq(QJurorPool.jurorPool.juror.jurorNumber));
        query.join(QJuror.juror)
            .on(QJurorPool.jurorPool.juror.jurorNumber.eq(QJuror.juror.jurorNumber));

        // Filter by court location
        query.where(QAppearance.appearance.locCode.eq(locCode));

        // Filter by previous 12 months
        query.where(QAppearance.appearance.attendanceDate.between(fromDate, toDate));


        // Only include processed expenses
        query.where(QAppearance.appearance.isDraftExpense.isFalse());
        query.where(QAppearance.appearance.appearanceStage.in(
            AppearanceStage.EXPENSE_ENTERED, AppearanceStage.EXPENSE_EDITED));


        // Only include appearances with transport expenses
        query.where(
            QAppearance.appearance.publicTransportDue.isNotNull()
                .or(QAppearance.appearance.hiredVehicleDue.isNotNull())
        );



        // Order by attendance date descending
        query.orderBy(QAppearance.appearance.attendanceDate.desc());


        // Build the where clause to only include expenses that exceed old limit but are within new limit
        var publicTransportCondition = QAppearance.appearance.publicTransportDue.isNull();
        var taxiCondition = QAppearance.appearance.hiredVehicleDue.isNull();

        // Public transport: total > old limit AND total <= new limit
        if (oldPublicTransportLimit != null && newPublicTransportLimit != null
            && newPublicTransportLimit.compareTo(oldPublicTransportLimit) > 0) {
            publicTransportCondition = QAppearance.appearance.publicTransportDue.gt(oldPublicTransportLimit)
                .and(QAppearance.appearance.publicTransportDue.loe(newPublicTransportLimit));
        }

        // Taxi: total > old limit AND total <= new limit
        if (oldTaxiLimit != null && newTaxiLimit != null
            && newTaxiLimit.compareTo(oldTaxiLimit) > 0) {
            taxiCondition = QAppearance.appearance.hiredVehicleDue.gt(oldTaxiLimit)
                .and(QAppearance.appearance.hiredVehicleDue.loe(newTaxiLimit));
        }

        // Include appearances where either public transport OR taxi meets the criteria
        query.where(publicTransportCondition.or(taxiCondition));

        // Order by attendance date descending, then juror number
        query.orderBy(
            QAppearance.appearance.attendanceDate.desc(),
            QJurorPool.jurorPool.juror.jurorNumber.asc()
        );
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData) {

        Map<String, StandardReportResponse.DataTypeValue> map = new ConcurrentHashMap<>();

        String locCode = SecurityUtil.getLocCode();
        var courtLocation = courtLocationRepository.findById(locCode).orElse(null);

        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusMonths(12);

        var auditRecords = courtLocationAuditService.getTransportLimitAuditHistory(locCode);


        // Add court name
        map.put("court_name", StandardReportResponse.DataTypeValue.builder()
            .displayName("Court Name")
            .dataType(String.class.getSimpleName())
            .value(courtLocation != null ? courtLocation.getName() : locCode)
            .build());

        // Add transport type and limits based on which limit changed
        if (!auditRecords.isEmpty()) {
            var latestAudit = auditRecords.get(auditRecords.size() - 1);

            String transportType = "";
            if (latestAudit.hasPublicTransportChanged() && latestAudit.hasTaxiChanged()) {
                transportType = "Public Transport and Taxi";
            } else if (latestAudit.hasPublicTransportChanged()) {
                transportType = "Public Transport";
            } else if (latestAudit.hasTaxiChanged()) {
                transportType = "Taxi";
            }


            map.put("transport_type", StandardReportResponse.DataTypeValue.builder()
                .displayName("Type")
                .dataType(String.class.getSimpleName())
                .value(transportType)
                .build());

            // Add old and new limits for public transport if it changed
            if (latestAudit.hasPublicTransportChanged()) {
                map.put("old_public_transport_limit", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Old Public Transport Limit")
                    .dataType(String.class.getSimpleName())
                    .value(formatLimit(latestAudit.getPublicTransportPreviousValue()))
                    .build());

                map.put("new_public_transport_limit", StandardReportResponse.DataTypeValue.builder()
                    .displayName("New Public Transport Limit")
                    .dataType(String.class.getSimpleName())
                    .value(formatLimit(latestAudit.getPublicTransportCurrentValue()))
                    .build());
            }

            // Add old and new limits for taxi if it changed
            if (latestAudit.hasTaxiChanged()) {
                map.put("old_taxi_limit", StandardReportResponse.DataTypeValue.builder()
                    .displayName("Old Taxi Limit")
                    .dataType(String.class.getSimpleName())
                    .value(formatLimit(latestAudit.getTaxiPreviousValue()))
                    .build());

                map.put("new_taxi_limit", StandardReportResponse.DataTypeValue.builder()
                    .displayName("New Taxi Limit")
                    .dataType(String.class.getSimpleName())
                    .value(formatLimit(latestAudit.getTaxiCurrentValue()))
                    .build());
            }
        }

        // Add date range (previous 12 months)
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

        // Add report creation date
        map.put("report_created", StandardReportResponse.DataTypeValue.builder()
            .displayName("Report Created")
            .dataType(LocalDate.class.getSimpleName())
            .value(DateTimeFormatter.ISO_DATE.format(LocalDate.now()))
            .build());

        return map;
    }

    private String formatLimit(BigDecimal value) {
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
     * Request validator for the expense payments using adjusted limits report.
     * Date range is not required as it's automatically set to previous 12 months.
     */
    public interface RequestValidator extends
        AbstractReport.Validators.AbstractRequestValidator {
        // No date range required - automatically uses previous 12 months
    }
}

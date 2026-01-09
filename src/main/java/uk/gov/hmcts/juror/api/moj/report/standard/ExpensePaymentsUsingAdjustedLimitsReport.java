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
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Drill-down report showing individual jurors who received expense payments
 * using adjusted transport limits at a specific court.
 *
 * This report is accessed from ManualAdjustmentsToExpenseLimitsReport by clicking
 * on a specific limit change record.
 *
 * Parameters:
 * - revisionNumber: The court_location_revision from financial_audit_details
 * - locCode: The court location code
 * - transportType: "Public Transport" or "Taxi"
 *
 * Uses financial_audit_details table to find jurors affected by the limit change.
 */
@Slf4j
@Component
public class ExpensePaymentsUsingAdjustedLimitsReport extends AbstractStandardReport {

    private final AppearanceRepository appearanceRepository;
    private final CourtLocationRepository courtLocationRepository;

    @Autowired
    public ExpensePaymentsUsingAdjustedLimitsReport(PoolRequestRepository poolRequestRepository,
                                                    AppearanceRepository appearanceRepository,
                                                    CourtLocationRepository courtLocationRepository) {
        super(poolRequestRepository,
                QAppearance.appearance,
                DataType.JUROR_NUMBER,
                DataType.FIRST_NAME,
                DataType.LAST_NAME,
                DataType.POOL_NUMBER_BY_JP,
                DataType.TRIAL_NUMBER,
                DataType.TOTAL_PAID);

        this.appearanceRepository = appearanceRepository;
        this.courtLocationRepository = courtLocationRepository;

        // Court users can access this report for their own court
        isCourtUserOnly();
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        // This report doesn't use QueryDSL - it overrides getStandardReportResponse instead
    }

    @Override
    public Class<? extends Validators.AbstractRequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    @Override
    public StandardReportResponse getStandardReportResponse(StandardReportRequest request) {
        log.info("Generating Expense Payments Using Adjusted Limits Report");

        // Validate required parameters
        Long revisionNumber = request.getRevisionNumber();
        String locCode = request.getLocCode();
        String transportType = request.getTransportType();

        if (revisionNumber == null) {
            throw new MojException.BadRequest("Revision number is required", null);
        }
        if (locCode == null || locCode.isEmpty()) {
            throw new MojException.BadRequest("Location code is required", null);
        }
        if (transportType == null || transportType.isEmpty()) {
            throw new MojException.BadRequest("Transport type is required", null);
        }

        log.info("Parameters - Revision: {}, LocCode: {}, Transport Type: {}",
                revisionNumber, locCode, transportType);

        // Query for expense payments based on transport type
        List<String> expensePayments;
        boolean isPublicTransport = "Public Transport".equalsIgnoreCase(transportType);

        if (isPublicTransport) {
            expensePayments = appearanceRepository.findPublicTransportExpensesByRevision(revisionNumber, locCode);
        } else {
            expensePayments = appearanceRepository.findTaxiExpensesByRevision(revisionNumber, locCode);
        }

        log.info("Found {} expense payment records", expensePayments.size());

        // Parse results
        List<ExpensePaymentRecord> records = parseExpensePayments(expensePayments);

        // Build table data
        StandardTableData tableData = buildTableData(records);

        // Build table data wrapper
        AbstractReportResponse.TableData<StandardTableData> tableDataWrapper =
                AbstractReportResponse.TableData.<StandardTableData>builder()
                        .headings(getColumnHeadings())
                        .data(tableData)
                        .build();

        // Build response with headings (using override method)
        Map<String, StandardReportResponse.DataTypeValue> headings =
                getHeadings(request, tableDataWrapper);

        return StandardReportResponse.builder()
                .headings(headings)
                .tableData(tableDataWrapper)
                .build();
    }

    /**
     * Parse raw expense payment strings into structured objects
     */
    private List<ExpensePaymentRecord> parseExpensePayments(List<String> expensePayments) {
        List<ExpensePaymentRecord> records = new ArrayList<>();

        for (String line : expensePayments) {
            try {
                List<String> parts = List.of(line.split(","));

                if (parts.size() < 6) {
                    log.warn("Invalid expense payment line (too few fields): {}", line);
                    continue;
                }

                String jurorNumber = parts.get(0);
                String firstName = parts.get(1);
                String lastName = parts.get(2);
                String poolNumber = parts.get(3);
                String trialNumber = parts.get(4);
                BigDecimal totalPaid = new BigDecimal(parts.get(5));

                ExpensePaymentRecord record = ExpensePaymentRecord.builder()
                        .jurorNumber(jurorNumber)
                        .firstName(firstName)
                        .lastName(lastName)
                        .poolNumber(poolNumber)
                        .trialNumber(trialNumber)
                        .totalPaid(totalPaid)
                        .build();

                records.add(record);

            } catch (Exception e) {
                log.warn("Error parsing expense payment line: {}", line, e);
            }
        }

        return records;
    }

    /**
     * Build table data from expense payment records
     */
    private StandardTableData buildTableData(List<ExpensePaymentRecord> records) {
        StandardTableData tableData = new StandardTableData();

        for (ExpensePaymentRecord record : records) {
            LinkedHashMap<String, Object> row = new LinkedHashMap<>();
            row.put("juror_number", record.getJurorNumber());
            row.put("first_name", record.getFirstName());
            row.put("last_name", record.getLastName());
            row.put("pool_number", record.getPoolNumber());
            row.put("trial_number", record.getTrialNumber());
            row.put("total_paid", String.format("£%.2f", record.getTotalPaid()));

            tableData.add(row);
        }

        return tableData;
    }

    /**
     * Get column headings for the table
     */
    private List<StandardReportResponse.TableData.Heading> getColumnHeadings() {
        return Arrays.asList(
                StandardReportResponse.TableData.Heading.builder()
                        .id("juror_number")
                        .name("Juror Number")
                        .dataType(String.class.getSimpleName())
                        .build(),
                StandardReportResponse.TableData.Heading.builder()
                        .id("first_name")
                        .name("First Name")
                        .dataType(String.class.getSimpleName())
                        .build(),
                StandardReportResponse.TableData.Heading.builder()
                        .id("last_name")
                        .name("Last Name")
                        .dataType(String.class.getSimpleName())
                        .build(),
                StandardReportResponse.TableData.Heading.builder()
                        .id("pool_number")
                        .name("Pool Number")
                        .dataType(String.class.getSimpleName())
                        .build(),
                StandardReportResponse.TableData.Heading.builder()
                        .id("trial_number")
                        .name("Trial Number")
                        .dataType(String.class.getSimpleName())
                        .build(),
                StandardReportResponse.TableData.Heading.builder()
                        .id("total_paid")
                        .name("Total Paid")
                        .dataType(String.class.getSimpleName())
                        .build()
        );
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
            StandardReportRequest request,
            AbstractReportResponse.TableData<StandardTableData> tableData) {

        // Extract parameters from request
        String courtName = courtLocationRepository.findByLocCode(request.getLocCode())
                .map(court -> court.getName() + " (" + request.getLocCode() + ")")
                .orElse(request.getLocCode());

        String transportType = request.getTransportType();

        Map<String, AbstractReportResponse.DataTypeValue> map = new ConcurrentHashMap<>();

        map.put("report_title", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Expense Payments Using Adjusted Limits")
                .dataType(String.class.getSimpleName())
                .value("Expense Payments Using Adjusted Limits")
                .build());

        map.put("court_name", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Court")
                .dataType(String.class.getSimpleName())
                .value(courtName)
                .build());

        map.put("transport_type", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Transport Type")
                .dataType(String.class.getSimpleName())
                .value(transportType)
                .build());

        map.put("revision_number", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Revision Number")
                .dataType(Long.class.getSimpleName())
                .value(request.getRevisionNumber())
                .build());

        map.put("total_records", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Total Records")
                .dataType(Integer.class.getSimpleName())
                .value(tableData.getData().size())
                .build());

        map.put("report_generated", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Report Generated")
                .dataType(LocalDate.class.getSimpleName())
                .value(DateTimeFormatter.ISO_DATE.format(LocalDate.now()))
                .build());

        return map;
    }

    /**
     * Inner class to represent an expense payment record
     */
    @lombok.Builder
    @lombok.Getter
    private static class ExpensePaymentRecord {
        private String jurorNumber;
        private String firstName;
        private String lastName;
        private String poolNumber;
        private String trialNumber;
        private BigDecimal totalPaid;
    }

    /**
     * Validator for request parameters
     */
    public interface RequestValidator extends
            Validators.AbstractRequestValidator,
            Validators.RequireRevisionNumber,
            Validators.RequireLocCode,
            Validators.RequireTransportType {
    }
}

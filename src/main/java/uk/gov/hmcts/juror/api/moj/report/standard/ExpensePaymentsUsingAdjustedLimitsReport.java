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
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.service.audit.CourtLocationAuditService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Detail report showing individual juror expense payments for a specific court
 * where transport limits were changed.
 * Shows the PAID amounts for public transport or taxi expenses within the last 12 months.
 * Displays all expenses of the specified transport type, regardless of amount.
 * Access control is handled by the frontend application.
 *
 * Parameters:
 * - courts: Optional court location code (drill-down from summary)
 * - transport_type: Required transport type filter ("Public Transport" or "Taxi")
 */
@Slf4j
@Component
public class ExpensePaymentsUsingAdjustedLimitsReport extends AbstractReport<StandardTableData> {

    private final CourtLocationRepository courtLocationRepository;
    private final CourtLocationAuditService courtLocationAuditService;
    private final AppearanceRepository appearanceRepository;
    private final JurorRepository jurorRepository;

    @Autowired
    public ExpensePaymentsUsingAdjustedLimitsReport(
        CourtLocationRepository courtLocationRepository,
        CourtLocationAuditService courtLocationAuditService,
        AppearanceRepository appearanceRepository,
        JurorRepository jurorRepository) {
        super(
            QAppearance.appearance,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.APPEARANCE_POOL_NUMBER,
            DataType.APPEARANCE_TRIAL_NUMBER,
            DataType.ATTENDANCE_DATE,
            DataType.OLD_LIMIT,
            DataType.NEW_LIMIT
        );
        this.courtLocationRepository = courtLocationRepository;
        this.courtLocationAuditService = courtLocationAuditService;
        this.appearanceRepository = appearanceRepository;
        this.jurorRepository = jurorRepository;
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
        return new StandardTableData();
    }

    @Override
    public StandardReportResponse getStandardReportResponse(StandardReportRequest request) {
        log.info("Starting ExpensePaymentsUsingAdjustedLimitsReport");

        // Determine which court to report on
        String locCode;
        if (request.getCourts() != null && !request.getCourts().isEmpty()) {
            locCode = request.getCourts().get(0);
            log.info("Using locCode from request: {}", locCode);
        } else {
            locCode = SecurityUtil.getLocCode();
            log.info("Using locCode from security context: {}", locCode);
        }

        // Get transport type from request (passed from frontend drill-down)
        String transportType = request.getTransportType();
        if (transportType == null || transportType.isEmpty()) {
            log.error("Transport type parameter is required but not provided");
            throw new IllegalArgumentException("Transport type parameter is required");
        }
        log.info("Transport type filter: {}", transportType);

        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusMonths(12);

        // Get the audit records to determine old and new limits
        var auditRecords = courtLocationAuditService.getTransportLimitAuditHistory(locCode);

        if (auditRecords.isEmpty()) {
            log.warn("No audit records found for locCode: {}", locCode);
            return createEmptyResponse(request, locCode, null, null, transportType);
        }

        var latestAudit = auditRecords.get(auditRecords.size() - 1);
        log.info("Found {} audit records, using latest", auditRecords.size());

        // Determine which limits to use based on transport type
        BigDecimal oldLimit = null;
        BigDecimal newLimit = null;
        boolean isPublicTransport = "Public Transport".equalsIgnoreCase(transportType);
        boolean isTaxi = "Taxi".equalsIgnoreCase(transportType);

        if (isPublicTransport) {
            oldLimit = latestAudit.getPublicTransportPreviousValue();
            newLimit = latestAudit.getPublicTransportCurrentValue();
            log.info("Using Public Transport limits: old={}, new={}", oldLimit, newLimit);
        } else if (isTaxi) {
            oldLimit = latestAudit.getTaxiPreviousValue();
            newLimit = latestAudit.getTaxiCurrentValue();
            log.info("Using Taxi limits: old={}, new={}", oldLimit, newLimit);
        } else {
            log.error("Invalid transport type: {}", transportType);
            throw new IllegalArgumentException("Invalid transport type. Must be 'Public Transport' or 'Taxi'");
        }

        // Validate that a limit change occurred (new limit must be different from old)
        if (oldLimit == null || newLimit == null || newLimit.compareTo(oldLimit) == 0) {
            log.warn("No limit change found for transport type: {}", transportType);
            return createEmptyResponse(request, locCode, oldLimit, newLimit, transportType);
        }

        // Query for appearances matching the criteria
        log.info("Querying appearances for locCode: {} between {} and {}", locCode, fromDate, toDate);
        List<Appearance> filteredAppearances = appearanceRepository.findAll().stream()
            .filter(app -> locCode.equals(app.getLocCode()))
            .filter(app -> app.getAttendanceDate() != null
                && !app.getAttendanceDate().isBefore(fromDate)
                && !app.getAttendanceDate().isAfter(toDate))
            .filter(app -> !app.isDraftExpense())
            .filter(app -> app.getAppearanceStage() == AppearanceStage.EXPENSE_ENTERED
                || app.getAppearanceStage() == AppearanceStage.EXPENSE_EDITED || app.getAppearanceStage() == AppearanceStage.EXPENSE_AUTHORISED)
            .toList();

        log.info("Found {} appearances matching initial criteria", filteredAppearances.size());

        StandardTableData tableData = new StandardTableData();

        // Process each appearance based on transport type
        for (Appearance appearance : filteredAppearances) {
            log.debug("Processing appearance for juror: {}, date: {}",
                      appearance.getJurorNumber(), appearance.getAttendanceDate());

            // Get juror details from Juror table using jurorNumber
            Optional<Juror> jurorOpt = jurorRepository.findById(appearance.getJurorNumber());
            String firstName = "";
            String lastName = "";

            if (jurorOpt.isPresent()) {
                Juror juror = jurorOpt.get();
                firstName = juror.getFirstName() != null ? juror.getFirstName() : "";
                lastName = juror.getLastName() != null ? juror.getLastName() : "";
            } else {
                log.warn("Juror not found for jurorNumber: {}", appearance.getJurorNumber());
            }

            // Check based on transport type filter - show ALL expenses of this type with paid amount > 0
            if (isPublicTransport) {
                // Show public transport expenses that have a paid amount
                BigDecimal paidAmount = appearance.getPublicTransportPaid();
                log.debug("Public Transport check - juror: {}, paid: {}",
                          appearance.getJurorNumber(), paidAmount);

                if (paidAmount != null && paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                    log.info("✓ Including Public Transport for juror {}: paid=£{}",
                             appearance.getJurorNumber(), paidAmount);

                    LinkedHashMap<String, Object> row = new LinkedHashMap<>();
                    row.put("juror_number", appearance.getJurorNumber());
                    row.put("first_name", firstName);
                    row.put("last_name", lastName);
                    row.put("paid", formatAmount(paidAmount));
                    row.put("pool_number", appearance.getPoolNumber());
                    row.put("trial_number", appearance.getTrialNumber());
                    row.put("attendance_date", appearance.getAttendanceDate());

                    tableData.add(row);
                } else {
                    log.debug("Skipping Public Transport for juror {} - no paid amount",
                              appearance.getJurorNumber());
                }
            } else if (isTaxi) {
                // Show taxi expenses that have a paid amount
                BigDecimal paidAmount = appearance.getHiredVehiclePaid();
                log.debug("Taxi check - juror: {}, paid: {}",
                          appearance.getJurorNumber(), paidAmount);

                if (paidAmount != null && paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                    log.info("✓ Including Taxi for juror {}: paid=£{}",
                             appearance.getJurorNumber(), paidAmount);

                    LinkedHashMap<String, Object> row = new LinkedHashMap<>();
                    row.put("juror_number", appearance.getJurorNumber());
                    row.put("first_name", firstName);
                    row.put("last_name", lastName);
                    row.put("paid", formatAmount(paidAmount));
                    row.put("pool_number", appearance.getPoolNumber());
                    row.put("trial_number", appearance.getTrialNumber());
                    row.put("attendance_date", appearance.getAttendanceDate());

                    tableData.add(row);
                } else {
                    log.debug("Skipping Taxi for juror {} - no paid amount",
                              appearance.getJurorNumber());
                }
            }
        }

        log.info("Found {} expense payment records for {}", tableData.size(), transportType);

        // Build the table data structure
        StandardReportResponse.TableData<StandardTableData> responseTableData =
            StandardReportResponse.TableData.<StandardTableData>builder()
                .headings(getColumnHeadings())
                .data(tableData)
                .build();

        // Build the complete response
        StandardReportResponse report = new StandardReportResponse();

        Map<String, StandardReportResponse.DataTypeValue> headings =
            new ConcurrentHashMap<>(getHeadings(
                request, responseTableData, locCode, oldLimit, newLimit, transportType));

        headings.put("report_created", StandardReportResponse.DataTypeValue.builder()
            .value(DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now()))
            .dataType(LocalDateTime.class.getSimpleName())
            .build());

        report.setHeadings(headings);
        report.setTableData(responseTableData);

        return report;
    }

    private StandardReportResponse createEmptyResponse(StandardReportRequest request, String locCode,
                                                       BigDecimal oldLimit,
                                                       BigDecimal newLimit,
                                                       String transportType) {
        StandardTableData tableData = new StandardTableData();

        StandardReportResponse.TableData<StandardTableData> responseTableData =
            StandardReportResponse.TableData.<StandardTableData>builder()
                .headings(getColumnHeadings())
                .data(tableData)
                .build();

        StandardReportResponse report = new StandardReportResponse();

        Map<String, StandardReportResponse.DataTypeValue> headings =
            new ConcurrentHashMap<>(getHeadings(
                request, responseTableData, locCode, oldLimit, newLimit, transportType));

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
    }

    private Map<String, StandardReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData,
        String locCode,
        BigDecimal oldLimit,
        BigDecimal newLimit,
        String transportType) {

        Map<String, StandardReportResponse.DataTypeValue> map = new ConcurrentHashMap<>();

        var courtLocation = courtLocationRepository.findById(locCode).orElse(null);
        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusMonths(12);

        // Add court name with loc code - format: "CHESTER (415)"
        String courtNameWithCode;
        if (courtLocation != null) {
            String courtName = courtLocation.getLocCourtName() != null
                ? courtLocation.getLocCourtName()
                : (courtLocation.getName() != null ? courtLocation.getName() : locCode);
            courtNameWithCode = String.format("%s (%s)", courtName, locCode);
        } else {
            courtNameWithCode = String.format("Unknown (%s)", locCode);
        }

        map.put("court_name", StandardReportResponse.DataTypeValue.builder()
            .displayName("Court Name")
            .dataType(String.class.getSimpleName())
            .value(courtNameWithCode)
            .build());

        // Add transport type from parameter
        map.put("transport_type", StandardReportResponse.DataTypeValue.builder()
            .displayName("Type")
            .dataType(String.class.getSimpleName())
            .value(transportType != null ? transportType : "")
            .build());

        // Add old and new limits
        map.put("old_limit", StandardReportResponse.DataTypeValue.builder()
            .displayName("Old Limit")
            .dataType(String.class.getSimpleName())
            .value(formatLimit(oldLimit))
            .build());

        map.put("new_limit", StandardReportResponse.DataTypeValue.builder()
            .displayName("New Limit")
            .dataType(String.class.getSimpleName())
            .value(formatLimit(newLimit))
            .build());

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

        return map;
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData) {

        // Determine which court to report on
        String locCode;
        if (request.getCourts() != null && !request.getCourts().isEmpty()) {
            locCode = request.getCourts().get(0);
        } else {
            locCode = SecurityUtil.getLocCode();
        }

        // Get transport type from request
        String transportType = request.getTransportType();
        if (transportType == null || transportType.isEmpty()) {
            transportType = "Unknown";
        }

        var auditRecords = courtLocationAuditService.getTransportLimitAuditHistory(locCode);

        if (auditRecords.isEmpty()) {
            return getHeadings(request, tableData, locCode, null, null, transportType);
        }

        var latestAudit = auditRecords.get(auditRecords.size() - 1);

        BigDecimal oldLimit = null;
        BigDecimal newLimit = null;

        if ("Public Transport".equalsIgnoreCase(transportType)) {
            oldLimit = latestAudit.getPublicTransportPreviousValue();
            newLimit = latestAudit.getPublicTransportCurrentValue();
        } else if ("Taxi".equalsIgnoreCase(transportType)) {
            oldLimit = latestAudit.getTaxiPreviousValue();
            newLimit = latestAudit.getTaxiCurrentValue();
        }

        return getHeadings(request, tableData, locCode, oldLimit, newLimit, transportType);
    }

    /**
     * Build the column headings for the table.
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
                .id("paid")
                .name("Paid")
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
                .id("attendance_date")
                .name("Attendance Date")
                .dataType(LocalDate.class.getSimpleName())
                .build()
        );
    }

    private String formatLimit(BigDecimal value) {
        if (value == null) {
            return "Not Set";
        }
        return String.format("£%.2f", value);
    }

    private String formatAmount(BigDecimal value) {
        if (value == null) {
            return "£0.00";
        }
        return String.format("£%.2f", value);
    }

    /**
     * Request validator for the expense payments using adjusted limits report.
     * Requires transport_type parameter.
     */
    public interface RequestValidator extends
        AbstractReport.Validators.AbstractRequestValidator,
        AbstractReport.Validators.RequireTransportType {
    }
}

package uk.gov.hmcts.juror.api.moj.report.standard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.audit.dto.TransportLimitAuditRecord;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.service.audit.CourtLocationAuditService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExpensePaymentsUsingAdjustedLimitsReport Tests")
class ExpensePaymentsUsingAdjustedLimitsReportTest {

    @Mock
    private CourtLocationRepository courtLocationRepository;

    @Mock
    private CourtLocationAuditService courtLocationAuditService;

    @Mock
    private AppearanceRepository appearanceRepository;

    @Mock
    private JurorRepository jurorRepository;

    @InjectMocks
    private ExpensePaymentsUsingAdjustedLimitsReport report;

    private static final String LOC_CODE = "415";
    private static final String COURT_NAME = "CHESTER";
    private static final String JUROR_NUMBER = "123456789";
    private static final String POOL_NUMBER = "415230101";

    private CourtLocation courtLocation;
    private Juror juror;

    @BeforeEach
    void setUp() {
        courtLocation = new CourtLocation();
        courtLocation.setLocCode(LOC_CODE);
        courtLocation.setName(COURT_NAME);
        courtLocation.setLocCourtName(COURT_NAME);

        juror = new Juror();
        juror.setJurorNumber(JUROR_NUMBER);
        juror.setFirstName("John");
        juror.setLastName("Doe");
    }

    @Test
    @DisplayName("Should return report with public transport expenses")
    void positivePublicTransportExpenses() {
        // Given
        StandardReportRequest request = createValidRequest("Public Transport");

        List<TransportLimitAuditRecord> auditRecords = createAuditRecords(
            new BigDecimal("5.00"), new BigDecimal("7.50"),
            new BigDecimal("10.00"), new BigDecimal("15.00")
        );

        List<Appearance> appearances = createPublicTransportAppearances();

        when(courtLocationAuditService.getTransportLimitAuditHistory(LOC_CODE)).thenReturn(auditRecords);
        when(courtLocationRepository.findById(LOC_CODE)).thenReturn(Optional.of(courtLocation));
        when(appearanceRepository.findExpensePaymentsForReport(
            eq(LOC_CODE), any(LocalDate.class), any(LocalDate.class), anySet()))
            .thenReturn(appearances);
        when(jurorRepository.findById(JUROR_NUMBER)).thenReturn(Optional.of(juror));

        // When
        StandardReportResponse response = report.getStandardReportResponse(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTableData().getData()).isNotEmpty();

        StandardTableData data = response.getTableData().getData();
        LinkedHashMap<String, Object> firstRow = data.get(0);

        assertThat(firstRow.get("juror_number")).isEqualTo(JUROR_NUMBER);
        assertThat(firstRow.get("first_name")).isEqualTo("John");
        assertThat(firstRow.get("last_name")).isEqualTo("Doe");
        assertThat(firstRow.get("paid")).isNotNull();
        assertThat(firstRow.get("pool_number")).isEqualTo(POOL_NUMBER);
    }

    @Test
    @DisplayName("Should return report with taxi expenses")
    void positiveTaxiExpenses() {
        // Given
        StandardReportRequest request = createValidRequest("Taxi");

        List<TransportLimitAuditRecord> auditRecords = createAuditRecords(
            new BigDecimal("5.00"), new BigDecimal("7.50"),
            new BigDecimal("10.00"), new BigDecimal("15.00")
        );

        List<Appearance> appearances = createTaxiAppearances();

        when(courtLocationAuditService.getTransportLimitAuditHistory(LOC_CODE)).thenReturn(auditRecords);
        when(courtLocationRepository.findById(LOC_CODE)).thenReturn(Optional.of(courtLocation));
        when(appearanceRepository.findExpensePaymentsForReport(
            eq(LOC_CODE), any(LocalDate.class), any(LocalDate.class), anySet()))
            .thenReturn(appearances);
        when(jurorRepository.findById(JUROR_NUMBER)).thenReturn(Optional.of(juror));

        // When
        StandardReportResponse response = report.getStandardReportResponse(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTableData().getData()).isNotEmpty();

        StandardTableData data = response.getTableData().getData();
        LinkedHashMap<String, Object> firstRow = data.get(0);

        assertThat(firstRow.get("paid")).asString().startsWith("£");
    }

    @Test
    @DisplayName("Should throw exception when transport type is missing")
    void negativeNoTransportType() {
        // Given
        StandardReportRequest request = StandardReportRequest.builder()
            .reportType("ExpensePaymentsUsingAdjustedLimitsReport")
            .courts(List.of(LOC_CODE))
            .transportType(null)
            .build();

        // When/Then
        assertThatThrownBy(() -> report.getStandardReportResponse(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Transport type parameter is required");
    }

    @Test
    @DisplayName("Should throw exception for invalid transport type")
    void negativeInvalidTransportType() {
        // Given
        StandardReportRequest request = StandardReportRequest.builder()
            .reportType("ExpensePaymentsUsingAdjustedLimitsReport")
            .courts(List.of(LOC_CODE))
            .transportType("Invalid Type")
            .build();

        List<TransportLimitAuditRecord> auditRecords = createAuditRecords(
            new BigDecimal("5.00"), new BigDecimal("7.50"),
            new BigDecimal("10.00"), new BigDecimal("15.00")
        );

        when(courtLocationAuditService.getTransportLimitAuditHistory(LOC_CODE)).thenReturn(auditRecords);

        // When/Then
        assertThatThrownBy(() -> report.getStandardReportResponse(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid transport type");
    }

    @Test
    @DisplayName("Should return empty report when no audit records exist")
    void positiveNoAuditRecords() {
        // Given
        StandardReportRequest request = createValidRequest("Public Transport");

        when(courtLocationAuditService.getTransportLimitAuditHistory(LOC_CODE))
            .thenReturn(new ArrayList<>());
        when(courtLocationRepository.findById(LOC_CODE)).thenReturn(Optional.of(courtLocation));

        // When
        StandardReportResponse response = report.getStandardReportResponse(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTableData().getData()).isEmpty();
    }

    @Test
    @DisplayName("Should return empty report when no limit change occurred")
    void positiveNoLimitChange() {
        // Given
        StandardReportRequest request = createValidRequest("Public Transport");

        // Old and new limits are the same
        List<TransportLimitAuditRecord> auditRecords = createAuditRecords(
            new BigDecimal("5.00"), new BigDecimal("5.00"),  // Same values
            new BigDecimal("10.00"), new BigDecimal("10.00")  // Same values
        );

        when(courtLocationAuditService.getTransportLimitAuditHistory(LOC_CODE)).thenReturn(auditRecords);
        when(courtLocationRepository.findById(LOC_CODE)).thenReturn(Optional.of(courtLocation));

        // When
        StandardReportResponse response = report.getStandardReportResponse(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTableData().getData()).isEmpty();
    }

    @Test
    @DisplayName("Should filter out expenses with zero paid amount")
    void positiveFiltersZeroPaidAmounts() {

        final StandardReportRequest request = createValidRequest("Public Transport");

        List<TransportLimitAuditRecord> auditRecords = createAuditRecords(
            new BigDecimal("5.00"), new BigDecimal("7.50"),
            new BigDecimal("10.00"), new BigDecimal("15.00")
        );

        List<Appearance> appearances = new ArrayList<>();
        // Appearance with zero paid amount
        appearances.add(createAppearance(LocalDate.now().minusDays(5),
                                         BigDecimal.ZERO, null));
        // Appearance with positive paid amount
        appearances.add(createAppearance(LocalDate.now().minusDays(3),
                                         new BigDecimal("6.50"), null));

        when(courtLocationAuditService.getTransportLimitAuditHistory(LOC_CODE)).thenReturn(auditRecords);
        when(courtLocationRepository.findById(LOC_CODE)).thenReturn(Optional.of(courtLocation));
        when(appearanceRepository.findExpensePaymentsForReport(
            eq(LOC_CODE), any(LocalDate.class), any(LocalDate.class), anySet()))
            .thenReturn(appearances);
        when(jurorRepository.findById(JUROR_NUMBER)).thenReturn(Optional.of(juror));

        // When
        StandardReportResponse response = report.getStandardReportResponse(request);

        // Then
        StandardTableData data = response.getTableData().getData();
        // Should only include the one with positive paid amount
        assertThat(data).hasSize(1);
    }

    @Test
    @DisplayName("Should include proper headings with court name, dates, and limits")
    void positiveHeadingsContent() {
        // Given
        StandardReportRequest request = createValidRequest("Public Transport");

        List<TransportLimitAuditRecord> auditRecords = createAuditRecords(
            new BigDecimal("5.00"), new BigDecimal("7.50"),
            new BigDecimal("10.00"), new BigDecimal("15.00")
        );

        when(courtLocationAuditService.getTransportLimitAuditHistory(LOC_CODE)).thenReturn(auditRecords);
        when(courtLocationRepository.findById(LOC_CODE)).thenReturn(Optional.of(courtLocation));
        when(appearanceRepository.findExpensePaymentsForReport(
            eq(LOC_CODE), any(LocalDate.class), any(LocalDate.class), anySet()))
            .thenReturn(new ArrayList<>());

        // When
        StandardReportResponse response = report.getStandardReportResponse(request);

        // Then
        Map<String, StandardReportResponse.DataTypeValue> headings = response.getHeadings();
        assertThat(headings).containsKeys(
            "court_name", "transport_type", "old_limit", "new_limit",
            "date_from", "date_to", "report_created"
        );

        assertThat(headings.get("court_name").getValue()).asString().contains(COURT_NAME);
        assertThat(headings.get("transport_type").getValue()).isEqualTo("Public Transport");
        assertThat(headings.get("old_limit").getValue()).asString().startsWith("£");
        assertThat(headings.get("new_limit").getValue()).asString().startsWith("£");
    }

    @Test
    @DisplayName("Should handle missing juror gracefully")
    void positiveMissingJuror() {
        // Given
        StandardReportRequest request = createValidRequest("Public Transport");

        List<TransportLimitAuditRecord> auditRecords = createAuditRecords(
            new BigDecimal("5.00"), new BigDecimal("7.50"),
            new BigDecimal("10.00"), new BigDecimal("15.00")
        );

        List<Appearance> appearances = createPublicTransportAppearances();

        when(courtLocationAuditService.getTransportLimitAuditHistory(LOC_CODE)).thenReturn(auditRecords);
        when(courtLocationRepository.findById(LOC_CODE)).thenReturn(Optional.of(courtLocation));
        when(appearanceRepository.findExpensePaymentsForReport(
            eq(LOC_CODE), any(LocalDate.class), any(LocalDate.class), anySet()))
            .thenReturn(appearances);
        when(jurorRepository.findById(JUROR_NUMBER)).thenReturn(Optional.empty());

        // When
        StandardReportResponse response = report.getStandardReportResponse(request);

        // Then - Should still create row with empty names
        assertThat(response.getTableData().getData()).isNotEmpty();
        LinkedHashMap<String, Object> firstRow = response.getTableData().getData().get(0);
        assertThat(firstRow.get("first_name")).isEqualTo("");
        assertThat(firstRow.get("last_name")).isEqualTo("");
    }

    // Helper methods

    private StandardReportRequest createValidRequest(String transportType) {
        return StandardReportRequest.builder()
            .reportType("ExpensePaymentsUsingAdjustedLimitsReport")
            .courts(List.of(LOC_CODE))
            .transportType(transportType)
            .build();
    }

    private List<TransportLimitAuditRecord> createAuditRecords(
        BigDecimal publicTransportOld, BigDecimal publicTransportNew,
        BigDecimal taxiOld, BigDecimal taxiNew) {

        TransportLimitAuditRecord record = TransportLimitAuditRecord.builder()
            .locCode(LOC_CODE)
            .courtName(COURT_NAME)
            .changeDateTime(LocalDateTime.now().minusMonths(1))
            .publicTransportPreviousValue(publicTransportOld)
            .publicTransportCurrentValue(publicTransportNew)
            .taxiPreviousValue(taxiOld)
            .taxiCurrentValue(taxiNew)
            .changedBy("admin-user")
            .revisionNumber(1L)
            .build();

        return List.of(record);
    }

    private List<Appearance> createPublicTransportAppearances() {
        List<Appearance> appearances = new ArrayList<>();
        appearances.add(createAppearance(
            LocalDate.now().minusDays(10),
            new BigDecimal("6.50"),
            null
        ));
        appearances.add(createAppearance(
            LocalDate.now().minusDays(5),
            new BigDecimal("7.25"),
            null
        ));
        return appearances;
    }

    private List<Appearance> createTaxiAppearances() {
        List<Appearance> appearances = new ArrayList<>();
        appearances.add(createAppearance(
            LocalDate.now().minusDays(8),
            null,
            new BigDecimal("12.50")
        ));
        appearances.add(createAppearance(
            LocalDate.now().minusDays(3),
            null,
            new BigDecimal("14.75")
        ));
        return appearances;
    }

    private Appearance createAppearance(
        LocalDate attendanceDate,
        BigDecimal publicTransportPaid,
        BigDecimal hiredVehiclePaid) {

        Appearance appearance = new Appearance();
        appearance.setJurorNumber(JUROR_NUMBER);
        appearance.setAttendanceDate(attendanceDate);
        appearance.setPoolNumber(POOL_NUMBER);
        appearance.setTrialNumber(null);
        appearance.setLocCode(LOC_CODE);
        appearance.setPublicTransportPaid(publicTransportPaid);
        appearance.setHiredVehiclePaid(hiredVehiclePaid);
        appearance.setAppearanceStage(AppearanceStage.EXPENSE_ENTERED);
        appearance.setDraftExpense(false);

        return appearance;
    }
}

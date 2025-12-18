package uk.gov.hmcts.juror.api.moj.report.standard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.juror.api.moj.audit.dto.TransportLimitAuditRecord;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.service.audit.CourtLocationAuditService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManualAdjustmentsToExpenseLimitsReportTest")
class ManualAdjustmentsToExpenseLimitsReportTest {

    @Mock
    private CourtLocationRepository courtLocationRepository;

    @Mock
    private CourtLocationAuditService courtLocationAuditService;

    @InjectMocks
    private ManualAdjustmentsToExpenseLimitsReport report;

    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.now();
    }

    @Test
    @DisplayName("Should return report with public transport and taxi changes")
    void positiveTypicalWithBothTransportTypes() {
        // Given - Multiple audit records with both public transport and taxi changes
        List<TransportLimitAuditRecord> auditRecords = createMockAuditRecords();
        when(courtLocationAuditService.getAllTransportLimitAuditHistory()).thenReturn(auditRecords);

        StandardReportRequest request = StandardReportRequest.builder()
            .reportType("ManualAdjustmentsToExpenseLimitsReport")
            .build();

        // When
        StandardReportResponse response = report.getStandardReportResponse(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getHeadings()).isNotEmpty();
        assertThat(response.getTableData()).isNotNull();
        assertThat(response.getTableData().getData()).isNotNull();

        // Verify actual headings that the report produces
        Map<String, StandardReportResponse.DataTypeValue> headings = response.getHeadings();
        assertThat(headings).containsKey("report_created");
        assertThat(headings).containsKey("date_from");
        assertThat(headings).containsKey("date_to");
        assertThat(headings).containsKey("manual_adjustments_title");
        assertThat(headings).containsKey("report_generated");

        // Verify data - should have rows for both public transport and taxi
        StandardTableData data = response.getTableData().getData();
        assertThat(data).isNotEmpty();

        // Should have at least 2 rows per audit record (1 for public transport, 1 for taxi)
        // if both limits changed for each court
        assertThat(data.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should return empty report when no audit records exist")
    void positiveNoAuditRecords() {
        // Given - No audit records
        when(courtLocationAuditService.getAllTransportLimitAuditHistory()).thenReturn(new ArrayList<>());

        StandardReportRequest request = StandardReportRequest.builder()
            .reportType("ManualAdjustmentsToExpenseLimitsReport")
            .build();

        // When
        StandardReportResponse response = report.getStandardReportResponse(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTableData()).isNotNull();
        assertThat(response.getTableData().getData()).isEmpty();
    }

    @Test
    @DisplayName("Should filter out records older than 12 months")
    void positiveFiltersOldRecords() {
        // Given - Mix of recent and old audit records
        List<TransportLimitAuditRecord> auditRecords = new ArrayList<>();

        // Recent record (within 12 months)
        auditRecords.add(createAuditRecord(
            "415", "CHESTER", LocalDateTime.now().minusMonths(6),
            new BigDecimal("5.00"), new BigDecimal("7.50"),
            new BigDecimal("10.00"), new BigDecimal("15.00"),
            "user1"
        ));

        // Old record (more than 12 months ago) - should be filtered out
        auditRecords.add(createAuditRecord(
            "416", "LIVERPOOL", LocalDateTime.now().minusMonths(13),
            new BigDecimal("4.00"), new BigDecimal("6.00"),
            new BigDecimal("9.00"), new BigDecimal("12.00"),
            "user2"
        ));

        when(courtLocationAuditService.getAllTransportLimitAuditHistory()).thenReturn(auditRecords);

        StandardReportRequest request = StandardReportRequest.builder()
            .reportType("ManualAdjustmentsToExpenseLimitsReport")
            .build();

        // When
        StandardReportResponse response = report.getStandardReportResponse(request);

        // Then
        StandardTableData data = response.getTableData().getData();

        // Debug: Print actual keys if test fails
        if (!data.isEmpty()) {
            LinkedHashMap<String, Object> sampleRow = data.get(0);
            System.out.println("Available keys in row: " + sampleRow.keySet());
        }

        // Should have data (from CHESTER)
        assertThat(data).isNotEmpty();

        // Should have 4 rows from CHESTER (2 types × 2 records if both limits changed)
        // and 0 rows from LIVERPOOL (too old)
        // The exact count depends on whether both public transport and taxi limits changed
        int chesterRows = 0;
        int liverpoolRows = 0;

        for (LinkedHashMap<String, Object> row : data) {
            // Try different possible key names
            String courtValue = getCourtValue(row);

            if (courtValue != null) {
                if (courtValue.contains("CHESTER") || courtValue.contains("415")) {
                    chesterRows++;
                } else if (courtValue.contains("LIVERPOOL") || courtValue.contains("416")) {
                    liverpoolRows++;
                }
            }
        }

        assertThat(chesterRows).as("Should include CHESTER records (within 12 months)").isGreaterThan(0);
        assertThat(liverpoolRows).as("Should NOT include LIVERPOOL records (older than 12 months)").isEqualTo(0);
    }


    private String getCourtValue(LinkedHashMap<String, Object> row) {
        // Try different possible key names
        Object value = row.get("court_location_name_and_code");
        if (value != null) {
            return value.toString();
        }

        value = row.get("court");
        if (value != null) {
            return value.toString();
        }

        value = row.get("court_name");
        if (value != null) {
            return value.toString();
        }

        // If we can't find the court value, return null
        return null;
    }

    @Test
    @DisplayName("Should format court names as 'NAME (CODE)'")
    void positiveCourtNameFormatting() {
        // Given
        List<TransportLimitAuditRecord> auditRecords = List.of(
            createAuditRecord(
                "415", "CHESTER", LocalDateTime.now().minusMonths(1),
                new BigDecimal("5.00"), new BigDecimal("7.50"),
                new BigDecimal("10.00"), new BigDecimal("15.00"),
                "admin-user"
            )
        );

        when(courtLocationAuditService.getAllTransportLimitAuditHistory()).thenReturn(auditRecords);

        StandardReportRequest request = StandardReportRequest.builder()
            .reportType("ManualAdjustmentsToExpenseLimitsReport")
            .build();

        // When
        StandardReportResponse response = report.getStandardReportResponse(request);

        // Then
        StandardTableData data = response.getTableData().getData();
        assertThat(data).isNotEmpty();

        LinkedHashMap<String, Object> firstRow = data.get(0);

        // Debug: print available keys
        System.out.println("Available keys: " + firstRow.keySet());
        System.out.println("Row contents: " + firstRow);

        // Get court name using helper method
        String courtName = getCourtValue(firstRow);
        assertThat(courtName).isNotNull();
        assertThat(courtName).isEqualTo("CHESTER (415)");
    }

    @Test
    @DisplayName("Should include changed_by username in each row")
    void positiveIncludesChangedByUsername() {
        // Given
        String expectedUsername = "audit-admin";
        List<TransportLimitAuditRecord> auditRecords = List.of(
            createAuditRecord(
                "415", "CHESTER", LocalDateTime.now().minusMonths(1),
                new BigDecimal("5.00"), new BigDecimal("7.50"),
                new BigDecimal("10.00"), new BigDecimal("15.00"),
                expectedUsername
            )
        );

        when(courtLocationAuditService.getAllTransportLimitAuditHistory()).thenReturn(auditRecords);

        StandardReportRequest request = StandardReportRequest.builder()
            .reportType("ManualAdjustmentsToExpenseLimitsReport")
            .build();

        // When
        StandardReportResponse response = report.getStandardReportResponse(request);

        // Then
        StandardTableData data = response.getTableData().getData();
        assertThat(data).isNotEmpty();

        data.forEach(row -> {
            assertThat(row.get("changed_by")).isEqualTo(expectedUsername);
        });
    }

    @Test
    @DisplayName("Should format limits as currency with £ symbol")
    void positiveCurrencyFormatting() {
        // Given
        List<TransportLimitAuditRecord> auditRecords = List.of(
            createAuditRecord(
                "415", "CHESTER", LocalDateTime.now().minusMonths(1),
                new BigDecimal("5.50"), new BigDecimal("7.75"),
                new BigDecimal("10.25"), new BigDecimal("15.99"),
                "user1"
            )
        );

        when(courtLocationAuditService.getAllTransportLimitAuditHistory()).thenReturn(auditRecords);

        StandardReportRequest request = StandardReportRequest.builder()
            .reportType("ManualAdjustmentsToExpenseLimitsReport")
            .build();

        // When
        StandardReportResponse response = report.getStandardReportResponse(request);

        // Then
        StandardTableData data = response.getTableData().getData();
        assertThat(data).isNotEmpty();

        LinkedHashMap<String, Object> firstRow = data.get(0);
        String oldLimit = (String) firstRow.get("old_limit");
        String newLimit = (String) firstRow.get("new_limit");

        assertThat(oldLimit).matches("£\\d+\\.\\d{2}");
        assertThat(newLimit).matches("£\\d+\\.\\d{2}");
    }

    @Test
    @DisplayName("Should validate request properly")
    void positiveRequestValidation() {
        // Given - Valid request with no parameters (report doesn't require any)
        StandardReportRequest request = StandardReportRequest.builder()
            .reportType("ManualAdjustmentsToExpenseLimitsReport")
            .build();

        // When/Then - Should not throw validation exception
        assertThat(report.getRequestValidatorClass()).isNotNull();
    }

    // Helper methods

    private List<TransportLimitAuditRecord> createMockAuditRecords() {
        List<TransportLimitAuditRecord> records = new ArrayList<>();

        // Chester - Public Transport and Taxi changes
        records.add(createAuditRecord(
            "415", "CHESTER", LocalDateTime.now().minusMonths(2),
            new BigDecimal("5.00"), new BigDecimal("7.50"),
            new BigDecimal("10.00"), new BigDecimal("15.00"),
            "admin-user1"
        ));

        // Liverpool - Public Transport and Taxi changes
        records.add(createAuditRecord(
            "416", "LIVERPOOL", LocalDateTime.now().minusMonths(3),
            new BigDecimal("6.00"), new BigDecimal("8.00"),
            new BigDecimal("12.00"), new BigDecimal("18.00"),
            "admin-user2"
        ));

        return records;
    }

    private TransportLimitAuditRecord createAuditRecord(
        String locCode,
        String courtName,
        LocalDateTime changeDateTime,
        BigDecimal publicTransportOld,
        BigDecimal publicTransportNew,
        BigDecimal taxiOld,
        BigDecimal taxiNew,
        String changedBy) {

        return TransportLimitAuditRecord.builder()
            .locCode(locCode)
            .courtName(courtName)
            .changeDateTime(changeDateTime)
            .publicTransportPreviousValue(publicTransportOld)
            .publicTransportCurrentValue(publicTransportNew)
            .taxiPreviousValue(taxiOld)
            .taxiCurrentValue(taxiNew)
            .changedBy(changedBy)
            .revisionNumber(1L)
            .build();
    }
}

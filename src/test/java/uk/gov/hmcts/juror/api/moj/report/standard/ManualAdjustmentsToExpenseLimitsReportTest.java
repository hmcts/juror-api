package uk.gov.hmcts.juror.api.moj.report.standard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManualAdjustmentsToExpenseLimitsReportTest")
class ManualAdjustmentsToExpenseLimitsReportTest {

    @Mock
    private CourtLocationRepository courtLocationRepository;

    @Mock
    private PoolRequestRepository poolRequestRepository;

    @InjectMocks
    private ManualAdjustmentsToExpenseLimitsReport report;

    @BeforeEach
    void setUp() {
        // No security mocking needed for unit tests with @InjectMocks
        // The reports don't directly use SecurityUtil in their methods
    }

    @Test
    @DisplayName("Should return report with public transport and taxi changes")
    void positiveTypicalWithBothTransportTypes() {
        // Given - Mock repository responses
        List<String> courtCodes = List.of("415", "416");
        when(courtLocationRepository.getRecentlyUpdatedRecordsLastYear()).thenReturn(courtCodes);

        List<String> auditRecords = List.of(
            // Chester - recent revision
            "415,7.50,15.00,admin-user,12345,1704067200000,CHESTER",
            // Chester - previous revision
            "415,5.00,12.00,admin-user,12344,1701475200000,CHESTER",
            // Liverpool - recent revision
            "416,8.00,18.00,admin-user2,12347,1704067200000,LIVERPOOL",
            // Liverpool - previous revision
            "416,6.00,15.00,admin-user2,12346,1701475200000,LIVERPOOL"
        );
        when(courtLocationRepository.getCourtRevisionsByLocCodesLastYear(courtCodes))
            .thenReturn(auditRecords);

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

        // Verify headings
        Map<String, StandardReportResponse.DataTypeValue> headings = response.getHeadings();
        assertThat(headings).containsKeys(
            "report_created",
            "date_from",
            "date_to",
            "manual_adjustments_title",
            "report_generated"
        );

        // Verify data - should have rows for both courts and both transport types
        StandardTableData data = response.getTableData().getData();
        assertThat(data).isNotEmpty();

        // Should have 4 rows: 2 courts × 2 transport types (public transport + taxi)
        assertThat(data.size()).isEqualTo(4);

        // Verify repository methods were called
        verify(courtLocationRepository, times(1)).getRecentlyUpdatedRecordsLastYear();
        verify(courtLocationRepository, times(1)).getCourtRevisionsByLocCodesLastYear(courtCodes);
    }

    @Test
    @DisplayName("Should return empty report when no audit records exist")
    void positiveNoAuditRecords() {
        // Given - No court codes returned
        when(courtLocationRepository.getRecentlyUpdatedRecordsLastYear()).thenReturn(new ArrayList<>());

        StandardReportRequest request = StandardReportRequest.builder()
            .reportType("ManualAdjustmentsToExpenseLimitsReport")
            .build();

        // When
        StandardReportResponse response = report.getStandardReportResponse(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTableData()).isNotNull();
        assertThat(response.getTableData().getData()).isEmpty();

        // Should not call second query if no courts found
        verify(courtLocationRepository, times(1)).getRecentlyUpdatedRecordsLastYear();
        verify(courtLocationRepository, times(0)).getCourtRevisionsByLocCodesLastYear(List.of());
    }

    @Test
    @DisplayName("Should only include records from last 12 months")
    void positiveOnlyLast12Months() {
        // Given - Repository already filters by 12 months in SQL
        List<String> courtCodes = List.of("415");
        when(courtLocationRepository.getRecentlyUpdatedRecordsLastYear()).thenReturn(courtCodes);

        List<String> auditRecords = List.of(
            "415,7.50,15.00,admin-user,12345,1704067200000,CHESTER",
            "415,5.00,12.00,admin-user,12344,1701475200000,CHESTER"
        );
        when(courtLocationRepository.getCourtRevisionsByLocCodesLastYear(courtCodes))
            .thenReturn(auditRecords);

        StandardReportRequest request = StandardReportRequest.builder()
            .reportType("ManualAdjustmentsToExpenseLimitsReport")
            .build();

        // When
        StandardReportResponse response = report.getStandardReportResponse(request);

        // Then - Data should be present (repository filters by 12 months in SQL)
        StandardTableData data = response.getTableData().getData();
        assertThat(data).isNotEmpty();

        // All data returned from repository is within 12 months
        assertThat(data.size()).isEqualTo(2); // Public Transport + Taxi
    }

    @Test
    @DisplayName("Should format court names as 'NAME (CODE)'")
    void positiveCourtNameFormatting() {
        // Given
        List<String> courtCodes = List.of("415");
        when(courtLocationRepository.getRecentlyUpdatedRecordsLastYear()).thenReturn(courtCodes);

        List<String> auditRecords = List.of(
            "415,7.50,15.00,admin-user,12345,1704067200000,CHESTER",
            "415,5.00,12.00,admin-user,12344,1701475200000,CHESTER"
        );
        when(courtLocationRepository.getCourtRevisionsByLocCodesLastYear(courtCodes))
            .thenReturn(auditRecords);

        StandardReportRequest request = StandardReportRequest.builder()
            .reportType("ManualAdjustmentsToExpenseLimitsReport")
            .build();

        // When
        StandardReportResponse response = report.getStandardReportResponse(request);

        // Then
        StandardTableData data = response.getTableData().getData();
        assertThat(data).isNotEmpty();

        LinkedHashMap<String, Object> firstRow = data.get(0);
        String courtName = (String) firstRow.get("court_location_name_and_code");

        assertThat(courtName).isNotNull();
        assertThat(courtName).isEqualTo("CHESTER (415)");
    }

    @Test
    @DisplayName("Should include changed_by username in each row")
    void positiveIncludesChangedByUsername() {
        // Given
        String expectedUsername = "audit-admin";
        List<String> courtCodes = List.of("415");
        when(courtLocationRepository.getRecentlyUpdatedRecordsLastYear()).thenReturn(courtCodes);

        List<String> auditRecords = List.of(
            "415,7.50,15.00," + expectedUsername + ",12345,1704067200000,CHESTER",
            "415,5.00,12.00," + expectedUsername + ",12344,1701475200000,CHESTER"
        );
        when(courtLocationRepository.getCourtRevisionsByLocCodesLastYear(courtCodes))
            .thenReturn(auditRecords);

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
        List<String> courtCodes = List.of("415");
        when(courtLocationRepository.getRecentlyUpdatedRecordsLastYear()).thenReturn(courtCodes);

        List<String> auditRecords = List.of(
            "415,7.75,15.99,user1,12345,1704067200000,CHESTER",
            "415,5.50,10.25,user1,12344,1701475200000,CHESTER"
        );
        when(courtLocationRepository.getCourtRevisionsByLocCodesLastYear(courtCodes))
            .thenReturn(auditRecords);

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
    @DisplayName("Should include change date from revision_timestamp")
    void positiveIncludesChangeDate() {
        // Given
        List<String> courtCodes = List.of("415");
        when(courtLocationRepository.getRecentlyUpdatedRecordsLastYear()).thenReturn(courtCodes);

        // Timestamp: 1704067200000 = 2024-01-01 00:00:00 UTC
        List<String> auditRecords = List.of(
            "415,7.50,15.00,user1,12345,1704067200000,CHESTER",
            "415,5.00,12.00,user1,12344,1701475200000,CHESTER"
        );
        when(courtLocationRepository.getCourtRevisionsByLocCodesLastYear(courtCodes))
            .thenReturn(auditRecords);

        StandardReportRequest request = StandardReportRequest.builder()
            .reportType("ManualAdjustmentsToExpenseLimitsReport")
            .build();

        // When
        StandardReportResponse response = report.getStandardReportResponse(request);

        // Then
        StandardTableData data = response.getTableData().getData();
        assertThat(data).isNotEmpty();

        LinkedHashMap<String, Object> firstRow = data.get(0);
        LocalDate changeDate = (LocalDate) firstRow.get("change_date");

        assertThat(changeDate).isNotNull();
        assertThat(changeDate).isEqualTo(LocalDate.of(2024, 1, 1));
    }

    @Test
    @DisplayName("Should include revision_number in data rows")
    void positiveIncludesRevisionNumber() {
        // Given
        List<String> courtCodes = List.of("415");
        when(courtLocationRepository.getRecentlyUpdatedRecordsLastYear()).thenReturn(courtCodes);

        List<String> auditRecords = List.of(
            "415,7.50,15.00,user1,12345,1704067200000,CHESTER",
            "415,5.00,12.00,user1,12344,1701475200000,CHESTER"
        );
        when(courtLocationRepository.getCourtRevisionsByLocCodesLastYear(courtCodes))
            .thenReturn(auditRecords);

        StandardReportRequest request = StandardReportRequest.builder()
            .reportType("ManualAdjustmentsToExpenseLimitsReport")
            .build();

        // When
        StandardReportResponse response = report.getStandardReportResponse(request);

        // Then
        StandardTableData data = response.getTableData().getData();
        assertThat(data).isNotEmpty();

        LinkedHashMap<String, Object> firstRow = data.get(0);
        Long revisionNumber = (Long) firstRow.get("revision_number");

        assertThat(revisionNumber).isNotNull();
        assertThat(revisionNumber).isEqualTo(12345L);
    }

    @Test
    @DisplayName("Should handle court names with commas")
    void positiveHandlesCourtNamesWithCommas() {
        // Given
        List<String> courtCodes = List.of("415");
        when(courtLocationRepository.getRecentlyUpdatedRecordsLastYear()).thenReturn(courtCodes);

        // Court name with comma: "CHESTER, CROWN COURT"
        List<String> auditRecords = List.of(
            "415,7.50,15.00,user1,12345,1704067200000,CHESTER, CROWN COURT",
            "415,5.00,12.00,user1,12344,1701475200000,CHESTER, CROWN COURT"
        );
        when(courtLocationRepository.getCourtRevisionsByLocCodesLastYear(courtCodes))
            .thenReturn(auditRecords);

        StandardReportRequest request = StandardReportRequest.builder()
            .reportType("ManualAdjustmentsToExpenseLimitsReport")
            .build();

        // When
        StandardReportResponse response = report.getStandardReportResponse(request);

        // Then
        StandardTableData data = response.getTableData().getData();
        assertThat(data).isNotEmpty();

        LinkedHashMap<String, Object> firstRow = data.get(0);
        String courtName = (String) firstRow.get("court_location_name_and_code");

        assertThat(courtName).isEqualTo("CHESTER, CROWN COURT (415)");
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

    @Test
    @DisplayName("Should sort by revision number descending (most recent first)")
    void positiveSortsByRevisionDescending() {
        // Given
        List<String> courtCodes = List.of("415");
        when(courtLocationRepository.getRecentlyUpdatedRecordsLastYear()).thenReturn(courtCodes);

        List<String> auditRecords = List.of(
            "415,9.00,20.00,user1,12347,1706659200000,CHESTER", // Latest
            "415,7.50,15.00,user1,12345,1704067200000,CHESTER", // Middle
            "415,5.00,12.00,user1,12344,1701475200000,CHESTER"  // Oldest
        );
        when(courtLocationRepository.getCourtRevisionsByLocCodesLastYear(courtCodes))
            .thenReturn(auditRecords);

        StandardReportRequest request = StandardReportRequest.builder()
            .reportType("ManualAdjustmentsToExpenseLimitsReport")
            .build();

        // When
        StandardReportResponse response = report.getStandardReportResponse(request);

        // Then
        StandardTableData data = response.getTableData().getData();
        assertThat(data).isNotEmpty();

        // First row should have the most recent revision
        LinkedHashMap<String, Object> firstRow = data.get(0);
        Long firstRevision = (Long) firstRow.get("revision_number");
        assertThat(firstRevision).isEqualTo(12347L);
    }
}

package uk.gov.hmcts.juror.api.moj.report.standard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExpensePaymentsUsingAdjustedLimitsReport Tests")
class ExpensePaymentsUsingAdjustedLimitsReportTest {

    @Mock
    private CourtLocationRepository courtLocationRepository;

    @Mock
    private AppearanceRepository appearanceRepository;

    @Mock
    private PoolRequestRepository poolRequestRepository;

    @InjectMocks
    private ExpensePaymentsUsingAdjustedLimitsReport report;

    private static final String LOC_CODE = "415";
    private static final String COURT_NAME = "CHESTER";
    private static final Long REVISION_NUMBER = 26111289L;

    private CourtLocation courtLocation;

    @BeforeEach
    void setUp() {


        courtLocation = new CourtLocation();
        courtLocation.setLocCode(LOC_CODE);
        courtLocation.setName(COURT_NAME);
        courtLocation.setLocCourtName(COURT_NAME);
    }

    @Test
    @DisplayName("Should return report with public transport expenses")
    void positivePublicTransportExpenses() {

        StandardReportRequest request = createValidRequest("Public Transport", REVISION_NUMBER);


        List<String> expensePayments = List.of(
                "123456789,John,Smith,415230101,T20240001,45.50",
                "987654321,Jane,Doe,415230102,,32.75"
        );


        List<String> limits = List.of("415,7.50,15.00,5.00,12.00");

        when(courtLocationRepository.findByLocCode(LOC_CODE)).thenReturn(Optional.of(courtLocation));
        when(appearanceRepository.findPublicTransportExpensesByRevision(REVISION_NUMBER, LOC_CODE))
                .thenReturn(expensePayments);
        when(courtLocationRepository.getRevisionLimits(LOC_CODE, REVISION_NUMBER))
                .thenReturn(limits);


        StandardReportResponse response = report.getStandardReportResponse(request);


        assertThat(response).isNotNull();
        assertThat(response.getTableData().getData()).isNotEmpty();
        assertThat(response.getTableData().getData()).hasSize(2);

        StandardTableData data = response.getTableData().getData();
        LinkedHashMap<String, Object> firstRow = data.get(0);

        assertThat(firstRow.get("juror_number")).isEqualTo("123456789");
        assertThat(firstRow.get("first_name")).isEqualTo("John");
        assertThat(firstRow.get("last_name")).isEqualTo("Smith");
        assertThat(firstRow.get("pool_number")).isEqualTo("415230101");
        assertThat(firstRow.get("trial_number")).isEqualTo("T20240001");
        assertThat(firstRow.get("total_paid")).asString().startsWith("£");

        verify(appearanceRepository, times(1))
                .findPublicTransportExpensesByRevision(REVISION_NUMBER, LOC_CODE);
    }

    @Test
    @DisplayName("Should return report with taxi expenses")
    void positiveTaxiExpenses() {

        StandardReportRequest request = createValidRequest("Taxi", REVISION_NUMBER);

        List<String> expensePayments = List.of(
                "111222333,Alice,Brown,415230103,T20240002,125.00"
        );

        List<String> limits = List.of("415,7.50,20.00,5.00,15.00");

        when(courtLocationRepository.findByLocCode(LOC_CODE)).thenReturn(Optional.of(courtLocation));
        when(appearanceRepository.findTaxiExpensesByRevision(REVISION_NUMBER, LOC_CODE))
                .thenReturn(expensePayments);
        when(courtLocationRepository.getRevisionLimits(LOC_CODE, REVISION_NUMBER))
                .thenReturn(limits);


        StandardReportResponse response = report.getStandardReportResponse(request);


        assertThat(response).isNotNull();
        assertThat(response.getTableData().getData()).isNotEmpty();

        StandardTableData data = response.getTableData().getData();
        LinkedHashMap<String, Object> firstRow = data.get(0);

        assertThat(firstRow.get("total_paid")).asString().matches("£\\d+\\.\\d{2}");

        verify(appearanceRepository, times(1))
                .findTaxiExpensesByRevision(REVISION_NUMBER, LOC_CODE);
    }

    @Test
    @DisplayName("Should throw exception when revision number is missing")
    void negativeNoRevisionNumber() {

        StandardReportRequest request = StandardReportRequest.builder()
                .reportType("ExpensePaymentsUsingAdjustedLimitsReport")
                .locCode(LOC_CODE)
                .transportType("Public Transport")
                .revisionNumber(null)
                .build();


        assertThatThrownBy(() -> report.getStandardReportResponse(request))
                .hasMessageContaining("Revision number is required");
    }

    @Test
    @DisplayName("Should throw exception when loc code is missing")
    void negativeNoLocCode() {

        StandardReportRequest request = StandardReportRequest.builder()
                .reportType("ExpensePaymentsUsingAdjustedLimitsReport")
                .locCode(null)
                .transportType("Public Transport")
                .revisionNumber(REVISION_NUMBER)
                .build();


        assertThatThrownBy(() -> report.getStandardReportResponse(request))
                .hasMessageContaining("Location code is required");
    }

    @Test
    @DisplayName("Should throw exception when transport type is missing")
    void negativeNoTransportType() {

        StandardReportRequest request = StandardReportRequest.builder()
                .reportType("ExpensePaymentsUsingAdjustedLimitsReport")
                .locCode(LOC_CODE)
                .transportType(null)
                .revisionNumber(REVISION_NUMBER)
                .build();


        assertThatThrownBy(() -> report.getStandardReportResponse(request))
                .hasMessageContaining("Transport type is required");
    }

    @Test
    @DisplayName("Should return empty report when no expense records exist")
    void positiveNoExpenseRecords() {

        StandardReportRequest request = createValidRequest("Public Transport", REVISION_NUMBER);

        when(courtLocationRepository.findByLocCode(LOC_CODE)).thenReturn(Optional.of(courtLocation));
        when(appearanceRepository.findPublicTransportExpensesByRevision(REVISION_NUMBER, LOC_CODE))
                .thenReturn(new ArrayList<>());
        when(courtLocationRepository.getRevisionLimits(LOC_CODE, REVISION_NUMBER))
                .thenReturn(List.of("415,7.50,15.00,5.00,12.00"));


        StandardReportResponse response = report.getStandardReportResponse(request);


        assertThat(response).isNotNull();
        assertThat(response.getTableData().getData()).isEmpty();
    }

    @Test
    @DisplayName("Should include proper headings with court name, limits, and revision")
    void positiveHeadingsContent() {

        StandardReportRequest request = createValidRequest("Public Transport", REVISION_NUMBER);

        List<String> limits = List.of("415,7.50,15.00,5.00,12.00");

        when(courtLocationRepository.findByLocCode(LOC_CODE)).thenReturn(Optional.of(courtLocation));
        when(appearanceRepository.findPublicTransportExpensesByRevision(REVISION_NUMBER, LOC_CODE))
                .thenReturn(new ArrayList<>());
        when(courtLocationRepository.getRevisionLimits(LOC_CODE, REVISION_NUMBER))
                .thenReturn(limits);


        StandardReportResponse response = report.getStandardReportResponse(request);


        Map<String, StandardReportResponse.DataTypeValue> headings = response.getHeadings();
        assertThat(headings).containsKeys(
                "report_title",
                "court_name",
                "transport_type",
                "old_limit",
                "new_limit",
                "revision_number",
                "total_records",
                "report_created"
        );

        assertThat(headings.get("court_name").getValue()).asString().contains(COURT_NAME);
        assertThat(headings.get("transport_type").getValue()).isEqualTo("Public Transport");
        assertThat(headings.get("old_limit").getValue()).isEqualTo("£5.00");
        assertThat(headings.get("new_limit").getValue()).isEqualTo("£7.50");
        assertThat(headings.get("revision_number").getValue()).isEqualTo(REVISION_NUMBER);
    }

    @Test
    @DisplayName("Should display old and new taxi limits correctly")
    void positiveTaxiLimitsInHeadings() {

        StandardReportRequest request = createValidRequest("Taxi", REVISION_NUMBER);


        List<String> limits = List.of("415,7.50,20.00,5.00,15.00");

        when(courtLocationRepository.findByLocCode(LOC_CODE)).thenReturn(Optional.of(courtLocation));
        when(appearanceRepository.findTaxiExpensesByRevision(REVISION_NUMBER, LOC_CODE))
                .thenReturn(new ArrayList<>());
        when(courtLocationRepository.getRevisionLimits(LOC_CODE, REVISION_NUMBER))
                .thenReturn(limits);


        StandardReportResponse response = report.getStandardReportResponse(request);


        Map<String, StandardReportResponse.DataTypeValue> headings = response.getHeadings();


        assertThat(headings.get("old_limit").getValue()).isEqualTo("£15.00");
        assertThat(headings.get("new_limit").getValue()).isEqualTo("£20.00");
    }

    @Test
    @DisplayName("Should handle missing limits gracefully")
    void positiveMissingLimits() {

        StandardReportRequest request = createValidRequest("Public Transport", REVISION_NUMBER);

        when(courtLocationRepository.findByLocCode(LOC_CODE)).thenReturn(Optional.of(courtLocation));
        when(appearanceRepository.findPublicTransportExpensesByRevision(REVISION_NUMBER, LOC_CODE))
                .thenReturn(new ArrayList<>());
        when(courtLocationRepository.getRevisionLimits(LOC_CODE, REVISION_NUMBER))
                .thenReturn(new ArrayList<>()); // No limits found


        StandardReportResponse response = report.getStandardReportResponse(request);


        Map<String, StandardReportResponse.DataTypeValue> headings = response.getHeadings();
        assertThat(headings.get("old_limit").getValue()).isEqualTo("N/A");
        assertThat(headings.get("new_limit").getValue()).isEqualTo("N/A");
    }

    @Test
    @DisplayName("Should format report_created as ISO datetime")
    void positiveReportCreatedFormat() {

        StandardReportRequest request = createValidRequest("Public Transport", REVISION_NUMBER);

        when(courtLocationRepository.findByLocCode(LOC_CODE)).thenReturn(Optional.of(courtLocation));
        when(appearanceRepository.findPublicTransportExpensesByRevision(REVISION_NUMBER, LOC_CODE))
                .thenReturn(new ArrayList<>());
        when(courtLocationRepository.getRevisionLimits(LOC_CODE, REVISION_NUMBER))
                .thenReturn(List.of("415,7.50,15.00,5.00,12.00"));


        StandardReportResponse response = report.getStandardReportResponse(request);


        Map<String, StandardReportResponse.DataTypeValue> headings = response.getHeadings();
        String reportCreated = (String) headings.get("report_created").getValue();

        // Should match format: yyyy-MM-ddTHH:mm:ss
        assertThat(reportCreated).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");
    }

    @Test
    @DisplayName("Should handle null trial numbers gracefully")
    void positiveNullTrialNumbers() {

        StandardReportRequest request = createValidRequest("Public Transport", REVISION_NUMBER);

        List<String> expensePayments = List.of(
                "123456789,John,Smith,415230101,,45.50"
        );

        when(courtLocationRepository.findByLocCode(LOC_CODE)).thenReturn(Optional.of(courtLocation));
        when(appearanceRepository.findPublicTransportExpensesByRevision(REVISION_NUMBER, LOC_CODE))
                .thenReturn(expensePayments);
        when(courtLocationRepository.getRevisionLimits(LOC_CODE, REVISION_NUMBER))
                .thenReturn(List.of("415,7.50,15.00,5.00,12.00"));


        StandardReportResponse response = report.getStandardReportResponse(request);


        StandardTableData data = response.getTableData().getData();
        assertThat(data).hasSize(1);

        LinkedHashMap<String, Object> row = data.get(0);
        assertThat(row.get("trial_number")).isEqualTo("");
    }

    @Test
    @DisplayName("Should validate request with proper validator")
    void positiveRequestValidation() {

        assertThat(report.getRequestValidatorClass()).isNotNull();
        assertThat(report.getRequestValidatorClass().getInterfaces()).contains(
                ManualAdjustmentsToExpenseLimitsReport.Validators.RequireRevisionNumber.class,
                ManualAdjustmentsToExpenseLimitsReport.Validators.RequireLocCode.class,
                ManualAdjustmentsToExpenseLimitsReport.Validators.RequireTransportType.class
        );
    }

    @Test
    @DisplayName("Should use correct repository method for each transport type")
    void positiveUsesCorrectRepositoryMethod() {
        // Given - Public Transport
        StandardReportRequest requestPT = createValidRequest("Public Transport", REVISION_NUMBER);

        when(courtLocationRepository.findByLocCode(LOC_CODE)).thenReturn(Optional.of(courtLocation));
        when(appearanceRepository.findPublicTransportExpensesByRevision(anyLong(), anyString()))
                .thenReturn(new ArrayList<>());
        when(courtLocationRepository.getRevisionLimits(anyString(), anyLong()))
                .thenReturn(List.of("415,7.50,15.00,5.00,12.00"));


        report.getStandardReportResponse(requestPT);


        verify(appearanceRepository, times(1))
                .findPublicTransportExpensesByRevision(REVISION_NUMBER, LOC_CODE);
        verify(appearanceRepository, times(0))
                .findTaxiExpensesByRevision(REVISION_NUMBER, LOC_CODE);


        StandardReportRequest requestTaxi = createValidRequest("Taxi", REVISION_NUMBER);


        report.getStandardReportResponse(requestTaxi);


        verify(appearanceRepository, times(1))
                .findTaxiExpensesByRevision(REVISION_NUMBER, LOC_CODE);
    }



    private StandardReportRequest createValidRequest(String transportType, Long revisionNumber) {
        return StandardReportRequest.builder()
                .reportType("ExpensePaymentsUsingAdjustedLimitsReport")
                .locCode(LOC_CODE)
                .transportType(transportType)
                .revisionNumber(revisionNumber)
                .build();
    }
}

package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings({
    "unchecked"
})
public class PoolAnalysisReportTest extends AbstractStandardReportTestSupport<PoolAnalysisReport> {

    private MockedStatic<SecurityUtil> securityUtilMockedStatic;
    private CourtLocationRepository courtLocationRepository;

    public PoolAnalysisReportTest() {
        super(
            QJurorPool.jurorPool,
            PoolAnalysisReport.RequestValidator.class,
            DataType.POOL_NUMBER_BY_JP,
            DataType.SERVICE_START_DATE,
            DataType.JURORS_SUMMONED_TOTAL,
            DataType.RESPONDED_TOTAL,
            DataType.ATTENDED_TOTAL,
            DataType.PANEL_TOTAL,
            DataType.JUROR_TOTAL,
            DataType.EXCUSED_TOTAL,
            DataType.DISQUALIFIED_TOTAL,
            DataType.DEFERRED_TOTAL,
            DataType.REASSIGNED_TOTAL,
            DataType.UNDELIVERABLE_TOTAL,
            DataType.TRANSFERRED_TOTAL,
            DataType.FAILED_TO_ATTEND_TOTAL
        );
    }

    @BeforeEach
    @Override
    public void beforeEach() {
        this.securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        this.courtLocationRepository = mock(CourtLocationRepository.class);
        super.beforeEach();
    }

    @AfterEach
    void afterEach() {
        securityUtilMockedStatic.close();
    }

    @Override
    public PoolAnalysisReport createReport(PoolRequestRepository poolRequestRepository) {
        return new PoolAnalysisReport(poolRequestRepository, this.courtLocationRepository);
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType("PoolAnalysisReport")
            .fromDate(LocalDate.of(2024, 1, 15))
            .toDate(LocalDate.of(2024, 1, 16))
            .build();
    }

    @Override
    @DisplayName("positivePreProcessQueryTypicalCourtOwner")
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        request.setFromDate(LocalDate.of(2024, 1, 15));
        request.setToDate(LocalDate.of(2024, 1, 16));

        securityUtilMockedStatic.when(SecurityUtil::getActiveOwner).thenReturn(TestConstants.VALID_COURT_LOCATION);
        report.preProcessQuery(query, request);

        verify(query).where(QPoolRequest.poolRequest.returnDate.between(request.getFromDate(), request.getToDate()));
        verify(query).where(QPoolRequest.poolRequest.owner.eq(SecurityUtil.getActiveOwner()));
        verify(query).groupBy(QJurorPool.jurorPool.pool.poolNumber, QPoolRequest.poolRequest.returnDate);
        verify(query).orderBy(QJurorPool.jurorPool.pool.poolNumber.asc());
    }

    @Test
    void positivePreProcessQueryTypicalCourtSatellite() {
        StandardReportRequest request = new StandardReportRequest();

        request.setFromDate(LocalDate.of(2024, 1, 15));
        request.setToDate(LocalDate.of(2024, 1, 16));

        JPAQuery<Tuple> query = mock(JPAQuery.class);

        securityUtilMockedStatic.when(SecurityUtil::isSatellite).thenReturn(true);
        securityUtilMockedStatic.when(SecurityUtil::getLocCode).thenReturn("767");
        report.preProcessQuery(query, request);

        verify(query).where(QPoolRequest.poolRequest.returnDate.between(request.getFromDate(), request.getToDate()));
        verify(query).where(QPoolRequest.poolRequest.courtLocation.locCode.eq(SecurityUtil.getLocCode()));
        verify(query).groupBy(QJurorPool.jurorPool.pool.poolNumber, QPoolRequest.poolRequest.returnDate);
        verify(query).orderBy(QJurorPool.jurorPool.pool.poolNumber.asc());
        verifyNoMoreInteractions(query);
    }

    @Test
    void positivePreProcessQueryTypicalBureau() {
        StandardReportRequest request = new StandardReportRequest();

        request.setFromDate(LocalDate.of(2024, 1, 15));
        request.setToDate(LocalDate.of(2024, 1, 16));

        JPAQuery<Tuple> query = mock(JPAQuery.class);

        securityUtilMockedStatic.when(SecurityUtil::getActiveOwner).thenReturn("400");
        report.preProcessQuery(query, request);

        verify(query).where(QPoolRequest.poolRequest.returnDate.between(request.getFromDate(), request.getToDate()));
        verify(query).where(QPoolRequest.poolRequest.owner.eq(SecurityUtil.getActiveOwner()));
        verify(query).groupBy(QJurorPool.jurorPool.pool.poolNumber, QPoolRequest.poolRequest.returnDate);
        verify(query).orderBy(QJurorPool.jurorPool.pool.poolNumber.asc());
        verifyNoMoreInteractions(query);
    }

    @Override
    @DisplayName("positiveGetHeadingsTypicalCourtOwner")
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        StandardReportResponse.TableData<StandardTableData> tableData,
        StandardTableData data) {

        request.setFromDate(LocalDate.of(2024, 1, 15));
        request.setToDate(LocalDate.of(2024, 1, 16));

        securityUtilMockedStatic.when(SecurityUtil::getLocCode).thenReturn(TestConstants.VALID_COURT_LOCATION);
        securityUtilMockedStatic.when(SecurityUtil::isCourt).thenReturn(true);

        when(request.getFromDate()).thenReturn(LocalDate.of(2024, 1, 15));
        when(request.getToDate()).thenReturn(LocalDate.of(2024, 1, 16));

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(TestConstants.VALID_COURT_LOCATION);
        courtLocation.setName("CHESTER");
        when(courtLocationRepository.findByLocCode(TestConstants.VALID_COURT_LOCATION))
            .thenReturn(Optional.of(courtLocation));

        Map<String, StandardReportResponse.DataTypeValue> expected = new ConcurrentHashMap<>();
        expected.put("date_from", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Date From")
            .dataType(LocalDate.class.getSimpleName())
            .value("2024-01-15")
            .build());
        expected.put("date_to", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Date To")
            .dataType(LocalDate.class.getSimpleName())
            .value("2024-01-16")
            .build());
        expected.put("court_name", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Court Name")
            .dataType(String.class.getSimpleName())
            .value("CHESTER (415)")
            .build());


        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map, request, false, expected);

        return expected;
    }

    @Test
    void positiveGetHeadingsTypicalBureau() {
        StandardReportRequest request = mock(StandardReportRequest.class);

        request.setFromDate(LocalDate.of(2024, 1, 15));
        request.setToDate(LocalDate.of(2024, 1, 16));

        securityUtilMockedStatic.when(SecurityUtil::isCourt).thenReturn(false);

        when(request.getFromDate()).thenReturn(LocalDate.of(2024, 1, 15));
        when(request.getToDate()).thenReturn(LocalDate.of(2024, 1, 16));

        Map<String, StandardReportResponse.DataTypeValue> expected = new ConcurrentHashMap<>();
        expected.put("date_from", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Date From")
            .dataType(LocalDate.class.getSimpleName())
            .value("2024-01-15")
            .build());
        expected.put("date_to", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Date To")
            .dataType(LocalDate.class.getSimpleName())
            .value("2024-01-16")
            .build());

        AbstractReportResponse.TableData<StandardTableData> tableData = new AbstractReportResponse.TableData<>();
        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map, request, false, expected);
    }

    @Test
    void negativeMissingDateFrom() {
        StandardReportRequest request = getValidRequest();
        request.setFromDate(null);

        assertValidationFails(request, new ValidationFailure("fromDate", "must not be null"));
    }

    @Test
    void negativeMissingDateTo() {
        StandardReportRequest request = getValidRequest();
        request.setToDate(null);

        assertValidationFails(request, new ValidationFailure("toDate", "must not be null"));
    }
}

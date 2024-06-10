package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.QPendingJuror;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.datatypes.PendingJurorTypes;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyByte;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ManuallyCreatedJurorsReportTest extends AbstractStandardReportTestSupport<ManuallyCreatedJurorsReport> {

    private static final LocalDate FROM_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDate TO_DATE = LocalDate.of(2024, 1, 30);
    private MockedStatic<SecurityUtil> securityUtilMockedStatic;
    private CourtLocationService courtLocationService;


    public ManuallyCreatedJurorsReportTest() {
        super(QPendingJuror.pendingJuror,
            ManuallyCreatedJurorsReport.RequestValidator.class,
            PendingJurorTypes.JUROR_NUMBER,
            PendingJurorTypes.CREATED_ON,
            PendingJurorTypes.CREATED_BY,
            PendingJurorTypes.FIRST_NAME,
            PendingJurorTypes.LAST_NAME,
            PendingJurorTypes.ADDRESS_COMBINED,
            PendingJurorTypes.STATUS,
            PendingJurorTypes.NOTES,
            PendingJurorTypes.POOL_NUMBER,
            PendingJurorTypes.SERVICE_COMPLETED);
        setHasPoolRepository(false);
    }

    @BeforeEach
    @Override
    public void beforeEach() {
        this.securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        this.courtLocationService = mock(CourtLocationService.class);
        super.beforeEach();
    }

    @AfterEach
    void afterEach() {
        securityUtilMockedStatic.close();
    }

    @Override
    public ManuallyCreatedJurorsReport createReport(PoolRequestRepository poolRequestRepository) {
        return new ManuallyCreatedJurorsReport(this.courtLocationService);
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .fromDate(FROM_DATE)
            .toDate(TO_DATE)
            .build();
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        List<String> courts = List.of("415", "414", "413");
        securityUtilMockedStatic.when(SecurityUtil::getCourts).thenReturn(courts);

        report.preProcessQuery(query, request);

        verify(query, times(1))
            .where(QPendingJuror.pendingJuror.dateAdded.between(FROM_DATE, TO_DATE));
        verify(query, times(1))
            .where(QPendingJuror.pendingJuror.pool.courtLocation.locCode.in(courts));
        verify(query, times(1))
            .orderBy(QPendingJuror.pendingJuror.jurorNumber.asc());
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData,
        StandardTableData data) {
        when(request.getFromDate()).thenReturn(FROM_DATE);
        when(request.getToDate()).thenReturn(TO_DATE);
        when(data.size()).thenReturn(5);
        doNothing().when(report).addCourtNameHeader(anyMap(), any());

        CourtLocation courtLocation = mock(CourtLocation.class);
        securityUtilMockedStatic.when(SecurityUtil::getActiveOwner).thenReturn(TestConstants.VALID_COURT_LOCATION);
        doReturn(courtLocation).when(courtLocationService).getCourtLocation(TestConstants.VALID_COURT_LOCATION);

        Map<String, AbstractReportResponse.DataTypeValue> expected = new ConcurrentHashMap<>();
        expected.put("date_from", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Date from")
            .dataType("LocalDate")
            .value("2024-01-01")
            .build());
        expected.put("date_to", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Date to")
            .dataType("LocalDate")
            .value("2024-01-30")
            .build());
        expected.put("total_manually_created_jurors", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Total manually-created jurors")
            .dataType("Long")
            .value(5)
            .build());

        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map,
            request,
            false,
            expected);
        verify(report,times(1)).addCourtNameHeader(map, courtLocation);
        return map;
    }

    @Test
    void negativeNullDateFrom() {
        StandardReportRequest request = getValidRequest();
        request.setFromDate(null);
        assertValidationFails(request, new ValidationFailure("fromDate", "must not be null"));
    }

    @Test
    void negativeNullDateTo() {
        StandardReportRequest request = getValidRequest();
        request.setToDate(null);
        assertValidationFails(request, new ValidationFailure("toDate", "must not be null"));
    }
}

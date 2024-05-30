package uk.gov.hmcts.juror.api.moj.report.grouped;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.ReportGroupBy;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.juror.api.TestConstants.COURT_NAME_DISPLAY_NAME;
import static uk.gov.hmcts.juror.api.TestConstants.COURT_NAME_KEY;
import static uk.gov.hmcts.juror.api.TestConstants.DATE_FROM_DISPLAY_NAME;
import static uk.gov.hmcts.juror.api.TestConstants.DATE_FROM_KEY;
import static uk.gov.hmcts.juror.api.TestConstants.DATE_TO_DISPLAY_NAME;
import static uk.gov.hmcts.juror.api.TestConstants.DATE_TO_KEY;
import static uk.gov.hmcts.juror.api.TestConstants.VALID_COURT_LOCATION;

class CompletionOfServiceReportTest extends AbstractGroupedReportTestSupport<CompletionOfServiceReport> {

    private static final LocalDate DATE_FROM_TEST_VALUE = LocalDate.of(2023, 3, 1);
    private static final LocalDate DATE_UNTIL_TEST_VALUE = LocalDate.of(2023, 3, 2);

    private MockedStatic<SecurityUtil> securityUtilMockedStatic;
    private CourtLocationService courtLocationService;

    public CompletionOfServiceReportTest() {
        super(
            QJurorPool.jurorPool,
            CompletionOfServiceReport.RequestValidator.class,
            ReportGroupBy.builder()
                .dataType(DataType.POOL_NUMBER_AND_COURT_TYPE)
                .removeGroupByFromResponse(true)
                .build(),
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.COMPLETION_DATE
        );
    }

    @BeforeEach
    @Override
    public void beforeEach() {
        this.securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        this.courtLocationService = mock(CourtLocationService.class);
        super.beforeEach();
    }

    @AfterEach
    public void afterEach() {
        securityUtilMockedStatic.close();
    }


    @Override
    public CompletionOfServiceReport createReport(PoolRequestRepository poolRequestRepository) {
        return new CompletionOfServiceReport(poolRequestRepository, courtLocationService);
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .fromDate(DATE_FROM_TEST_VALUE)
            .toDate(DATE_UNTIL_TEST_VALUE)
            .build();
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        securityUtilMockedStatic.when(SecurityUtil::isCourt).thenReturn(true);
        securityUtilMockedStatic.when(SecurityUtil::getLocCode).thenReturn(VALID_COURT_LOCATION);
        report.preProcessQuery(query,request);

        verify(query).where(QJurorPool.jurorPool.juror.completionDate.between(request.getFromDate(),
            request.getToDate()));
        verify(query).where(QJurorPool.jurorPool.location.eq(SecurityUtil.getLocCode()));
        verify(query).orderBy(QJurorPool.jurorPool.juror.jurorNumber.asc());
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request, AbstractReportResponse.TableData<GroupedTableData> tableData,
        GroupedTableData data) {

        when(request.getFromDate()).thenReturn(DATE_FROM_TEST_VALUE);
        when(request.getToDate()).thenReturn(DATE_UNTIL_TEST_VALUE);
        when(tableData.getData().getSize()).thenReturn(3L);
        securityUtilMockedStatic.when(SecurityUtil::isCourt).thenReturn(true);
        securityUtilMockedStatic.when(SecurityUtil::getLocCode).thenReturn(VALID_COURT_LOCATION);

        CourtLocation courtLocation = mock(CourtLocation.class);
        when(courtLocationService.getCourtLocation(VALID_COURT_LOCATION)).thenReturn(courtLocation);
        doReturn(getCourtNameEntry()).when(report).getCourtNameHeader(courtLocation);

        Map<String, AbstractReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map, request, false,
            Map.of(DATE_FROM_KEY, AbstractReportResponse.DataTypeValue.builder()
                    .displayName(DATE_FROM_DISPLAY_NAME)
                    .dataType(LocalDate.class.getSimpleName())
                    .value(String.valueOf(DATE_FROM_TEST_VALUE))
                    .build(),
                DATE_TO_KEY, AbstractReportResponse.DataTypeValue.builder()
                    .displayName(DATE_TO_DISPLAY_NAME)
                    .dataType(LocalDate.class.getSimpleName())
                    .value(String.valueOf(DATE_UNTIL_TEST_VALUE))
                    .build(),
                "total_pool_members_completed", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Total pool members completed")
                    .dataType(Long.class.getSimpleName())
                    .value(3L)
                    .build(),
                COURT_NAME_KEY, AbstractReportResponse.DataTypeValue.builder()
                    .displayName(COURT_NAME_DISPLAY_NAME)
                    .dataType(String.class.getSimpleName())
                    .value("CHESTER (415)")
                    .build()
            ));

        return map;
    }

    @Test
    void negativeMissingToDate() {
        StandardReportRequest request = getValidRequest();
        request.setToDate(null);
        assertValidationFails(request, new ValidationFailure("toDate", "must not be null"));
    }

    @Test
    void negativeMissingFromDate() {
        StandardReportRequest request = getValidRequest();
        request.setFromDate(null);
        assertValidationFails(request, new ValidationFailure("fromDate", "must not be null"));
    }
}

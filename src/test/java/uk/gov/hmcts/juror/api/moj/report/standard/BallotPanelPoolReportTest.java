package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class BallotPanelPoolReportTest extends AbstractStandardReportTestSupport<BallotPanelPoolReport> {

    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    public BallotPanelPoolReportTest() {
        super(
            QJurorPool.jurorPool,
            BallotPanelPoolReport.RequestValidator.class,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.JUROR_POSTCODE
        );
        setHasPoolRepository(false);
    }

    @Override
    public BallotPanelPoolReport createReport(PoolRequestRepository poolRequestRepository) {
        return new BallotPanelPoolReport();
    }

    @BeforeEach
    @Override
    public void beforeEach() {
        super.beforeEach();
        securityUtilMockedStatic = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    public void afterEach() {
        TestUtils.afterAll();
        securityUtilMockedStatic.close();
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .poolNumber(TestConstants.VALID_POOL_NUMBER)
            .build();
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {

        request.setPoolNumber(TestConstants.VALID_POOL_NUMBER);
        securityUtilMockedStatic.when(SecurityUtil::getCourts).thenReturn(List.of(TestConstants.VALID_COURT_LOCATION));
        report.preProcessQuery(query, request);

        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.poolNumber.eq(request.getPoolNumber()));
        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.courtLocation.locCode.in(SecurityUtil.getCourts()));
        verify(query, times(1))
            .where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.RESPONDED));
        verify(query, times(1))
            .orderBy(QJurorPool.jurorPool.juror.lastName.asc(), QJurorPool.jurorPool.juror.jurorNumber.asc());
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData,
        StandardTableData data) {

        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map, request, false, Map.of());
        return map;
    }

    @Test
    void negativeMissingPoolNumber() {
        StandardReportRequest request = getValidRequest();
        request.setPoolNumber(null);
        assertValidationFails(request, new ValidationFailure("poolNumber", "must not be null"));
    }
}

package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.TestConstants;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UndeliverableListReportTest extends AbstractStandardReportTestSupport<UndeliverableListReport> {
    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    public UndeliverableListReportTest() {
        super(QJurorPool.jurorPool,
            UndeliverableListReport.RequestValidator.class,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.JUROR_POSTAL_ADDRESS);
    }

    @BeforeEach
    @Override
    public void beforeEach() {
        super.beforeEach();
        securityUtilMockedStatic = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void afterEach() {
        securityUtilMockedStatic.close();
    }

    // common tests
    @Override
    public UndeliverableListReport createReport(PoolRequestRepository poolRequestRepository) {
        return new UndeliverableListReport(poolRequestRepository);
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
        report.preProcessQuery(query, request);

        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.poolNumber.eq(TestConstants.VALID_POOL_NUMBER));
        verify(query, times(1))
            .where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.UNDELIVERABLE));
        verify(query, times(1))
            .orderBy(QJurorPool.jurorPool.juror.jurorNumber.asc());
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData,
        StandardTableData data) {

        when(data.size()).thenReturn(5);

        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map,
            request,
            true,
            Map.of(
                "total_undelivered",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total undelivered")
                    .dataType(Long.class.getSimpleName())
                    .value(5)
                    .build()
            ));

        verify(tableData, times(1)).getData();
        verify(data, times(1)).size();
        return map;
    }

    // custom tests
    @Test
    void negativeMissingPoolNumber() {
        StandardReportRequest request = getValidRequest();
        request.setPoolNumber(null);
        assertValidationFails(request, new ValidationFailure("poolNumber", "must not be null"));
    }

    @Test
    void negativeInvalidPoolNumber() {
        StandardReportRequest request = getValidRequest();
        request.setPoolNumber(TestConstants.INVALID_POOL_NUMBER);
        assertValidationFails(request, new ValidationFailure("poolNumber", "must match \"^\\d{9}$\""));
    }
}
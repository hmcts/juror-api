package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AvailableListByPoolReportTest extends AbstractStandardReportTestSupport<AvailableListByPoolReport> {

    public AvailableListByPoolReportTest() {
        super(QJurorPool.jurorPool,
            AvailableListByPoolReport.RequestValidator.class,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.STATUS,
            DataType.JUROR_REASONABLE_ADJUSTMENT_WITH_MESSAGE,
            DataType.ON_CALL);
    }

    @Override
    public AvailableListByPoolReport createReport(PoolRequestRepository poolRequestRepository) {
        return new AvailableListByPoolReport(poolRequestRepository);
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .poolNumber(TestConstants.VALID_POOL_NUMBER)
            .includeJurorsOnCall(false)
            .includePanelMembers(false)
            .respondedJurorsOnly(false)
            .build();
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        doNothing().when(report).addStandardFilters(any(), any());
        report.preProcessQuery(query, request);
        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.poolNumber.eq(request.getPoolNumber()));
        verify(query, times(1))
            .orderBy(QJurorPool.jurorPool.pool.poolNumber.asc(),
                QJurorPool.jurorPool.juror.jurorNumber.asc());
        verify(report, times(1))
            .addStandardFilters(query, request);
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData,
        StandardTableData data) {

        StandardTableData standardTableData = mock(StandardTableData.class);
        when(standardTableData.getSize()).thenReturn(5L);
        when(tableData.getData()).thenReturn(standardTableData);

        Map<String, AbstractReportResponse.DataTypeValue> headings = report.getHeadings(request, tableData);
        assertHeadingContains(headings,
            request,
            true,
            Map.of(
                "total_available_pool_members",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total available pool members")
                    .dataType(Long.class.getSimpleName())
                    .value(5L)
                    .build()
            ));
        return headings;
    }

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

    @Test
    void negativeMissingIncludePanelMembers() {
        StandardReportRequest request = getValidRequest();
        request.setIncludePanelMembers(null);
        assertValidationFails(request, new ValidationFailure("includePanelMembers", "must not be null"));
    }

    @Test
    void negativeMissingIncludeJurorsOnCall() {
        StandardReportRequest request = getValidRequest();
        request.setIncludeJurorsOnCall(null);
        assertValidationFails(request, new ValidationFailure("includeJurorsOnCall", "must not be null"));
    }

    @Test
    void negativeMissingRespondedJurorsOnly() {
        StandardReportRequest request = getValidRequest();
        request.setRespondedJurorsOnly(null);
        assertValidationFails(request, new ValidationFailure("respondedJurorsOnly", "must not be null"));
    }
}

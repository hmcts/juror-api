package uk.gov.hmcts.juror.api.moj.report.grouped;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.IDataType;
import uk.gov.hmcts.juror.api.moj.report.grouped.groupby.GroupByPoolStatusAndGraphReport;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PoolStatusAndGraphReportTest extends AbstractGroupedReportTestSupport<PoolStatusAndGraphReport> {

    public PoolStatusAndGraphReportTest() {
        super(QJurorPool.jurorPool,
            PoolStatusAndGraphReport.RequestValidator.class,
            new GroupByPoolStatusAndGraphReport(),
            DataType.IS_ACTIVE,
            DataType.STATUS,
            DataType.JUROR_POOL_COUNT);
    }

    @Override
    public PoolStatusAndGraphReport createReport(PoolRequestRepository poolRequestRepository) {
        return new PoolStatusAndGraphReport(poolRequestRepository);
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
        doNothing().when(report).addGroupBy(any(), any(IDataType[].class));

        report.preProcessQuery(query, request);

        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.poolNumber.eq(request.getPoolNumber()));
        verify(report, times(1)).addGroupBy(
            query,
            DataType.IS_ACTIVE,
            DataType.STATUS
        );
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<GroupedTableData> tableData,
        GroupedTableData data) {

        doReturn(2L).when(report).calculateTotal(any(), eq("Active pool members"));
        doReturn(3L).when(report).calculateTotal(any(), eq("Inactive pool members"));
        when(request.getPoolNumber()).thenReturn(TestConstants.VALID_POOL_NUMBER);

        PoolRequest poolRequest = mock(PoolRequest.class);
        when(poolRequest.getPoolNumber()).thenReturn(TestConstants.VALID_POOL_NUMBER);
        when(poolRequest.getNumberRequested()).thenReturn(20);
        doReturn(poolRequest).when(report).getPoolRequest(TestConstants.VALID_POOL_NUMBER);

        Map<String, AbstractReportResponse.DataTypeValue> headings = report.getHeadings(request, tableData);

        assertHeadingContains(headings,
            request,
            false,
            Map.of(
                "pool_number",
                AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Pool number")
                    .dataType(String.class.getSimpleName())
                    .value(TestConstants.VALID_POOL_NUMBER)
                    .build(),
                "total_pool_members",
                AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Total pool members")
                    .dataType(Long.class.getSimpleName())
                    .value(5L)
                    .build(),
                "originally_requested_by_court",
                AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Originally requested by court")
                    .dataType(Long.class.getSimpleName())
                    .value(20)
                    .build()
            ));

        verify(report, times(1))
            .calculateTotal(tableData, "Active pool members");
        verify(report, times(1))
            .calculateTotal(tableData, "Inactive pool members");
        verify(report, times(1))
            .getPoolRequest(TestConstants.VALID_POOL_NUMBER);
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
}

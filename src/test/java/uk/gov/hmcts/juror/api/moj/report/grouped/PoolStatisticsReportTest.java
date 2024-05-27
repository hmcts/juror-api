package uk.gov.hmcts.juror.api.moj.report.grouped;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.IDataType;
import uk.gov.hmcts.juror.api.moj.report.ReportGroupBy;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.time.LocalDate;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class PoolStatisticsReportTest extends AbstractGroupedReportTestSupport<PoolStatisticsReport> {

    private static final LocalDate FROM_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDate TO_DATE = LocalDate.of(2024, 1, 30);

    public PoolStatisticsReportTest() {
        super(QJurorPool.jurorPool,
            PoolStatisticsReport.RequestValidator.class,
            ReportGroupBy.builder()
                .dataType(DataType.POOL_RETURN_DATE_BY_JP)
                .removeGroupByFromResponse(true)
                .build(),
            DataType.STATUS,
            DataType.JUROR_POOL_COUNT);
        setHasPoolRepository(false);
    }

    @Override
    public PoolStatisticsReport createReport(PoolRequestRepository poolRequestRepository) {
        return new PoolStatisticsReport();
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
        doNothing().when(report).addGroupBy(any(), any(IDataType[].class));
        report.preProcessQuery(query, request);
        verify(query)
            .where(QJurorPool.jurorPool.pool.returnDate.between(FROM_DATE, TO_DATE));

        verify(query).orderBy(QJurorPool.jurorPool.status.statusDesc.asc());

        verify(report, times(1)).addGroupBy(query,
            DataType.POOL_RETURN_DATE_BY_JP,
            DataType.STATUS
        );
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<GroupedTableData> tableData,
        GroupedTableData data) {

        Map<String, AbstractReportResponse.DataTypeValue> headings = report.getHeadings(request, tableData);
        assertHeadingContains(headings,
            request,
            false,
            Map.of()
        );
        return Map.of();
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

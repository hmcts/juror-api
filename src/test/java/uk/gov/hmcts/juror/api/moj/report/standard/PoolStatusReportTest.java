package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PoolStatusReportTest extends AbstractStandardReportTestSupport<PoolStatusReport> {


    private final JurorPoolRepository jurorPoolRepository;

    public PoolStatusReportTest() {
        super(QJurorPool.jurorPool,
            PoolStatusReport.RequestValidator.class,
            DataType.SUMMONS_TOTAL,
            DataType.RESPONDED_TOTAL,
            DataType.PANEL_TOTAL,
            DataType.JUROR_TOTAL,
            DataType.EXCUSED_TOTAL,
            DataType.DISQUALIFIED_TOTAL,
            DataType.DEFERRED_TOTAL,
            DataType.REASSIGNED_TOTAL,
            DataType.UNDELIVERABLE_TOTAL,
            DataType.TRANSFERRED_TOTAL);
        this.jurorPoolRepository = mock(JurorPoolRepository.class);
    }

    @Override
    public PoolStatusReport createReport(PoolRequestRepository poolRequestRepository) {
        return new PoolStatusReport(poolRequestRepository, this.jurorPoolRepository);
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .poolNumber("415000001")
            .build();
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        report.preProcessQuery(query, request);
        verify(query).where(QJurorPool.jurorPool.pool.poolNumber.eq(request.getPoolNumber()));
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData,
        StandardTableData data) {
        final String poolNumber = "41500001";
        PoolRequest poolRequest = createPoolRequest(poolNumber);
        doReturn("41500001").when(request).getPoolNumber();
        when(report.getPoolRequestRepository().findByPoolNumber(poolNumber)).thenReturn(Optional.of(poolRequest));

        when(this.jurorPoolRepository.count(QJurorPool.jurorPool.pool.poolNumber.eq("41500001")))
            .thenReturn(2L);

        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map, request, false, Map.of(
            "pool_number", AbstractReportResponse.DataTypeValue.builder()
                .value(request.getPoolNumber())
                .dataType("String")
                .displayName("Pool number")
                .build(),
            "total_pool_members", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Total pool members")
                .dataType("Long")
                .value(2L)
                .build(),
            "total_requested_by_court", AbstractReportResponse.DataTypeValue.builder()
                .dataType("Long")
                .value(20)
                .displayName("Originally requested by court")
                .build()
        ));
        return map;
    }

    private PoolRequest createPoolRequest(String poolNumber) {
        return PoolRequest.builder()
            .poolNumber(poolNumber)
            .totalNoRequired(20)
            .dateCreated(LocalDateTime.now())
            .owner("415")
            .build();
    }
}

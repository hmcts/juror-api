package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.mockito.Mock;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.LawOfDemeter")
public class PoolStatusReportTest extends AbstractStandardReportTestSupport<PoolStatusReport> {

    @Mock
    private JurorPoolRepository jurorPoolRepository;

    public PoolStatusReportTest() {
        super(QJurorPool.jurorPool,
            PoolStatusReport.Validators.class,
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
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(StandardReportRequest request,
                                        AbstractReportResponse.TableData<List<LinkedHashMap<String, Object>>> tableData,
                                        List<LinkedHashMap<String,
                                            Object>> data) {
        PoolRequestRepository repository = mock(PoolRequestRepository.class);
        PoolRequest poolRequest = createPoolRequest();
        when(repository.findByPoolNumber(request.getPoolNumber())).thenReturn(Optional.of(poolRequest));
        doReturn("41500001").when(request).getPoolNumber();

        Map<String, AbstractReportResponse.DataTypeValue> map = new ConcurrentHashMap<>();
        map.put("pool_number", AbstractReportResponse.DataTypeValue.builder()
            .value(request.getPoolNumber())
            .dataType("String")
            .displayName("Pool number")
            .build());
        map.put("total_pool_members", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Total pool members")
            .dataType("Long")
            .value(0)
            .build());
        map.put("total_requested_by_court", AbstractReportResponse.DataTypeValue.builder()
            .dataType("Long")
            .value(poolRequest.getTotalNoRequired())
            .displayName("Originally requested by court")
            .build());

        return map;
    }

    private PoolRequest createPoolRequest() {
        return PoolRequest.builder()
            .poolNumber("415000001")
            .totalNoRequired(20)
            .dateCreated(LocalDateTime.now())
            .owner("415")
            .build();
    }
}

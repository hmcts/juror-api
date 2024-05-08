package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.report.AbstractReport;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@SuppressWarnings("PMD.LawOfDemeter")
public class PoolStatusReport extends AbstractStandardReport {
    private final JurorPoolRepository jurorPoolRepository;

    @Autowired
    public PoolStatusReport(PoolRequestRepository poolRequestRepository, JurorPoolRepository jurorPoolRepository) {
        super(poolRequestRepository,
            QJurorPool.jurorPool,
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

        this.jurorPoolRepository = jurorPoolRepository;
        isBureauUserOnly();
    }

    @Override
    public Class<? extends Validators.AbstractRequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QJurorPool.jurorPool.pool.poolNumber.eq(request.getPoolNumber()));
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(StandardReportRequest request,
                                 AbstractReportResponse.TableData<List<LinkedHashMap<String, Object>>> tableData) {

        PoolRequest poolRequest =
            getPoolRequestRepository().findByPoolNumber(request.getPoolNumber()).orElseThrow(() ->
                new MojException.NotFound("Cannot find pool number " + request.getPoolNumber(),
                    null));

        Map<String, AbstractReportResponse.DataTypeValue> map = new ConcurrentHashMap<>();
        map.put("pool_number", AbstractReportResponse.DataTypeValue.builder()
            .value(request.getPoolNumber())
            .dataType("String")
            .displayName("Pool number")
            .build());
        map.put("total_pool_members", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Total pool members")
            .dataType("Long")
            .value(jurorPoolRepository.count(QJurorPool.jurorPool.pool.poolNumber.eq(request.getPoolNumber())))
            .build());
        map.put("total_requested_by_court", AbstractReportResponse.DataTypeValue.builder()
            .dataType("Long")
            .value(poolRequest.getTotalNoRequired())
            .displayName("Originally requested by court")
            .build());

        return map;
    }

    public interface RequestValidator extends
        AbstractReport.Validators.AbstractRequestValidator,
        AbstractReport.Validators.RequirePoolNumber {
    }
}
